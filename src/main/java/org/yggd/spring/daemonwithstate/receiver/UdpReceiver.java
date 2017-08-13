package org.yggd.spring.daemonwithstate.receiver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UdpReceiver implements Receiver {

    private final int bufferSize;
    private final String encoding;
    private final DatagramSocket socket;


    public UdpReceiver(@Value("${app.udp.so-timeout}") int interval,
                       @Value("${app.udp.listen-port}") int port,
                       @Value("${app.udp.buffer-size}") int bufferSize,
                       @Value("${app.udp.encoding}") String encoding) throws SocketException {
        this.bufferSize = bufferSize;
        this.encoding = encoding;
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(interval);
    }

    @Override
    public ReceiveMessage receive() throws InterruptedException {
        final DatagramPacket packet = new DatagramPacket(new byte[bufferSize], 0, bufferSize);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e) {
            final ReceiveMessage timeoutMessage = new ReceiveMessage();
            timeoutMessage.setState(RequestState.POLLING);
            timeoutMessage.setJobName("sampleJob");
            return timeoutMessage;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return parseReceiveMessage(
                new String(packet.getData(), packet.getOffset(), packet.getLength(), Charset.forName(encoding))
        );
    }

    ReceiveMessage parseReceiveMessage(String str) {
        final ReceiveMessage message = new ReceiveMessage();
        final List<String> parameters = new ArrayList<>();
        Arrays.stream(str.split(",")).forEach(s -> {
            if (s.startsWith("mode=")) {
                message.setState(determineState(s.substring("mode=".length()).toUpperCase()));
            } else if (s.startsWith("job=")) {
                message.setJobName(s.substring("job=".length()));
            } else {
                parameters.add(s);
            }
        });
        message.setJobParameters(parameters.stream()
                .filter(s -> s.contains("\r") || s.contains("\n"))
                .collect(Collectors.joining(","))
        );
        return message;
    }

    RequestState determineState(String stateStr) {
        // よくわからない状態を表す文字列が届いたらポーリング扱い
        return RequestState.containsString(stateStr) ? RequestState.valueOf(stateStr) : RequestState.POLLING;
    }

    @PreDestroy
    public void preDestroy() {
        socket.close();
    }
}
