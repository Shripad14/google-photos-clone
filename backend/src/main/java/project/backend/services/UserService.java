package project.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.dto.UserResponse;
import project.backend.entity.User;
import project.backend.exception.UnauthorizedException;
import project.backend.repository.UserRespository;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRespository userRespository;

    public UserService(UserRespository userRespository){
        this.userRespository = userRespository;
    }

    public UserResponse getUserResponseByEmail(String email){
        return toUserResponse(getByEmail(email));
    }

    public User getByEmail(String email){
        return userRespository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public Optional<String> findByEmail(UUID userId){
        return userRespository.findById(userId).map(User::getEmail);
    }

    public boolean existsById(UUID userId){
        return userRespository.existsById(userId);
    }

    public UserResponse toUserResponse(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
