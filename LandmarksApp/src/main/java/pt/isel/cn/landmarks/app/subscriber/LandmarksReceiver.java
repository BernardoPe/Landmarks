package pt.isel.cn.landmarks.app.subscriber;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;

/**
 * LandmarksReceiver is a class that implements the MessageReceiver interface to handle
 * messages received from a Google Cloud Pub/Sub subscription.
 * <p>
 * It processes the message by extracting relevant attributes and passing them to a LandmarksProcessor
 * for further processing.
 */
public class LandmarksReceiver implements MessageReceiver {
    private final LandmarksProcessor landmarksProcessor;

    public LandmarksReceiver(LandmarksProcessor landmarksProcessor) {
        this.landmarksProcessor = landmarksProcessor;
    }

    @Override
    public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
        String requestId = pubsubMessage.getAttributesOrDefault("requestId", "");
        String photoId = pubsubMessage.getAttributesOrDefault("photoId", "");
        String photoName = pubsubMessage.getAttributesOrDefault("photoName", "");
        String blobName = pubsubMessage.getAttributesOrDefault("blobName", "");
        String bucketName = pubsubMessage.getAttributesOrDefault("bucketName", "");

        landmarksProcessor.processMessage(requestId, photoId, photoName, blobName, bucketName);

        ackReplyConsumer.ack();
    }
}