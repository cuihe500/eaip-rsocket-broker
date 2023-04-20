package org.eaip.rsocket.gateway.grpc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import io.rsocket.Payload;
import org.eaip.rsocket.metadata.MessageMimeTypeMetadata;
import org.eaip.rsocket.metadata.RSocketCompositeMetadata;
import org.eaip.rsocket.metadata.RSocketMimeType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Payload utils
 *
 * @author CuiChangHe
 */
public class PayloadUtils {
    private static final MessageMimeTypeMetadata protobufMetaEncoding = new MessageMimeTypeMetadata(RSocketMimeType.Protobuf);
    private static final LoadingCache<Class<?>, Method> parseFromMethodStore = Caffeine.newBuilder()
            .maximumSize(Integer.MAX_VALUE)
            .build(targetClass -> targetClass.getMethod("parseFrom", ByteBuffer.class));
    private static ByteBuf compositeMetadataWithEncoding;

    public static ByteBuf getCompositeMetaDataWithEncoding() {
        if (compositeMetadataWithEncoding == null) {
            ByteBuf byteBuf = RSocketCompositeMetadata.from(protobufMetaEncoding).getContent();
            compositeMetadataWithEncoding = Unpooled.copiedBuffer(byteBuf);
            ReferenceCountUtil.release(byteBuf);
        }
        return compositeMetadataWithEncoding;
    }

    public static <T> T payloadToResponseObject(Payload payload, Class<T> responseClass) throws InvocationTargetException, IllegalAccessException {
        Method method = parseFromMethodStore.get(responseClass);
        if (method != null) {
            //noinspection unchecked
            return (T) method.invoke(null, payload.data().nioBuffer());
        }
        return null;
    }
}
