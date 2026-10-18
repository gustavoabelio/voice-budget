package dio.budgeting.infrastructure.http;

import dio.budgeting.application.GetCategoryTotalUseCase;
import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryTotalEndpointTest {
    private TransactionRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() throws Exception {
        repository = mock(TransactionRepository.class);
        var controller = new TransactionController(
                mock(PersistTransactionUseCase.class),
                mock(ListTransactionsByCategoryUseCase.class),
                new GetCategoryTotalUseCase(repository),
                mock(TranscriptionModel.class),
                new ByteArrayResource("Assistente financeiro".getBytes(StandardCharsets.UTF_8)),
                mock(ChatClient.Builder.class, RETURNS_SELF),
                mock(TextToSpeechModel.class));
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnCategoryTotalAsJson() throws Exception {
        when(repository.findAllByCategory(Category.GROCERIES)).thenReturn(List.of(
                new Transaction("Mercado", 2550, Category.GROCERIES),
                new Transaction("Padaria", 1025, Category.GROCERIES)));

        mvc.perform(get("/transactions/GROCERIES/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.transactionCount").value(2))
                .andExpect(jsonPath("$.totalInReais").value(35.75));
    }

    @Test
    void shouldRejectUnknownCategory() throws Exception {
        mvc.perform(get("/transactions/UNKNOWN/total"))
                .andExpect(status().isBadRequest());
    }
}
