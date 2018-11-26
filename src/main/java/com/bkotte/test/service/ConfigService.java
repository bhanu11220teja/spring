package com.bkotte.test.service;

import com.bkotte.test.model.Config;
import com.bkotte.test.model.SsoConfigDTO;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    public Single<List<SsoConfigDTO>> generateTabularConfig(Config config) {
        return Single.fromCallable(() -> {
            String value = config.getValue();
            return Collections.list(new StringTokenizer(value, ",")).stream()
                    .map(configString -> mapToDomain((String) configString))
                    .collect(Collectors.toList());
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation());
    }

    private SsoConfigDTO mapToDomain(String config) {
        String[] properties = config.split("\\|");
        if (properties.length != 7)
            throw new RuntimeException("Invalid configuration");
        SsoConfigDTO ssoConfigDTO = new SsoConfigDTO();
        String[] applicationIds = properties[0].split("\\.");
        if (applicationIds.length != 2)
            throw new RuntimeException("Invalid configuration");
        ssoConfigDTO.setSourceApplicationId(applicationIds[0]);
        ssoConfigDTO.setTargetApplicationId(applicationIds[1]);
        ssoConfigDTO.setSchema(properties[1]);
        ssoConfigDTO.setTtl(properties[2]);
        ssoConfigDTO.setRequestType(properties[3]);
        ssoConfigDTO.setContentType(properties[4]);
        ssoConfigDTO.setClientType(properties[5]);
        ssoConfigDTO.setUrl(properties[6]);
        return ssoConfigDTO;
    }
}
