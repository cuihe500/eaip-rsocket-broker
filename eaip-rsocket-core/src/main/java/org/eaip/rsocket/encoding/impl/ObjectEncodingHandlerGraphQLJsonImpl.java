package org.eaip.rsocket.encoding.impl;

import org.eaip.rsocket.metadata.RSocketMimeType;
import org.jetbrains.annotations.NotNull;

/**
 * GraphQL json encoding - application/graphql+json
 *
 * @author CuiCHangHe
 */
public class ObjectEncodingHandlerGraphQLJsonImpl extends ObjectEncodingHandlerJsonImpl {
    @NotNull
    @Override
    public RSocketMimeType mimeType() {
        return RSocketMimeType.GraphQLJson;
    }
}
