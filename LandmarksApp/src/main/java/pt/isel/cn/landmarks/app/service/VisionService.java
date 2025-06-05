package pt.isel.cn.landmarks.app.service;

import com.google.cloud.vision.v1.*;
import pt.isel.cn.landmarks.domain.Landmark;
import pt.isel.cn.landmarks.domain.Location;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for detecting landmarks in images using Google Cloud Vision API.
 */
public class VisionService {

    public List<Landmark> detectLandmarks(String imageUri) throws IOException {
        ImageSource imageSource = ImageSource.newBuilder().setImageUri(imageUri).build();
        return detectLandmarks(imageSource);
    }

    private List<Landmark> detectLandmarks(ImageSource imageSource) throws IOException {
        List<Landmark> landmarks = new ArrayList<>();
        List<AnnotateImageRequest> requests = new ArrayList<>();

        Image image = Image.newBuilder().setSource(imageSource).build();
        Feature feature = Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();

        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.println("Error: " + res.getError().getMessage());
                    continue;
                }
                for (EntityAnnotation annotation : res.getLandmarkAnnotationsList()) {
                    LocationInfo locationInfo = annotation.getLocationsList().iterator().next();
                    landmarks.add(new Landmark(
                            annotation.getDescription(),
                            new Location(locationInfo.getLatLng().getLatitude(), locationInfo.getLatLng().getLongitude()),
                            annotation.getScore()
                    ));
                }
            }
        }

        return landmarks;
    }
}
