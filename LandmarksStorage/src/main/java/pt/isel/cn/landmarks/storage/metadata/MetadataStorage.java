package pt.isel.cn.landmarks.storage.metadata;

import pt.isel.cn.landmarks.domain.AnalysisMetadata;
import pt.isel.cn.landmarks.domain.LandmarkMetadata;
import pt.isel.cn.landmarks.domain.Status;

import java.util.List;

/**
 * This interface defines the contract metadata storage.
 * <p>
 * It provides methods to save, update, and retrieve photo analysis metadata.
 */
public interface MetadataStorage {
    /**
     * Saves the analysis metadata.
     *
     * @param requestId The unique identifier for the request.
     * @param metadata The metadata to save.
     */
    public void saveAnalysisMetadata(String requestId, AnalysisMetadata metadata);

    /**
     * Updates the analysis metadata.
     *
     * @param requestId The unique identifier for the request.
     * @param landmarks An array of LandmarkMetadata objects representing the detected landmarks.
     * @param status The status of the analysis.
     */
    public void updateAnalysisMetadata(String requestId, List<LandmarkMetadata> landmarks, Status status);

    /**
     * Retrieves the analysis metadata by photo ID.
     *
     * @param requestId The unique identifier for the request.
     * @return The analysis metadata for the specified photo ID.
     */
    public AnalysisMetadata getAnalysisMetadata(String requestId);

    /**
     * Retrieves all analysis metadata above a certain confidence threshold.
     *
     * @return An array of all analysis metadata.
     */
    public AnalysisMetadata[] getAnalysisMetadataByConfidenceThreshold(
        double confidenceThreshold
    );
}
