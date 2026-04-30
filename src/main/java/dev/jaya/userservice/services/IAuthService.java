package dev.jaya.userservice.services;

import dev.jaya.userservice.models.User;
import dev.jaya.userservice.pojos.UserToken;

public interface IAuthService {
    User signUp(String name, String email, String password);
    UserToken login(String email, String password);
    Boolean validateToken(String token);
}
