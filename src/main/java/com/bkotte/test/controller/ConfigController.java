package com.bkotte.test.controller;

import com.bkotte.test.model.Config;
import com.bkotte.test.model.SsoConfigDTO;
import com.bkotte.test.service.ConfigService;
import com.bkotte.test.service.WriteCsvToResponse;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/configs")
@Slf4j
@CrossOrigin
public class ConfigController {

    @Autowired
    ConfigService userService; //Service which will do all data retrieval/manipulation work

    @PostMapping("/table")
    public DeferredResult<List<SsoConfigDTO>> getConfigAsTable(@NotNull @Valid @RequestBody Config config) {
        DeferredResult<List<SsoConfigDTO>> deferredResult = new DeferredResult<>();
        Single<List<SsoConfigDTO>> result = userService.generateTabularConfig(config);
        result.subscribe(deferredResult::setResult, deferredResult::setErrorResult);
        return deferredResult;
    }

    @PostMapping("/compare")
    public void addSource(@NotNull @Valid @RequestBody Config config, @RequestParam MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), "UTF-8");
        try (
                InputStream is = file.getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            CsvToBean<SsoConfigDTO> csvToBean = new CsvToBeanBuilder(reader)
                    .withType(SsoConfigDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            Iterator<SsoConfigDTO> csvUserIterator = csvToBean.iterator();

            while (csvUserIterator.hasNext()) {
                SsoConfigDTO csvUser = csvUserIterator.next();
                System.out.println(csvUser);
            }
        }
    }

    @GetMapping(value = "/downloadCSV", produces = "text/csv")
    public void downloadCSV(@RequestHeader(value = "Config") String config, HttpServletResponse response) throws IOException {

        List<SsoConfigDTO> configList = userService.generateTabularConfig(new Config(config))
                .blockingGet();
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=configs.csv";
        response.setHeader(headerKey, headerValue);
        WriteCsvToResponse.writeConfigs(response.getWriter(), configList);

            /*String csvFileName = "configs.csv";
            response.setContentType("text/csv");

            // creates mock data
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"",
                    csvFileName);
            response.setHeader(headerKey, headerValue);
            ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                    CsvPreference.STANDARD_PREFERENCE);

            String[] header = new String[]{"sourceApplicationId", "targetApplicationId", "schema", "ttl",
                    "requestType", "contentType", "clientType", "url"};
            csvWriter.writeHeader(header);
            for (SsoConfigDTO configDTO : configList) {
                csvWriter.write(configDTO, header);
            }
            csvWriter.close();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=configs.csv")
                .body(resource);*/

    }
}