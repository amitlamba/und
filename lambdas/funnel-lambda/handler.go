package main

import (
	"funnel-lambda/funnel"
	"github.com/aws/aws-lambda-go/lambda"
)

func main() {
	// Make the handler available for Remote Procedure Call by AWS Lambda
	lambda.Start(funnel.CalculateFunnels)
}
