package com.und

import com.mongodb.BasicDBObjectBuilder
import com.mongodb.DBObject
import com.mongodb.MongoClient
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.junit4.SpringRunner

//@RunWith(SpringRunner::class)
//@DataMongoTest
class MongoEmbededTest {

    //@Autowired
    private lateinit var mongoTemplate: MongoTemplate
    @Before
    fun setup(){
        mongoTemplate = MongoTemplate(MongoClient("localhost", 27017), "test")
    }
    @Test
    fun test() {
        // given
        val objectToSave = BasicDBObjectBuilder.start()
                .add("key", "value")
                .get()

        // when
        mongoTemplate.save(objectToSave, "collection")

        // then
        //        assertThat(mongoTemplate.findAll<T>(DBObject.class, "collection")).extracting("key")
        //                .containsOnly("value");

        val obj = mongoTemplate.findAll(DBObject::class.java, "collection")
        val size = obj.size
        assertEquals(2, size.toLong())
    }
}