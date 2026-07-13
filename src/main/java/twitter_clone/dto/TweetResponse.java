package twitter_clone.dto;

import java.time.LocalDateTime;

public class TweetResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    private Long authorId;
    private String authorUsername;
    private String authorProfileImageUrl;

    private int likesCount;
    private boolean likedByCurrentUser;

    private int retweetsCount;
    private boolean retweetedByCurrentUser;

    // Present only if this tweet is a retweet
    private Boolean isRetweet = false;
    private Long originalTweetId;
    private String originalAuthorUsername;
    private String originalContent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getAuthorProfileImageUrl() { return authorProfileImageUrl; }
    public void setAuthorProfileImageUrl(String authorProfileImageUrl) { this.authorProfileImageUrl = authorProfileImageUrl; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public int getRetweetsCount() { return retweetsCount; }
    public void setRetweetsCount(int retweetsCount) { this.retweetsCount = retweetsCount; }

    public boolean isRetweetedByCurrentUser() { return retweetedByCurrentUser; }
    public void setRetweetedByCurrentUser(boolean retweetedByCurrentUser) { this.retweetedByCurrentUser = retweetedByCurrentUser; }

    public Boolean getIsRetweet() { return isRetweet; }
    public void setIsRetweet(Boolean isRetweet) { this.isRetweet = isRetweet; }

    public Long getOriginalTweetId() { return originalTweetId; }
    public void setOriginalTweetId(Long originalTweetId) { this.originalTweetId = originalTweetId; }

    public String getOriginalAuthorUsername() { return originalAuthorUsername; }
    public void setOriginalAuthorUsername(String originalAuthorUsername) { this.originalAuthorUsername = originalAuthorUsername; }

    public String getOriginalContent() { return originalContent; }
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
}
