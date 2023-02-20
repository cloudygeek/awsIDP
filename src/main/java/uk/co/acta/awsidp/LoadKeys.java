package uk.co.acta.awsidp;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
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
import java.util.*;

public class LoadKeys {

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
        getKeys();
    }


    //OctetKeyPair.parse(jwk.toJSONString());

    private static List<OctetKeyPair> getKeys() {
        setup();

        List<OctetKeyPair> keys = new ArrayList<>();

        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        try {
            ScanRequest request = ScanRequest.builder()
                    .tableName(tableName)
                    .build();

            ScanResponse response = ddb.scan(request);
            Logger.log("Received " + response.count() + " keys");
            for (Map<String, AttributeValue> dynKey : response.items()) {
                String exp = dynKey.get("exp").n();
                if (Long.parseLong(exp) < System.currentTimeMillis()){
                    Logger.log(dynKey.get("kid") + " key is expired " + exp);
                    continue;
                }
                OctetKeyPair keyPair = OctetKeyPair.parse(dynKey.get("key").s());

                keys.add(keyPair);
                Logger.log(keyPair.toPublicJWK().toJSONString());

            }

            Logger.log("Finished loading keys");
        } catch (ResourceNotFoundException e) {
            Logger.log("Error: The Amazon DynamoDB table " + tableName + " can't be found.");
            throw new RuntimeException(e);
        } catch (DynamoDbException e) {
            Logger.log("Error: DynamoDB " + e.getMessage());
            for (String keyV : itemValues.keySet()) {
                Logger.log("itemValues " + keyV + "=" + itemValues.get(keyV));
            }
            e.printStackTrace(System.err);
            throw new RuntimeException(e);
        } catch (Exception ex) {
            Logger.log("Error in DynDBLogger " + ex.getMessage());
            throw new RuntimeException(ex);
        }
        Logger.log("Returning " + keys.size() + " keys");
        return keys;

    }
}
