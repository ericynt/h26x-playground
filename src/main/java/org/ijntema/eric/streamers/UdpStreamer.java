package org.ijntema.eric.streamers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class UdpStreamer extends Thread {

    private       int                        sendPort    = 55555;
    private final ArrayBlockingQueue<byte[]> packetQueue = new ArrayBlockingQueue<>(500);

    public UdpStreamer (int sendPort) {

        this.sendPort = sendPort;
    }

    public void run () {

        log.info("Starting UDP streamer");

        try (DatagramSocket socket = new DatagramSocket()) {

            while (true) {

                try {

                    // Blocks until new data is available
                    byte[] bytes = this.packetQueue.take();
                    InetAddress sendHost = InetAddress.getLoopbackAddress();
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, sendHost, sendPort);

                    socket.send(packet);
                } catch (InterruptedException | IOException e) {

                    throw new RuntimeException(e);
                }
            }
        } catch (SocketException e) {

            throw new RuntimeException(e);
        }
    }
}