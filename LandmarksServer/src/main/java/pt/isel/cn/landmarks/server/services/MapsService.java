package pt.isel.cn.landmarks.server.services;

import pt.isel.cn.landmarks.domain.Config;
import pt.isel.cn.landmarks.domain.Either;
import pt.isel.cn.landmarks.domain.Location;
import pt.isel.cn.landmarks.server.error.MapsError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class MapsService {
    private static final String API_URL = "https://maps.googleapis.com/maps/api/staticmap?";
    private static final String ZOOM = "15";
    private static final String SIZE = "600x300";

    private static final HttpClient client = HttpClient.newHttpClient();

    private static final Logger logger = Logger.getLogger(MapsService.class.getName());

    public Either<MapsError, byte[]> getMap(Location location) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "center=" + location.latitude() + "," + location.longitude() +
                            "&zoom=" + ZOOM +
                            "&size=" + SIZE +
                            "&key=" + Config.API_KEY))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                logger.severe("Error fetching map: " + response.statusCode());
                return Either.left(new MapsError());
            }

            byte[] mapData = response.body().readAllBytes();
            response.body().close();
            return Either.right(mapData);
        } catch (IOException | InterruptedException e) {
            logger.severe("Error fetching map: " + e.getMessage());
            return Either.left(new MapsError());
        }
    }
}
