package com.und

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.web.model.Event
import com.und.web.model.GlobalFilter
import com.und.web.model.PropertyFilter
import com.und.web.model.Segment
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.io.BufferedReader
import java.time.Instant
import java.time.ZoneId
import java.util.*

//@RunWith(SpringRunner::class)
//@SpringBootTest
//@EnableSwagger2
class ApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun segment() {
        var json = "{\"didEvents\":{\"joinCondition\":{\"conditionType\":\"AllOf\",\"anyOfCount\":null},\"events\":[{\"name\":\"Search\",\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-12-20\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Search\",\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-12-20\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}},{\"name\":\"Search\",\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-12-20\"]},\"propertyFilters\":[],\"whereFilter\":{\"whereFilterName\":\"Count\",\"operator\":\"GreaterThan\",\"values\":[0]}}]},\"didNotEvents\":{\"joinCondition\":{\"conditionType\":\"AnyOf\",\"anyOfCount\":null},\"events\":[]},\"globalFilters\":[]," +
                "\"geographyFilters\":[{\"country\":{\"id\":\"102\",\"name\":\"Indonesia\"},\"state\":{\"id\":\"1691\",\"name\":\"Solo\"},\"city\":{\"id\":\"21536\",\"name\":\"Solo\"}}]" +
                "\"type\":\"Behaviour\"}"
        json = "{\"didEvents\":{\"events\":[{\"dateFilter\":{\"operator\":\"Before\",\"values\":[\"2018-08-06\"]},\"whereFilter\":{\"operator\":\"GreaterThan\",\"values\":[1],\"whereFilterName\":\"Count\"},\"name\":\"Search\",\"propertyFilters\":[{\"values\":[\"small\"],\"name\":\"Size\",\"type\":\"string\",\"operator\":\"Equals\"}]}],\"joinCondition\":{\"conditionType\":\"AllOf\"}}," +
                "\"didNotEvents\":{\"events\":[{\"dateFilter\":{\"operator\":\"After\",\"values\":[\"2018-07-01\"]},\"whereFilter\":{},\"name\":\"View\",\"propertyFilters\":[{\"values\":[\"small\"],\"name\":\"Size\",\"type\":\"string\",\"operator\":\"Equals\"}]}],\"joinCondition\":{\"conditionType\":\"AnyOf\"}},\"globalFilters\":[{\"values\":[\"Male\"]," +
                "\"globalFilterType\":\"Demographics\",\"name\":\"gender\",\"type\":\"string\",\"operator\":\"Equals\"}]," +
                "\"geographyFilters\":[{\"country\":{\"id\":\"102\",\"name\":\"Indonesia\"},\"state\":{\"id\":\"1691\",\"name\":\"Solo\"},\"city\":{\"id\":\"21536\",\"name\":\"Solo\"}}],\"type\":\"Behaviour\"}"
        var segment = ObjectMapper().readValue(json, Segment::class.java)

        var listOfAggregation = mutableListOf<AggregationOperation>()
        //geography match
        //already method present uncomment commented code and viceversa
//        var criteria=filterGeography(segment.geographyFilters,"userId")
//        var geographyAgg=Aggregation.match(criteria)

        var geographyCriterias = mutableListOf<Criteria>()
        segment.geographyFilters.forEach {
            geographyCriterias.add(Criteria().andOperator(Criteria("country").`is`(""), Criteria("state").`is`(""), Criteria("city").`is`("")))
        }
        var geographyAgg = Aggregation.match(Criteria().orOperator(*geographyCriterias.toTypedArray()))

        listOfAggregation.add(geographyAgg)

        //divide globalfilter into event and user
        var globalFilters = globalFilters(segment.globalFilters)
        if (!globalFilters.first.isEmpty())
            listOfAggregation.add(Aggregation.match(Criteria().andOperator(*globalFilters.first.toTypedArray())))

        //didnot
        var didnot = segment.didNotEvents?.events
        var listOfDidNotAgg = mutableListOf<AggregationOperation>()
        didnot?.forEachIndexed { index, event ->
            listOfDidNotAgg.add(Aggregation.match(Criteria().norOperator(Criteria().andOperator())))
        }
        listOfAggregation.addAll(listOfDidNotAgg)

        //didmatch
        var did = segment.didEvents?.events
        var didAgg = Aggregation.facet()
        did?.forEachIndexed { index, event ->
            didAgg = didAgg.and(Aggregation.match(Criteria().andOperator()), Aggregation.group(), Aggregation.match(Criteria())).`as`("pipe" + index)

        }
        var setIntersection: SetOperators.SetIntersection
        if (did!!.size >= 2) {
            setIntersection = SetOperators.SetOperatorFactory("pipe0").intersects("pipe1")

            did?.forEachIndexed { index, event ->
                if (index > 1) {
                    setIntersection = setIntersection.intersects("pipe" + index)
                }
            }
            var afterFacetStage = Aggregation.project().and(setIntersection).`as`("userId")
            var unwindAfterFacetStage = Aggregation.unwind("userId")

            listOfAggregation.add(didAgg)
            listOfAggregation.add(afterFacetStage)
            listOfAggregation.add(unwindAfterFacetStage)

        } else if (did!!.size == 1) {
            var project = Aggregation.project().and("pipe0").`as`("userId")
            var unwindAfterFacetStage = Aggregation.unwind("userId")
            listOfAggregation.add(didAgg)
            listOfAggregation.add(project)
            listOfAggregation.add(unwindAfterFacetStage)
        }
        //common to all after did
        var group = Aggregation.group("userId")
        var convertToOBjectId = ConvertOperators.ToObjectId.toObjectId("_id")
        var project = Aggregation.project().and(convertToOBjectId).`as`("_id")
        var sort = Aggregation.sort(Sort.Direction.ASC, "_id")
        var lookup = Aggregation.lookup("3_eventUser", "_id", "_id", "user")
        var unwindOperation = Aggregation.unwind("user")
        var replaceRootOperation = Aggregation.replaceRoot("user")

        listOfAggregation.add(group)
        listOfAggregation.add(project)
        listOfAggregation.add(sort)
        listOfAggregation.add(lookup)
        listOfAggregation.add(unwindOperation)
        listOfAggregation.add(replaceRootOperation)
        //eventuser match
        if (!globalFilters.second.isEmpty())
            listOfAggregation.add(Aggregation.match(Criteria().andOperator(*globalFilters.second.toTypedArray())))

        println(Aggregation.newAggregation(listOfAggregation))
    }

    fun globalFilters(globalFilter: List<GlobalFilter>): Pair<List<Criteria>, List<Criteria>> {

        return Pair(emptyList(), emptyList())
    }
}
