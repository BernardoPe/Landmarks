package pt.isel.cn.landmarks.server.error;

public final class PhotoSubmitError extends Error {
    public PhotoSubmitError() {
        super("Could not submit photo");
    }
}
