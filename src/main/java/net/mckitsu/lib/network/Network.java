package net.mckitsu.lib.network;

import net.mckitsu.lib.network.util.EncryptAes;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

public abstract class Network{
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final boolean isMaster;
    private final TcpChannel tcpChannel;
    private final Handshake networkHandshake;
    private EncryptAes encryptAES;



    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */
    protected abstract void onConnect();
    protected abstract void onDisconnect();
    protected abstract void onConnectFail();
    protected abstract void onSlotOpen(NetworkSlot networkSlot);

    /* **************************************************************************************
     *  Construct Method
     */

    public Network(TcpChannel tcpChannel){
        this.isMaster = true;
        this.tcpChannel = tcpChannel;
        this.networkHandshake = new HandshakeMaster();
        CompletionHandler<EncryptAes, Void> handler = new CompletionHandler<EncryptAes, Void>() {
            @Override
            public void completed(EncryptAes result, Void attachment) {
                System.out.println("successful");
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("fail");
            }
        };
        this.networkHandshake.action(this, handler);
    }

    public Network(){
        this.isMaster = false;
        this.tcpChannel = new TcpChannel();
        this.networkHandshake = new HandshakeSlave();
    }




    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /*----------------------------------------
     *  connect
     *----------------------------------------*/
    public void connect(SocketAddress remoteAddress){
        if(isMaster)
            return;

        if(this.tcpChannel.isConnect())
            return;

        CompletionHandler<Void, Object> handler = new CompletionHandler<Void, Object>() {
            @Override
            public void completed(Void result, Object attachment) {
                CompletionHandler<EncryptAes, Void> handler = new CompletionHandler<EncryptAes, Void>() {
                    @Override
                    public void completed(EncryptAes result, Void attachment) {
                        Network.this.encryptAES = result;
                        try {
                            Network.this.onConnect();
                        }catch (Throwable ignore){}
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        try{
                            Network.this.onConnectFail();
                        }catch (Throwable ignore){}
                    }
                };

                Network.this.networkHandshake.action(Network.this, handler);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    Network.this.onConnectFail();
                }catch (Throwable ignore){}
            }
        };

        this.tcpChannel.connect(remoteAddress, null, handler);
    }



    /*----------------------------------------
     *  close
     *----------------------------------------*/
    public void disconnect(){
        synchronized (this.tcpChannel){
            if(this.tcpChannel.isConnect()){
                this.tcpChannel.close();
                this.onDisconnect();
            }
        }
    }



    /*----------------------------------------
     *  close
     *----------------------------------------*/
    public NetworkSlot openSlot(NetworkSlotEvent networkSlotEvent){
        return new NetworkSlot(networkSlotEvent);
    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    /*----------------------------------------
     *  directWrite
     *----------------------------------------*/
    protected  <A> void directWrite(ByteBuffer byteBuffer, A attachment, CompletionHandler<Integer, A> handler){
        this.tcpChannel.write(byteBuffer, attachment, handler);
    }



    /*----------------------------------------
     *  directRead
     *----------------------------------------*/
    protected <A> void directRead(ByteBuffer byteBuffer, A attachment, CompletionHandler<Integer, A> handler){
        this.tcpChannel.read(byteBuffer, attachment, handler);
    }


    
    /* **************************************************************************************
     *  Protected Method <Override>
     */

    /* **************************************************************************************
     *  Protected Method <Static>
     */

    /* **************************************************************************************
     *  Private Method
     */

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */


}
