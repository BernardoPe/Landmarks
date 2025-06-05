package pt.isel.cn.landmarks.storage.metadata;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import pt.isel.cn.landmarks.domain.AnalysisMetadata;
import pt.isel.cn.landmarks.domain.Config;
import pt.isel.cn.landmarks.domain.LandmarkMetadata;
import pt.isel.cn.landmarks.domain.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FirestoreMetadataStorage implements MetadataStorage {
    private final Firestore firestore;

    public FirestoreMetadataStorage(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public void saveAnalysisMetadata(String requestId, AnalysisMetadata metadata) {
        WriteBatch batch = firestore.batch();

        batch.set(
                firestore.collection(Config.METADATA_COLLECTION)
                        .document(requestId),
                new AnalysisMetadataDTO(
                        metadata.photoId(),
                        metadata.photoName(),
                        metadata.status()
                )
        );

        if (metadata.landmarks() != null) {
            for (int i = 0; i < metadata.landmarks().size(); i++) {
                LandmarkMetadata landmark = metadata.landmarks().get(i);
                batch.set(
                        firestore.collection(Config.METADATA_COLLECTION)
                                .document(requestId)
                                .collection("landmarks")
                                .document(requestId + "_" + i),
                        landmark
                );
            }
        }

        batch.commit();
    }

    @Override
    public void updateAnalysisMetadata(String requestId, List<LandmarkMetadata> landmarks, Status status) {
        WriteBatch batch = firestore.batch();

        batch.update(
                firestore.collection(Config.METADATA_COLLECTION)
                        .document(requestId),
                "status", status
        );

        if (landmarks != null) {
            for (int i = 0; i < landmarks.size(); i++) {
                LandmarkMetadata landmark = landmarks.get(i);
                batch.set(
                        firestore.collection(Config.METADATA_COLLECTION)
                                .document(requestId)
                                .collection("landmarks")
                                .document(requestId + "_" + i),
                        landmark
                );
            }
        }

        batch.commit();
    }

    @Override
    public AnalysisMetadata getAnalysisMetadata(String requestId) {
        try {
            AnalysisMetadataDTO metadata = firestore.collection(Config.METADATA_COLLECTION)
                    .document(requestId)
                    .get()
                    .get()
                    .toObject(AnalysisMetadataDTO.class);

            List<LandmarkMetadata> landmarks = firestore.collection(Config.METADATA_COLLECTION)
                    .document(requestId)
                    .collection("landmarks")
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(doc -> doc.toObject(LandmarkMetadata.class))
                    .toList();

            if (metadata == null) {
                return null;
            }

            return new AnalysisMetadata(
                    metadata.photoId(),
                    metadata.photoName(),
                    metadata.status(),
                    landmarks
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public AnalysisMetadata[] getAnalysisMetadataByConfidenceThreshold(double confidenceThreshold) {
        try {
            var landmarkDocs = firestore.collectionGroup("landmarks")
                    .whereGreaterThan("confidence", confidenceThreshold)
                    .get()
                    .get()
                    .getDocuments();

            var requestIds = landmarkDocs.stream()
                    .map(doc -> doc.getReference().getParent().getParent().getId())
                    .distinct()
                    .toList();

            var metadataFutures = firestore.getAll(
                    requestIds.stream()
                            .map(id -> firestore.collection(Config.METADATA_COLLECTION).document(id))
                            .toArray(DocumentReference[]::new)
            );

            var landmarkFutures = requestIds.stream()
                    .collect(Collectors.toMap(
                            id -> id,
                            id -> firestore.collection(Config.METADATA_COLLECTION)
                                    .document(id)
                                    .collection("landmarks")
                                    .get()
                    ));

            var metadataSnapshots = metadataFutures.get();
            Map<String, List<LandmarkMetadata>> landmarksMap = new HashMap<>();
            for (var entry : landmarkFutures.entrySet()) {
                var docs = entry.getValue().get().getDocuments();
                landmarksMap.put(entry.getKey(),
                        docs.stream()
                                .map(doc -> doc.toObject(LandmarkMetadata.class))
                                .toList()
                );
            }

            List<AnalysisMetadata> result = new ArrayList<>();
            for (var snapshot : metadataSnapshots) {
                if (snapshot.exists()) {
                    AnalysisMetadataDTO dto = snapshot.toObject(AnalysisMetadataDTO.class);
                    String id = snapshot.getId();
                    result.add(new AnalysisMetadata(
                            dto.photoId(),
                            dto.photoName(),
                            dto.status(),
                            landmarksMap.getOrDefault(id, List.of())
                    ));
                }
            }

            return result.toArray(AnalysisMetadata[]::new);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
