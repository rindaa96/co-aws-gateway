package com.indivaragroup.woi.mobile.awsgateway.apipath;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RintisApi implements ApiEndpoint {
    VALIDATE_RINTIS_AUTH( "/v1/api/validate/token-signature"),
    QRIS_PAYMENT_CREDIT( "/v1/qris/payment/credit"),
    QRIS_CHECK_STATUS("/v1/qris/checkStatus"),
    QRIS_REFUND("/v1/qris/refund");

    private final String endPoint;

    public static RintisApi getByEndPoint(String endPointFromGateway) {
        for (RintisApi api : RintisApi.values()) {
            if (endPointFromGateway.contains(api.endPoint)) {
                return api;
            }
        }
        return null;
    }

}
