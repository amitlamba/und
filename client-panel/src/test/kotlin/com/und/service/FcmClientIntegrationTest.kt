package com.und.service

import com.fasterxml.jackson.annotation.JsonProperty
import de.bytefish.fcmjava.client.FcmClient
import de.bytefish.fcmjava.client.settings.PropertiesBasedSettings
import de.bytefish.fcmjava.model.options.FcmMessageOptions
import de.bytefish.fcmjava.model.topics.Topic
import de.bytefish.fcmjava.requests.topic.TopicUnicastMessage
import org.junit.Ignore
import org.junit.Test
import java.time.Duration

@Ignore
class FcmClientIntegrationTest {
    private inner class PersonData(@get:JsonProperty("firstName")
                                   val firstName: String, @get:JsonProperty("lastName")
                                   val lastName: String)

    @Test
//    @Ignore("This is an Integration Test using system properties to contact the FCM Server")
    @Throws(Exception::class)
    fun sendTopicMessageTest() {

        // Create the Client using system-properties-based settings:
        FcmClient(PropertiesBasedSettings.createFromDefault()).use { client ->

            // Message Options:
            val options = FcmMessageOptions.builder()
                    .setTimeToLive(Duration.ofHours(1))
                    .build()

            // Send a Message:
            val response = client.send(TopicUnicastMessage(options, Topic("news"), PersonData("Philipp", "Wagner")))

            // Assert Results:
            assert(response != null)

            // Make sure there are no errors:
            //assert(response.messageId != null)
            assert(response.errorCode == null)
        }
    }
}