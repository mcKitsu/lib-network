package net.mckitsu.lib.network;

import net.mckitsu.lib.network.net.NetClient;
import net.mckitsu.lib.network.net.NetClientSlot;
import net.mckitsu.lib.network.net.NetServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        //TcpTest tcpTest = new TcpTest();
        //tcpTest.run();
        //System.out.println("End Test");
        //Thread.sleep(30000);
        Main main = new Main();
        main.run();
    }

    private void run(){
        System.out.println("Start");
        byte[] verifyKey = {1,2,3,4,5};
        Server server = new Server(verifyKey);
        server.run();

        Client client = new Client(verifyKey);
        client.run();


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        server.netServer.stop();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End");
    }
}

class Client{
    private final byte[] verifyKey;
    public NetClient netClient;

    private void onClose(NetClientSlot netClientSlot){
        System.out.println("Client slot close: slotId = " + netClientSlot.slotId);
    }

    public Client(byte[] verifyKey){
        this.verifyKey = verifyKey;
    }

    private void onConnect(NetClient netClient){
        System.out.println("Client Connect");

        NetClientSlot netClientSlot = netClient.openSlot();
        netClientSlot.event.setOnClose(this::onClose);

        netClientSlot.send("Hello slot".getBytes(StandardCharsets.US_ASCII));
        netClientSlot.send("Data".getBytes(StandardCharsets.US_ASCII));

    }

    private void onConnectFail(){
        System.out.println("Connect fail");
    }

    private void onDisconnect(){
        System.out.println("Client onDisconnect");
    }

    private void onRemoteDisconnect(){
        System.out.println("Client onRemoteDisconnect");
    }

    public void run() {
        try {
            netClient = new NetClient(verifyKey);
            netClient.event.setOnConnect(this::onConnect);
            netClient.event.setOnConnectFail(this::onConnectFail);
            netClient.event.setOnDisconnect(this::onDisconnect);
            netClient.event.setOnRemoteDisconnect(this::onRemoteDisconnect);

            netClient.connect(new InetSocketAddress("127.0.0.1", 8888), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Server{
    private final byte[] verifyKey;
    public NetServer netServer;

    public Server(byte[] verifyKey){
        this.verifyKey = verifyKey;
    }

    private void onRead(byte[] data){
        System.out.println(Arrays.toString(data));
    }

    private void onClose(NetClientSlot netClientSlot){
        System.out.println("Server slot onClose: slotId = " + netClientSlot.slotId);
    }

    private void show(NetClientSlot netClientSlot){
        System.out.println("READ: " + new String(netClientSlot.read(), StandardCharsets.US_ASCII));
    }

    private void acceptSlot(NetClientSlot netClientSlot){
        System.out.println("NetServer Open a new slot : SlotId = " + netClientSlot.slotId);
        netClientSlot.event.setOnReceiver(this::show);
        netClientSlot.event.setOnClose(this::onClose);
    }


    public void run() {
        System.out.println("Server Start");

        this.netServer = new NetServer(verifyKey) {
            @Override
            protected void onAccept(NetClient netClient) {
                System.out.println("NetServer onAccept");
                netClient.event.setOnAccept(Server.this::acceptSlot);
            }
        };

        netServer.start(new InetSocketAddress(8888));
    }
}