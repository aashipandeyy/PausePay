package com.financeautopilot.repository;

import io.lettuce.core.dynamic.annotation.Param;
import com.financeautopilot.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccountId(Long accountId);

    // transactions in a date range for monthly summary
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.bankAccount.user.id = :userId
        AND t.date BETWEEN :startDate AND :endDate
        AND t.type = 'DEBIT'
        ORDER BY t.date DESC
    """)
    List<Transaction> findDebitsByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate")LocalDate endDate
    );

    // total spent in a category this month - behaviour engine
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.bankAccount.user.id = :userId
        AND t.category = :category
        AND t.date BETWEEN :startDate AND :endDate
        AND t.type = 'DEBIT'
    """)
    BigDecimal sumByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // how many times a category was used this week - freq detection
    @Query("""
        SELECT COUNT(t) FROM Transaction t
        WHERE t.bankAccount.user.id = :userId
        AND t.category = :category
        AND t.date BETWEEN :startDate AND :endDate
        AND t.type = 'DEBIT'
    """)
    Long countByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // total spent today
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.bankAccount.user.id = :userId
        AND t.date = :today
        AND t.type = 'DEBIT'
    """)
    BigDecimal sumSpentToday(
            @Param("userId") Long userId,
            @Param("today") LocalDate today
    );

    // all uncategorized transactions - for nightly job
    List<Transaction> findByCategoryAndBankAccountUserId(String category, Long userId);
}
