package com.indivaragroup.woi.mobile.awsgateway.config;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.woi.mobile.awsgateway.outbound.IdpEndpoint;
import lombok.Data;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Data
@Configuration
@ConfigurationProperties(prefix = "outbound.idp.http-client")
public class IdpHttpClientConfiguration {

  private String baseUrl;
  private long connectTimeoutInMilliseconds;
  private long readTimeoutInMilliseconds;
  private String logLevel;
  @Bean
  public OkHttpClient idpHttpClient(IdpHttpClientConfiguration configuration) {
    final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    interceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(configuration.getLogLevel()));

    return new OkHttpClient
        .Builder().addInterceptor(interceptor)
        .connectTimeout(configuration.getConnectTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
        .readTimeout(configuration.getReadTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
        .writeTimeout(configuration.getReadTimeoutInMilliseconds(), TimeUnit.MILLISECONDS)
        .build();
  }

  @Bean
  public Retrofit idpRetrofit(
      IdpHttpClientConfiguration configuration,
      @Qualifier("idpHttpClient") OkHttpClient httpClient
  ) {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature());
    Retrofit.Builder builder = new Retrofit.Builder();
    builder.client(httpClient);
    builder.baseUrl(configuration.getBaseUrl());
    builder.addConverterFactory(JacksonConverterFactory.create(objectMapper));

    return builder.build();
  }

  @Bean
  public IdpEndpoint idpEndpoint(
      @Qualifier("idpRetrofit") Retrofit retrofit
  ) {

    return retrofit.create(IdpEndpoint.class);
  }
}
