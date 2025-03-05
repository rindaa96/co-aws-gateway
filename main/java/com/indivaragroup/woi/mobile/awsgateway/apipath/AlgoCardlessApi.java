package com.indivaragroup.woi.mobile.awsgateway.apipath;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlgoCardlessApi implements ApiEndpoint {
    VALIDATE_TOKEN_B2B("00", "/validate-token/b2b"),
    ACCESS_TOKEN_B2B("73", "/v1.0/access-token/b2b"),
    CANCEL_PAYMENT("46", "/v1.0/emoney/otc-cancel"),
    CREATE_PAYMENT("44", "/v1.0/emoney/otc-cashout");

    private final String code;
    private final String endPoint;

    public static AlgoCardlessApi getByEndPoint(String endPointFromGateway) {
        for (AlgoCardlessApi api : AlgoCardlessApi.values()) {
            if (endPointFromGateway.contains(api.endPoint)) {
                return api;
            }
        }
        return null; // or throw exception if needed
    }
}
