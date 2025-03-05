package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.RintisValidateTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class QrisRestClient {

    private final QrisEndPoint qrisEndPoint;

    public SnapAbstractResponse validateTokenSignature(String token,
                                                       String timestamp,
                                                       String signature,
                                                       String requestUri,
                                                       String requestBody) {

        Response<SnapAbstractResponse> retrofitResponse = null;
        try {
            retrofitResponse =
                    qrisEndPoint.validateTokenSignature(token, timestamp, signature, requestUri, requestBody).execute();
        } catch (IOException e) {
            log.error("Failed to call endpoint rintis authentication api", e);
        }

        return ofNullable(retrofitResponse)
                .map(Response::body)
                .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
                .orElse(null);
    }

}
