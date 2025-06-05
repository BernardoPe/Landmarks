package pt.isel.cn.landmarks.server.error;

public final class PublishError extends Error {
    public PublishError() {
        super("Could not publish photo");
    }
}
