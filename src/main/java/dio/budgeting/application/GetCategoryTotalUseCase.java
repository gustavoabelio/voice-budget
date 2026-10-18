package dio.budgeting.application;

import dio.budgeting.application.output.CategoryTotalOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GetCategoryTotalUseCase {
    private final TransactionRepository transactionRepository;

    public GetCategoryTotalUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "get-category-total", description = "Consulta a quantidade e o total de gastos de uma categoria. O campo totalInReais já está em reais.")
    public CategoryTotalOutput execute(@ToolParam(description = "Categoria dos gastos") Category category) {
        var transactions = transactionRepository.findAllByCategory(category);
        long totalInCents = 0;
        for (var transaction : transactions) {
            totalInCents = Math.addExact(totalInCents, transaction.getAmount());
        }

        return new CategoryTotalOutput(category, transactions.size(), BigDecimal.valueOf(totalInCents, 2));
    }
}
