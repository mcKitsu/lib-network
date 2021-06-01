import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.ClientEvent;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    public void run(){
        byte[] testData = new byte[16];
        for(int i=0; i<16; i++){
            testData[i] = (byte)i;
        }



        System.out.println("Client");
        TcpClient tcpClient = new TcpClient(16384, new ClientEvent() {
            public int count = 0;

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
                System.out.println("onTransfer[" + count + "]: " + Arrays.toString(data));
                count++;
            }

            @Override
            public void onReceiver(TcpClient tcpClient, byte[] data) {
                System.out.println("onReceiver: " + Arrays.toString(data));
            }
        });
        tcpClient.connect(new InetSocketAddress("127.0.0.1", 8888));
        Scanner scanner = new Scanner(System.in);
        while(true){
            String read = scanner.nextLine();
            for(int i=0; i<1024; i++)
                tcpClient.write(testData);
        }
    }
}
