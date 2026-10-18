package dio.budgeting.application;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetCategoryTotalUseCaseTest {
    private TransactionRepository repository;
    private GetCategoryTotalUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        useCase = new GetCategoryTotalUseCase(repository);
    }

    @Test
    void shouldSumCategoryExpensesAndConvertCentsToReais() {
        when(repository.findAllByCategory(Category.GROCERIES)).thenReturn(List.of(
                new Transaction("Mercado", 2550, Category.GROCERIES),
                new Transaction("Padaria", 1025, Category.GROCERIES)));

        var result = useCase.execute(Category.GROCERIES);

        assertThat(result.category()).isEqualTo(Category.GROCERIES);
        assertThat(result.transactionCount()).isEqualTo(2);
        assertThat(result.totalInReais()).isEqualByComparingTo("35.75");
        verify(repository).findAllByCategory(Category.GROCERIES);
    }

    @Test
    void shouldReturnZeroWhenCategoryHasNoTransactions() {
        when(repository.findAllByCategory(Category.PHARMA)).thenReturn(List.of());

        var result = useCase.execute(Category.PHARMA);

        assertThat(result.category()).isEqualTo(Category.PHARMA);
        assertThat(result.transactionCount()).isZero();
        assertThat(result.totalInReais()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldPreserveSingleCentAmounts() {
        when(repository.findAllByCategory(Category.AUTO)).thenReturn(List.of(
                new Transaction("Ajuste 1", 1, Category.AUTO),
                new Transaction("Ajuste 2", 2, Category.AUTO)));

        var result = useCase.execute(Category.AUTO);

        assertThat(result.totalInReais()).isEqualByComparingTo("0.03");
    }

    @Test
    void shouldRejectSumThatExceedsLongCapacityInsteadOfReturningAnIncorrectTotal() {
        when(repository.findAllByCategory(Category.GROCERIES)).thenReturn(List.of(
                new Transaction("Limite", Long.MAX_VALUE, Category.GROCERIES),
                new Transaction("Mais um centavo", 1, Category.GROCERIES)));

        assertThatThrownBy(() -> useCase.execute(Category.GROCERIES))
                .isInstanceOf(ArithmeticException.class);
    }
}
