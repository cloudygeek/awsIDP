package uk.co.acta.awsidp;

import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.model.StsRequest;

public class TestGCI {
    public static void main(String[] args){

//        AWSSecurityTokenServiceClientBuilder.standard().build().getCallerIdentity(new GetCallerIdentityRequest()).getAccount();



        //GetCallerIdentityRequest request = GetCallerIdentityRequest.builder().build();
        //StsRequest r =

        //StsAssumeRoleCredentialsProvider.builder().build();
        StsClient client = StsClient.builder().region(Region.EU_WEST_1).build();

        System.out.println(client.getCallerIdentity().arn());
        System.out.println(client.getCallerIdentity().account());



    }
}
