package repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(@NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email);
}
