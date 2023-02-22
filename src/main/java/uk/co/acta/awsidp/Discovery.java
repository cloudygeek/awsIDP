package uk.co.acta.awsidp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import uk.co.acta.awsidp.util.Logger;

public class Discovery implements RequestHandler<Object, String> {


    public static final String BASE_URL = "https://ida.acta.io";
    public static final String WELL_KNOWN_URL = ".well-known";
    public static final String OPENID_CONFIGURATION_URL = WELL_KNOWN_URL + "/openid-configuration";
    // .well-known/openid-configuration

    Gson gson = new GsonBuilder().create();

    @Override
    public String handleRequest(Object event, Context context) {
        LambdaLogger logger = context.getLogger();
        Logger.setLogger(logger);
        // log execution details
        //  logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        // logger.log("CONTEXT: " + gson.toJson(context));
        // process event
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass().toString());


        uk.co.acta.awsidp.models.Discovery discovery = new uk.co.acta.awsidp.models.Discovery();
        discovery.setIssuer(BASE_URL + "/");
        discovery.setAuthorization_endpoint(BASE_URL + "/authorize");
        discovery.setToken_endpoint(BASE_URL + "/token");
        discovery.setJwks_uri(BASE_URL + "/keys");
        discovery.addScope("okta_idp");
        discovery.addResponseType("token");
        discovery.addAuthMethod("aws_role");

        String json = gson.toJson(discovery);
        logger.log("JSON = " + json);

        return json;
    }
}
