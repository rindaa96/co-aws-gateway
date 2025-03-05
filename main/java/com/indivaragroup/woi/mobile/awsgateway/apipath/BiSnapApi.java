package com.indivaragroup.woi.mobile.awsgateway.apipath;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Getter
public enum BiSnapApi {
    ACCESS_TOKEN_B2B("73", "/merchant/api/v1.0/access-token/b2b"),
    ACCESS_TOKEN_B2B2C("74", "/merchant/api/v1.0/access-token/b2b2c"),
    VALIDATE_TOKEN_B2B("00", "/merchant/api/v1.0/validate-token/b2b"),
    VALIDATE_TOKEN_B2B2C("75", "/merchant/api/v1.0/validate-token/b2b2c"),
    DIRECT_DEBIT_PAYMENT("54", "/merchant/api/v1.0/debit/payment-host-to-host"),
    DIRECT_DEBIT_CANCEL("57", "/merchant/api/v1.0/debit/cancel"),
    DIRECT_DEBIT_REFUND("58", "/merchant/api/v1.0/debit/refund"),
    DIRECT_DEBIT_STATUS("58", "/merchant/api/v1.0/debit/status"),
    TRANSACTION_HISTORY_LIST("12", "/merchant/api/v1.0/transaction-history-list"),
    REGISTRATION_ACCOUNT_BINDING("07","/merchant/api/v1.0/registration-account-binding"),
    REGISTRATION_ACCOUNT_UNBINDING("09","/merchant/api/v1.0/registration-account-unbinding"),
    GET_AUTH_CODE("10", "/merchant/api/v1.0/get-auth-code"),
    QR_MPM_GENERATE("47", "/transaction-qris/api/v1.0/qr/qr-mpm-generate"),
    QR_MPM_QUERY("51", "/transaction-qris/api/v1.0/qr/qr-mpm-query"),
    QR_MPM_REFUND("78", "/transaction-qris/api/v1.0/qr/qr-mpm-refund"),
    QR_MPM_CANCEL("77", "/transaction-qris/api/v1.0/qr/qr-mpm-cancel"),
    VALIDATE_ASYMMETRIC_SIGNATURE("00", "/v1.0/validate/asymmetric-signature"),
    VALIDATE_SYMMETRIC_SIGNATURE("00", "/v1.0/validate/symmetric-signature"),
    BALANCE_INQUIRY("11", "/merchant/api/v1.0/balance-inquiry"),
    PAYMENT_CPM("60", "/transaction-qris/api/v1.0/qr/qr-cpm-payment"),
    REFUND_CPM("80", "/transaction-qris/api/v1.0/qr/qr-cpm-refund"),
    CANCEL_CPM("62", "/transaction-qris/api/v1.0/qr/qr-cpm-cancel");

    private final String serviceCode;
    private final String endpoint;

    public static List<String> getAllEndpoints() {
        return Arrays.stream(BiSnapApi.values())
                .map(BiSnapApi::getEndpoint)
                .collect(Collectors.toList());
    }

    public static BiSnapApi getByEndpoint(String endpoint) {
        return Stream.of(BiSnapApi.values())
                .filter(biSnapEndpoint -> endpoint.contains(biSnapEndpoint.getEndpoint()))
                .findFirst()
                .orElse(null);
    }
}
