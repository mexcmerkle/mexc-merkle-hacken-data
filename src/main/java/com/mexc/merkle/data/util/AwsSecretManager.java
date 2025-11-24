package com.mexc.merkle.data.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * aws secret manager
 */
@Slf4j
public class AwsSecretManager {
    String secretName = "mexc.merkle";
    Region region = null;
    // Create a Secrets Manager client
    SecretsManagerClient client = SecretsManagerClient.builder()
            .region(region)
            .build();
    GetSecretValueResponse getSecretValueResponse = null;
    Map<String, String> map = new HashMap<>();

    public AwsSecretManager(String regionName) {
        try {
            region = Region.of(regionName);
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> readValue = objectMapper.readValue(getSecretValueResponse.secretString(), new TypeReference<Map<String, String>>() {
            });
            if (MapUtils.isNotEmpty(readValue)) {
                map.putAll(readValue);
            } else {
                log.error("getSecretValueResponse,value failed value:{}", getSecretValueResponse.secretString());
            }
        } catch (Throwable e) {
            log.error("get aws secret error", e);
        }
    }

    public String getValue(String key) {
        return map.getOrDefault(key, null);
    }

    public String getSecretField(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        if (key.matches("config\\(([^)]+)\\)")) {
            String value = key.replaceAll("config\\(([^)]+)\\)", "$1");
            return value.replace(secretName, "").replaceFirst(".", "");
        }
        return null;
    }
}
