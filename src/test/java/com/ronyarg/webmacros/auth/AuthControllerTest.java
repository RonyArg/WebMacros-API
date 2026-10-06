package com.ronyarg.webmacros.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ronyarg.webmacros.auth.config.SecurityConfig;
import com.ronyarg.webmacros.auth.dto.RegisterRequest;
import com.ronyarg.webmacros.auth.dto.RegisterResponse;
import com.ronyarg.webmacros.auth.exception.EmailAlreadyExistsException;
import com.ronyarg.webmacros.user.Role;

@Import (SecurityConfig.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthService authService;

        private static final String REGISTER_URL = "/auth/register";
        private static final String VALID_JSON = """
                        {
                            "name": "Juan",
                            "email": "juan123@test.com",
                            "password": "prueba1234"
                        }
                        """;
        private static final RegisterResponse REGISTER_RESPONSE = new RegisterResponse(
                        1L,
                        "Juan",
                        "juan123@test.com",
                        Role.USER);

        @Test
        @DisplayName("register: with valid data returns CREATED response")
        void register_withValidData_returnsCreatedResponse() throws Exception {
                when(authService.register(any(RegisterRequest.class))).thenReturn(REGISTER_RESPONSE);

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.name").value("Juan"))
                        .andExpect(jsonPath("$.email").value("juan123@test.com"))
                        .andExpect(jsonPath("$.role").value("USER"));

                ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
                verify(authService).register(captor.capture());
                RegisterRequest capturedRequest = captor.getValue();

                assertThat(capturedRequest.name()).isEqualTo("Juan");
                assertThat(capturedRequest.email()).isEqualTo("juan123@test.com");
                assertThat(capturedRequest.password()).isEqualTo("prueba1234");
        }

        @Test
        @DisplayName ("register: with blank name returns BAD_REQUEST 400")
        void register_withBlankName_returnsBadRequest() throws Exception {
                String json = """
                                {
                                    "name": "",
                                    "email": "juan123@test.com",
                                    "password": "prueba1234"
                                }
                                """;
                
                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.message").value("El nombre es obligatorio"));

                verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName ("register: with blank email returns BAD_REQUEST 400")
        void register_withBlankEmail_returnsBadRequest() throws Exception {
                String json = """
                                {
                                    "name": "Juan",
                                    "email": "",
                                    "password": "prueba1234"
                                }
                                """;
                
                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.message").value("El correo electrónico es obligatorio"));

                verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName ("register: with invalid email returns BAD_REQUEST 400")
        void register_withInvalidEmail_returnsBadRequest() throws Exception {
                String json = """
                                {
                                    "name": "Juan",
                                    "email": "juan123test.com",
                                    "password": "prueba1234"
                                }
                                """;
                
                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.message").value("El correo electrónico no es válido"));

                verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test
        @DisplayName ("register: with blank password returns BAD_REQUEST 400")
        void register_withBlankPassword_returnsBadRequest() throws Exception {
                String json = """
                                {
                                    "name": "Juan",
                                    "email": "juan123@test.com",
                                    "password": ""
                                }
                                """;

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.message").value("La contraseña es obligatoria"));

                verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test 
        @DisplayName ("register: with short password returns BAD_REQUEST 400")
        void register_withShortPassword_returnsBadRequest() throws Exception {
                String json = """
                                {
                                    "name": "Juan",
                                    "email": "juan123@test.com",
                                    "password": "123"
                                }
                                """;

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(json))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.status").value(400))
                        .andExpect(jsonPath("$.error").value("Bad Request"))
                        .andExpect(jsonPath("$.message").value("La contraseña debe tener al menos 6 caracteres"));

                verify(authService, never()).register(any(RegisterRequest.class));
        }

        @Test 
        @DisplayName ("register: with duplicate email returns CONFLICT 409")
        void register_withDuplicateEmail_returnsConflict() throws Exception {
                when(authService.register(any(RegisterRequest.class))).thenThrow(new EmailAlreadyExistsException("juan123@test.com"));

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(409))
                        .andExpect(jsonPath("$.error").value("Conflict"))
                        .andExpect(jsonPath("$.message").value("El correo electrónico ya está registrado"));
                
                verify(authService).register(any(RegisterRequest.class));
        }

        @Test 
        @DisplayName ("register: with unexpected exception returns INTERNAL_SERVER_ERROR 500")
        void register_withUnexpectedException_returnsInternalServerError() throws Exception {
                when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Unexpected error"));

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                        .andExpect(status().isInternalServerError())
                        .andExpect(jsonPath("$.status").value(500))
                        .andExpect(jsonPath("$.error").value("Internal Server Error"))
                        .andExpect(jsonPath("$.message").value("Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo más tarde."));
                
                verify(authService).register(any(RegisterRequest.class));
        }

        @Test 
        @DisplayName ("register: with data integrity violation returns CONFLICT 409")
        void register_withDataIntegrityViolation_returnsConflict() throws Exception {
                when(authService.register(any(RegisterRequest.class))).thenThrow(new DataIntegrityViolationException("Data integrity violation"));

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(409))
                        .andExpect(jsonPath("$.error").value("Conflict"))
                        .andExpect(jsonPath("$.message").value("No se pudo completar la operación porque los datos entraron en conflicto con información existente."));
                
                verify(authService).register(any(RegisterRequest.class));
        }

        @Test 
        @DisplayName ("register: allows access without authentication")
        void register_allowsAccessWithoutAuthentication() throws Exception {
                when(authService.register(any(RegisterRequest.class))).thenReturn(REGISTER_RESPONSE);

                mockMvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                        .andExpect(status().isCreated());
        }
}