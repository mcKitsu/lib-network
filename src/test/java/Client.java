import net.mckitsu.lib.network.tcp.TcpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    public void run(){
        System.out.println("Client");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
