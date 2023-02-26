package uk.co.acta.awsidp;




import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import uk.co.acta.awsidp.models.*;
import uk.co.acta.awsidp.util.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWKInterface implements RequestHandler<HashMap<String, Object>, String> {

    Gson gson = new GsonBuilder().create();

    private static void setup() {

    }

    public static void main(String[] args) {




    }


    //OctetKeyPair.parse(jwk.toJSONString());

    @Override
    public String handleRequest(HashMap<String, Object> event, Context context) {
        LambdaLogger logger = context.getLogger();
        Logger.setLogger(logger);

        logger.log("event = " + event.getClass());
        logger.log("event = " + event);
        logger.log("event body = " + event.get("body"));
        JWKRequest request = gson.fromJson((String) event.get("body"), JWKRequest.class);

        logger.log(request.toString());
        getARN(request);


        /*
        JWKRequest(
        url=https://sts.eu-west-2.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15,
            AuthorizationHeader={
              Authorization=[AWS4-HMAC-SHA256 Credential=AKIARTSHWZXNELZUM25U/20230226/eu-west-2/sts/aws4_request, SignedHeaders=host;x-amz-date, Signature=b3e942f9a22df8e7caf23b65d599cd72e864cb6e3d3cde9b0a5025a648b9f007],
                 Host=[sts.eu-west-2.amazonaws.com],
                 X-Amz-Date=[20230226T150105Z]
            }
         )
         */






        LambdaResponse response = new LambdaResponse();
        response.setStatusCode(200);
        response.setMessage("OK");
        String json = gson.toJson(response);
        return json;

    }

    private void getARN(JWKRequest request) {
        try {
            SdkHttpFullRequest signedRequest = SdkHttpFullRequest.builder()
                    .headers(request.getHeaders())
                    .host(request.getHost())
                    .uri(new URI(request.getUrl()))
                    .method(SdkHttpMethod.GET) ////SdkHttpMethod.fromValue()
                    .build();

            HttpExecuteRequest executeRequest = HttpExecuteRequest.builder()
                    .request(signedRequest)
                    .build();

            SdkHttpClient httpClient = ApacheHttpClient.builder().build();


            Logger.log("Calling AWS");
            HttpExecuteResponse executeResponse = httpClient.prepareRequest(executeRequest).call();

            Logger.log("Parsing Response");
            SdkHttpFullResponse response = SdkHttpFullResponse.builder()
                    .statusCode(executeResponse.httpResponse().statusCode())
                    .content(executeResponse.responseBody().get())
                    .headers(executeResponse.httpResponse().headers())
                    .build();


            Logger.log("statusCode = " + response.statusCode());
            String xml = new String(response.content().get().readAllBytes());
            Logger.log("body = " + xml);

            XmlMapper xmlMapper = new XmlMapper();
            GetCallerIdentityResponse callerIdentity = xmlMapper.readValue(xml, GetCallerIdentityResponse.class);
            Logger.log("arn = " + callerIdentity.getGetCallerIdentityResult().getArn());




        } catch (URISyntaxException e) {
            Logger.log("Error parsing URL " + request.getUrl());
            throw new RuntimeException(e);
        } catch (IOException e) {
            Logger.log("Error calling aws " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
