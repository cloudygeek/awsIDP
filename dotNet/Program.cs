// See https://aka.ms/new-console-template for more information

using Amazon;
using Amazon.Runtime;

using Amazon.SecurityToken;
using Amazon.SecurityToken.Model;
using Amazon.SecurityToken.Model.Internal.MarshallTransformations;
using Amazon.Lambda.Core;
using System;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

using AWS.Lambda.Powertools.Logging;

[assembly: LambdaSerializer(typeof(Amazon.Lambda.Serialization.SystemTextJson.DefaultLambdaJsonSerializer))]


namespace awsIDPtest;
public class Adrian

{
    [Logging(LogEvent = true)]
      public async Task<string> FunctionHandler(dynamic input, ILambdaContext context)
    {
            //Console.WriteLine(DateTime.Now + " Starting FunctionHandler");
         Logger.LogInformation("Starting FunctionHandler");
         //Console.WriteLine(input);

        string result = await signCallerIdentity();
        return result;
    }

      static async Task<string> signCallerIdentity()
    {        
        Console.WriteLine("Starting signCallerIdentity");
        // Initialize the Amazon Security Token Service client
        var stsClient = new AmazonSecurityTokenServiceClient(RegionEndpoint.EUWest1);
          //var stsClient = new AmazonSecurityTokenServiceClient(new StoredProfileAWSCredentials("AdministratorAccess-405378602119"), RegionEndpoint.EUWest1);
      var signedURL = "";


        try
        {            
           var getCallerIdentityRequest = new GetCallerIdentityRequest();
        //var request = new Amazon.Runtime.Internal.DefaultRequest(getCallerIdentityRequest, "AWSSecurityTokenServiceV2");

        //request.Endpoint = new Uri(stsClient.Config.DetermineServiceURL());
        //request.HttpMethod = "GET";
        //request.ResourcePath = "/";

        // Add parameters required for the GetCallerIdentity request
        //request.Parameters["Action"] = getCallerIdentityRequest.GetType().Name;
        //request.Parameters["Version"] = "2011-06-15";

         var credentials = FallbackCredentialsFactory.GetCredentials();
        // Instantiate RequestMetrics
        var metrics = new Amazon.Runtime.Internal.Util.RequestMetrics();

        // Sign the request
        //Amazon.Runtime.Internal.Auth.AWS4Signer signer = new Amazon.Runtime.Internal.Auth.AWS4Signer();
        //signer.Sign(request, stsClient.Config, metrics, credentials.GetCredentials().AccessKey, credentials.GetCredentials().SecretKey);        

        // Generate the pre-signed URL
        //var presignedUrl = Amazon.Runtime.AmazonServiceClient.ComposeUrl(request);


var iamRequest = GetCallerIdentityRequestMarshaller.Instance.Marshall(new GetCallerIdentityRequest());
   var amazonSecurityTokenServiceConfig = new AmazonSecurityTokenServiceConfig();

         
iamRequest.Endpoint = new Uri(amazonSecurityTokenServiceConfig.DetermineServiceURL());
iamRequest.ResourcePath = "/";

iamRequest.Headers.Add("User-Agent", "https://github.com/rajanadar/vaultsharp/0.11.1000");
iamRequest.Headers.Add("X-Amz-Security-Token", credentials.GetCredentials().Token);
iamRequest.Headers.Add("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

Amazon.Runtime.Internal.Auth.AWS4Signer signer = new();
signer.Sign(iamRequest, amazonSecurityTokenServiceConfig, metrics, credentials.GetCredentials().AccessKey, credentials.GetCredentials().SecretKey);

// This is the point, when you have the final set of required Headers.
var iamSTSRequestHeaders = iamRequest.Headers;
Console.WriteLine(iamSTSRequestHeaders);

 string jsonString = JsonSerializer.Serialize(iamSTSRequestHeaders);
 Console.WriteLine(jsonString);

//var base64EncodedIamRequestHeaders = Convert.ToBase64String(Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(iamSTSRequestHeaders)));



        //Console.WriteLine("Pre-signed URL: " + presignedUrl.AbsoluteUri);   
        //Console.WriteLine("Pre-signed URL: " + presignedUrl);   
        
        }
        catch (AmazonSecurityTokenServiceException e)
        {
            Console.WriteLine("Error: " + e.Message);
              Logger.LogError(e.Message);
        }
        Logger.LogInformation("signedURL = " + signedURL);
        return signedURL;
    }
    static async Task<string> getCallerIdentity()
    {        
        Console.WriteLine(DateTime.Now + " Starting");
        // Initialize the Amazon Security Token Service client
        var stsClient = new AmazonSecurityTokenServiceClient(RegionEndpoint.EUWest1);
          //var stsClient = new AmazonSecurityTokenServiceClient(new StoredProfileAWSCredentials("AdministratorAccess-405378602119"), RegionEndpoint.EUWest1);
      var arn = "";

        // Create and initialize a GetCallerIdentityRequest object
        var request = new GetCallerIdentityRequest();

        // Make the GetCallerIdentity API call
        try
        {            
            var response = await stsClient.GetCallerIdentityAsync(request);

            // Print the caller identity information
            Console.WriteLine("Account: " + response.Account);
            Console.WriteLine("UserId: " + response.UserId);
            Console.WriteLine("Arn: " + response.Arn);
            arn = response.Arn;
        }
        catch (AmazonSecurityTokenServiceException e)
        {
            Console.WriteLine("Error: " + e.Message);
              Logger.LogError(e.Message);
        }
        return arn;
    }
}
