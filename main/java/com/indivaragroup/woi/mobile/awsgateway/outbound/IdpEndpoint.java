package com.indivaragroup.woi.mobile.awsgateway.outbound;


import com.indivaragroup.woi.mobile.awsgateway.dto.ApiResponse;
import com.indivaragroup.woi.mobile.awsgateway.dto.AuthorizationResponse;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.CONTENT_TYPE_JSON;
import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.X_APPLICATION_TOKEN;

public interface IdpEndpoint {

  @Headers({CONTENT_TYPE_JSON})
  @POST("v1/auth/authorization")
  Call<ApiResponse<AuthorizationResponse>> authorizeRequest(
      @Header(X_APPLICATION_TOKEN) String token
  );
}
