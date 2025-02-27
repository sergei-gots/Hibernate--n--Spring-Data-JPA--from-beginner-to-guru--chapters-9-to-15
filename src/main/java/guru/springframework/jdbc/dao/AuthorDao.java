package guru.springframework.jdbc.dao;

import guru.springframework.jdbc.domain.Author;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by sergei on 18/02/2025
 */
public interface AuthorDao {

    Author getById(Long id);

    Author findAuthorByName(String firstName, String lastName);

    List<Author> findAll(Pageable pageable);

    List<Author> findAllByLastNameSortByFirstName(String lastName, Pageable pageable);

    List<Author> findAllByLastNameLike(String lastName, Pageable pageable);

    Author saveNewAuthor(Author author);

    Author updateAuthor(Author author);

    void deleteById(Long id);
}
