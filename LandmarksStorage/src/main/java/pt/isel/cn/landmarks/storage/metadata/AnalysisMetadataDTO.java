package pt.isel.cn.landmarks.storage.metadata;

import pt.isel.cn.landmarks.domain.Status;

public record AnalysisMetadataDTO(String photoId, String photoName, Status status) {}
