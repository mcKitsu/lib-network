import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.ClientEvent;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Server{

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }

    public void run(){
        System.out.println("Server");
        final TcpClient[] tcpClientLocal = new TcpClient[1];
        TcpListener tcpListener = new TcpListener() {
            @Override
            public void onOpenFail(Throwable e) {

            }

            @Override
            public void onAccept(TcpChannel tcpChannel) {
                new TcpClient(tcpChannel, 16384, new ClientEvent() {
                    public int count = 0;

                    @Override
                    public void onConnect(TcpClient tcpClient) {
                        tcpClientLocal[0] = tcpClient;
                        System.out.println("onConnect");
                    }

                    @Override
                    public void onConnectFail(TcpClient tcpClient) {
                        System.out.println("onConnectFail");
                    }

                    @Override
                    public void onDisconnect(TcpClient tcpClient) {
                        System.out.println("onDisconnect");
                    }

                    @Override
                    public void onDisconnectRemote(TcpClient tcpClient) {
                        System.out.println("onDisconnectRemote");
                    }

                    @Override
                    public void onTransfer(TcpClient tcpClient, byte[] data) {
                        System.out.println("onTransfer: " + Arrays.toString(data));
                    }

                    @Override
                    public void onReceiver(TcpClient tcpClient, byte[] data) {
                        System.out.println("onReceiver[" + count + "]: " + Arrays.toString(data));
                        count++;
                    }
                });
            }
        };

        tcpListener.start(new InetSocketAddress(8888));
        Scanner scanner = new Scanner(System.in);
        while (true){
            String read = scanner.nextLine();
            try{
                tcpClientLocal[0].write(read.getBytes(StandardCharsets.UTF_8));
            }catch (Throwable ignore){}
        }

    }
}
