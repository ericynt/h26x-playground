package org.ijntema.eric.streamers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.dsl.Udp;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@SpringBootApplication
public class UdpVideoStreamerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UdpVideoStreamerApplication.class, args);
    }
}

@Configuration
@EnableIntegration
@IntegrationComponentScan
class UdpConfig {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;

    @Bean
    public MessageChannel udpOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public UnicastSendingMessageHandler udpMessageHandler() {
        return Udp.outboundAdapter(HOST, PORT)
                  .get();
    }

    @Bean
    public IntegrationFlow udpOutboundFlow() {
        return IntegrationFlows.from(udpOutboundChannel())
                               .handle(udpMessageHandler())
                               .get();
    }
}

@MessagingGateway(defaultRequestChannel = "udpOutboundChannel")
interface UdpGateway {
    void sendToUdp(byte[] data);
}
