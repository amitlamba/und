package com.und.config

//import ch.rasc.bsoncodec.time.LocalDateDateCodec
//import ch.rasc.bsoncodec.time.LocalDateTimeDateCodec
import org.springframework.cloud.stream.annotation.EnableBinding
import com.mongodb.MongoClient
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import com.mongodb.MongoClientOptions
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDbFactory


//@EnableBinding(EventStream::class)
class Config {


/*    var host:String = "192.168.0.109"

     var dbName:String = "eventdbdev"

    @Bean
    @Throws(Exception::class)
    fun mongoDbFactory(): MongoDbFactory {
        val registry = CodecRegistries.fromRegistries(
                CodecRegistries.fromCodecs(LocalDateTimeDateCodec()),
                CodecRegistries.fromCodecs(LocalDateDateCodec()),
                MongoClient.getDefaultCodecRegistry()
        )
        val options = MongoClientOptions
                .builder()
                .codecRegistry(registry)
                .build()
        return SimpleMongoDbFactory(MongoClient(host, options), dbName)
    }*/

/*    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        mapper.registerModule(JavaTimeModule())
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        return mapper
    }*/
}