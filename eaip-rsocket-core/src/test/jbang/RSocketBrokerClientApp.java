//usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.slf4j:slf4j-simple:1.7.30
//DEPS org.projectlombok:lombok:1.18.12
//DEPS org.eaip.rsocket:eaip-rsocket-core:1.0.0.RC2

import org.eaip.rsocket.client.RSocketBrokerClient;
import org.eaip.rsocket.client.RSocketBrokerConnector;
import org.eaip.rsocket.encoding.JsonUtils;
import org.eaip.rsocket.metadata.RSocketMimeType;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * jbang RSocket Broker Client App
 *
 * @author CuiCHangHe
 */
public class RSocketBrokerClientApp {
    private static final char[] jwtToken = "".toCharArray();
    private static final List<String> brokers = Collections.singletonList("tcp://127.0.0.1:9999");

    public static void main(String[] args) throws Exception {
        RSocketBrokerClient brokerClient = RSocketBrokerConnector
                .create()
                .appName("MockApp")
                .dataMimeType(RSocketMimeType.Json)
                .jwtToken(jwtToken)
                //.service("org.eaip.service.DemoMockService", DemoMockService.class, (DemoMockService) id -> Mono.just("Hello " + id))
                .brokers(brokers)
                .connect();
        UserService userService = userService(brokerClient);
        User user = userService.findById(1).block();
        System.out.println(JsonUtils.toJsonText(user));
        //WordService wordService = client.wordService();
        //System.out.println(wordService.lowercase("Hello").block());
        brokerClient.dispose();
    }

    public static UserService userService(RSocketBrokerClient brokerClient) {
        return brokerClient.buildService(UserService.class, "org.eaip.user.UserService");
    }

    public static WordService wordService(RSocketBrokerClient brokerClient) {
        return brokerClient.buildService(WordService.class, "org.eaip.WordService");
    }

    public interface WordService {
        Mono<String> uppercase(String text);

        Mono<String> lowercase(String text);
    }

    public interface UserService {
        Mono<User> findById(Integer id);

        Mono<String> getAppName();
    }

    @FunctionalInterface
    public interface DemoMockService {

        Mono<String> hello(Integer id);
    }

    @Data
    public static class User implements Serializable {
        private Integer id;
        private String nick;
        private String email;
        private String phone;
    }
}
