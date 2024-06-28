package org.ijntema.eric.streamers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class UdpVideoStreamer implements CommandLineRunner {

    private static final String VIDEO_FILE_PATH = "echo-hereweare.mp4";
    private static final int BUFFER_SIZE = 512; // Adjust based on your network capabilities

    @Autowired
    private UdpGateway udpGateway;

    @Override
    public void run(String... args) throws IOException {
        try (InputStream inputStream = new FileInputStream(ResourceUtils.getFile("classpath:" + VIDEO_FILE_PATH))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] data = new byte[bytesRead];
                System.arraycopy(buffer, 0, data, 0, bytesRead);
                udpGateway.sendToUdp(data);
                // Adjust sleep time based on the desired bitrate or frame rate
                Thread.sleep(1000 / 30); // Simulate 30 FPS
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
