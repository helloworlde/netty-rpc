package io.github.helloworlde.netty.rpc.example.springboot.server;

import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class NettyRpcServerApplicationTests {

    @MockBean
    private CollectorRegistry collectorRegistry;

    @Test
    void contextLoads() {
    }

}
