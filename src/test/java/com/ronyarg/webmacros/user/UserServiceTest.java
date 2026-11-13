package com.ronyarg.webmacros.user;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ronyarg.webmacros.exception.ResourceNotFoundException;
import com.ronyarg.webmacros.user.dto.UserResponse;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks 
    private UserService userService;

    private static final String NAME = "Juan";
    private static final String EMAIL = "juan123@test.com";

    @Test
    @DisplayName ("getProfile: with existing user returns UserResponse")
    void getProfile_existingUser_returnsUserResponse() {
        User user = new User();
        user.setName(NAME);
        user.setEmail(EMAIL);
        user.setPassword("password");
        user.setRole(Role.USER);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile(EMAIL);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.email()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName ("getProfile: with non-existing user throws ResourceNotFoundException")
    void getProfile_nonExistingUser_throwsResourceNotFoundException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(EMAIL))
                .isExactlyInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuario no encontrado");
        verify(userRepository).findByEmail(EMAIL);
    }
}
