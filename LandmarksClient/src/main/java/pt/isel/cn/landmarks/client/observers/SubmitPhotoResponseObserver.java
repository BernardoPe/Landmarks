package pt.isel.cn.landmarks.client.observers;

import io.grpc.stub.StreamObserver;
import landmarks.SubmitIdentifier;

public class SubmitPhotoResponseObserver implements StreamObserver<SubmitIdentifier> {
    @Override
    public void onNext(SubmitIdentifier id) {
        System.out.println("Photo submitted with request ID: " + id.getIdentifier());
    }

    @Override
    public void onError(Throwable t) {
        System.err.println("Error during submitPhoto: " + t.getMessage());
    }

    @Override
    public void onCompleted() {
        System.out.println("Photo submission completed.");
    }
}
