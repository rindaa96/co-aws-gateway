package com.indivaragroup.woi.mobile.awsgateway.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationResponse implements Serializable {

  private static final long serialVersionUID = 8137707848050754267L;

  private UUID id;

  private String username;
}
