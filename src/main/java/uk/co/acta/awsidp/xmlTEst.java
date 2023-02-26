package uk.co.acta.awsidp;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import uk.co.acta.awsidp.models.GetCallerIdentityResponse;


import java.io.File;
import java.io.IOException;

public class xmlTEst {
    public static void main(String[] args) throws IOException {

        XmlMapper xmlMapper = new XmlMapper();
        File file = new File("/Users/adrian/IdeaProjects/CKOIDP/events/gciResponse.xml");

        GetCallerIdentityResponse callerIdentity = xmlMapper.readValue(file, GetCallerIdentityResponse.class);
        System.out.println(callerIdentity.toString());
        //Logger.log("arn = " + callerIdentity.getGetCallerIdentityResult().getArn());
    }
}
