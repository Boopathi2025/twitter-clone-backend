package twitter_clone.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import twitter_clone.dto.UserResponse;
import twitter_clone.exception.ApiException;
import twitter_clone.service.UserService;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(HttpServletRequest request) {
        return ResponseEntity.ok(userService.getById(currentUserId(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getByUsername(username));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(userService.search(query));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return ResponseEntity.ok(userService.updateProfile(currentUserId(request), body.get("bio"), null));
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<UserResponse> uploadProfileImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        try {
            Path dir = Path.of(uploadDir);
            Files.createDirectories(dir);
            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            file.transferTo(target);

            String url = "/uploads/profile/" + filename;
            return ResponseEntity.ok(userService.updateProfile(currentUserId(request), null, url));
        } catch (IOException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image: " + e.getMessage());
        }
    }
}
