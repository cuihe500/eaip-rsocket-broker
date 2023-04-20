package org.eaip.rsocket.gateway;

import io.netty.buffer.ByteBuf;
import io.rsocket.RSocket;
import io.rsocket.util.ByteBufPayload;
import org.eaip.rsocket.gateway.auth.JwtAuthenticationService;
import org.eaip.rsocket.metadata.GSVRoutingMetadata;
import org.eaip.rsocket.metadata.MessageMimeTypeMetadata;
import org.eaip.rsocket.metadata.RSocketCompositeMetadata;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.eaip.rsocket.upstream.UpstreamManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;

/**
 * main controller
 *
 * @author CuiChangHe
 */
@Controller
public class MainController {
    private static MessageMimeTypeMetadata jsonMetaEncoding = new MessageMimeTypeMetadata(RSocketMimeType.Json);
    private static MessageMimeTypeMetadata cloudEventsEncoding = new MessageMimeTypeMetadata(RSocketMimeType.CloudEventsJson);
    @Autowired
    private JwtAuthenticationService authenticationService;
    @Value("${restapi.auth-required}")
    private boolean authRequired;
    private RSocket rsocket;

    public MainController(UpstreamManager upstreamManager) {
        rsocket = upstreamManager.findBroker().getLoadBalancedRSocket();
    }

    @RequestMapping(value = "/api/{serviceName}/{method}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<ByteBuf>> handle(@PathVariable("serviceName") String serviceName,
                                                @PathVariable("method") String method,
                                                @RequestParam(name = "group", required = false, defaultValue = "") String group,
                                                @RequestParam(name = "version", required = false, defaultValue = "") String version,
                                                @RequestBody(required = false) ByteBuf body,
                                                @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authorizationValue,
                                                @RequestHeader(name = "Content-Type", required = false, defaultValue = "") String contentType
    ) {
        boolean authenticated;
        if (!authRequired) {
            authenticated = true;
        } else {
            authenticated = authAuthorizationValue(authorizationValue);
        }
        if (!authenticated) {
            return Mono.error(new Exception(RsocketErrorCode.message("RST-500403")));
        }
        try {
            GSVRoutingMetadata routingMetadata = new GSVRoutingMetadata(group, serviceName, method, version);
            RSocketCompositeMetadata compositeMetadata;
            if (contentType.startsWith("application/cloudevents+json")) {
                compositeMetadata = RSocketCompositeMetadata.from(routingMetadata, cloudEventsEncoding);
            } else {
                compositeMetadata = RSocketCompositeMetadata.from(routingMetadata, jsonMetaEncoding);
            }
            ByteBuf bodyBuf = body == null ? EMPTY_BUFFER : body;
            return rsocket.requestResponse(ByteBufPayload.create(bodyBuf, compositeMetadata.getContent()))
                    .map(payload -> {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.setCacheControl(CacheControl.noCache().getHeaderValue());
                        return new ResponseEntity<>(payload.data(), headers, HttpStatus.OK);
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private boolean authAuthorizationValue(String authorizationValue) {
        if (authorizationValue == null || authorizationValue.isEmpty()) {
            return false;
        }
        String jwtToken = authorizationValue;
        if (authorizationValue.contains(" ")) {
            jwtToken = authorizationValue.substring(authorizationValue.lastIndexOf(" ") + 1);
        }
        return authenticationService.auth(jwtToken) != null;
    }

}
