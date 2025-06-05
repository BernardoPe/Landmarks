package pt.isel.cn.landmarks.app.subscriber;

/**
 * Functional interface for processing messages from the landmarks subscriber.
 */
@FunctionalInterface
public interface LandmarksProcessor {
    void processMessage(String requestId, String message, String photoName, String blobName, String bucketName);
}