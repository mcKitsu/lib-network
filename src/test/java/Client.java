import net.mckitsu.lib.network.tcp.TcpChannel;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

public class Client {
    public static void main(String[] args){
        TcpChannel tcpChannel = new TcpChannel();

        CompletionHandler<Void, Void> handler = new CompletionHandler<Void, Void>() {
            @Override
            public void completed(Void result, Void attachment) {
                System.out.println("Connect");
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("Connect Fail");
            }
        };

        tcpChannel.connect(new InetSocketAddress("127.0.0.1", 8888), null, handler);

        while(true);
    }

}
