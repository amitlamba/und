package com.und.model.utils.fcm

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FcmMessageIos(
        //Targets
        /**
         * This parameter specifies the recipient of a message.
         * The value can be a device's registration token, a device group's notification key, or a single topic (prefixed with /topics/). To send to multiple topics, use the conditionparameter.
         */
        val to: String? = null,
        /**
         * This parameter specifies the recipient of a multicast message, a message sent to more than one registration token.
         * The value should be an array of registration tokens to which to send the multicast message. The array must contain at least 1 and at most 1000 registration tokens. To send a message to a single device, use the to parameter.
         * Multicast messages are only allowed using the HTTP JSON format.
         */
        val registration_ids: List<String>? = null,
        /**
         * This parameter specifies a logical expression of conditions that determine the message target.
         * Supported condition: Topic, formatted as "'yourTopic' in topics". This value is case-insensitive.
         * Supported operators: &&, ||. Maximum two operators per topic message supported.
         */
        val condition: String? = null,

        //Options
        /**
         * This parameter identifies a group of messages (e.g., with collapse_key: "Updates Available") that can be collapsed, so that only the last message gets sent when delivery can be resumed. This is intended to avoid sending too many of the same messages when the device comes back online or becomes active.
         * Note that there is no guarantee of the order in which messages get sent.
         * Note: A maximum of 4 different collapse keys is allowed at any given time. This means a FCM connection server can simultaneously store 4 different messages per client app. If you exceed this number, there is no guarantee which 4 collapse keys the FCM connection server will keep.
         */
        val collapse_key: String? = null,
        /**
         * Sets the priority of the message. Valid values are "normal" and "high." On iOS, these correspond to APNs priorities 5 and 10.
         * By default, notification messages are sent with high priority, and data messages are sent with normal priority. Normal priority optimizes the client app's battery consumption and should be used unless immediate delivery is required. For messages with normal priority, the app may receive the message with unspecified delay.
         * When a message is sent with high priority, it is sent immediately, and the app can wake a sleeping device and open a network connection to your server.
         * For more information, see Setting the priority of a message.
         */
        val priority: String? = null,
        /**
         * On iOS, use this field to represent content-available in the APNs payload. When a notification or message is sent and this is set to true, an inactive client app is awoken, and the message is sent through APNs as a silent notification and not through the FCM connection server. Note that silent notifications in APNs are not guaranteed to be delivered, and can depend on factors such as the user turning on Low Power Mode, force quitting the app, etc. On Android, data messages wake the app by default. On Chrome, currently not supported.
         */
        val content_available: Boolean? = null,
        /**
         * Currently for iOS 10+ devices only. On iOS, use this field to represent mutable-content in the APNs payload. When a notification is sent and this is set to true, the content of the notification can be modified before it is displayed, using a FcmMessage Service app extension. This parameter will be ignored for Android and web.
         */
        val mutable_content: Boolean? = null,
        /**
         * This parameter specifies how long (in seconds) the message should be kept in FCM storage if the device is offline. The maximum time to live supported is 4 weeks, and the default value is 4 weeks. For more information, see Setting the lifespan of a message.
         */
        val time_to_live: Int? = null,
        /**
         * This parameter specifies the package name of the application where the registration tokens must match in order to receive the message.
         */
        val restricted_package_name: String? = null,
        /**
         * This parameter, when set to true, allows developers to test a request without actually sending a message.
         * The default value is false.
         */
        val dry_run: Boolean? = null,

        //Payload
        /**
         * This parameter specifies the custom key-value pairs of the message's payload.
         * For example, with data:{"score":"3x1"}:
         * On iOS, if the message is sent via APNs, it represents the custom data fields. If it is sent via FCM connection server, it would be represented as key value dictionary in AppDelegate application:didReceiveRemoteNotification:.
         * On Android, this would result in an intent extra named score with the string value 3x1.
         * The key should not be a reserved word ("from" or any word starting with "google" or "gcm"). Do not use any of the words defined in this table (such as collapse_key).
         * Values in string types are recommended. You have to convert values in objects or other non-string data types (e.g., integers or booleans) to string.
         */
        val data: Map<String, String>? = null,
        /**
         * This parameter specifies the predefined, user-visible key-value pairs of the notification payload. See FcmMessage payload support for detail. For more information about notification message and data message options, see Message types. If a notification payload is provided, or the content_available option is set to true for a message to an iOS device, the message is sent through APNs, otherwise it is sent through the FCM connection server.
         */
        val notification: NotificationPayloadIos? = null
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class NotificationPayloadIos(
        val title: String? = null,
        val body: String? = null,
        val sound: String? = null,
        val badge: String? = null,
        val click_action: String? = null,
        val subtitle: String? = null,
        val body_loc_key: String? = null,
        val body_loc_args: List<String>? = null,
        val title_loc_key: String? = null,
        val title_loc_args: List<String>? = null
)
