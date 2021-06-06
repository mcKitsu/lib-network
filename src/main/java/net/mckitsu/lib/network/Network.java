package net.mckitsu.lib.network;

import net.mckitsu.lib.network.tcp.TcpChannel;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Network extends TcpChannel{
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    protected Network(AsynchronousSocketChannel asynchronousSocketChannel){
        super(asynchronousSocketChannel);

    }

    public Network(){

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
    @Override
    public <A> void connect(SocketAddress remoteAddress, A attachment, CompletionHandler<Void,? super A> handler){
        super.connect(remoteAddress, attachment, handler);
    }



    /*----------------------------------------
     *  close
     *----------------------------------------*/
    @Override
    public void close(){
        super.close();
    }



    /*----------------------------------------
     *  close
     *----------------------------------------*/
    public NetworkSlot openSlot(){
        return new NetworkSlot();
    }

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

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
