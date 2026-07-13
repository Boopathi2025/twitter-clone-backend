package twitter_clone.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import twitter_clone.dto.AuthResponse;
import twitter_clone.dto.LoginRequest;
import twitter_clone.dto.RegisterRequest;
import twitter_clone.dto.UserResponse;
import twitter_clone.entity.User;
import twitter_clone.exception.ApiException;
import twitter_clone.jwt.JwtUtil;
import twitter_clone.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException(HttpStatus.CONFLICT, "Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username/email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, UserResponse.from(user));
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.from(user);
    }

    public UserResponse getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return UserResponse.from(user);
    }

    public List<UserResponse> search(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query)
                .stream().map(UserResponse::from).toList();
    }

    public UserResponse updateProfile(Long userId, String bio, String profileImageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (bio != null) user.setBio(bio);
        if (profileImageUrl != null) user.setProfileImageUrl(profileImageUrl);
        return UserResponse.from(userRepository.save(user));
    }

    // Package-private helper used by other services that need the raw entity
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
