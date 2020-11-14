package net.mckitsu.lib.network;

import net.mckitsu.lib.network.net.NetChannel;
import net.mckitsu.lib.network.net.NetClient;
import net.mckitsu.lib.network.net.NetServer;
import net.mckitsu.lib.network.tcp.TcpChannel;
import net.mckitsu.lib.network.tcp.TcpClient;
import net.mckitsu.lib.network.tcp.TcpListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpTest {

    public void run(){
        TcpListenerTest tcpListenerTest = new TcpListenerTest();
        TcpClientTest tcpClientTest = new TcpClientTest();
        tcpListenerTest.run();
        try {
            tcpClientTest.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
class TcpClientTest{
    private NetClient netClient;
    private byte[] verifyKey = {1,2,3,4,5};
    public void run() throws IOException {
        netClient = new NetClient(verifyKey);
        netClient.connect(new InetSocketAddress("127.0.0.1", 8888), 0);

    }
}

class TcpListenerTest{
    ExecutorService executorService = Executors.newCachedThreadPool();
    private byte[] verifyKey = {1,2,3,4,5};
    private NetServer netServer;
    public void run(){
       netServer = new NetServer(verifyKey) {
           @Override
           protected void onAccept(NetClient netClient) {
               System.out.println("NetServer onAccept");
               netClient.openSlot();
           }
       };


       netServer.start(new InetSocketAddress(8888));
    }

}
