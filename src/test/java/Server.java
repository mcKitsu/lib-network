import net.mckitsu.lib.network.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class Server {
    TcpClient tcpClient;
    public static final String format = "%1$tF %1$tT [%4$s] %5$s%6$s%n";

    public static void main(String[] args){
        //System.setProperty("java.util.logging.SimpleFormatter.format", format);
        Server server = new Server();
        server.testTcpServer();
    }

    public void testTcpServer(){
        TcpListener tcpListener = new TcpListener() {
            @Override
            protected void onAccept(TcpChannel tcpChannel) {
                Logger.getGlobal().info("onAccept: " + tcpChannel.getRemoteAddress());
                tcpClient = new TcpClient(tcpChannel, null, null);
            }
        };
        tcpListener.start(new InetSocketAddress("127.0.0.1", 8888));

        Scanner scanner = new Scanner(System.in);
        while (true){
            try {
                byte data[] = scanner.nextLine().getBytes(StandardCharsets.US_ASCII);
                tcpClient.write(data, null, null);
            }catch (Throwable exc){
                Logger.getGlobal().warning(exc.toString());
            }
        }
    }
}
