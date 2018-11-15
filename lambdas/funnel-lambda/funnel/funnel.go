package funnel

import (
	"errors"
	"sort"
)

type FunnelData struct {
	UserData    []UserData `json:"userData"`
	EventsOrder []string   `json:"eventsOrder"`
	MaxInterval int        `json:"maxInterval"`
}

type UserData struct {
	UserId       string            `json:"userId"`
	Chronologies []EventChronology `json:"chronologies"`
}

type EventChronology struct {
	Event      string `json:"event"`
	Attribute  string `json:"attribute"`
	Chronology []int  `json:"chronology"`
}

type Step struct {
	EventName string `json:"eventName"`
	Order     int    `json:"order"`
}

type FunnelStep struct {
	Event     Step   `json:"step"`
	Attribute string `json:"property"`
	Count     int    `json:"count"`
}

func CalculateFunnels(funnelData FunnelData) ([]FunnelStep, error) {
	if len(funnelData.UserData) == 0 {
		return make([]FunnelStep, 0), errors.New("No input data found")
	}

	resultMap := make(map[string]map[string]int)
	orderedEventMap := orderMap(funnelData.EventsOrder)
	for _, v := range funnelData.UserData {
		attributeData := groupByAttribute(v)
		for attribute, chronologies := range attributeData {
			sort.SliceStable(chronologies, func(i, j int) bool {
				return orderedEventMap[chronologies[i].Event] < orderedEventMap[chronologies[j].Event]
			})

			attributeData := calculateFunnelsForSteps(chronologies, funnelData.EventsOrder, funnelData.MaxInterval)
			mergeAttributeData(resultMap, attribute, attributeData)
		}
	}

	return buildResult(resultMap, orderedEventMap), nil
}

func orderMap(eventsOrder []string) map[string]int {
	result := make(map[string]int, len(eventsOrder))

	index := 1
	for _, event := range eventsOrder {
		result[event] = index
		index++
	}
	return result
}

func groupByAttribute(userData UserData) map[string][]EventChronology {
	result := make(map[string][]EventChronology)
	for _, chronology := range userData.Chronologies {
		if result[chronology.Attribute] == nil {
			result[chronology.Attribute] = make([]EventChronology, 0)
		}
		result[chronology.Attribute] = append(result[chronology.Attribute], chronology)
	}
	return result
}

func calculateFunnelsForSteps(chronology []EventChronology, eventsOrder []string, maxInterval int) map[string]int {
	validChronologies := make([][]int, 0)
	for i := 0; i < len(chronology); i++ {
		if chronology[i].Event == eventsOrder[i] {
			validChronologies = append(validChronologies, chronology[i].Chronology)
		} else {
			//in case of any event missing from chronologies, no valid funnel can be longer than it
			break
		}
	}

	result := make(map[string]int, len(validChronologies))
	for i := 0; i < len(validChronologies); i++ {
		result[eventsOrder[i]] = numOfFunnels(validChronologies[:i+1], maxInterval)
	}

	return result
}

func mergeAttributeData(result map[string]map[string]int, attribute string, attributeData map[string]int) {
	if result[attribute] == nil {
		result[attribute] = attributeData
		return
	}
	for event, count := range attributeData {
		result[attribute][event] = result[attribute][event] + count
	}
}

func buildResult(dataMap map[string]map[string]int, orderMap map[string]int) []FunnelStep {
	result := make([]FunnelStep, 0)
	for attribute, eventCount := range dataMap {
		for event, count := range eventCount {
			result = append(result, FunnelStep{Event: Step{EventName: event, Order: orderMap[event]}, Attribute: attribute, Count: count})
		}
	}
	return result
}

func numOfFunnels(chronology [][]int, maxInterval int) int {
	if len(chronology) == 0 {
		return 0
	}

	if len(chronology) == 1 {
		return len(chronology[0])
	}

	numOfSteps := len(chronology)
	indexHolder := make([]int, numOfSteps)

	for i := 0; i < numOfSteps; i++ {
		indexHolder[i] = len(chronology[i]) - 1
	}

	totalFunnels := 0
	lastStep := chronology[numOfSteps-1]
	noMoreFunnels := false
	for i := len(lastStep) - 1; i >= 0; i-- {
		//Move all the steps till these steps are behind the last step time
		lastStepTime := lastStep[i]
		for j := numOfSteps - 2; j >= 0; j-- {
			for indexHolder[j] >= 0 && chronology[j][indexHolder[j]] >= lastStepTime {
				indexHolder[j]--
			}

			if indexHolder[j] < 0 {
				noMoreFunnels = true
				break
			}
			lastStepTime = chronology[j][indexHolder[j]]
		}

		if noMoreFunnels {
			break
		}

		//Now try to form a funnel
		index := numOfSteps - 2
		lastStepTime = lastStep[i]
		remainingTime := maxInterval - (lastStepTime - chronology[index][indexHolder[index]])
		if remainingTime >= 0 && index == 0 {
			totalFunnels++
			continue
		}

		for index = index - 1; index >= 0 && remainingTime >= 0; index-- {
			remainingTime = remainingTime - (chronology[index+1][indexHolder[index+1]] - chronology[index][indexHolder[index]])
		}

		if remainingTime >= 0 && index == -1 {
			totalFunnels++
		}

	}

	return totalFunnels
}
