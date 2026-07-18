package com.transactguard.transaction.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transactguard.transaction.domain.Account;
import com.transactguard.transaction.repository.AccountRepository;
import com.transactguard.transaction.repository.OutboxEventRepository;
import com.transactguard.transaction.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        accountRepository.save(new Account("acct_test_001", "USD", new BigDecimal("1000.00"), true));
    }

    @Test
    void exposesOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TransactGuard API"))
                .andExpect(jsonPath("$.paths['/api/v1/transactions/authorize']").exists());
    }

    @Test
    void missingRoutesReturnNotFoundInsteadOfUnexpectedError() throws Exception {
        mockMvc.perform(get("/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource was not found"));
    }

    @Test
    void authorizesTransactionAndPersistsOutboxEvent() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("idem-approve-0001", "100.00"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.transactionId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.riskDecision").value("APPROVE"));

        Account account = accountRepository.findByAccountId("acct_test_001").orElseThrow();
        assertThat(account.getAvailableBalance()).isEqualByComparingTo("900.00");
        assertThat(outboxEventRepository.count()).isEqualTo(1);
    }

    @Test
    void reusesExistingAuthorizationForSameIdempotentRequest() throws Exception {
        String firstResponse = mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("idem-repeat-0001", "50.00"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("idem-repeat-0001", "50.00"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode first = objectMapper.readTree(firstResponse);
        JsonNode second = objectMapper.readTree(secondResponse);

        assertThat(second.get("transactionId").asText()).isEqualTo(first.get("transactionId").asText());
        assertThat(transactionRepository.count()).isEqualTo(1);
        assertThat(accountRepository.findByAccountId("acct_test_001").orElseThrow().getAvailableBalance())
                .isEqualByComparingTo("950.00");
    }

    @Test
    void rejectsIdempotencyKeyReuseWithDifferentPayload() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("idem-conflict-0001", "50.00"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("idem-conflict-0001", "60.00"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Idempotency key was already used for a different request"));
    }

    @Test
    void rejectsRawPanInsteadOfTokenizedCardValue() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "acct_test_001",
                                  "cardToken": "4111111111111111",
                                  "merchantId": "merchant_urban_roast",
                                  "merchantCategoryCode": "5411",
                                  "merchantCountry": "US",
                                  "currency": "USD",
                                  "amount": 25.00,
                                  "idempotencyKey": "idem-invalid-0001"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.cardToken").exists());
    }

    private AuthorizeTransactionRequest request(String idempotencyKey, String amount) {
        return new AuthorizeTransactionRequest(
                "acct_test_001",
                "tok_testCard0001",
                "merchant_urban_roast",
                "5411",
                "US",
                "USD",
                new BigDecimal(amount),
                idempotencyKey
        );
    }
}
