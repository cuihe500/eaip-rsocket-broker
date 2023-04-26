package org.eaip.rsocket.config.server.controller;

import org.eaip.rsocket.cloudevents.CloudEventsNotifyService;
import org.eaip.rsocket.cloudevents.Json;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;
import org.eaip.rsocket.config.server.ConfigurationServiceSupport;
import org.eaip.rsocket.events.ConfigEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * config controller
 *
 * @author leijuan
 */
@RestController
@RequestMapping("/config")
public class ConfigController {
    @Autowired
    private ConfigurationServiceSupport configSupport;
    @Autowired
    private CloudEventsNotifyService notifyService;

    private final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @PostMapping("/update/{appName}/{key}")
    public Mono<Void> update(@PathVariable(name = "appName") String appName,
                             @PathVariable(name = "key") String key,
                             @RequestBody String body) {
        log.info("Update server config.appName:{},key:{}",appName,key);
        return configSupport.put(appName, key, body);
    }

    @PostMapping("/refresh/{appName}")
    public Mono<Void> refresh(@PathVariable(name = "appName") String appName) {
        log.info("Refresh server config.appName:{}",appName);
        return configSupport.get(appName, "application.properties")
                .map(body -> {
                    ConfigEvent configEvent = new ConfigEvent(appName, "text/plain", body);
                    return Json.serializeAsText(RSocketCloudEventBuilder.builder(configEvent).build());
                }).flatMap(jsonText -> notifyService.notifyAll(appName, jsonText));
    }

    @PostMapping("/refresh/{appName}/{appId}")
    public Mono<Void> refresh(@PathVariable(name = "appName") String appName,
                              @PathVariable(name = "appId") String appId,
                              @RequestBody String body) {
        ConfigEvent configEvent = new ConfigEvent(appName, "text/plain", body);
        String jsonText = Json.serializeAsText(RSocketCloudEventBuilder.builder(configEvent).build());
        log.info("Refresh server config.appName:{},appId:{}",appName,appId);
        return notifyService.notify(appId, jsonText);
    }

    @GetMapping("/last/{appName}/{key}")
    public Mono<String> fetch(@PathVariable(name = "appName") String appName,
                              @PathVariable(name = "key") String key) {
        log.info("Fetch latest server config.appName:{},key:{}",appName,key);
        return configSupport.get(appName, key);
    }
}
