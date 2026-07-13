package twitter_clone.repository;

import twitter_clone.entity.Tweet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TweetRepository extends JpaRepository<Tweet, Long> {
    List<Tweet> findAllByOrderByCreatedAtDesc();
    List<Tweet> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
    List<Tweet> findByAuthorIdInOrderByCreatedAtDesc(List<Long> authorIds);
}
