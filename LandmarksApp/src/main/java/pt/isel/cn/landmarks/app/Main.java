package pt.isel.cn.landmarks.app;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.StorageOptions;
import pt.isel.cn.landmarks.app.service.VisionService;
import pt.isel.cn.landmarks.app.subscriber.LandmarksSubscriber;
import pt.isel.cn.landmarks.storage.blob.BlobStorage;
import pt.isel.cn.landmarks.storage.blob.GoogleCloudStorage;
import pt.isel.cn.landmarks.storage.metadata.FirestoreMetadataStorage;
import pt.isel.cn.landmarks.storage.metadata.MetadataStorage;

public class Main {
    public static void main(String[] args) {
        BlobStorage blobStorage = new GoogleCloudStorage(StorageOptions.getDefaultInstance().getService());
        MetadataStorage metadataStorage = new FirestoreMetadataStorage(FirestoreOptions.getDefaultInstance().getService());
        LandmarksSubscriber subscriber = new LandmarksSubscriber();
        VisionService visionService = new VisionService();
        LandmarksApp app = new LandmarksApp(blobStorage, metadataStorage, subscriber, visionService);
        app.run();
    }
}
