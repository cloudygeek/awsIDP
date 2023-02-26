package uk.co.acta.awsidp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetCallerIdentityResult {
    @JsonProperty("Account")
    private long account;
    @JsonProperty("UserId")
    private String userId;
    @JsonProperty("Arn")
    private String arn;

}
