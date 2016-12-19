# Qrvey Sample 2
This sample demonstrates the use of RESTFetcher to choreograph a collection of microservices to deliver an application for self-sign-up for email subscription to scheduled and ad-hoc surveys.
This sample uses the free tier of the qrvey.com API. To use this you will need to get an API Key - see http://www.qrvey.com/ for details on how to do this. You will also need an Amazon Web Services (AWS) account - see https://aws.amazon.com/ - and 
you will need to verify one of your email addresses with AWS SES (Simple Email Service) - see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/verify-email-addresses.html 
for details on this. Note, to send to non-verified email addresses - eg to send out Qrvey invitations - you will need to move your account out of sandbox mode. See http://docs.aws.amazon.com/ses/latest/DeveloperGuide/request-production-access.html for details on doing that. 

# Sample usage -- WARNING - Sample still under construction.

  The following CloudFormation template can be used to run this example in your AWS account using the CloudFormation Create Stack button, or using the AWS CLI.
  
  https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/CloudFormation/au-com-thinkronicity-RESTFetcher-Sample-Qrvey2-V1.0.0.template
  
  This template creates an instance of the RESTFetcher Lambda function along with an S3 Bucket configured with a sample XML and properties file. It also creates instances of the
  S3ZipFileLoader Lambda function (https://github.com/imhogan/S3ZipFileLoader) and S3PutFile Lambda function (https://github.com/imhogan/PutS3File), which are respectively used to extract project files from a ZIP and write configuration data.
  
  The template loads the code and files from a bucket in the AWS region the Stack is being deployed in.   These buckets are readable by any AWS account.
  
  The sample implements commands to allow the creation and management of Qrvey user accounts and Qrveys. The following diagram shows the use of the implemented sample.
  
![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/Sample1Processing.png "Sample Overview")

  The CloudFormation template parameters allow you to enter your Qrvey API Key and define system configuration parameters which are stored in file config/Qrvey1.properties in
  an S3 bucket created in your AWS account.
  
  When the CloudFormation script completes you will receive an email with a list of Qrvey templates to choose from and instructions on how to create Qrveys from these templates, and how to delete the Qrvey account created by the template.
  
  When you create a Qrvey from a template you will be emailed instructions on how to send additional invitations and emailed a link to the Qrvey results page.
 
 Enjoy!
 
 Ian_MacDonald_Hogan@yahoo.com
 
 # Step by step 
 
 The following screenshots should help to get you started with this sample - good luck!
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep1.png "Step 1")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep2.png "Step 2")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep3.png "Step 3")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep4.png "Step 4")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep5.png "Step 5")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep6.png "Step 6")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep7.png "Step 7")
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFStep8.png "Step 8")
 
 # Results 
 
 In S3 you will find your bucket has been created and populated with the configuration files:
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFResult1.png "Results 1")
 
 You should shortly receive an email with instructions:
 
 ![alt text](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/Qrvey1/img/CFResult2.png "Results 2")
