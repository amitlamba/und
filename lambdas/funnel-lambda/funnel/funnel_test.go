package funnel

import (
	"encoding/json"
	"testing"
)

func TestEmptyChronology(t *testing.T) {
	chronology := [][]int{}
	numOfFunnels := numOfFunnels(chronology, 60)
	if numOfFunnels != 0 {
		t.Errorf("Failed for empty chronology")
	}
}

func TestSingleStepChronology(t *testing.T) {
	chronology := [][]int{{1, 2, 3}}
	numOfFunnels := numOfFunnels(chronology, 30)
	if numOfFunnels != 3 {
		t.Errorf("Failed for single step chronology")
	}
}

func TestSimpleTwoStepChronology(t *testing.T) {
	chronology := [][]int{{10, 20, 30, 40}, {15, 25}}
	numOfFunnels := numOfFunnels(chronology, 30)
	// (10, 15) (20, 25)
	if numOfFunnels != 2 {
		t.Errorf("Failed for simple two step chronology, expected: %d, actual: %d", 2, numOfFunnels)
	}
}

func TestBoundaryTwoStepChronology1(t *testing.T) {
	chronology := [][]int{{10, 20, 30, 40}, {10}}
	numOfFunnels := numOfFunnels(chronology, 10)
	if numOfFunnels != 0 {
		t.Errorf("Failed for simple two step chronology, expected: %d, actual: %d", 0, numOfFunnels)
	}
}

func TestBoundaryTwoStepChronology2(t *testing.T) {
	chronology := [][]int{{10, 20, 30, 40}, {10, 20, 35}}
	numOfFunnels := numOfFunnels(chronology, 10)
	// (10, 20) (30, 35)
	if numOfFunnels != 2 {
		t.Errorf("Failed for simple two step chronology, expected: %d, actual: %d", 0, numOfFunnels)
	}
}

func TestComplexTwoStepChronology(t *testing.T) {
	chronology := [][]int{{10, 20, 30, 40}, {15, 25}}
	numOfFunnels := numOfFunnels(chronology, 30)
	// (10, 15) (20, 25)
	if numOfFunnels != 2 {
		t.Errorf("Failed for simple two step chronology, expected: %d, actual: %d", 2, numOfFunnels)
	}
}

func TestBoundaryThreeStepChronology1(t *testing.T) {
	chronology := [][]int{{10, 20, 30}, {20, 30}, {25}}
	numOfFunnels := numOfFunnels(chronology, 10)
	if numOfFunnels != 0 {
		t.Errorf("Failed for boundary three step chronology, expected: %d, actual: %d", 0, numOfFunnels)
	}
}

func TestBoundaryThreeStepChronology2(t *testing.T) {
	chronology := [][]int{{10, 20, 30}, {20, 30}, {25}}
	numOfFunnels := numOfFunnels(chronology, 15)
	// (10, 20, 25)
	if numOfFunnels != 1 {
		t.Errorf("Failed for boundary three step chronology, expected: %d, actual: %d", 1, numOfFunnels)
	}
}

func TestThreeStepChronology(t *testing.T) {
	//funnels = (30, 40, 45) (60, 65, 70) (65, 75, 80)
	chronology := [][]int{{10, 20, 30, 50, 60, 65, 95}, {10, 25, 30, 40, 60, 65, 75, 95, 100}, {15, 45, 50, 70, 80, 85, 90}}
	numOfFunnels := numOfFunnels(chronology, 15)
	if numOfFunnels != 3 {
		t.Errorf("Failed for three step chronology, expected: %d, actual: %d", 3, numOfFunnels)
	}
}

func TestFunnel(t *testing.T) {
	userData := UserData{UserId: "1001", Chronologies: []EventChronology{EventChronology{Attribute: "all", Event: "search", Chronology: []int{10, 20, 30, 40, 50}}, EventChronology{Attribute: "all", Event: "view", Chronology: []int{15, 25, 45, 65}},
		EventChronology{Attribute: "all", Event: "charged", Chronology: []int{30, 50, 70}}}}

	funnelData := FunnelData{UserData: []UserData{userData}, MaxInterval: 30, EventsOrder: []string{"search", "view", "charged"}}

	jsonData, errorInInput := json.Marshal(funnelData)

	println(jsonData)

	if errorInInput != nil {
		panic(errorInInput)
	}

	funnelSteps, errorInOutput := CalculateFunnels(funnelData)
	if errorInOutput != nil {
		panic(errorInOutput)
	}

	if len(funnelSteps) != 3 {
		t.Errorf("Failed for Funnel, expected: %d, actual: %d", 3, len(funnelSteps))
	}

}
