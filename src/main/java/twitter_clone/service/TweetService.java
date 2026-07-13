package twitter_clone.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twitter_clone.dto.TweetResponse;
import twitter_clone.entity.Tweet;
import twitter_clone.entity.User;
import twitter_clone.exception.ApiException;
import twitter_clone.repository.TweetRepository;
import twitter_clone.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetService(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TweetResponse createTweet(Long authorId, String content) {
        if (content == null || content.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tweet content cannot be empty");
        }
        if (content.length() > 280) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tweet content cannot exceed 280 characters");
        }
        Tweet tweet = new Tweet(authorId, content);
        tweet = tweetRepository.save(tweet);
        return toResponse(tweet, authorId);
    }

    @Transactional
    public TweetResponse retweet(Long userId, Long originalTweetId) {
        Tweet original = tweetRepository.findById(originalTweetId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tweet not found"));

        Tweet retweet = new Tweet(userId, original.getContent());
        retweet.setOriginalTweetId(original.getId());
        retweet = tweetRepository.save(retweet);

        original.getRetweetedByUserIds().add(userId);
        tweetRepository.save(original);

        return toResponse(retweet, userId);
    }

    @Transactional
    public void deleteTweet(Long userId, Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tweet not found"));
        if (!tweet.getAuthorId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own tweets");
        }
        tweetRepository.delete(tweet);
    }

    @Transactional
    public TweetResponse toggleLike(Long userId, Long tweetId) {
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tweet not found"));

        if (tweet.getLikedByUserIds().contains(userId)) {
            tweet.getLikedByUserIds().remove(userId);
        } else {
            tweet.getLikedByUserIds().add(userId);
        }
        tweet = tweetRepository.save(tweet);
        return toResponse(tweet, userId);
    }

    @Transactional(readOnly = true)
    public List<TweetResponse> getAllTweets(Long currentUserId) {
        List<Tweet> tweets = tweetRepository.findAllByOrderByCreatedAtDesc();
        return toResponseList(tweets, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<TweetResponse> getTweetsByAuthor(Long authorId, Long currentUserId) {
        List<Tweet> tweets = tweetRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
        return toResponseList(tweets, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<TweetResponse> getFeedForUser(List<Long> followedIds, Long currentUserId) {
        // "My Feed" = tweets from the current user + anyone they follow.
        // followedIds already includes currentUserId when called from the controller.
        List<Tweet> tweets = tweetRepository.findByAuthorIdInOrderByCreatedAtDesc(followedIds);
        return toResponseList(tweets, currentUserId);
    }

    // ---- helpers ----

    private TweetResponse toResponse(Tweet tweet, Long currentUserId) {
        return toResponseList(List.of(tweet), currentUserId).get(0);
    }

    private List<TweetResponse> toResponseList(List<Tweet> tweets, Long currentUserId) {
        if (tweets.isEmpty()) return List.of();

        // Batch-resolve every author (and every original-tweet author for retweets)
        // in one query instead of N+1 lazy lookups.
        List<Long> authorIds = tweets.stream().map(Tweet::getAuthorId).distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> originalIds = tweets.stream()
                .map(Tweet::getOriginalTweetId).filter(java.util.Objects::nonNull).distinct().toList();
        Map<Long, Tweet> originalsById = originalIds.isEmpty() ? Map.of() :
                tweetRepository.findAllById(originalIds).stream()
                        .collect(Collectors.toMap(Tweet::getId, t -> t));

        return tweets.stream().map(tweet -> {
            TweetResponse dto = new TweetResponse();
            dto.setId(tweet.getId());
            dto.setContent(tweet.getContent());
            dto.setCreatedAt(tweet.getCreatedAt());
            dto.setAuthorId(tweet.getAuthorId());

            User author = usersById.get(tweet.getAuthorId());
            if (author != null) {
                dto.setAuthorUsername(author.getUsername());
                dto.setAuthorProfileImageUrl(author.getProfileImageUrl());
            }

            dto.setLikesCount(tweet.getLikedByUserIds().size());
            dto.setLikedByCurrentUser(currentUserId != null && tweet.getLikedByUserIds().contains(currentUserId));

            dto.setRetweetsCount(tweet.getRetweetedByUserIds().size());
            dto.setRetweetedByCurrentUser(currentUserId != null && tweet.getRetweetedByUserIds().contains(currentUserId));

            if (tweet.getOriginalTweetId() != null) {
                dto.setIsRetweet(true);
                dto.setOriginalTweetId(tweet.getOriginalTweetId());
                Tweet original = originalsById.get(tweet.getOriginalTweetId());
                if (original != null) {
                    dto.setOriginalContent(original.getContent());
                    User originalAuthor = usersById.get(original.getAuthorId());
                    if (originalAuthor != null) {
                        dto.setOriginalAuthorUsername(originalAuthor.getUsername());
                    }
                }
            }

            return dto;
        }).toList();
    }
}
