package se.magnus.microservices.core.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

// SpringBootTest and @DataJpaTest are mutually exclusive
// When the @SpringBootTest annotation is active, all accesses are against the review-db
@SpringBootTest

// When the @DataJpaTest annotation is active, all accesses are against the
// embedded, in-memory H2 database.
// Note that for this to work, build.gradle needs to have the H2
// testImplementation active.
// (i.e., testImplementation('com.h2database:h2')

// @DataJpaTest

@Transactional(propagation = NOT_SUPPORTED)
public class PersistenceTests {

  @Autowired
  private ReviewRepository repository;

  private ReviewEntity savedEntity;

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();

    ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
    savedEntity = repository.save(entity);

    assertEqualsReview(entity, savedEntity);
  }

  @Test
  public void create() {

    ReviewEntity newEntity = new ReviewEntity(1, 3, "a", "s", "c");
    repository.save(newEntity);

    ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEqualsReview(newEntity, foundEntity);

    assertEquals(2, repository.count());
  }

  @Test
  public void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long) foundEntity.getVersion());
    assertEquals("a2", foundEntity.getAuthor());
  }

  @Test
  public void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  public void getByProductId() {
    List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

    assertThat(entityList, hasSize(1));
    assertEqualsReview(savedEntity, entityList.get(0));
  }

  // @Test(expected = DataIntegrityViolationException.class)
  // public void duplicateError() {
  // ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
  // repository.save(entity);
  // }

  @Test
  public void optimisticLockError() {

    // Store the saved entity in two separate entity objects
    ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
    ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

    // Update the entity using the first entity object
    entity1.setAuthor("a1");
    repository.save(entity1);

    // Update the entity using the second entity object.
    // This should fail since the second entity now holds a old version number, i.e.
    // a Optimistic Lock Error
    try {
      entity2.setAuthor("a2");
      repository.save(entity2);

      fail("Expected an OptimisticLockingFailureException");
    } catch (OptimisticLockingFailureException e) {
    }

    // Get the updated entity from the database and verify its new sate
    ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (int) updatedEntity.getVersion());
    assertEquals("a1", updatedEntity.getAuthor());
  }

  private void assertEqualsReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
    assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
    assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
    assertEquals(expectedEntity.getContent(), actualEntity.getContent());
  }
}