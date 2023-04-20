package org.eaip.rsocket.encoding.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.eaip.rsocket.encoding.EncodingException;
import org.eaip.rsocket.encoding.JsonUtils;
import org.eaip.rsocket.encoding.ObjectEncodingHandler;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.Arrays;

/**
 * Object json encoding
 *
 * @author CuiCHangHe
 */
public class ObjectEncodingHandlerJsonImpl implements ObjectEncodingHandler {
    private boolean ktJson;

    public ObjectEncodingHandlerJsonImpl() {
        try {
            Class.forName("kotlinx.serialization.json.Json");
            ktJson = true;
        } catch (Exception e) {
            ktJson = false;
        }
    }

    @NotNull
    @Override
    public RSocketMimeType mimeType() {
        return RSocketMimeType.Json;
    }

    @Override
    public ByteBuf encodingParams(@Nullable Object[] args) throws EncodingException {
        if (isArrayEmpty(args)) {
            return EMPTY_BUFFER;
        }
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
            JsonUtils.objectMapper.writeValue((OutputStream) bos, args);
            return byteBuf;
        } catch (Exception e) {
            ReferenceCountUtil.safeRelease(byteBuf);
            throw new EncodingException(RsocketErrorCode.message("RST-700500", "Object[]", "ByteBuf"), e);
        }
    }

    @Override
    @Nullable
    public Object decodeParams(ByteBuf data, @Nullable Class<?>... targetClasses) throws EncodingException {
        if (data.readableBytes() > 0 && !isArrayEmpty(targetClasses)) {
            try {
                final byte firstByte = data.getByte(0);
                if (firstByte == '[') {
                    return JsonUtils.readJsonArray(data, targetClasses);
                } else {
                    return JsonUtils.readJsonValue(data, targetClasses[0]);
                }
            } catch (Exception e) {
                throw new EncodingException(RsocketErrorCode.message("RST-700501", "bytebuf", Arrays.toString(targetClasses)), e);
            }
        }
        return null;
    }

    @Override
    @NotNull
    public ByteBuf encodingResult(@Nullable Object result) throws EncodingException {
        if (result == null) {
            return EMPTY_BUFFER;
        }
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            if (ktJson && KotlinSerializerSupport.isKotlinSerializable(result.getClass())) {
                return Unpooled.wrappedBuffer(KotlinSerializerSupport.encodeAsJson(result));
            } else {
                ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
                JsonUtils.objectMapper.writeValue((OutputStream) bos, result);
                return byteBuf;
            }
        } catch (Exception e) {
            ReferenceCountUtil.safeRelease(byteBuf);
            throw new EncodingException(RsocketErrorCode.message("RST-700500", result.getClass().getCanonicalName(), "ByteBuf"), e);
        }
    }

    @Override
    @Nullable
    public Object decodeResult(ByteBuf data, @Nullable Class<?> targetClass) throws EncodingException {
        if (data.readableBytes() > 0 && targetClass != null) {
            try {
                if (ktJson && KotlinSerializerSupport.isKotlinSerializable(targetClass)) {
                    byte[] bytes = new byte[data.readableBytes()];
                    data.readBytes(bytes);
                    return KotlinSerializerSupport.decodeFromJson(bytes, targetClass);
                } else {
                    return JsonUtils.readJsonValue(data, targetClass);
                }
            } catch (Exception e) {
                throw new EncodingException(RsocketErrorCode.message("RST-700501", "bytebuf", targetClass.getName()), e);
            }
        }
        return null;
    }
}
