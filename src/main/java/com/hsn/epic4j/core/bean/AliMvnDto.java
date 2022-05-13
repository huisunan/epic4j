package com.hsn.epic4j.core.bean;

import lombok.Data;

import java.util.List;

/**
 * @author hsn
 * 2022/1/4
 * AliMvnDto
 */
@Data
public class AliMvnDto {
    private List<AliMvnItemDto> object;
    private Boolean successful;

    @Data
    public static class AliMvnItemDto {
        private String artifactId;
        private String classifier;
        private String fileName;
        private String groupId;
        private String id;
        private String packaging;
        private String repositoryId;
        private String version;
    }
}
