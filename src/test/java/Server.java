import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.ClientEvent;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Server{
    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }

    public void run(){
        System.out.println("Server");
        TcpListener tcpListener = new TcpListener() {
            @Override
            public void onOpenFail(Throwable e) {

            }

            @Override
            public void onAccept(TcpChannel tcpChannel) {
                new TcpClient(tcpChannel, 16384, new ClientEvent() {
                    @Override
                    public void onConnect(TcpClient tcpClient) {
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

                    }

                    @Override
                    public void onReceiver(TcpClient tcpClient, byte[] data) {

                    }
                });
            }
        };

        tcpListener.start(new InetSocketAddress(8888));
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        tcpListener.stop();
    }
}
