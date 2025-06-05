package pt.isel.cn.landmarks.client.error;

public class ClientError {
    private final String message;
    public ClientError(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
}
