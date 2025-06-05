package pt.isel.cn.landmarks.domain;

/**
 * This class represents metadata for a landmark.
 * <p>
 * A landmark is a point of interest, which can be
 * a monument, a building, or any other significant location.
 *
 * @param name The name of the landmark.
 * @param location The location of the landmark.
 * @param confidence The confidence level of the landmark detection.
 */
public record LandmarkMetadata(
    String name,
    Location location,
    double confidence
) {}