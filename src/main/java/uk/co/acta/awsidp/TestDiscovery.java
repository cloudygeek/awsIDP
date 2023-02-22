package uk.co.acta.awsidp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import uk.co.acta.awsidp.models.Discovery;

import java.util.Map;

public class TestDiscovery implements RequestHandler<Object, String>{


    public static final String WELL_KNOWN_URL = ".well-known";
    public static final String OPENID_CONFIGURATION_URL = WELL_KNOWN_URL + "/openid-configuration";

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String handleRequest(Object event, Context context) {
        LambdaLogger logger = context.getLogger();

        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        // process event
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());


        Discovery discovery = new Discovery();
        discovery.setIssuer("https://idp.acta.io/");
        discovery.setAuthorization_endpoint("https://idp.acta.io/authorize");
        discovery.setToken_endpoint("https://idp.acta.io/token");
        discovery.setJwks_uri("https://idp.acta.io/keys");
        discovery.addScope("okta_idp");
        discovery.addResponseType("token");
        discovery.addAuthMethod("aws_role");

        String json = gson.toJson(discovery);
        logger.log("JSON = "  + json);


        return json;
    }
}
