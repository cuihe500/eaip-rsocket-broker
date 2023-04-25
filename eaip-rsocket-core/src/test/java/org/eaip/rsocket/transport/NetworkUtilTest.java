package org.eaip.rsocket.transport;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eaip.rsocket.transport.NetworkUtil.isInternalIp;

public class NetworkUtilTest {

    @Test
    public void testIsInternalIp() {
        assertThat(isInternalIp("192.168.11.11")).isTrue();
        assertThat(isInternalIp("127.0.0.1")).isTrue();
        assertThat(isInternalIp("localhost")).isTrue();
        assertThat(isInternalIp("taobao.com")).isFalse();
    }
}
