package twitter_clone.repository;

import twitter_clone.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByTweetIdOrderByCreatedAtAsc(Long tweetId);
    void deleteByTweetId(Long tweetId);
}
