package dev.jaya.userservice.controllers;

import dev.jaya.userservice.dtos.LoginRequestDTO;
import dev.jaya.userservice.dtos.SignupRequestDTO;
import dev.jaya.userservice.dtos.UserDTO;
import dev.jaya.userservice.models.User;
import dev.jaya.userservice.pojos.UserToken;
import dev.jaya.userservice.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signUp(@RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            User user = authService.signUp(signupRequestDTO.getName(), signupRequestDTO.getEmail(), signupRequestDTO.getPassword());
            UserDTO userDTO = user.toDTO();
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        }
        catch (Exception e) {
            return null;
        }
    }


    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try{
            UserToken userToken = authService.login(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            System.out.println(userToken);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add(HttpHeaders.COOKIE, userToken.getToken());
            HttpHeaders responseHeaders = new HttpHeaders(headers);
            return new ResponseEntity<>(userToken.getUser().toDTO(),responseHeaders,HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

}
