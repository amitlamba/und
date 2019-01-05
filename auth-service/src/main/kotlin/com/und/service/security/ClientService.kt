package com.und.service.security


import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.mongodb.MongoClient
import com.und.common.utils.TenantProvider
import com.und.model.jpa.security.Client
import com.und.model.mongo.CommonMetadata
import com.und.repository.jpa.ClientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File


@Service
@Transactional
class ClientService {

    @Autowired
    lateinit var clientRepository: ClientRepository

    @Value("\${und.metadata.userProperties}")
    lateinit var userPropertiesFile: String

    @Value("\${und.metadata.eventMetadata}")
    lateinit var eventMetadataFile: String


    fun save(client: Client): Client {
        return clientRepository.save(client)
    }

    fun findByEmail(email: String): Client? {
        return clientRepository.findByEmail(email)
    }

    fun findById(id: Long): Client {
        val clientOption = clientRepository.findById(id)
        return if (clientOption.isPresent) clientOption.get() else Client()
    }

    fun updateClient(client: Client): Boolean {
        val clientId = client.id
        if (clientId != null) {
            val savedClient = findById(clientId)
            savedClient.firstname = client.firstname ?: savedClient.firstname
            savedClient.lastname = client.lastname ?: savedClient.lastname
            savedClient.phone = client.phone ?: savedClient.phone
            savedClient.address = client.address ?: savedClient.address
            clientRepository.save(savedClient)
        }
        return clientId != null
    }

    fun insertMetadata(clientId:Long) {
        val userProperties = File(userPropertiesFile).readText(Charsets.UTF_8)
        clientRepository.saveUserProperties(clientId, userProperties)
        //val eventMetadata = File(eventMetadataFile).readText(Charsets.UTF_8)
        //clientRepository.saveEventMetadta(clientId, eventMetadata)

    }

}

fun main(args: Array<String>) {

    val userProperties = """
        {userProperties :    [
  {
    "name": "Demographics",
    "properties": [
      {
        "name": "age",
        "dataType": "string",
        "options": [

        ]
      },
      {
        "name": "gender",
        "dataType": "string",
        "options": [
          "M",
          "F"
        ]
      }
    ]
  },
  {
    "name": "Technographics",
    "properties": [
      {
        "name": "browser",
        "dataType": "string",
        "options": [
          "Chrome",
          "Firefox",
          "Internet Explorer",
          "Mobile Application",
          "Opera",
          "Others",
          "Safari",
          "Sea Monkey",
          "UC Browser"
        ]
      },
      {
        "name": "os",
        "dataType": "string",
        "options": [
          "Android",
          "Blackberry",
          "ios",
          "Linux",
          "Mac",
          "Others",
          "Windows"
        ]
      },
      {
        "name": "device",
        "dataType": "string",
        "options": [
          "Desktop",
          "Mobile",
          "Tablet",
          "TV"
        ]
      }
    ]
  },
  {
    "name": "Reachability",
    "properties": [
      {
        "name": "hasDeviceToken",
        "dataType": "boolean",
        "options": [
          "Yes",
          "No"
        ]
      },
      {
        "name": "hasEmailAddress",
        "dataType": "boolean",
        "options": [
          "Yes",
          "No"
        ]
      },
      {
        "name": "hasPhoneNumber",
        "dataType": "boolean",
        "options": [
          "Yes",
          "No"
        ]
      },
      {
        "name": "unsubscribedPush",
        "dataType": "boolean",
        "options": [
          "Yes",
          "No"
        ]
      },
      {
        "name": "unsubscribedEmail",
        "dataType": "Boolean",
        "options": [
          "Yes",
          "No"
        ]
      },
      {
        "name": "unsubscribedSMS",
        "dataType": "boolean",
        "options": [
          "Yes",
          "No"
        ]
      }
    ]
  },
  {
    "name": "AppFields",
    "properties": [
      {
        "name": "appversion",
        "dataType": "string",
        "options": [

        ]
      },
      {
        "name": "make",
        "dataType": "string",
        "options": [

        ]
      },
      {
        "name": "model",
        "dataType": "string",
        "options": [

        ]
      },
      {
        "name": "os",
        "dataType": "string",
        "options": [

        ]
      },
      {
        "name": "sdkversion",
        "dataType": "string",
        "options": [

        ]
      }
    ]
  }
]}
        """.trimIndent()

   // val metadata : List<CommonMetadata> = jacksonObjectMapper().readValue(userProperties)
    //val parsed = BasicDBList()
    //metadata.forEach { s -> parsed.add(s) }
    //println(parsed)
    val dbObject :DBObject = BasicDBObject.parse(userProperties) as DBObject
    val parsed = dbObject.get("userProperties") as BasicDBList
    //print(dbObject)
    val client :MongoClient = MongoClient("192.168.0.109:27017")
    val template : MongoTemplate = MongoTemplate(client, "1_test")
    TenantProvider().setTenat("111")
    template.insert(parsed,  CommonMetadata::class.java)
}