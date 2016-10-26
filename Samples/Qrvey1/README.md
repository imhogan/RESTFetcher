# Qrvey Sample 1
This sample demonstrates the use of RESTFetcher to automate survey creation and distribution, using the free tier of the qrvey.com API. To use this you will need
to get an API Key - see http

# Sample usage

A simple example is fetching data for all respondents to a Survey Monkey survey. You need to get the set of respondent ids, then call the API passing in the list of respondent ids.

The following CloudFormation template demonstrates this:
  
  https://s3-ap-northeast-1.amazonaws.com/au-com-thinkronicity-opencode-apne1/CloudFormation/au-com-thinkronicity-RESTFetcher-Sample-SurveyMonkey.template
  
  This template creates an instance of the Lambda function along with an S3 Bucket configured with a sample XML and properties file.
  
  The template loads the code and files from a bucket in the AWS region the Stack is being deployed in.   These buckets are readable by any AWS account.
  
  The sample implements two commands:
  
    GetSurveyList - this fetches details of all surveys in a Survey Monkey account and generates a PDF report listing these details, using Apache FOP amd emails the report.
    
    GetAllSurveys - this fetches the list of surveys and then gets the details for each survey, combining them into a single XML file and emailing the result. 
  
 To run the sample use the Actions > Configure Test Event menu item in the Lambda Function console and enter the following event JSON:
```JSON
{
  "S3_Bucket": "bucket-name-created-by-cloudformation",
  "S3_FilePrefix": "Test",
  "configurationURI": "s3://${S3_Bucket}/config/${S3_FilePrefix}SurveyMonkeyProcessing.properties",
  "command": "command-name",
  "TestID": "Test",
  "EmailSenderName": "Sender Name",
  "EmailSender": "Sender Email",
  "DeleteFile": "Dummy",
  "traceDebug": "true",
  "API_Key": "your-api-key",
  "authToken": "your-auth-token"
}
```

The following table explains the placeholders used in the above sample JSON.

| Placeholder | Usage |
| --- | --- |
|```bucket-name-created-by-cloudformation```|The bucket name created by the stack - this is shown in the CloudFormation stack output.|
|```command-name```|GetSurveyList or GetAllSurveys|
|```Sender Name```|The email sender name for the body of the email message.|
|```Sender Email```|The sender email address, eg "Sender Name\<Sender.Name@emaildomain\>".|
|```your-api-key```|Your Survey Monkey API key.|
|```your-auth-token```|The Authorisation Token for your Survey Monkey API key.|
   
 See https://developer.surveymonkey.com/api/v3/#getting-started for instructions on registering a Survey Monkey App. 
 
 
 Once created, you can use the App in Draft mode for 90 days to test the API functionality.
  
 Ian.Hogan@THINKronicity.com.au