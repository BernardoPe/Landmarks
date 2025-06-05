package pt.isel.cn.landmarks.server;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import landmarks.*;
import pt.isel.cn.landmarks.domain.AnalysisMetadata;
import pt.isel.cn.landmarks.domain.Either;
import pt.isel.cn.landmarks.domain.LandmarkMetadata;
import pt.isel.cn.landmarks.server.error.LookupError;
import pt.isel.cn.landmarks.server.error.LookupErrorType;
import pt.isel.cn.landmarks.server.error.MapsError;
import pt.isel.cn.landmarks.server.error.PhotosByConfidenceError;
import pt.isel.cn.landmarks.server.observers.PhotoSubmitStreamObserver;
import pt.isel.cn.landmarks.server.services.MapsService;
import pt.isel.cn.landmarks.server.services.Service;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class LandmarksServer extends LandmarksServiceGrpc.LandmarksServiceImplBase {
    private final Service service;
    private final MapsService mapsService;
    private static final Logger logger = Logger.getLogger(LandmarksServer.class.getName());

    public LandmarksServer(Service service, MapsService mapsService) {
        this.service = service;
        this.mapsService = mapsService;
    }

    @Override
    public StreamObserver<SubmitPhotoRequest> submitPhoto(StreamObserver<SubmitIdentifier> responseObserver) {
        return new PhotoSubmitStreamObserver(responseObserver, service);
    }

    @Override
    public void lookupResults(SubmitIdentifier request, StreamObserver<LookupResults> responseObserver) {
        String requestId = request.getIdentifier().trim();
        logger.info("Looking up results for " + requestId);

        Either<LookupErrorType, AnalysisMetadata> result = service.lookupResults(requestId);

        if (result.isLeft()) {
            String message = LookupError.getMessageFor(result.getLeft());
            Status status = switch (result.getLeft()) {
                case NOT_FOUND -> Status.NOT_FOUND;
                case PENDING -> Status.UNAVAILABLE;
                case FAILED -> Status.FAILED_PRECONDITION;
                default -> Status.UNKNOWN;
            };
            responseObserver.onError(status.withDescription(message).asException());
            return;
        }

        AnalysisMetadata metadata = result.getRight();
        List<LandmarkMetadata> landmarks = metadata.landmarks();

        if (landmarks.isEmpty()) {
            responseObserver.onNext(LookupResults.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }

        LandmarkMetadata highestConfidenceLandmark = landmarks.stream()
                .max(Comparator.comparingDouble(LandmarkMetadata::confidence))
                .orElseThrow();

        Either<MapsError, byte[]> mapResult = mapsService.getMap(highestConfidenceLandmark.location());

        LookupResults.Builder responseBuilder = LookupResults.newBuilder()
                .addAllLandmarks(landmarks.stream()
                        .map(landmark -> Landmark.newBuilder()
                                .setName(landmark.name())
                                .setConfidence(landmark.confidence())
                                .setLatitude(landmark.location().latitude())
                                .setLongitude(landmark.location().longitude())
                                .build()
                        ).toList());

        mapResult.ifRight(mapBytes -> responseBuilder.setMap(ByteString.copyFrom(mapBytes)));
        mapResult.ifLeft(error -> logger.severe("Error fetching map: " + error));

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPhotos(ConfidenceThreshold request, StreamObserver<GetPhotosResponse> responseObserver) {
        double confidenceThreshold = request.getConfidenceThreshold();

        logger.info("Getting photos with confidence threshold: " + confidenceThreshold);

        if (confidenceThreshold < 0 || confidenceThreshold > 1) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Confidence threshold must be between 0 and 1").asException());
            return;
        }

        Either<PhotosByConfidenceError, List<AnalysisMetadata>> result = service.getResultsByConfidenceThreshold(confidenceThreshold);

        if (result.isLeft()) {
            String message = result.getLeft().getMessage();
            responseObserver.onError(Status.UNKNOWN.withDescription(message).asException());
            return;
        }

        List<AnalysisMetadata> metadataList = result.getRight();

        if (metadataList.isEmpty()) {
            responseObserver.onNext(GetPhotosResponse.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }

        responseObserver.onNext(
                GetPhotosResponse.newBuilder()
                        .addAllPhotos(metadataList.stream()
                                .map(metadata -> {
                                    LandmarkMetadata highestLandmark = getHighestConfidenceLandmark(metadata.landmarks());
                                    return Photo.newBuilder()
                                            .setPhotoName(metadata.photoName())
                                            .setLandmarkName(highestLandmark.name())
                                            .setConfidence(highestLandmark.confidence())
                                            .build();
                                })
                                .toList())
                        .build()
        );

        responseObserver.onCompleted();
    }

    private LandmarkMetadata getHighestConfidenceLandmark(List<LandmarkMetadata> landmarks) {
        return landmarks.stream()
                .max(Comparator.comparingDouble(LandmarkMetadata::confidence))
                .orElseThrow(() -> new IllegalStateException("No landmarks found"));
    }
}