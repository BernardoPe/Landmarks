package pt.isel.cn.landmarks.server.error;

public final class MapsError extends Error {
    public MapsError() {
        super("Could not find map");
    }
}
