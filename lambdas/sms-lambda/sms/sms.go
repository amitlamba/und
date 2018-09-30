package sms

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
)

var (
	ErrNameNotProvided = errors.New("no name was provided in the HTTP body")
)

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


func SmsHandler(event ExotelInput) (ExotelResponse, error) {

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

}

func exotelApi(input ExotelInput) (ExotelResponse) {
	sid := input.Sid
	token := input.Token
	from := input.From
	to := input.To
	body := input.Body
	exotelUrl := fmt.Sprintf("https://%s:%s@api.exotel.com//v1/Accounts/%s/Sms/send.json?From=%s&To=%s&Body=%s", sid, token, sid, from, to, body)
	log.Printf(exotelUrl)

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
