package main

import (

	"github.com/aws/aws-lambda-go/lambda"
)

func main() {
	lambda.Start(sms.SmsHandler)
	/*	event := ExotelInput{
			Sid:   "shiv53",
			Body:  "HelloWorld",
			From:  "09513886363",
			To:    "7838540240",
			Token: "54743a3bab3609e08473ace008df1d64bcf998cd",
		}

		response, err := Handler(event)
		log.Print(response.)
		log.Print(err)*/

}
