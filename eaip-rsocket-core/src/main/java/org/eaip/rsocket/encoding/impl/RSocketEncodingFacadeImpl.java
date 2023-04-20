package org.eaip.rsocket.encoding.impl;

import io.netty.buffer.ByteBuf;
import org.eaip.rsocket.encoding.EncodingException;
import org.eaip.rsocket.encoding.ObjectEncodingHandler;
import org.eaip.rsocket.encoding.RSocketEncodingFacade;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.eaip.rsocket.encoding.ObjectEncodingHandler.EMPTY_BUFFER;

/**
 * RSocket encoding facade implementation
 *
 * @author CuiCHangHe
 */
public class RSocketEncodingFacadeImpl implements RSocketEncodingFacade {
    /**
     * composite metadata ByteBuf for message mime types
     */
    public static final RSocketEncodingFacade instance = new RSocketEncodingFacadeImpl();
    private Logger log = LoggerFactory.getLogger(RSocketEncodingFacadeImpl.class);
    private Map<RSocketMimeType, ObjectEncodingHandler> handlerMap = new HashMap<>();

    public RSocketEncodingFacadeImpl() {
        String vmName = System.getProperty("java.vm.name");
        if ("Substrate VM".equals(vmName)) {  //GraalVM Native image with JSON encoding only
            addEncodingHandler(new ObjectEncodingHandlerJsonImpl());
            addEncodingHandler(new ObjectEncodingHandlerGraphQLJsonImpl());
            addEncodingHandler(new ObjectEncodingHandlerHessianImpl());
        } else {
            ServiceLoader<ObjectEncodingHandler> serviceLoader = ServiceLoader.load(ObjectEncodingHandler.class);
            for (ObjectEncodingHandler objectEncodingHandler : serviceLoader) {
                addEncodingHandler(objectEncodingHandler);
            }
        }
    }

    private void addEncodingHandler(ObjectEncodingHandler objectEncodingHandler) {
        RSocketMimeType mimeType = objectEncodingHandler.mimeType();
        handlerMap.put(mimeType, objectEncodingHandler);
    }

    @NotNull
    @Override
    public ByteBuf encodingParams(@Nullable Object[] args, RSocketMimeType encodingType) {
        try {
            ObjectEncodingHandler handler = handlerMap.get(encodingType);
            return handler.encodingParams(args);
        } catch (Exception e) {
            log.error(RsocketErrorCode.message("RST-700500", "Object[]", encodingType.getName()), e);
            return EMPTY_BUFFER;
        }
    }

    @Override
    public @Nullable Object decodeParams(RSocketMimeType encodingType, @Nullable ByteBuf data, @Nullable Class<?>... targetClasses) {
        try {
            if (data == null || data.readableBytes() == 0) return null;
            return handlerMap.get(encodingType).decodeParams(data, targetClasses);
        } catch (Exception e) {
            log.error(RsocketErrorCode.message("RST-700501", encodingType.getName(), "Object[]"), e);
            return null;
        }
    }

    @NotNull
    @Override
    public ByteBuf encodingResult(@Nullable Object result, RSocketMimeType encodingType) throws EncodingException {
        try {
            return handlerMap.get(encodingType).encodingResult(result);
        } catch (Exception e) {
            log.error(RsocketErrorCode.message("RST-700500", result != null ? result.getClass() : "Null", encodingType.getName()), e);
            return EMPTY_BUFFER;
        }
    }

    @Override
    public @Nullable Object decodeResult(RSocketMimeType encodingType, @Nullable ByteBuf data, @Nullable Class<?> targetClass) {
        try {
            if (data == null || data.readableBytes() == 0) return null;
            //convert to raw output without decoding
            if (targetClass == ByteBuffer.class) {
                return data.nioBuffer();
            } else if (targetClass == ByteBuf.class) {
                return data;
            }
            return handlerMap.get(encodingType).decodeResult(data, targetClass);
        } catch (Exception e) {
            log.error(RsocketErrorCode.message("RST-700501", encodingType.getName(), targetClass != null ? targetClass.getName() : "Null"), e);
            return null;
        }
    }

    //check encoding type exist or not
    private void checkMimeTypeAvailable(RSocketMimeType encodingType) throws EncodingException {
        if (!handlerMap.containsKey(encodingType)) {
            String message = RsocketErrorCode.message("RST-700405", encodingType.getType());
            throw new EncodingException(message, new Exception(message));
        }
    }
}
