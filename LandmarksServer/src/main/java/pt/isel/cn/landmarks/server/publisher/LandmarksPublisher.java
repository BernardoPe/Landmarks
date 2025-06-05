package pt.isel.cn.landmarks.server.publisher;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import pt.isel.cn.landmarks.domain.Config;
import pt.isel.cn.landmarks.server.error.PublishError;

import java.util.logging.Logger;

public class LandmarksPublisher {
    private final static Logger logger = Logger.getLogger(LandmarksPublisher.class.getName());

    public void publish(
        String requestId,
        String photoId,
        String photoName
    ) {
        TopicName topicName = TopicName.ofProjectTopicName(Config.PROJECT_ID, Config.PHOTOS_BUCKET);
        try {
            Publisher publisher = Publisher.newBuilder(topicName).build();

            PubsubMessage message = PubsubMessage.newBuilder()
                    .putAttributes("requestId", requestId)
                    .putAttributes("photoId", photoId)
                    .putAttributes("photoName", photoName)
                    .putAttributes("blobName", photoId)
                    .putAttributes("bucketName", Config.PHOTOS_BUCKET)
                    .build();

            ApiFuture<String> future = publisher.publish(message);
            String messageId = future.get();

            logger.info("Message published with ID: " + messageId);

            publisher.shutdown();
        } catch (Exception e) {
            logger.severe("Error publishing message: " + e.getMessage());
            new PublishError();
        }
    }
}
