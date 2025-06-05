package pt.isel.cn.landmarks.server.error;

public final class PhotosByConfidenceError extends Error {
    public PhotosByConfidenceError() {
        super("An error occurred while fetching photos by confidence");
    }
}
