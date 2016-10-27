# Qrvey Sample 1
This sample demonstrates the use of RESTFetcher to automate survey creation and distribution, using the free tier of the qrvey.com API. To use this you will need
to get an API Key - see http://www.qrvey.com/ for details on how to do this. You will also need an Amazon Web Services account - see https://aws.amazon.com/

# Sample usage

This example 
The following CloudFormation template demonstrates this:
  
  https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/CloudFormation/au-com-thinkronicity-RESTFetcher-Sample-Qrvey1-V1.0.0.template
  
  This template creates an instance of the RESTFetcher Lambda function along with an S3 Bucket configured with a sample XML and properties file.
  
  The template loads the code and files from a bucket in the AWS region the Stack is being deployed in.   These buckets are readable by any AWS account.
  
  The sample implements the commands to allow the creation and management of Qrvey user accounts and Qrveys. 

  The CloudFormation template parameters allow you to enter your Qrvey API Key and define system configuration parameters which are stored in file config/Qrvey1.properties in
  an S3 bucket created in your AWS account.
  
  When the CloudFormation script completes you will receive an email with a list of Qrvey templates to choose from and instructions on how to create Qrveys from these templates, and how to delete the Qrvey account created by the template.
  
  When you create a Qrvey from a template you get instructions on how to send additional invitations and a link to the Qrvey results page is also provded.
 
 Enjoy!
 
 Ian_MacDonald_Hogan@yahoo.com