import net.mckitsu.lib.network.Network;
import net.mckitsu.lib.network.NetworkSlot;
import net.mckitsu.lib.network.TcpChannel;
import net.mckitsu.lib.network.TcpListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.Scanner;

public class Server {
    public static final String format = "%1$tF %1$tT [%4$s] %5$s%6$s%n";

    public static void main(String[] args){
        //System.setProperty("java.util.logging.SimpleFormatter.format", format);
        Server server = new Server();
        server.TcpListenerTest();
    }

    public void TcpListenerTest(){
        final TcpChannel[] testTcpChannel = new TcpChannel[1];
        TcpListener tcpListener = new TcpListener() {
            @Override
            protected void onAccept(TcpChannel tcpChannel) {
                java.util.logging.Logger.getGlobal().info("Connect");
                testTcpChannel[0] = tcpChannel;
                ByteBuffer readTmp = ByteBuffer.allocate(16384);
                tcpChannel.read(readTmp, readTmp, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        attachment.flip();
                        byte[] tmp = new byte[attachment.remaining()];
                        java.util.logging.Logger.getGlobal().info(Arrays.toString(tmp));
                        attachment.clear();
                        tcpChannel.read(attachment, attachment, this);
                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {

                    }
                });
            }
        };




        tcpListener.start(new InetSocketAddress("127.0.0.1", 8888));
        Scanner scanner = new Scanner(System.in);
        ByteBuffer cache = ByteBuffer.allocate(16384);
        while (true){
            String inputString = scanner.nextLine();
            cache.clear();
            cache.put(inputString.getBytes());
            cache.flip();
            try {
                testTcpChannel[0].write(cache, cache, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {

                    }

                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {

                    }
                });
            }catch (Throwable ignore){}

        }

    }

    public void NetworkServer(){
        TcpListener tcpListener = new TcpListener() {
            @Override
            protected void onAccept(TcpChannel tcpChannel) {
                java.util.logging.Logger.getGlobal().info("onAccept " + tcpChannel.getRemoteAddress());
                new Network(tcpChannel) {
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
            }
        };


        tcpListener.start(new InetSocketAddress("127.0.0.1", 8888));
        while(true);
    }
}
