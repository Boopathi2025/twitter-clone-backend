package twitter_clone.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tweet")
public class Tweet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store just the author's id instead of a @ManyToOne User relationship.
    // This is the key design decision that eliminates LazyInitializationException:
    // there is no Hibernate proxy to fail to initialize, because there's no
    // lazy association here at all. The service layer resolves the User by id
    // when it needs to build a response DTO.
    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 280)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // If this tweet is a retweet of another tweet, store the original tweet's id.
    // Null for original tweets.
    private Long originalTweetId;

    // Plain set of user ids who liked this tweet - loaded EAGERLY (it's just Longs,
    // not entities, so eager loading here is cheap and safe, and never throws
    // "no Session" errors when serialized to JSON after the request closes).
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tweet_likes", joinColumns = @JoinColumn(name = "tweet_id"))
    @Column(name = "user_id")
    private Set<Long> likedByUserIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tweet_retweets", joinColumns = @JoinColumn(name = "tweet_id"))
    @Column(name = "user_id")
    private Set<Long> retweetedByUserIds = new HashSet<>();

    public Tweet() {}

    public Tweet(Long authorId, String content) {
        this.authorId = authorId;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getOriginalTweetId() { return originalTweetId; }
    public void setOriginalTweetId(Long originalTweetId) { this.originalTweetId = originalTweetId; }

    public Set<Long> getLikedByUserIds() { return likedByUserIds; }
    public void setLikedByUserIds(Set<Long> likedByUserIds) { this.likedByUserIds = likedByUserIds; }

    public Set<Long> getRetweetedByUserIds() { return retweetedByUserIds; }
    public void setRetweetedByUserIds(Set<Long> retweetedByUserIds) { this.retweetedByUserIds = retweetedByUserIds; }
}
