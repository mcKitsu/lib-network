import net.mckitsu.lib.network.Network;
import net.mckitsu.lib.network.NetworkSlot;
import net.mckitsu.lib.network.TcpChannel;

import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static final String format = "%1$tF %1$tT [%4$s] %5$s%6$s%n";

    public static void main(String[] args){
        //System.setProperty("java.util.logging.SimpleFormatter.format", format);
        Client client = new Client();
        client.tcpChannelClient();
    }

    public void tcpChannelClient(){
        TcpChannel tcpChannel = new TcpChannel();
        tcpChannel.connect(new InetSocketAddress("127.0.0.1", 8888), null, new CompletionHandler<Void, Object>() {

            @Override
            public void completed(Void result, Object attachment) {
                synchronized (Client.this){
                    Client.this.notify();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });

        try {
            synchronized (this){
                wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        java.util.logging.Logger.getGlobal().info("connect");
        ByteBuffer readCache = ByteBuffer.allocate(16384);
        tcpChannel.read(readCache, readCache, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                byte[] data = new byte[attachment.remaining()];
                attachment.put(data, 0, data.length);
                java.util.logging.Logger.getGlobal().info(Arrays.toString(data));
                attachment.clear();
                tcpChannel.read(attachment, attachment, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {

            }
        });

        Scanner scanner = new Scanner(System.in);
        ByteBuffer writeCache = ByteBuffer.allocate(16384);
        while (true){
            String inputString = scanner.nextLine();
            try {
                writeCache.clear();
                writeCache.put(inputString.getBytes());
                tcpChannel.write(writeCache, writeCache, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {

                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {

                    }
                });
            }catch (Throwable ignore){};
        }
    }

    public void networkClient(){
        Network network = new Network() {
            @Override
            protected void onConnect() {
                java.util.logging.Logger.getGlobal().info("onConnect");
            }

            @Override
            protected void onDisconnect() {
                java.util.logging.Logger.getGlobal().info("onDisconnect");
            }

            @Override
            protected void onConnectFail() {
                java.util.logging.Logger.getGlobal().info("onConnectFail");
            }

            @Override
            protected void onSlotOpen(NetworkSlot networkSlot) {
                java.util.logging.Logger.getGlobal().info("onSlotOpen");
            }
        };

        network.connect(new InetSocketAddress("127.0.0.1", 8888));
        while(true);
    }

}
