# Email Monitor Sample
This sample demonstrates the use of RESTFetcher to choreograph a collection of microservices to deliver an email list server.
This sample requires an Amazon Web Services (AWS) account - see https://aws.amazon.com/ - and 
you will need to verify one of your email addresses with AWS SES (Simple Email Service) - see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/verify-email-addresses.html 
for details on this. Note, to send to non-verified email addresses - eg to send out emails to subscribers - you will need to move your account out of sandbox mode. See http://docs.aws.amazon.com/ses/latest/DeveloperGuide/request-production-access.html for details on doing that. 

# Sample usage -- WARNING - Sample still under construction.

The initial step is to get SES event monitoring working.
 ... 
 
 

|Stack Link  |Stack Purpose  | 
|--|--| 
|[Tool Stack](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/CloudFormation/ian-macdonald-hogan-RESTFetcher-Sample-EmailMonitor1-01-base-v1.0.0.template) | This loads the base Lambda tools annd creates the S3 bucket. | 
|[Files Stack](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/CloudFormation/ian-macdonald-hogan-RESTFetcher-Sample-EmailMonitor1-02-files-v1.0.0.template) | This loads the application files into the S3 bucket. | 
|[API Stack](https://s3-ap-southeast-2.amazonaws.com/au-com-thinkronicity-opencode-apse2/CloudFormation/ian-macdonald-hogan-RESTFetcher-Sample-EmailMonitor1-01-apigateway-v1.0.0.template) | This creates the API Gateway services and initialises the system. | 

 
 
 Enjoy!
 
 Ian_MacDonald_Hogan@yahoo.com
 
 # Step by step 
 
 *To be continued ...*
 
 
 TODO: 
 * Deactivate debug mode in post-processing page
 * Document and test installation procedure
 * Add simple handler for New Residents form - issue PDF letter?
 
