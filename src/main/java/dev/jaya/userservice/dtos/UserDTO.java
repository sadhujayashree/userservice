package dev.jaya.userservice.dtos;

import dev.jaya.userservice.models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private List<Role> roles;

}
