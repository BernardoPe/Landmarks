package pt.isel.cn.landmarks.domain;

/**
 * This class represents a geographical location with latitude and longitude.
 *
 * @param latitude The latitude of the location.
 * @param longitude The longitude of the location.
 */
public record Location(double latitude, double longitude) {}