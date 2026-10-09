package com.ronyarg.webmacros.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ronyarg.webmacros.auth.dto.LoginRequest;
import com.ronyarg.webmacros.auth.dto.LoginResponse;
import com.ronyarg.webmacros.auth.dto.RegisterRequest;
import com.ronyarg.webmacros.auth.dto.RegisterResponse;
import com.ronyarg.webmacros.auth.exception.EmailAlreadyExistsException;
import com.ronyarg.webmacros.auth.exception.InvalidCredentialsException;
import com.ronyarg.webmacros.auth.security.JwtService;
import com.ronyarg.webmacros.user.Role;
import com.ronyarg.webmacros.user.User;
import com.ronyarg.webmacros.user.UserRepository;

@Service 
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    

    public AuthService(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository, 
        JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional 
    public RegisterResponse register(RegisterRequest request){
        String email = request.email().trim().toLowerCase();
        String name = request.name().trim();

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }  
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        // all users will have the role of USER by default
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }

    @Transactional (readOnly = true)
    public LoginResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if(user == null) {
            throw new InvalidCredentialsException();
        }
        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user);
        return new LoginResponse(
            token,
            user.getName(),
            user.getEmail()
        );
    }

}
