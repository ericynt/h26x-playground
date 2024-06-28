package org.ijntema.eric.streamers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpStreamer extends Thread {

    private final DatagramSocket             socket;
    private final byte[]                     buf         = new byte[256];
    @Getter
    private final ArrayBlockingQueue<byte[]> packetQueue = new ArrayBlockingQueue<>(500);

    public UdpStreamer () throws SocketException {

        socket = new DatagramSocket(55555);
    }

    public void run () {

        log.info("Starting UDP streamer");

        try {

            InetAddress address = null;
            int port = 0;

            while (true) {

                if (address == null) {

                    DatagramPacket receivedPacket
                            = new DatagramPacket(buf, buf.length);
                    try {

                        socket.receive(receivedPacket);
                    } catch (IOException e) {

                        throw new RuntimeException(e);
                    }

                    address = receivedPacket.getAddress();
                    port = receivedPacket.getPort();

                    log.info("Received connection from {}:{}", address, port);
                } else {

                    try {

                        byte[] bytes = this.packetQueue.take(); // blocks until new data is available
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

                        log.info("Sending UDP packet to {}:{}", address, port);

                        socket.send(packet);
                    } catch (InterruptedException | IOException e) {

                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {

            socket.close();
        }
    }
}