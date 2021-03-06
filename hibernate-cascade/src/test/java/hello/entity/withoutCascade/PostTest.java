package hello.entity.withoutCascade;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import hello.AbstractJpaTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.persistence.PersistenceException;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@DatabaseSetup("/withoutCascade/post_comment.xml")
public class PostTest extends AbstractJpaTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void removePost_WhenNotRemoveDependenceComments_ShouldThrowException() {
        this.thrown.expect(PersistenceException.class);
        this.thrown.expectMessage("org.hibernate.exception.ConstraintViolationException: could not execute statement");

        Post post = entityManager.find(Post.class, 1L);

        // cannot delete - because exist foreign key
        entityManager.remove(post);

        flushAndClean();
    }

    @Test
    public void removePost_WhenBeforeRemoveLinkDependenceComments_ShouldBeRemovedOnlyRelations() {
        Post post = entityManager.find(Post.class, 1L);

        // remove only link to parent and persist
        post.getComments().stream()
                .peek(c -> c.setPost(null))
                .forEach(c -> entityManager.persist(c));

        entityManager.remove(post);

        flushAndClean();

        Post postAfterDelete = entityManager.find(Post.class, 1L);
        assertThat(postAfterDelete, equalTo(null));

        Comment comment1 = entityManager.find(Comment.class, 1L);
        assertThat(comment1.getPost(), equalTo(null));
    }

    @Test
    public void removePost_WhenBeforeRemoveDependenceComments_ShouldBeRemovedObjects() {
        Post post = entityManager.find(Post.class, 1L);

        // remove post children's
        post.getComments().forEach(c -> entityManager.remove(c));

        entityManager.remove(post);

        flushAndClean();

        Post postAfterDelete = entityManager.find(Post.class, 1L);
        assertThat(postAfterDelete, equalTo(null));

        Comment comment1 = entityManager.find(Comment.class, 1L);
        assertThat(comment1, equalTo(null));
    }

    @Test
    public void tryDeletePostDependenciesByCleanOnlyCollections_ShouldBeNothingDeleted() {
        Post post = entityManager.find(Post.class, 1L);

        post.setComments(null);
        entityManager.persist(post);

        flushAndClean();

        Post clientAfterCleanCollection = entityManager.find(Post.class, 1L);
        assertThat(clientAfterCleanCollection.getComments().size(), equalTo(2)); // nothing deleted

        Post post2 = entityManager.find(Post.class, 1L);

        post2.getComments().clear();
        entityManager.persist(post2);

        flushAndClean();

        Post clientAfterCleanCollection2 = entityManager.find(Post.class, 1L);
        assertThat(clientAfterCleanCollection2.getComments().size(), equalTo(2)); // nothing deleted
    }

    @Test
    public void tryAddToPostNewAccount_WhenOnlySetInCollection_ShouldBeNothingAdded() {
        Comment comment5 = entityManager.find(Comment.class, 5L); // without relation with Post
        Post post = entityManager.find(Post.class, 1L);

        List<Comment> comments = post.getComments();
        comments.add(comment5);

        post.getComments().clear();
        post.getComments().addAll(comments);

        entityManager.persist(post);

        flushAndClean();

        Post postAfterPersist = entityManager.find(Post.class, 1L);
        assertThat(postAfterPersist.getComments().size(), equalTo(2)); // nothing added
    }

    @Test
    public void tryAddToPostNewAccount_WhenSetParentObjectForNewDeps_ShouldBeAdded() {
        Comment comment5 = entityManager.find(Comment.class, 5L); // without relation with Post
        Post post = entityManager.find(Post.class, 1L);

        // set parent
        comment5.setPost(post);

        entityManager.persist(comment5);

        flushAndClean();

        Post postAfterPersist = entityManager.find(Post.class, 1L);
        assertThat(postAfterPersist.getComments().size(), equalTo(3)); // added new
    }
}