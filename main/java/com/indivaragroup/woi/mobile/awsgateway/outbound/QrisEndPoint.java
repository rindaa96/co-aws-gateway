package com.indivaragroup.woi.mobile.awsgateway.outbound;

import com.indivaragroup.woi.mobile.awsgateway.dto.RintisValidateTokenRequest;
import com.indivaragroup.woi.mobile.awsgateway.error.SnapAbstractResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.indivaragroup.woi.mobile.awsgateway.constant.Header.*;

public interface QrisEndPoint {

    @Headers({CONTENT_TYPE_JSON})
    @POST("v1/api/validate/token-signature")
    Call<SnapAbstractResponse> validateTokenSignature(
            @Header("TOKEN") String token,
            @Header(RTS_TIMESTAMP) String timestamp,
            @Header(RTS_SIGNATURE) String signature,
            @Header(REQUEST_URI) String requestUri,
            @Body String requestBody
    );

}
