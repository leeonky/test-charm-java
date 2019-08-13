package com.github.leeonky.map.spec.map;

import com.github.leeonky.map.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class PolymorphicMappingViaView {
    private final Mapper mapper = new Mapper(getClass().getPackage().getName());
    private final Transaction paypalTransaction = new PaypalTransaction()
            .setPaypalId("001")
            .setId("P1");
    private final Transaction creditCardTransaction = new CreditCardTransaction()
            .setCardNumber("6602")
            .setId("P2");
    private final TransactionLogs transactionLogs = new TransactionLogs().setTransactions(asList(paypalTransaction, creditCardTransaction));

    private final TransactionLogMap transactionLogMap = new TransactionLogMap().setTransactionMap(new HashMap<String, Transaction>() {{
        put(paypalTransaction.getId(), paypalTransaction);
        put(creditCardTransaction.getId(), creditCardTransaction);
    }});

    @Test
    void support_property_polymorphic_mapping_via_mapping_view() {
        TransactionLog transactionLog = new TransactionLog().setTransaction(paypalTransaction);

        assertThat(((DetailTransactionLogDTO) mapper.map(transactionLog, Detail.class)).transaction)
                .isInstanceOf(SimplePaypalTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");
    }

    @Test
    void support_property_collection_element_polymorphic_mapping_via_mapping_view() {
        List<SimpleTransactionDTO> transactionList = ((TransactionLogListDTO) mapper.map(transactionLogs, TransactionLogListDTO.class)).transactions;

        assertThat(transactionList).hasSize(2);
        assertThat(transactionList.get(0))
                .isInstanceOf(SimpleTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");
        assertThat(transactionList.get(1))
                .isInstanceOf(SimpleCreditCardTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P2")
                .hasFieldOrPropertyWithValue("cardNumber", "6602");

        SimpleTransactionDTO[] transactionArray = ((TransactionLogArrayDTO) mapper.map(transactionLogs, TransactionLogArrayDTO.class)).transactions;

        assertThat(transactionArray).hasSize(2);
        assertThat(transactionArray[0])
                .isInstanceOf(SimpleTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");
        assertThat(transactionArray[1])
                .isInstanceOf(SimpleCreditCardTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P2")
                .hasFieldOrPropertyWithValue("cardNumber", "6602");
    }

    @Test
    void support_property_map_element_polymorphic_mapping_via_mapping_view() {
        Map<String, SimpleTransactionDTO> transactionMap = ((TransactionLogMapDTO) mapper.map(transactionLogMap, TransactionLogMapDTO.class)).transactionMap;

        assertThat(transactionMap).hasSize(2);
        assertThat(transactionMap.get("P1"))
                .isInstanceOf(SimpleTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P1")
                .hasFieldOrPropertyWithValue("paypalId", "001");
        assertThat(transactionMap.get("P2"))
                .isInstanceOf(SimpleCreditCardTransactionDTO.class)
                .hasFieldOrPropertyWithValue("id", "P2")
                .hasFieldOrPropertyWithValue("cardNumber", "6602");
    }

    @Test
    void should_return_all_candidate_class_for_super_class_and_view() {
        assertThat(mapper.findSubMappings(SimpleTransactionDTO.class, Simple.class))
                .containsOnly(SimplePaypalTransactionDTO.class, SimpleCreditCardTransactionDTO.class);
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
    public static class PaypalTransaction extends Transaction {
        private String paypalId;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CreditCardTransaction extends Transaction {
        private String cardNumber;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TransactionLog {
        private Transaction transaction;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TransactionLogs {
        private List<Transaction> transactions;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class TransactionLogMap {
        private Map<String, Transaction> transactionMap;
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

    @MappingFrom(TransactionLogs.class)
    static class TransactionLogListDTO {

        @MappingView(Simple.class)
        public List<SimpleTransactionDTO> transactions;
    }

    @MappingFrom(TransactionLogs.class)
    static class TransactionLogArrayDTO {

        @MappingView(Simple.class)
        public SimpleTransactionDTO[] transactions;
    }

    @MappingFrom(TransactionLogMap.class)
    static class TransactionLogMapDTO {

        @MappingView(Simple.class)
        public Map<String, SimpleTransactionDTO> transactionMap;
    }
}
