package com.indivaragroup.woi.mobile.awsgateway.outbound;

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
public class CardlessRestClient {

  private final CardlessEndpoint cardlessEndpoint;

  public SnapAbstractResponse authorizeRequest(String token) {

    Response<SnapAbstractResponse> retrofitResponse = null;
    try {
      retrofitResponse = cardlessEndpoint.authorizeRequest(token).execute();
    } catch (IOException e) {
      log.error("Failed to call endpoint Authorize Request", e);
    }

    return ofNullable(retrofitResponse)
            .map(Response::body)
            .map(response -> new SnapAbstractResponse(response.getResponseCode(), response.getResponseMessage()))
            .orElse(null);
  }
}