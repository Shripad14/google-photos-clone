package project.backend.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.dto.AuthResponse;
import project.backend.dto.LoginRequest;
import project.backend.dto.RegisterRequest;
import project.backend.entity.RefreshToken;
import project.backend.entity.User;
import project.backend.exception.UnauthorizedException;
import project.backend.repository.RefreshTokenRepository;
import project.backend.repository.UserRespository;
import project.backend.exception.ResourceConflictException;

import java.time.Instant;


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

    @Transactional
    public AuthResponse login(LoginRequest loginRequest){
        String email = loginRequest.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, loginRequest.password())
        );

        User user = userService.getByEmail(email);
        refreshTokenRepository.deleteByUserId(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue){
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if(refreshToken.getExpiresAt().isBefore(Instant.now())){
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException("Refresh token expired.");
        }

        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    private AuthResponse buildAuthResponse(User user){
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshTokenValue();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(jwtService.refreshTokenExpiry())
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                refreshTokenValue,
                userService.toUserResponse(user)
        );
    }

}
