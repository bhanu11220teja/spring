package com.bkotte.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsoConfigDTO {
    private String sourceApplicationId;
    private String targetApplicationId;
    private String schema;
    private String ttl;
    private String requestType;
    private String contentType;
    private String clientType;
    private String url;
}
