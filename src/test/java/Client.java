import net.mckitsu.lib.network.Network;
import net.mckitsu.lib.network.NetworkSlot;
import net.mckitsu.lib.network.TcpChannel;
import net.mckitsu.lib.network.TcpClient;
import sun.nio.ch.Net;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client extends NetworkTest {
    public static final String format = "%1$tF %1$tT [%4$s] %5$s%6$s%n";

    public static void main(String[] args){
        //System.setProperty("java.util.logging.SimpleFormatter.format", format);
        Client client = new Client();
        client.testTcpClient();
    }

    public void testTcpClient(){
        this.network.connect(new InetSocketAddress("127.0.0.1", 8888), this::onConnect, this::onConnectFail, this::onDisconnect);

        Scanner scanner = new Scanner(System.in);
        while (true){
            try {
                byte[] data = scanner.nextLine().getBytes(StandardCharsets.US_ASCII);
                for(int i=0; i<256; i++)
                    this.network.write(ByteBuffer.allocate(4).putInt(i).array(), this::onWrite, this::onWriteFail);
            }catch (Throwable exc){
                Logger.getGlobal().warning(exc.toString());
            }

        }

    }
}
