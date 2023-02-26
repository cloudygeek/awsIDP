package uk.co.acta.awsidp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetCallerIdentityResponse {


    //private String xmlns;

    @JsonProperty("GetCallerIdentityResult")
    private GetCallerIdentityResult getCallerIdentityResult;
    @JsonProperty("ResponseMetadata")
    private ResponseMetadata responseMetadata;
}

