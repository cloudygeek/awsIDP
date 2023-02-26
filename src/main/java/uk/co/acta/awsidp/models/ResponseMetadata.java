package uk.co.acta.awsidp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResponseMetadata {
    @JsonProperty("RequestId")
    private String requestId;

}

