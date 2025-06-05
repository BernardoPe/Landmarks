package pt.isel.cn.landmarks.iplookup.dto;

import java.util.List;

public record IPsPayload(
    List<String> ips
) { }
