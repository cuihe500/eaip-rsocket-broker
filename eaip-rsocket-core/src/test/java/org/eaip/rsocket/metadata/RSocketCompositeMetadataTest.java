package org.eaip.rsocket.metadata;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.util.ByteBufPayload;
import org.eaip.rsocket.cloudevents.CloudEventImpl;
import org.eaip.rsocket.cloudevents.Json;
import org.eaip.rsocket.cloudevents.RSocketCloudEventBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * composite metadata test
 *
 * @author CuiCHangHe
 */
public class RSocketCompositeMetadataTest {

    public static Payload cloudEventToPayload(CloudEventImpl<?> cloudEvent) {
        RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(new MessageMimeTypeMetadata(RSocketMimeType.CloudEventsJson));
        return ByteBufPayload.create(Unpooled.wrappedBuffer(Json.serialize(cloudEvent)), compositeMetadata.getContent());
    }

    @Test
    public void testCompositeMetadata() throws Exception {
        RSocketCompositeMetadata compositeMetadata = new RSocketCompositeMetadata();
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.setUuid(UUID.randomUUID().toString());
        appMetadata.setName("user-service");
        appMetadata.setIp("192.168.0.1");
        appMetadata.setRsocketPorts(Collections.singletonMap(52252, "tcp"));
        HashMap<String, String> tempMap = new HashMap<>();
        tempMap.put("first", "second");
        appMetadata.setMetadata(tempMap);
        compositeMetadata.addMetadata(appMetadata);
        compositeMetadata.addMetadata(new MessageMimeTypeMetadata(RSocketMimeType.Hessian));
        compositeMetadata.addMetadata(new BearerTokenMetadata("xxx.yyy.zz".toCharArray()));
        compositeMetadata.addMetadata(new GSVRoutingMetadata("", "org.eaip.UserService", "findById", "1.0.0"));
        ByteBuf buffer = compositeMetadata.getContent();
        //System.out.println(buffer.capacity());
        RSocketCompositeMetadata temp = RSocketCompositeMetadata.from(Unpooled.copiedBuffer(buffer));
        Assertions.assertNotNull(temp.getRoutingMetaData());
        compositeMetadata.getMetadata(RSocketMimeType.Application);
    }

    @Test
    public void testSetupMetadata() throws Exception {
        RSocketCompositeMetadata compositeMetadata = new RSocketCompositeMetadata();
        ByteBuf buffer = compositeMetadata.getContent();
        //System.out.println(buffer.capacity());
        RSocketCompositeMetadata temp = RSocketCompositeMetadata.from(Unpooled.wrappedBuffer(buffer.array()));
        System.out.println(temp.getMetadata(RSocketMimeType.ServiceRegistry));
    }

    @Test
    public void testMimetype() {
        System.out.println(RSocketMimeType.valueOf((byte) 5));
    }

    @Test
    public void testCloudEvents() throws Exception {
        final CloudEventImpl<String> cloudEvent = RSocketCloudEventBuilder
                .builder("欢迎")
                .build();
        Payload payload = cloudEventToPayload(cloudEvent);
        payload.getMetadata().rewind();
        RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(payload.metadata());
        MessageMimeTypeMetadata dataEncodingMetadata = compositeMetadata.getDataEncodingMetadata();
        Assertions.assertNotNull(dataEncodingMetadata);
        System.out.println(dataEncodingMetadata.getMimeType());
    }

    @Test
    public void testServiceIdRoutingMetadata() {
        Integer remoteServiceId = 114;
        BinaryRoutingMetadata serviceIdRoutingMetadata = new BinaryRoutingMetadata(remoteServiceId, 112);
        RSocketCompositeMetadata compositeMetadata = RSocketCompositeMetadata.from(serviceIdRoutingMetadata);
        ByteBuf compositeByteBuf = compositeMetadata.getContent();
        compositeByteBuf.resetReaderIndex();
        byte metadataTypeId = compositeByteBuf.getByte(0);
        System.out.println(metadataTypeId);
        Assertions.assertEquals(metadataTypeId, (byte) (WellKnownMimeType.MESSAGE_RSOCKET_BINARY_ROUTING.getIdentifier() | 0x80));
        int length = compositeByteBuf.getInt(0) & 0xFF;
        Assertions.assertEquals(length, 8);
        int serviceId = compositeByteBuf.getInt(4);
        Assertions.assertEquals(serviceId, remoteServiceId);
    }
}
