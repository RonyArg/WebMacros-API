package com.ronyarg.webmacros.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ronyarg.webmacros.auth.dto.RegisterRequest;
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

    public void register(RegisterRequest request){
        if(userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("El correo electrónico ya está registrado");
        }  
        
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        // all users will have the role of USER by default
        user.setRole(Role.USER);
        userRepository.save(user);
    }

}
