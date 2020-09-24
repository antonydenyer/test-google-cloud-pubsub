package com.antonydenyer

import com.antonydenyer.PubSubClientFactory.channelProvider
import com.antonydenyer.PubSubClientFactory.subscriberStubSettings
import com.antonydenyer.PubSubClientFactory.subscriptionAdminClient
import com.antonydenyer.PubSubClientFactory.topicAdminClient
import com.google.api.gax.core.NoCredentialsProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub
import com.google.protobuf.ByteString
import com.google.pubsub.v1.AcknowledgeRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.PullRequest
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.TopicName
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test


internal class BasicTopicAdminServiceIntegrationTest {

    @Test
    fun `can get list of topics`() {
        topicAdminClient.createTopic(TopicName.of("project-id", "a-topic"))

        val topic = topicAdminClient.listTopics(ProjectName.of("project-id")).iterateAll().first()

        assertThat(topic.name, equalTo("projects/project-id/topics/a-topic"))
    }

    @Test
    fun `can publish to a topic`() {
        val data = ByteString.copyFromUtf8("payload")
        val pubsubMessage = PubsubMessage.newBuilder()
                .setData(data)
                .build()

        val topic = topicAdminClient.createTopic(TopicName.of("project-id", "another-topic"))

        val publisher = Publisher
                .newBuilder(topic.name)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setChannelProvider(channelProvider)
                .build()

        publisher.publish(pubsubMessage)
    }

    @Test
    fun `can fetch message from a subscriptions and acknowledge`() {
        val data = ByteString.copyFromUtf8("payload")

        val pubsubMessage = PubsubMessage
                .newBuilder()
                .setData(data)
                .build()

        val topic = topicAdminClient.createTopic(TopicName.of("project-id", "another-topic"))

        val subscription = subscriptionAdminClient.createSubscription(
                ProjectSubscriptionName.of("project-id", "subscription"),
                TopicName.parse(topic.name),
                PushConfig.getDefaultInstance(),
                300
        )

        val publisher = Publisher
                .newBuilder(topic.name)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .setChannelProvider(channelProvider)
                .build()

        publisher.publish(pubsubMessage)

        val pullRequest = PullRequest.newBuilder()
                .setMaxMessages(1)
                .setSubscription(subscription.name)
                .build()

        val subscriber = GrpcSubscriberStub.create(subscriberStubSettings)

        val pullResponse = subscriber.pullCallable().call(pullRequest)

        val acknowledgeRequest = AcknowledgeRequest.newBuilder()
                .setSubscription(subscription.name)
                .addAckIds(pullResponse.receivedMessagesList.first().ackId)
                .build()

        subscriber.acknowledgeCallable().call(acknowledgeRequest)

        assertThat(pullResponse.receivedMessagesList.first().message.data.toStringUtf8(), equalTo("payload"))
    }
}
