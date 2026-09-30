package com.ronyarg.webmacros.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;


import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ronyarg.webmacros.auth.dto.RegisterRequest;
import com.ronyarg.webmacros.auth.dto.RegisterResponse;
import com.ronyarg.webmacros.auth.exception.EmailAlreadyExistsException;
import com.ronyarg.webmacros.user.Role;
import com.ronyarg.webmacros.user.User;
import com.ronyarg.webmacros.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock 
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private static final String NAME = "Juan";
    private static final String EMAIL = "juan123@test.com";
    private static final String PASSWORD = "prueba1234";
    private static final String ENCODED_PASSWORD = "hashbcrypt";

    private RegisterRequest validRequest() {
        return new RegisterRequest (NAME, EMAIL, PASSWORD);
    }
    private RegisterRequest requestWithEmail(String email) {
        return new RegisterRequest(NAME, email, PASSWORD);
    }
    private RegisterRequest requestWithName(String name) {
        return new RegisterRequest(name, EMAIL, PASSWORD);
    }

    @Test
    @DisplayName ("register: with valid data saves user and returns RegisterResponse")
    void register_withValidData_savesUserAndReturnsResponse() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // simulate that the user is saved and assigned an ID
            return user;
        });

        RegisterResponse response = authService.register(validRequest());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName ("register: save password encrypted, never store plain password")
    void register_validData_savesPasswordEncrypted() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(validRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture()); // Capture the User object passed to save
        User savedUser = captor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(savedUser.getPassword()).isNotEqualTo(PASSWORD);
    }

    @Test
    @DisplayName ("register: set role to USER by default")
    void register_validData_setsRoleToUserByDefault() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(validRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName ("register: normalizes email to lowercase and trims whitespace")
    void register_validData_normalizesEmail() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        authService.register(requestWithEmail("  JUan123@test.COM  "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test @DisplayName ("register: trims whitespace from name")
    void register_validData_trimsName() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        authService.register(requestWithName("  Juan  "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(NAME);
    }
    
    @Test
    @DisplayName ("register: with existing email throws EmailAlreadyExistsException")
    void register_withExistingEmail_throwsEmailAlreadyExistsException() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(validRequest()))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining(EMAIL);

        verify(userRepository,never()).save(any(User.class));
        verify(passwordEncoder,never()).encode(any());
    }
}
