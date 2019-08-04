package com.github.leeonky.map.spec;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class PropertyViewMapping {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Transaction paypalTransaction = new PaypalTransaction()
            .setPaypalId("001")
            .setId("P1");
    private final Transaction creditCardTransaction = new CreditCardTransaction()
            .setCardNumber("6602")
            .setId("P2");

    @Test
    void support_property_polymorphic_mapping_via_mapping_view() {
        TransactionLog transactionLog = new TransactionLog().setTransaction(paypalTransaction);

        assertThat(((DetailTransactionLogDTO) mapper.map(transactionLog, Detail.class)).transaction)
                .isInstanceOf(SimplePaypalTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");
    }

    @Test
    void support_property_list_element_polymorphic_mapping_via_mapping_view() {
        TransactionLogs transactionLogs = new TransactionLogs().setTransactions(asList(paypalTransaction, creditCardTransaction));

        List<SimpleTransactionDTO> transactions = ((DetailTransactionLogsDTO) mapper.map(transactionLogs, Detail.class)).transactions;

        assertThat(transactions).hasSize(2);

        assertThat(transactions.get(0))
                .isInstanceOf(SimpleTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");

        assertThat(transactions.get(1))
                .isInstanceOf(SimpleCreditCardTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P2")
                .hasFieldOrPropertyWithValue("cardNumber", "6602");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class Transaction {
        private String id;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class PaypalTransaction extends Transaction {
        private String paypalId;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class CreditCardTransaction extends Transaction {
        private String cardNumber;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class TransactionLog {
        private Transaction transaction;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    static class TransactionLogs {
        private List<Transaction> transactions;
    }

    static abstract class SimpleTransactionDTO {
        public String id;
    }

    @Mapping(from = PaypalTransaction.class, view = Simple.class)
    static class SimplePaypalTransactionDTO extends SimpleTransactionDTO {
        public String paypalId;
    }

    @Mapping(from = CreditCardTransaction.class, view = Simple.class)
    static class SimpleCreditCardTransactionDTO extends SimpleTransactionDTO {
        public String cardNumber;
    }

    @Mapping(from = TransactionLog.class, view = Detail.class)
    static class DetailTransactionLogDTO {

        @MappingView(Simple.class)
        public SimpleTransactionDTO transaction;
    }

    @Mapping(from = TransactionLogs.class, view = Detail.class)
    static class DetailTransactionLogsDTO {

        @MappingView(Simple.class)
        public List<SimpleTransactionDTO> transactions;
    }
}
