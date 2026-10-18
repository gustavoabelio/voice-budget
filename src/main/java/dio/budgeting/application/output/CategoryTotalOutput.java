package dio.budgeting.application.output;

import dio.budgeting.domain.Category;

import java.math.BigDecimal;

public record CategoryTotalOutput(Category category, int transactionCount, BigDecimal totalInReais) {
}
