package pt.isel.cn.landmarks.app;

import pt.isel.cn.landmarks.domain.Landmark;
import pt.isel.cn.landmarks.domain.LandmarkMetadata;
import pt.isel.cn.landmarks.domain.Status;
import pt.isel.cn.landmarks.app.service.VisionService;
import pt.isel.cn.landmarks.app.subscriber.LandmarksSubscriber;
import pt.isel.cn.landmarks.storage.blob.BlobStorage;
import pt.isel.cn.landmarks.storage.metadata.MetadataStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main application class for processing landmarks.
 * It subscribes to a message queue, analyzes images for landmarks,
 * and saves the results to a metadata storage.
 */
public class LandmarksApp {
    private final BlobStorage blobStorage;
    private final MetadataStorage metadataStorage;
    private final LandmarksSubscriber subscriber;
    private final VisionService visionService;

    private static final Logger logger = Logger.getLogger(LandmarksApp.class.getName());

    public LandmarksApp(BlobStorage blobStorage, MetadataStorage metadataStorage, LandmarksSubscriber subscriber, VisionService visionService) {
        this.blobStorage = blobStorage;
        this.metadataStorage = metadataStorage;
        this.subscriber = subscriber;
        this.visionService = visionService;
    }

    public void run() {
        subscriber.subscribe((requestId, photoId, photoName, blobName, bucketName) -> {
            String photoUrl = blobStorage.getPublicUrl(bucketName, blobName);
            processLandmarkDetection(requestId, photoUrl);
        });
    }

    private void processLandmarkDetection(String requestId, String photoUrl) {
        try {
            List<Landmark> landmarks = visionService.detectLandmarks(photoUrl);
            List<LandmarkMetadata> landmarkMetadataList = landmarks.stream()
                    .map(landmark -> new LandmarkMetadata(
                            landmark.name(),
                            landmark.location(),
                            landmark.confidence()
                    ))
                    .toList();
            metadataStorage.updateAnalysisMetadata(
                    requestId,
                    landmarkMetadataList,
                    Status.SUCCESS
            );
            logger.info("Landmarks detected: " + landmarkMetadataList.size());
        } catch (Exception e) {
            logger.severe("Error during landmark detection: " + e.getMessage());
            metadataStorage.updateAnalysisMetadata(
                    requestId,
                    new ArrayList<>(),
                    Status.FAILURE
            );
        }
    }
}