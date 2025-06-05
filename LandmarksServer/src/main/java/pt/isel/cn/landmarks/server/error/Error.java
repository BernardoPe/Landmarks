package pt.isel.cn.landmarks.server.error;

sealed public class Error permits LookupError, MapsError, PhotoSubmitError, PhotosByConfidenceError, PublishError {

    private final String message;

    public Error(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}