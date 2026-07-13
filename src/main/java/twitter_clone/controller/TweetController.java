package twitter_clone.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitter_clone.dto.TweetResponse;
import twitter_clone.service.TweetService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping
    public ResponseEntity<List<TweetResponse>> getAllTweets(HttpServletRequest request) {
        return ResponseEntity.ok(tweetService.getAllTweets(currentUserId(request)));
    }

    @GetMapping("/user/{authorId}")
    public ResponseEntity<List<TweetResponse>> getTweetsByAuthor(@PathVariable Long authorId, HttpServletRequest request) {
        return ResponseEntity.ok(tweetService.getTweetsByAuthor(authorId, currentUserId(request)));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<TweetResponse>> getMyFeed(HttpServletRequest request) {
        Long userId = currentUserId(request);
        // Simple feed: just the current user's own tweets for now.
        // Swap in a real followedIds list here once a Follow relationship is added.
        return ResponseEntity.ok(tweetService.getFeedForUser(List.of(userId), userId));
    }

    @PostMapping
    public ResponseEntity<TweetResponse> createTweet(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return ResponseEntity.ok(tweetService.createTweet(currentUserId(request), body.get("content")));
    }

    @PostMapping("/{tweetId}/retweet")
    public ResponseEntity<TweetResponse> retweet(@PathVariable Long tweetId, HttpServletRequest request) {
        return ResponseEntity.ok(tweetService.retweet(currentUserId(request), tweetId));
    }

    @PostMapping("/{tweetId}/like")
    public ResponseEntity<TweetResponse> toggleLike(@PathVariable Long tweetId, HttpServletRequest request) {
        return ResponseEntity.ok(tweetService.toggleLike(currentUserId(request), tweetId));
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Void> deleteTweet(@PathVariable Long tweetId, HttpServletRequest request) {
        tweetService.deleteTweet(currentUserId(request), tweetId);
        return ResponseEntity.noContent().build();
    }
}
