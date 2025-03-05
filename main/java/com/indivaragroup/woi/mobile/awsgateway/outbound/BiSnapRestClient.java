package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.ApplicationTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BiSnapRestClient {
    private final BiSnapEndpoint biSnapServiceEndpoint;

    public SnapAbstractResponse validateToken(String clientKey, ApplicationTokenRequest token) {
        Response<SnapAbstractResponse> retrofitResponse = null;
        try {
            retrofitResponse = biSnapServiceEndpoint.validateToken(clientKey, token).execute();
        } catch (IOException e) {
            log.error("Failed to call endpoint Authorize Request", e);
        }

        return Optional.ofNullable(retrofitResponse)
                .map(Response::body)
                .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
                .orElse(null);
    }

    public SnapAbstractResponse validateTokenb2b2c(String clientKey, ApplicationTokenRequest token) {
        Response<SnapAbstractResponse> retrofitResponse = null;
        try {
            retrofitResponse = biSnapServiceEndpoint.validateTokenb2b2c(clientKey, token).execute();
        } catch (IOException e) {
            log.error("Failed to call endpoint Authorize Request", e);
        }

        return Optional.ofNullable(retrofitResponse)
                .map(Response::body)
                .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
                .orElse(null);
    }

    public SnapAbstractResponse validateAsymmetricSignature(String signature, String timestamp, String clientKey) {
        Response<SnapAbstractResponse> retrofitResponse = null;
        try {
            retrofitResponse = biSnapServiceEndpoint.validateAsymmetricSignature(signature, timestamp, clientKey).execute();
        } catch (IOException e) {
            log.error("Failed to call endpoint validateAsymmetricSignature Request", e);
        }

        return Optional.ofNullable(retrofitResponse)
                .map(Response::body)
                .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
                .orElse(null);
    }

    public SnapAbstractResponse validateSymmetricSignature(String signature, String timestamp,
                                                           String clientKey, String externalId,
                                                           String requestBody, String urlEndpoint) {
        Response<SnapAbstractResponse> retrofitResponse = null;
        try {
            retrofitResponse = biSnapServiceEndpoint.validateSymmetricSignature(signature, timestamp,
                    clientKey, externalId, requestBody, urlEndpoint).execute();
        } catch (IOException e) {
            log.error("Failed to call endpoint validateSymmetricSignature Request", e);
        }

        return Optional.ofNullable(retrofitResponse)
                .map(Response::body)
                .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
                .orElse(null);
    }
}
