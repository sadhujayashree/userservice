package dev.jaya.userservice.models;

import dev.jaya.userservice.dtos.UserDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class User extends BaseModel {
    private String name;
    private String password;
    private String email;
    @ManyToMany
    private List<Role> roles;

    public UserDTO toDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(this.name);
        userDTO.setEmail(this.email);
        userDTO.setId(this.getId());
        userDTO.setRoles(this.roles);
        return userDTO;
    }
}
