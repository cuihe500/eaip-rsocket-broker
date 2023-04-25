package org.eaip.rsocket.registry.client.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * RSocket registry client app
 *
 * @author CuiChangHe
 */
@SpringBootApplication
@EnableDiscoveryClient
public class RSocketRegistryClientApp {
    public static void main(String[] args) {
        SpringApplication.run(RSocketRegistryClientApp.class, args);
    }
}
