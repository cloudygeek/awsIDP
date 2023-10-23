using Amazon.Lambda.Core;
using System;
using System.Diagnostics;
using System.Net.Http;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using Amazon;
using Amazon.Runtime;
using Amazon.Runtime.CredentialManagement;
using Amazon.Runtime.Internal;
using Amazon.Runtime.Internal.Auth;
using Amazon.Runtime.Internal.Util;
using Amazon.SecurityToken;
using Amazon.SecurityToken.Model;
using Amazon.SecurityToken.Model.Internal.MarshallTransformations;
using Aws4RequestSigner;

// Assembly attribute to enable the Lambda function's JSON input to be converted into a .NET class.
[assembly: LambdaSerializer(typeof(Amazon.Lambda.Serialization.SystemTextJson.DefaultLambdaJsonSerializer))]

namespace gciNet2;

public class Function
{

    /// <summary>
    /// IDP Test Client
    /// </summary>
    /// <param name="input"></param>
    /// <param name="context"></param>
    /// <returns></returns>
    public async Task<string> FunctionHandler(dynamic input, ILambdaContext context)
    {
        Console.WriteLine("Function Handler");

        await GetCallerID4("eu-west-2");

        return "OK";
    }


    static async Task GetCallerID()
    {

        Console.WriteLine("GCI\n");


        // Get SSO credentials from the information in the shared config file.
        //var ssoCreds = LoadSsoCredentials(profile);        

        var iamRequest = GetCallerIdentityRequestMarshaller.Instance.Marshall(new GetCallerIdentityRequest());
        var amazonSecurityTokenServiceConfig = new AmazonSecurityTokenServiceConfig { RegionEndpoint = RegionEndpoint.EUWest1 };

        //var credentials = new StoredProfileAWSCredentials(profile);

        using var stsClient = new AmazonSecurityTokenServiceClient(RegionEndpoint.EUWest1);

        //using var stsClient = new AmazonSecurityTokenServiceClient(amazonSecurityTokenServiceConfig);

        var request = new GetCallerIdentityRequest();
        var response = await stsClient.GetCallerIdentityAsync(request);

        Console.WriteLine($"Account: {response.Account}");
        Console.WriteLine($"Arn: {response.Arn}");
        Console.WriteLine($"UserId: {response.UserId}");

    }

    static async Task GetCallerID2()
    {

        Console.WriteLine("GCI-2\n");


        // Get SSO credentials from the information in the shared config file.
        //var ssoCreds = LoadSsoCredentials(profile);    
        string endpointUrl = "https://sts.eu-west-2.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15";

        string sessionToken = Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
        string apiKey = Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
        string apiSecret = Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");
        var creds = new EnvironmentVariablesAWSCredentials();

        //var creds =  new SessionAWSCredentials(apiKey, apiSecret, sessionToken);

        //Console.WriteLine($"getToken {getToken}");

        var amazonSecurityTokenServiceConfig = new AmazonSecurityTokenServiceConfig { RegionEndpoint = RegionEndpoint.EUWest2 };

        var iamRequest = GetCallerIdentityRequestMarshaller.Instance.Marshall(new GetCallerIdentityRequest());
        iamRequest.Endpoint = new Uri(endpointUrl);
        iamRequest.ResourcePath = "/";
        iamRequest.Headers.Add("x-amz-security-token", sessionToken);

        using var stsClient = new AmazonSecurityTokenServiceClient(creds, RegionEndpoint.EUWest2);

        // Sign the request
        var signer = new Amazon.Runtime.Internal.Auth.AWS4Signer();

        var metrics = new Amazon.Runtime.Internal.Util.RequestMetrics();

        signer.SignRequest(iamRequest, stsClient.Config, metrics, apiKey, apiSecret);

        //signer.Sign(iamRequest, stsClient.Config, metrics, creds.GetCredentials());
        Console.WriteLine("Path =  " + iamRequest.ResourcePath);

        foreach (var kvp in iamRequest.Headers)
        {
            Console.WriteLine($"Key: {kvp.Key} Value: {kvp.Value}");
        }

        using (var httpClient = new HttpClient())
        {
            var httpRequestMessage = new HttpRequestMessage
            {
                RequestUri = new Uri(iamRequest.Endpoint.ToString()), // your signed URL
                Method = HttpMethod.Get,
            };

            // Add headers, content and other necessary stuff to httpRequestMessage here

            var response = await httpClient.SendAsync(httpRequestMessage);
            var responseContent = await response.Content.ReadAsStringAsync();

            Console.WriteLine($"StatusCode: {response.StatusCode}");
            Console.WriteLine($"Response: {responseContent}");
        }

        //var base64EncodedIamRequestHeaders = Convert.ToBase64String(Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(iamRequest.Headers)));
        //Console.WriteLine(base64EncodedIamRequestHeaders);


    }

    static async Task GetCallerID3()
    {
        Console.WriteLine("GCI3");

        string sessionToken = Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
        string apiKey = Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
        string apiSecret = Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");

        string endpointUrl = "https://sts.eu-west-2.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15";
        var signer = new AWS4RequestSigner(apiKey, apiSecret);
        var content = new StringContent("{...}", Encoding.UTF8, "application/json");
        var request = new HttpRequestMessage
        {
            Method = HttpMethod.Get,
            RequestUri = new Uri(endpointUrl),
            //Content = content
        };

        request = await signer.Sign(request, "sts", "eu-west-2");


        foreach (var kvp in request.Headers)
        {
            Console.WriteLine($"Key: {kvp.Key} Value: {kvp.Value}");
        }

        var client = new HttpClient();
        var response = await client.SendAsync(request);

        var responseStr = await response.Content.ReadAsStringAsync();
        Console.WriteLine($"responseStr = {responseStr}");

    }


    static async Task GetCallerID4(string region)
    {
        Console.WriteLine("GCI4");
        string sessionToken = Environment.GetEnvironmentVariable("AWS_SESSION_TOKEN");
        string apiKey = Environment.GetEnvironmentVariable("AWS_ACCESS_KEY_ID");
        string apiSecret = Environment.GetEnvironmentVariable("AWS_SECRET_ACCESS_KEY");

        string endpointUrl = "https://sts.eu-west-2.amazonaws.com/?Action=GetCallerIdentity&Version=2011-06-15";

        //var credentials = new ImmutableCredentials(apiKey, apiSecret, sessionToken);
        var credentials = new EnvironmentVariablesAWSCredentials();

    

        var client = new HttpClient();
        var response = await client.GetAsync(
          endpointUrl,
          regionName: region,
          serviceName: "sts",
          credentials: credentials);

        Console.WriteLine(response);

        string responseBody = await response.Content.ReadAsStringAsync();
        Console.WriteLine(responseBody);


    }

}
