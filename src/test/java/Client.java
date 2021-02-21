import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.ClientEvent;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    public void run(){
        System.out.println("Client");
        TcpClient tcpClient = new TcpClient(8192, new ClientEvent() {
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
        tcpClient.connect(new InetSocketAddress("127.0.0.1", 8888));
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
