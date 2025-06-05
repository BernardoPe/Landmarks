package pt.isel.cn.landmarks.iplookup.error;

public class IPLookupError {
    private final String message;

    public IPLookupError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}