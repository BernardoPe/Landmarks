package pt.isel.cn.landmarks.server.observers;

import com.google.cloud.WriteChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import landmarks.SubmitIdentifier;
import landmarks.SubmitPhotoRequest;
import pt.isel.cn.landmarks.domain.Either;
import pt.isel.cn.landmarks.server.error.PhotoSubmitError;
import pt.isel.cn.landmarks.server.services.Service;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class PhotoSubmitStreamObserver implements StreamObserver<SubmitPhotoRequest> {
    private final StreamObserver<SubmitIdentifier> responseObserver;
    private final MessageDigest messageDigest;
    private final Service service;

    private boolean metadataReceived = false;
    private WriteChannel writer;
    private String clientHash;
    private String photoName;

    private static final Logger logger = Logger.getLogger(PhotoSubmitStreamObserver.class.getName());

    public PhotoSubmitStreamObserver(StreamObserver<SubmitIdentifier> responseObserver, Service service) {
        this.service = service;
        this.responseObserver = responseObserver;
        this.messageDigest = createSha256Digest();
    }

    @Override
    public void onNext(SubmitPhotoRequest chunk) {
        try {
            if (chunk.hasMetadata()) {
                handleMetadata(chunk);
            } else {
                handlePhotoChunk(chunk);
            }
        } catch (IOException e) {
            logger.severe("Error writing photo bytes: " + e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        logger.severe("Error in stream: " + throwable.getMessage());
        responseObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        String photoHash = bytesToHex(messageDigest.digest());

        if (!photoHash.equals(clientHash)) {
            logger.info("Hash mismatch: expected " + clientHash + ", got " + photoHash);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Hash mismatch: expected " + clientHash + ", got " + photoHash)
                    .asException());
            cleanup();
            return;
        }

        Either<PhotoSubmitError, String> result = service.submitRequest(clientHash, photoName);

        if (result.isLeft()) {
            logger.severe("Error submitting photo analysis request: " + result.getLeft());
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Error submitting photo analysis request: " + result.getLeft())
                    .asException());
            closeWriter();
            return;
        }

        SubmitIdentifier response = SubmitIdentifier
                .newBuilder()
                .setIdentifier(result.getRight())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        closeWriter();

        logger.info("Photo submitted successfully with ID: " + result.getRight());
    }

    private void handleMetadata(SubmitPhotoRequest chunk) throws IOException {
        clientHash = chunk.getMetadata().getHash();
        photoName = chunk.getMetadata().getName();
        if (!service.photoExists(clientHash)) {
            writer = service.getPhotoWriter(clientHash);
        }
        metadataReceived = true;
    }

    private void handlePhotoChunk(SubmitPhotoRequest chunk) throws IOException {
        if (!metadataReceived) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Metadata not received before photo data")
                    .asException());
            return;
        }
        byte[] data = chunk.getPhotoChunk().toByteArray();
        messageDigest.update(data);
        if (writer != null) {
            writer.write(ByteBuffer.wrap(data));
        }
    }

    private static MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void cleanup() {
        service.deletePhoto(clientHash);
        closeWriter();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.severe("Error closing writer: " + e.getMessage());
            }
        }
    }
}
