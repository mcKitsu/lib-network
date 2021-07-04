import net.mckitsu.lib.network.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class Server extends NetworkTest {
    public static final String format = "%1$tF %1$tT [%4$s] %5$s%6$s%n";

    public static void main(String[] args){
        //System.setProperty("java.util.logging.SimpleFormatter.format", format);
        Server server = new Server();
        server.testTcpServer();
    }

    public void accept(TcpChannel tcpChannel){
        Logger.getGlobal().info("onAccept: " + tcpChannel.getRemoteAddress());
        this.network.connect(tcpChannel, this::onConnect, this::onConnectFail, this::onDisconnect);
    }

    public void testTcpServer(){
        TcpListener tcpListener = new TcpListener();
        tcpListener.start(new InetSocketAddress("127.0.0.1", 8888), this::accept);

        Scanner scanner = new Scanner(System.in);
        while (true){
            try {
                byte data[] = scanner.nextLine().getBytes(StandardCharsets.US_ASCII);
                this.network.write(data, this::onWrite, this::onWriteFail);
            }catch (Throwable exc){
                Logger.getGlobal().warning(exc.toString());
            }
        }
    }
}
