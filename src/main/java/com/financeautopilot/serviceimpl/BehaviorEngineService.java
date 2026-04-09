package com.financeautopilot.serviceimpl;

import com.financeautopilot.dto.response.NudgeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.financeautopilot.model.Transaction;
import org.springframework.stereotype.Service;
import com.financeautopilot.repository.BudgetRepository;
import com.financeautopilot.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Service
@Slf4j
@RequiredArgsConstructor
public class BehaviorEngineService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;

    // called after every new debit transaction, checks 4 things in order - returns first nudge found
    public NudgeResponse evaluate(Transaction transaction, Long userId) {

        if(!"DEBIT".equals(transaction.getType().name())) {
            return NudgeResponse.builder().hasNudge(false).build();
        }

        String category = transaction.getCategory() != null
                ? transaction.getCategory().name()
                : "OTHER";

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        // check 1 - budget warning - used 80% of food budget
        NudgeResponse budgetNudge = checkBudget(userId, category, monthStart, today);
        if(budgetNudge.getHasNudge()) return budgetNudge;

        // check 2 - freq warning - ordered food 5 times this week
        NudgeResponse freqNudge = checkFrequency(userId, category, weekStart, today);
        if(freqNudge.getHasNudge()) return freqNudge;

        // check 3 - daily spend awareness - spent 900 today
        NudgeResponse dailyNudge = checkDailySpend(userId, today);
        if(dailyNudge.getHasNudge()) return dailyNudge;

        // check 4 - monthly category total - spent 6000 on food this month
        NudgeResponse monthlyNudge = checkMonthlyTotal(userId, category, monthStart, today);
        if(monthlyNudge.getHasNudge()) return monthlyNudge;

        return NudgeResponse.builder().hasNudge(false).build();
    }

    private NudgeResponse checkBudget(Long userId, String category,
                                      LocalDate start, LocalDate end) {
        return budgetRepository.findByUserIdAndCategory(userId, category)
                .map(budget -> { BigDecimal spent = transactionRepository
                            .sumByUserAndCategoryAndDateRange(userId, category, start, end);

                    BigDecimal limit = budget.getMonthlyLimit();
                    double percentage = spent.divide(limit, 4, RoundingMode.HALF_UP)
                            .doubleValue() * 100;

                    if (percentage >= 100) {
                        return NudgeResponse.builder()
                                .hasNudge(true)
                                .nudgeType("OVERSPEND")
                                .message(String.format(
                                        "You've exceeded your %s budget! Spent ₹%.0f of ₹%.0f limit.",
                                        category.toLowerCase(), spent, limit))
                                .build();
                    } else if (percentage >= 80) {
                        return NudgeResponse.builder()
                                .hasNudge(true)
                                .nudgeType("BUDGET_WARNING")
                                .message(String.format(
                                        "You've used %.0f%% of your %s budget. ₹%.0f remaining.",
                                        percentage, category.toLowerCase(),
                                        limit.subtract(spent)))
                                .build();
                    }
                    return NudgeResponse.builder().hasNudge(false).build();
                })
                .orElse(NudgeResponse.builder().hasNudge(false).build());
    }

    // frequency alert
    private NudgeResponse checkFrequency(Long userId, String category,
                                         LocalDate weekStart, LocalDate today) {
        Long count = transactionRepository
                .countByUserAndCategoryAndDateRange(userId, category, weekStart, today);

        // Trigger if ordered same category 5+ times this week
        if (count >= 5) {
            return NudgeResponse.builder()
                    .hasNudge(true)
                    .nudgeType("FREQUENCY_ALERT")
                    .message(String.format(
                            "You've made %d %s transactions this week. That's quite frequent.",
                            count, category.toLowerCase()))
                    .build();
        }
        return NudgeResponse.builder().hasNudge(false).build();
    }

    // daily spend awareness
    private NudgeResponse checkDailySpend(Long userId, LocalDate today) {
        BigDecimal spentToday = transactionRepository.sumSpentToday(userId, today);

        // Trigger if spent over ₹1000 in a single day
        if (spentToday.compareTo(new BigDecimal("1000")) > 0) {
            return NudgeResponse.builder()
                    .hasNudge(true)
                    .nudgeType("DAILY_LIMIT")
                    .message(String.format(
                            "You've already spent ₹%.0f today.", spentToday))
                    .build();
        }
        return NudgeResponse.builder().hasNudge(false).build();
    }

    // monthly category total awareness
    private NudgeResponse checkMonthlyTotal(Long userId, String category,
                                            LocalDate start, LocalDate end) {
        BigDecimal monthlyTotal = transactionRepository
                .sumByUserAndCategoryAndDateRange(userId, category, start, end);

        // only show if spent more than ₹500 in this category
        if (monthlyTotal.compareTo(new BigDecimal("500")) > 0) {
            return NudgeResponse.builder()
                    .hasNudge(true)
                    .nudgeType("MONTHLY_AWARENESS")
                    .message(String.format(
                            "You've spent ₹%.0f on %s this month.",
                            monthlyTotal, category.toLowerCase()))
                    .build();
        }
        return NudgeResponse.builder().hasNudge(false).build();
    }
}
