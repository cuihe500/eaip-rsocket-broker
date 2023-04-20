package org.eaip.rsocket.encoding.impl;

import io.netty.buffer.ByteBuf;
import org.eaip.rsocket.metadata.RSocketMimeType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * RSocketEncodingFacadeImpl Test
 *
 * @author CuiCHangHe
 */
public class RSocketEncodingFacadeImplTest {
    private RSocketEncodingFacadeImpl encodingFacade = new RSocketEncodingFacadeImpl();
    private RSocketMimeType[] mimeTypes = new RSocketMimeType[]{RSocketMimeType.Hessian,
            RSocketMimeType.Json,
            RSocketMimeType.Protobuf};

    @Test
    public void testEncoding() {
        Map<RSocketMimeType, ByteBuf> store = new HashMap<>();
        TempAccount account = new TempAccount(1L, "leijuan", new Date());
        for (RSocketMimeType mimeType : mimeTypes) {
            ByteBuf byteBuf = encodingFacade.encodingResult(account, mimeType);
            store.put(mimeType, byteBuf);
        }
        for (RSocketMimeType mimeType : mimeTypes) {
            TempAccount result = (TempAccount) encodingFacade.decodeResult(mimeType, store.get(mimeType), TempAccount.class);
            Assertions.assertNotNull(result);
            System.out.println(result.getName());
        }
    }
}
