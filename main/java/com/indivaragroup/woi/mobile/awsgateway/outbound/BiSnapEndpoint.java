package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.ApplicationTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import retrofit2.Call;
import retrofit2.http.*;

import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.*;

public interface BiSnapEndpoint {
    @Headers({CONTENT_TYPE_JSON})
    @POST("v1.0/validate-token/b2b")
    Call<SnapAbstractResponse> validateToken(@Header(X_CLIENT_KEY) String clientKey,
                                             @Body ApplicationTokenRequest token);

    @Headers({CONTENT_TYPE_JSON})
    @POST("v1.0/validate-token/b2b2c")
    Call<SnapAbstractResponse> validateTokenb2b2c(@Header(X_CLIENT_KEY) String clientKey,
                                                  @Body ApplicationTokenRequest token);

    @Headers({CONTENT_TYPE_JSON})
    @POST("v1.0/validate/asymmetric-signature")
    Call<SnapAbstractResponse> validateAsymmetricSignature(@Header(X_SIGNATURE) String signature,
                                                           @Header(X_TIMESTAMP) String timestamp,
                                                           @Header(X_CLIENT_KEY) String clientKey);

    @Headers({CONTENT_TYPE_JSON})
    @POST("v1.0/validate/symmetric-signature")
    Call<SnapAbstractResponse> validateSymmetricSignature(@Header(X_SIGNATURE) String signature,
                                                          @Header(X_TIMESTAMP) String timestamp,
                                                          @Header(X_PARTNER_ID) String partnerId,
                                                          @Header(X_EXTERNAL_ID) String externalId,
                                                          @Body String requestBody,
                                                          @Query("urlEndPoint") String urlEndpoint);
}
