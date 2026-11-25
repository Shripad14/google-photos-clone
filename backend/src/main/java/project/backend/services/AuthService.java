package project.backend.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.dto.AuthResponse;
import project.backend.dto.RegisterRequest;
import project.backend.entity.User;
import project.backend.repository.RefreshTokenRepository;
import project.backend.repository.UserRespository;
import project.backend.exception.ResourceConflictException;


@Service
public class AuthService {

    private final UserRespository userRespository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRespository userRespository,
            RefreshTokenRepository refreshTokenRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService

    ){
        this.userRespository = userRespository;
        this.refreshTokenRepository = refreshTokenRepository ;
        this.userService = userService ;
        this.passwordEncoder = passwordEncoder  ;
        this.authenticationManager = authenticationManager ;
        this.jwtService = jwtService ;
    }


    @Transactional
    public AuthResponse register(RegisterRequest request){
        String email = request.email().toLowerCase().trim();

        if(userRespository.existsByEmail(email)){
            throw new ResourceConflictException("Email already exists!!");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName().trim())
                .build();

        userRespository.save(user);
        return buildAuthResponse(user);
    }


}
