package pt.isel.cn.landmarks.server.error;

import pt.isel.cn.landmarks.domain.AnalysisMetadata;
import pt.isel.cn.landmarks.domain.Status;

public final class LookupError extends Error {

    public LookupError(LookupErrorType type) {
        super(getMessageFor(type));
    }

    public static String getMessageFor(LookupErrorType type) {
        return switch (type) {
            case NOT_FOUND -> "Could not find request with that ID";
            case PENDING -> "Request is still being processed";
            case FAILED -> "Request failed";
            default -> "Could not find results for request with that ID";
        };
    }

    public static LookupErrorType getTypeFor(AnalysisMetadata metadata) {
        if (metadata == null) return LookupErrorType.NOT_FOUND;
        return switch (metadata.status()) {
            case Status.IN_PROGRESS -> LookupErrorType.PENDING;
            case Status.FAILURE -> LookupErrorType.FAILED;
            default -> LookupErrorType.UNKNOWN;
        };
    }

    public static LookupError from(AnalysisMetadata metadata) {
        return new LookupError(getTypeFor(metadata));
    }
}

