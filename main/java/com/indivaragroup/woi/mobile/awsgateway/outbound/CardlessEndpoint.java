package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.*;

public interface CardlessEndpoint {

  @Headers({CONTENT_TYPE_JSON})
  @POST("v1.0/validate-token/b2b")
  Call<SnapAbstractResponse> authorizeRequest(
      @Header(X_APPLICATION_TOKEN) String token
  );
}
