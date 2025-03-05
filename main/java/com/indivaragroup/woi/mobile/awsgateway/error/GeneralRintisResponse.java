package com.indivaragroup.woi.mobile.awsgateway.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneralRintisResponse {
    private ResponseStatus responseStatus;
    private MerchantTransactionStatusResponse merchantTransactionStatusResponse;
    private ConsumerData consumerData;
    private MerchantData merchantData;
    private TransactionData transactionData;
    private Object res;

    @Getter
    @Setter
    public static class ResponseStatus {
        private String reason;
        private String responseCode;
    }

    @Getter
    @Setter
    public static class MerchantTransactionStatusResponse {
        private String invoiceNumber;
    }

    @Getter
    @Setter
    public static class ConsumerData {
        private String cpan;
        private String issuerId;
    }

    @Getter
    @Setter
    public static class MerchantData {
        private String mpan;
        private String merchantId;
        private String categoryCode;
    }

    @Getter
    @Setter
    public static class TransactionData {
        private String additionalData;
        private String additionalDataNational;
        private String amountFee;
        private String approvalCode;
        private String captureDate;
        private String currency;
        private String localTransactionDate;
        private String localTransactionTime;
        private String settlementDate;
        private String pointOfServiceEntryMode;
        private String processingCode;
        private String rrn;
        private String stan;
        private String transmissionDateTime;
        private Integer totalAmount;
        private CardAcceptorData cardAcceptorData;
        private SenderId senderId;
    }

    @Getter
    @Setter
    public static class CardAcceptorData {
        private String cardAcceptorCity;
        private String cardAcceptorCountryCode;
        private String cardAcceptorName;
        private String cardAcceptorTerminalID;
    }

    @Getter
    @Setter
    public static class SenderId {
        private String acquiringInstitutionId;
        private String forwardingInstitutionId;
    }

}
