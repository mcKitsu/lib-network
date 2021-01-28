
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Server{
    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }

    public void run(){
        System.out.println("Server");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
