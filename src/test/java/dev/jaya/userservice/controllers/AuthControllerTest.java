package dev.jaya.userservice.controllers;

import dev.jaya.userservice.dtos.LoginRequestDTO;
import dev.jaya.userservice.dtos.SignupRequestDTO;
import dev.jaya.userservice.dtos.UserDTO;
import dev.jaya.userservice.models.User;
import dev.jaya.userservice.pojos.UserToken;
import dev.jaya.userservice.services.IAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthController.class)
class AuthControllerTest {
    @Autowired
    private AuthController authController;

    @MockitoBean
    private IAuthService authService;

    @Test
    void signUpReturnsCreatedUserWhenRequestIsValid() {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setName("Jaya");
        signupRequestDTO.setEmail("jaya@test.com");
        signupRequestDTO.setPassword("pass123");

        User user = new User();
        user.setId(1L);
        user.setName("Jaya");
        user.setEmail("jaya@test.com");

        when(authService.signUp("Jaya", "jaya@test.com", "pass123")).thenReturn(user);

        ResponseEntity<UserDTO> response = authController.signUp(signupRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Jaya", response.getBody().getName());
        assertEquals("jaya@test.com", response.getBody().getEmail());
        verify(authService).signUp("Jaya", "jaya@test.com", "pass123");
    }

    @Test
    void signUpReturnsNullWhenServiceThrowsException() {
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO();
        signupRequestDTO.setName("Jaya");
        signupRequestDTO.setEmail("jaya@test.com");
        signupRequestDTO.setPassword("pass123");

        when(authService.signUp(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("signup failed"));

        ResponseEntity<UserDTO> response = authController.signUp(signupRequestDTO);

        assertNull(response);
        verify(authService).signUp("Jaya", "jaya@test.com", "pass123");
    }

    @Test
    void loginReturnsUserAndCookieWhenCredentialsAreValid() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("jaya@test.com");
        loginRequestDTO.setPassword("pass123");

        User user = new User();
        user.setId(10L);
        user.setName("Jaya");
        user.setEmail("jaya@test.com");
        UserToken userToken = new UserToken(user, "token=abc123");

        when(authService.login("jaya@test.com", "pass123")).thenReturn(userToken);

        ResponseEntity<UserDTO> response = authController.login(loginRequestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Jaya", response.getBody().getName());
        assertEquals("jaya@test.com", response.getBody().getEmail());
        assertEquals("token=abc123", response.getHeaders().getFirst(HttpHeaders.COOKIE));
        verify(authService).login("jaya@test.com", "pass123");
    }

    @Test
    void loginReturnsForbiddenWhenServiceThrowsException() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("jaya@test.com");
        loginRequestDTO.setPassword("wrong-pass");

        when(authService.login("jaya@test.com", "wrong-pass"))
                .thenThrow(new RuntimeException("invalid credentials"));

        ResponseEntity<UserDTO> response = authController.login(loginRequestDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());
        verify(authService).login("jaya@test.com", "wrong-pass");
    }
}