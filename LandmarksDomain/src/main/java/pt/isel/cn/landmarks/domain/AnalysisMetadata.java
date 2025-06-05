package pt.isel.cn.landmarks.domain;

import java.util.List;

/**
 * This class represents the metadata of the result
 * of a landmark photo analysis.
 * <p>
 * @param photoId - The unique identifier for the photo.
 * @param photoName - The name of the photo.
 * @param status - The status of the analysis.
 * @param landmarks - An array of LandmarkMetadata objects representing the detected landmarks.
 */
public record AnalysisMetadata(
    String photoId,
    String photoName,
    Status status,
    List<LandmarkMetadata> landmarks
) {}
