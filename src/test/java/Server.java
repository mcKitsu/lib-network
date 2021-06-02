import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;

public class Server {

    public static void main(String[] args){
        TcpListener tcpListener = new TcpListener();

        CompletionHandler<TcpChannel, Void> handler = new CompletionHandler<TcpChannel, Void>() {
            @Override
            public void completed(TcpChannel result, Void attachment) {
                System.out.println("on Connect");
            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        };

        tcpListener.start(new InetSocketAddress("127.0.0.1", 8888), handler);

        while(true);
    }
}
