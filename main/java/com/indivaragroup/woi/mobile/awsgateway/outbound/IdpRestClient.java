package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.ApiResponse;
import com.indivaragroup.woi.mobile.awsgateway.dto.AuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;

import java.io.IOException;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
@Component
public class IdpRestClient {

  private final IdpEndpoint idpEndpoint;

  public AuthorizationResponse authorizeRequest(String token) {

    Response<ApiResponse<AuthorizationResponse>> retrofitResponse = null;
    try {
      retrofitResponse = idpEndpoint.authorizeRequest(token).execute();
    } catch (IOException e) {
      log.error("Failed to call endpoint Authorize Request", e);
    }

    return ofNullable(retrofitResponse)
            .map(Response::body)
            .map(ApiResponse::getData)
            .orElse(null);
  }
}