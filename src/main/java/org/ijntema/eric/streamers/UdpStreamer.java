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
public class UdpStreamer extends Thread {

    private final DatagramSocket             socket;
    @Getter
    private final ArrayBlockingQueue<byte[]> packetQueue = new ArrayBlockingQueue<>(500);

    public UdpStreamer () throws SocketException {

        socket = new DatagramSocket(55554);
    }

    public void run () {

        log.info("Starting UDP streamer");

        try {

            while (true) {

                try {

                    byte[] bytes = this.packetQueue.take(); // blocks until new data is available
                    InetAddress sendHost = InetAddress.getByName("localhost");
                    int sendPort = 55555;
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length, sendHost, sendPort);

                    socket.send(packet);
                } catch (InterruptedException | IOException e) {

                    throw new RuntimeException(e);
                }
            }
        } finally {

            log.info("Shutting down UDP streamer");

            socket.close();
        }
    }
}