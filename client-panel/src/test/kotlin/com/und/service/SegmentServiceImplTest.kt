package com.und.service

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@Ignore
@RunWith(SpringRunner::class)
@SpringBootTest
class SegmentServiceImplTest {

    @Autowired
    lateinit var eventMetadataService:EventMetadataService
    
    @Test
    fun getSegmentationUsersTestWithDummy() {
/*        val basicSegmentationServiceImpl = SegmentServiceImpl()
        val dummyEventUser = basicSegmentationServiceImpl.getSegmentationUsers(1)
        println(dummyEventUser)
        var eventmetadata = eventMetadataService.getEventMetadata()
        println(eventmetadata)*/

    }
}