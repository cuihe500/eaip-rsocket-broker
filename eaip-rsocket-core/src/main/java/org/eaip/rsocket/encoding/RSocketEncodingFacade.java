package org.eaip.rsocket.encoding;

import io.netty.buffer.ByteBuf;
import org.eaip.rsocket.encoding.impl.RSocketEncodingFacadeImpl;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * RSocket Encoding Facade
 *
 * @author CuiCHangHe
 */
public interface RSocketEncodingFacade {

    /**
     * get RSocket encoding facade singleton
     *
     * @return encoding facade
     */
    static RSocketEncodingFacade getInstance() {
        return RSocketEncodingFacadeImpl.instance;
    }

    @NotNull
    ByteBuf encodingParams(@Nullable Object[] args, RSocketMimeType encodingType) throws EncodingException;

    @Nullable
    Object decodeParams(RSocketMimeType encodingType, @Nullable ByteBuf data, @Nullable Class<?>... targetClasses) throws EncodingException;

    @NotNull
    ByteBuf encodingResult(@Nullable Object result, RSocketMimeType encodingType) throws EncodingException;

    @Nullable
    Object decodeResult(RSocketMimeType encodingType, @Nullable ByteBuf data, @Nullable Class<?> targetClass) throws EncodingException;
}
