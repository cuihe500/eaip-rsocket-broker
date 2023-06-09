package org.eaip.rsocket.encoding.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.eaip.rsocket.encoding.EncodingException;
import org.eaip.rsocket.encoding.ObjectEncodingHandler;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.eaip.rsocket.observability.RsocketErrorCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Object cbor encoding
 *
 * @author CuiCHangHe
 */
public class ObjectEncodingHandlerCborImpl implements ObjectEncodingHandler {
    private ObjectMapper objectMapper = new ObjectMapper(new CBORFactory());
    private boolean ktCbor = true;

    public ObjectEncodingHandlerCborImpl() {
        try {
            Class.forName("kotlinx.serialization.cbor.Cbor");
        } catch (Exception e) {
            ktCbor = false;
        }
    }

    @NotNull
    @Override
    public RSocketMimeType mimeType() {
        return RSocketMimeType.CBOR;
    }

    @Override
    public ByteBuf encodingParams(@Nullable Object[] args) throws EncodingException {
        if (isArrayEmpty(args)) {
            return EMPTY_BUFFER;
        }
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
            objectMapper.writeValue((OutputStream) bos, args[0]);
            return byteBuf;
        } catch (Exception e) {
            ReferenceCountUtil.safeRelease(byteBuf);
            throw new EncodingException(RsocketErrorCode.message("RST-700500", Arrays.toString(args), "Bytebuf"), e);
        }
    }

    @Override
    public Object decodeParams(ByteBuf data, @Nullable Class<?>... targetClasses) throws EncodingException {
        if (data.readableBytes() > 0 && targetClasses != null && targetClasses.length > 0) {
            try {
                return objectMapper.readValue((InputStream) new ByteBufInputStream(data), targetClasses[0]);
            } catch (Exception e) {
                throw new EncodingException(RsocketErrorCode.message("RST-700501", "ByteBuf", Arrays.toString(targetClasses)), e);
            }
        }
        return null;
    }

    @Override
    @NotNull
    public ByteBuf encodingResult(@Nullable Object result) throws EncodingException {
        if (result != null) {
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream bos = new ByteBufOutputStream(byteBuf);
                objectMapper.writeValue((OutputStream) bos, result);
                return byteBuf;
            } catch (Exception e) {
                ReferenceCountUtil.safeRelease(byteBuf);
                throw new EncodingException(RsocketErrorCode.message("RST-700500", result.toString(), "Bytebuf"), e);
            }
        }
        return EMPTY_BUFFER;
    }

    @Override
    @Nullable
    public Object decodeResult(ByteBuf data, @Nullable Class<?> targetClass) throws EncodingException {
        if (data.readableBytes() > 0 && targetClass != null) {
            try {
                //Kotlin Cbor Serializer
                if (ktCbor && KotlinSerializerSupport.isKotlinSerializable(targetClass)) {
                    byte[] bytes = new byte[data.readableBytes()];
                    data.readBytes(bytes);
                    return KotlinSerializerSupport.decodeFromCbor(bytes, targetClass);
                }
                return objectMapper.readValue((InputStream) new ByteBufInputStream(data), targetClass);
            } catch (Exception e) {
                throw new EncodingException(RsocketErrorCode.message("RST-700501", "ByteBuf", targetClass.getName()), e);
            }
        }
        return null;
    }
}
