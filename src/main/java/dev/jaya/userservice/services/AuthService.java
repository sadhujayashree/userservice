package dev.jaya.userservice.services;

import dev.jaya.userservice.exceptions.IncorrectPasswordException;
import dev.jaya.userservice.exceptions.UserAlreadyExistException;
import dev.jaya.userservice.exceptions.UserNotRegisteredException;
import dev.jaya.userservice.models.Role;
import dev.jaya.userservice.models.Session;
import dev.jaya.userservice.models.State;
import dev.jaya.userservice.models.User;
import dev.jaya.userservice.pojos.UserToken;
import dev.jaya.userservice.repositories.RoleRepo;
import dev.jaya.userservice.repositories.SessionRepo;
import dev.jaya.userservice.repositories.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService{
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private SessionRepo sessionRepo;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    SecretKey secretKey;

    @Override
    public User signUp(String name, String email, String password){
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isPresent()){
            throw new UserAlreadyExistException("User already exist");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setCreatedAt(new Date());
        user.setModifiedAt(new Date());
        user.setState(State.ACTIVE);

        Role role;
        Optional<Role> optionalRole = roleRepo.findByName("DEFAULT");
        if(optionalRole.isPresent()){
            role = optionalRole.get();
        }else{
            role = new Role();
            role.setName("DEFAULT");
            role.setCreatedAt(new Date());
            role.setModifiedAt(new Date());
            role.setState(State.ACTIVE);
            roleRepo.save(role);
        }
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepo.save(user);
    }

    public UserToken login(String email, String password){
        Optional<User> optionalUser = userRepo.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new UserNotRegisteredException("User not registered");
        }
        User user = optionalUser.get();
        if(bCryptPasswordEncoder.matches(password, user.getPassword())){
//            Generate the token
            Map<String,Object> payload = new HashMap<>();
            Long nowInMilli = System.currentTimeMillis(); //return timestamp in epoch
            payload.put("iat", nowInMilli);
            payload.put("exp", nowInMilli+10000000); //100k milli-seconds as expiry time period
            payload.put("userId", user.getId());
            payload.put("iss", "Jaya");
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .toList();
            payload.put("scope", roleNames);

            String token = Jwts.builder().claims(payload).signWith(secretKey).compact();

            Session userSession = new Session();
            userSession.setCreatedAt(new Date());
            userSession.setModifiedAt(new Date());
            userSession.setState(State.ACTIVE);
            userSession.setUser(user);
            userSession.setToken(token);
            sessionRepo.save(userSession);

            return new UserToken(user, token);
        }else{
            throw new IncorrectPasswordException("Incorrect password");
        }
    }
    public Boolean validateToken(String token){
        Optional<Session> optionalSession = sessionRepo.findByToken(token);
        if(optionalSession.isEmpty()){
            return false;
        }
        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();

        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        System.out.println(claims);

        /*
        Extracting the payload from the JWT using a parser which contains the secret key

         */

        Long expiryTime = (Long) claims.get("exp");
        Long nowInMills = System.currentTimeMillis();

        if(nowInMills > expiryTime){
            Session session = optionalSession.get();
            session.setState(State.INACTIVE);
            sessionRepo.save(session);
            return false;
        }
        return true;
    }
}
