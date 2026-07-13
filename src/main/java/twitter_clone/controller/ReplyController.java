package twitter_clone.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitter_clone.dto.ReplyResponse;
import twitter_clone.service.ReplyService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replies")
public class ReplyController {

    private final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping("/tweet/{tweetId}")
    public ResponseEntity<List<ReplyResponse>> getRepliesForTweet(@PathVariable Long tweetId) {
        return ResponseEntity.ok(replyService.getRepliesForTweet(tweetId));
    }

    @PostMapping("/tweet/{tweetId}")
    public ResponseEntity<ReplyResponse> createReply(@PathVariable Long tweetId,
                                                       @RequestBody Map<String, String> body,
                                                       HttpServletRequest request) {
        return ResponseEntity.ok(replyService.createReply(currentUserId(request), tweetId, body.get("content")));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long replyId, HttpServletRequest request) {
        replyService.deleteReply(currentUserId(request), replyId);
        return ResponseEntity.noContent().build();
    }
}
