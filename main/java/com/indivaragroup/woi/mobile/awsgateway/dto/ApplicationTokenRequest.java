package com.indivaragroup.woi.mobile.awsgateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTokenRequest {

    @JsonProperty("X-Application-Token")
    private String xApplicationToken;
}
