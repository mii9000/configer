# Configer

This library will listen to a SQS Queue for messages of S3 Event Notifications. When a message will be received it will read the configured file from S3 bucket and update the local configuration/properties file to match the properties in the S3 bucket.

This is meant to be used as a short-term/temporary solution to a more comprehensive solution like Consul KV + Consul Template.