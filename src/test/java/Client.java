import net.mckitsu.lib.network.net.NetClient;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }

    public void run(){
        System.out.println("Client");

        NetClient netClient = new NetClient(16384);
        netClient.event.setEvent(new NetClientEventHandle());
        netClient.connect(new InetSocketAddress("127.0.0.1", 8888));

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}
