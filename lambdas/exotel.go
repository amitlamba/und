package main

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/aws/aws-lambda-go/lambda"
	"log"
	"net/http"
)

var (
	// ErrNameNotProvided is thrown when a name is not provided
	ErrNameNotProvided = errors.New("no name was provided in the HTTP body")
)

func main() {
	lambda.Start(Handler)
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

type ExotelInput struct {
	Sid   string `json:"sid"`
	Token string `json:"token"`
	From  string `json:"from"`
	To    string `json:"to"`
	Body  string `json:"body"`
}

type ExotelResponse struct {
	Status  int    `json:"status"`
	Message string `json:"message"`
}

// Handler is your Lambda function handler
// It uses Amazon API Gateway request/responses provided by the aws-lambda-go/events package,
// However you could use other event sources (S3, Kinesis etc), or JSON-decoded primitive types such as 'string'.
func Handler(event ExotelInput) (ExotelResponse, error) {


	if event.Body == "" {
		return ExotelResponse{Status: 501, Message: "Body is empty"}, ErrNameNotProvided
	}

	log.Printf("Processing Lambda request %s\n", event)

	response := exotelApi(event)

	return response, nil

}

func toJsonHelper(resp *http.Response) (ExotelResponse) {
	if resp.StatusCode == 200 {
		// Fill the record with the data from the JSON
		var record smsMessageResponse

		// Use json.Decode for reading streams of JSON data
		if err := json.NewDecoder(resp.Body).Decode(&record); err != nil {
			log.Println(err)
		}

		log.Printf("Phone No. = %s", record.SMSMessage.From)
		log.Printf("Status = %d", record.SMSMessage.Status)
		return ExotelResponse{
			Status:  record.SMSMessage.Status,
			Message: "Message Sent",
		}

	} else {
		var errorism Exception

		// Use json.Decode for reading streams of JSON data
		if err := json.NewDecoder(resp.Body).Decode(&errorism); err != nil {
			log.Println(err)
		}

		log.Printf("Message = %s", errorism.RestException.Message)
		log.Printf("Status = %d", errorism.RestException.Status)
		return ExotelResponse{
			Status:  errorism.RestException.Status,
			Message: errorism.RestException.Message,
		}

	}
	//fmt.Println("Phone No. = ", record.SMSMessage.From)
	//fmt.Println("Phone No. = ", record.SMSMessage.From)

	// If no name is provided in the HTTP request body, throw an error
}

func exotelApi(input ExotelInput) (ExotelResponse) {
	sid := input.Sid
	token := input.Token
	from := input.From
	to := input.To
	body := input.Body
	log.Printf("hitting url https://%s:%s@api.exotel.com//v1/Accounts/%s/Sms/send.json?From=%s&To=%s&Body=%s", sid, token, sid, from, to, body)
	exotelUrl := fmt.Sprintf("https://%s:%s@api.exotel.com//v1/Accounts/%s/Sms/send.json?From=%s&To=%s&Body=%s", sid, token, sid, from, to, body)

	// Build the request
	req, err := http.NewRequest("POST", exotelUrl, nil)
	if err != nil {
		log.Fatal("NewRequest: ", err)
	}

	authgorizationHeader := base64.StdEncoding.EncodeToString([]byte(fmt.Sprintf("%s:%s", sid, token)))

	req.Header.Add("Authorization", "Basic "+authgorizationHeader)

	// For control over HTTP client headers,
	// redirect policy, and other settings,
	// create a Client
	// A Client is an HTTP client
	client := &http.Client{}
	// Send the request via a client
	// Do sends an HTTP request and
	// returns an HTTP response
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal("Do: ", err)
	}
	response := toJsonHelper(resp)
	// Callers should close resp.Body
	// when done reading from it
	// Defer the closing of the body
	defer resp.Body.Close()
	//b, _ := ioutil.ReadAll(resp.Body)

	log.Printf("response status %d ", response.Status)
	log.Printf("response message %s ", response.Message)
	return response

}

type smsMessageResponse struct {
	SMSMessage MessageResponse `json:"SMSMessage"`
}

type MessageResponse struct {
	Sid        string `json:"Sid"`
	AccountSid string `json:"AccountSid"`
	From       string `json:"From"`
	Status     int    `json:"Status"`
}
type Exception struct {
	RestException RestException `json:"RestException"`
}
type RestException struct {
	Status  int    `json:"Status"`
	Message string `json:"Message"`
}
