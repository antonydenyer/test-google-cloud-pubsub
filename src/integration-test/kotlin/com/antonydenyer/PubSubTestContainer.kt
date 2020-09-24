package com.antonydenyer

import com.antonydenyer.PubSubTestContainer.pubsubEmulator
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings
import io.grpc.ManagedChannelBuilder
import mu.KotlinLogging
import org.testcontainers.containers.wait.strategy.Wait

object PubSubTestContainer {

    private val logger = KotlinLogging.logger {}

    val pubsubEmulator by lazy { startEmulator() }

    private fun startEmulator(): KGenericContainer {
        return KGenericContainer("gcr.io/google.com/cloudsdktool/cloud-sdk")
                .withExposedPorts(8085)
                .withCreateContainerCmdModifier {
                    it.withEntrypoint("gcloud", "beta", "emulators", "pubsub", "start", "--host-port=0.0.0.0:8085")
                }
                .waitingFor(Wait.forLogMessage(".*Server started.*", 1))
                .apply {
                    start()
                    followOutput {
                        logger.debug { it }
                    }
                }

    }

}

object PubSubClientFactory {

    val channelProvider: TransportChannelProvider by lazy {
        val channel = ManagedChannelBuilder
                .forAddress(pubsubEmulator.host, pubsubEmulator.firstMappedPort)
                .usePlaintext()
                .build()
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
    }


    val topicAdminClient: TopicAdminClient by lazy {
        TopicAdminClient
                .create(
                        TopicAdminSettings.newBuilder()
                                .setTransportChannelProvider(channelProvider)
                                .setCredentialsProvider(NoCredentialsProvider.create())
                                .build())
    }


    val subscriptionAdminClient: SubscriptionAdminClient by lazy {
        SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(NoCredentialsProvider.create())
                        .build()
        )

    }

    val subscriberStubSettings: SubscriberStubSettings = SubscriberStubSettings.newBuilder()
            .setCredentialsProvider(NoCredentialsProvider.create())
            .setTransportChannelProvider(channelProvider)
            .build()


}