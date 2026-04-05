package repository;

import model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserId(Long id);
}
