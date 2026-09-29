package com.ronyarg.webmacros.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ronyarg.webmacros.auth.dto.RegisterRequest;
import com.ronyarg.webmacros.auth.dto.RegisterResponse;
import com.ronyarg.webmacros.auth.exception.EmailAlreadyExistsException;
import com.ronyarg.webmacros.user.Role;
import com.ronyarg.webmacros.user.User;
import com.ronyarg.webmacros.user.UserRepository;

@Service 
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    

    public AuthService(
        PasswordEncoder passwordEncoder,
        UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional 
    public RegisterResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new EmailAlreadyExistsException(request.email());
        }  
        
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        // all users will have the role of USER by default
        user.setRole(Role.USER);
        userRepository.save(user);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole()
        );
    }

}
