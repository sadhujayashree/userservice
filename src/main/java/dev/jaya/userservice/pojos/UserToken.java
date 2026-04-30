package dev.jaya.userservice.pojos;

import dev.jaya.userservice.models.User;
import lombok.Getter;

@Getter
public class UserToken {
    private User user;
    private String token;
    public UserToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

}
