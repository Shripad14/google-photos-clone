package project.backend.services;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.backend.repository.UserRespository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRespository userRespository;

    public UserDetailsServiceImpl(UserRespository userRespository){
        this.userRespository = userRespository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRespository.findByEmail(email.toLowerCase())
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .authorities("ROLE_ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
