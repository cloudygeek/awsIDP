package uk.co.acta.awsidp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;

import org.joda.time.DateTime;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.GenerateRandomRequest;
import software.amazon.awssdk.services.kms.model.GenerateRandomResponse;
import uk.co.acta.awsidp.util.Logger;

import java.security.SecureRandom;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class RotateKeys  implements RequestHandler<Object, String> {

    private static DynamoDbClient ddb;

    private static final String tableName = "idpKeys";

    private static void setup() {

        if (Logger.isLamba()) {
            if (ddb == null) {
                ddb = DynamoDbClient.builder()
                        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                        .region(Region.EU_WEST_2)
                        .build();
            }
        } else {
            if (ddb == null) {
                ddb = DynamoDbClient.builder()
                        .credentialsProvider(ProfileCredentialsProvider.create("default"))
                        .region(Region.EU_WEST_2)
                        .build();
            }
        }
    }

    public static void main(String[] args) {
        OctetKeyPair jwk = rotateKeys();
        if (jwk!=null) {
            storeKey(jwk);
        }
    }

    @Override
    public String handleRequest(Object event, Context context) {
        LambdaLogger logger = context.getLogger();
        Logger.setLogger(logger);
        OctetKeyPair jwk = rotateKeys();
        if (jwk!=null) {
            storeKey(jwk);
        }
        return "OK";
    }

    private static OctetKeyPair rotateKeys() {
        try {
            GenerateRandomRequest request = GenerateRandomRequest.builder().numberOfBytes(512).build();
            GenerateRandomResponse response;
            try (KmsClient client = KmsClient.builder().region(Region.EU_WEST_2).build()) {
                response = client.generateRandom(request);
            }

            byte[] seed = response.plaintext().asByteArray();

            Logger.log("Got random seed");
            SecureRandom secureRandom = new SecureRandom(seed);

            DateTime issuedAt = new DateTime();
            DateTime expiryTime = new DateTime();
            expiryTime = expiryTime.plusHours(96);
            String kid = UUID.randomUUID().toString();
            OctetKeyPair jwk = new OctetKeyPairGenerator(Curve.Ed25519)
                    .secureRandom(secureRandom)
                    .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key (optional)
                    .keyID(kid) // give the key a unique ID (optional)
                    .issueTime(issuedAt.toDate()) // issued-at timestamp (optional)
                    .expirationTime(expiryTime.toDate())
                    .generate();

            Logger.log("Created jwk");
            return jwk;

            //Base64URL x = jwk.getX();
            //Base64URL d = jwk.getD();
        } catch (JOSEException ex) {
            Logger.log("RotateKeys JOSEException " + ex.getMessage());
        }

        return null;

    }

    //OctetKeyPair.parse(jwk.toJSONString());

    private static void storeKey(OctetKeyPair key) {
        setup();

        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        try {
            itemValues.put("kid", AttributeValue.builder().s(key.getKeyID()).build());
            itemValues.put("key", AttributeValue.builder().s(key.toJSONString()).build());

            itemValues.put("inserted", AttributeValue.builder().n("" + new Date().getTime()).build());
            itemValues.put("iat", AttributeValue.builder().n("" + key.getIssueTime().getTime()).build());
            itemValues.put("exp", AttributeValue.builder().n("" + key.getExpirationTime().getTime()).build());

            Calendar expiry = Calendar.getInstance();
            expiry.add(Calendar.DAY_OF_YEAR, 7);
            itemValues.put("expireRecord", AttributeValue.builder().n("" + expiry.getTime().getTime()/1000).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(itemValues)
                    .conditionExpression("attribute_not_exists(kid)")
                    .build();

            ddb.putItem(request);

            Logger.log("Stored JWK in DDB");
        } catch (ResourceNotFoundException e) {
            Logger.log("Error: The Amazon DynamoDB table " + tableName + " can't be found.");
            throw new RuntimeException(e);
        } catch (ConditionalCheckFailedException e) {
            Logger.log(key.getKeyID() + " Already exists");
        } catch (DynamoDbException e) {
            Logger.log("Error: DynamoDB " + e.getMessage());
            for (String keyV : itemValues.keySet()) {
                Logger.log("itemValues " + keyV + "=" + itemValues.get(keyV));
            }
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        } catch (Exception ex) {
            Logger.log(key.getKeyID() + " Error in DynDBLogger " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
