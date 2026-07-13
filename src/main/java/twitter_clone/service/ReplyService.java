package twitter_clone.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitter_clone.dto.ReplyResponse;
import twitter_clone.entity.Reply;
import twitter_clone.entity.User;
import twitter_clone.exception.ApiException;
import twitter_clone.repository.ReplyRepository;
import twitter_clone.repository.TweetRepository;
import twitter_clone.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public ReplyService(ReplyRepository replyRepository, TweetRepository tweetRepository, UserRepository userRepository) {
        this.replyRepository = replyRepository;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReplyResponse createReply(Long authorId, Long tweetId, String content) {
        if (content == null || content.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reply content cannot be empty");
        }
        if (!tweetRepository.existsById(tweetId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tweet not found");
        }
        Reply reply = new Reply(tweetId, authorId, content);
        reply = replyRepository.save(reply);
        return toResponseList(List.of(reply)).get(0);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getRepliesForTweet(Long tweetId) {
        List<Reply> replies = replyRepository.findByTweetIdOrderByCreatedAtAsc(tweetId);
        return toResponseList(replies);
    }

    @Transactional
    public void deleteReply(Long userId, Long replyId) {
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reply not found"));
        if (!reply.getAuthorId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own replies");
        }
        replyRepository.delete(reply);
    }

    private List<ReplyResponse> toResponseList(List<Reply> replies) {
        if (replies.isEmpty()) return List.of();

        List<Long> authorIds = replies.stream().map(Reply::getAuthorId).distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return replies.stream().map(reply -> {
            ReplyResponse dto = new ReplyResponse();
            dto.setId(reply.getId());
            dto.setTweetId(reply.getTweetId());
            dto.setAuthorId(reply.getAuthorId());
            dto.setContent(reply.getContent());
            dto.setCreatedAt(reply.getCreatedAt());
            User author = usersById.get(reply.getAuthorId());
            if (author != null) {
                dto.setAuthorUsername(author.getUsername());
                dto.setAuthorProfileImageUrl(author.getProfileImageUrl());
            }
            return dto;
        }).toList();
    }
}
