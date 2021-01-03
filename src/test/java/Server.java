import net.mckitsu.lib.network.net.NetClient;
import net.mckitsu.lib.network.net.NetServer;

import java.net.InetSocketAddress;
import java.util.Scanner;

public class Server extends NetClientEventHandle{
    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }

    public void run(){
        System.out.println("Server");

        NetServer netServer = new NetServer(16384) {
            @Override
            protected void onAccept(NetClient netClient) {
                System.out.println("Server::onAccept");
                netClient.event.setEvent(Server.this);
            }
        };

        netServer.start(new InetSocketAddress("127.0.0.1", 8888));

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        netServer.stop();
    }
}
