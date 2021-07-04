package net.mckitsu.lib.network.local;

import net.mckitsu.lib.network.TcpChannel;
import net.mckitsu.lib.network.TcpClient;
import net.mckitsu.lib.network.util.CompletionHandlerEvent;

import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

public class NetworkBasic {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */
    protected final TcpClient tcpClient;



    /* **************************************************************************************
     *  Variable <Private>
     */
    private final CompletionHandler<Void, ConnectAttachment> eventConnect
            = new CompletionHandlerEvent<>(this::eventConnectCompletion, this::eventConnectFailed);

    private Runnable onDisconnect;


    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */

    /**
     * 建構子
     */
    public NetworkBasic(){
        this.tcpClient = new TcpClient();
    }


    /* **************************************************************************************
     *  Public Method
     */

    /**
     *
     * @param tcpChannel 已經連接的TcpChannel通道。
     * @param completion 連線成功時呼叫。
     * @param failed 連線失敗時呼叫。
     * @param disconnect 連線成功後並段開連接時呼叫。
     */
    public void connect(TcpChannel tcpChannel, Runnable completion, Consumer<Throwable> failed, Runnable disconnect){
        ConnectAttachment connectAttachment = new ConnectAttachment(completion, failed, disconnect);
        this.tcpClient.connect(tcpChannel, connectAttachment, this.eventConnect);
    }


    /**
     *
     * @param remoteAddress 連線目標位置。
     * @param completion 連線成功時呼叫。
     * @param failed 連線失敗時呼叫。
     * @param disconnect 連線成功後並段開連接時呼叫。
     */
    public void connect(SocketAddress remoteAddress, Runnable completion, Consumer<Throwable> failed, Runnable disconnect){
        ConnectAttachment connectAttachment = new ConnectAttachment(completion, failed, disconnect);
        this.tcpClient.connect(remoteAddress, connectAttachment, this.eventConnect);
    }


    /**
     *
     * @param data 寫入資料
     * @param completion 當寫入成功時調用。
     * @param failed 當寫入失敗時調用。
     */
    public void write(byte[] data, Consumer<byte[]> completion, Consumer<Throwable> failed){
        tcpClient.write(data, null, new TransferAttachment(completion, failed));
    }


    /**
     *
     * @param completion 當寫入成功時調用。
     * @param failed 當寫入失敗時調用。
     */
    public void read(Consumer<byte[]> completion, Consumer<Throwable> failed){
        tcpClient.read(null, new TransferAttachment(completion, failed));
    }

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */

    /**
     *
     * @param data 寫入資料
     * @param completion 當寫入成功時調用。
     * @param failed 當寫入失敗時調用。
     */
    public void directWrite(byte[] data, Consumer<byte[]> completion, Consumer<Throwable> failed){
        tcpClient.write(data, null, new TransferAttachment(completion, failed));
    }


    /**
     *
     * @param completion 當寫入成功時調用。
     * @param failed 當寫入失敗時調用。
     */
    public void directRead(Consumer<byte[]> completion, Consumer<Throwable> failed){
        tcpClient.read(null, new TransferAttachment(completion, failed));
    }


    /**
     * 離線事件處理
     *
     */
    protected void executeDisconnect(){
        try {
            this.onDisconnect.run();
        }catch (Throwable ignore){}
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

    /**
     * CompletionHandler事件，當TcpClient連線成功時調用。
     *
     * @param result Void
     * @param attachment ConnectAttachment
     */
    private void eventConnectCompletion(Void result, ConnectAttachment attachment){
        this.onDisconnect = attachment.disconnect;
        try {
            attachment.completion.run();
        }catch (Throwable ignore){}

    }


    /**
     * CompletionHandler事件，當TcpClient連線失敗時調用。
     *
     * @param exc Exception
     * @param attachment ConnectAttachment
     */
    private void eventConnectFailed(Throwable exc, ConnectAttachment attachment){
        try {
            attachment.failed.accept(exc);
        }catch (Throwable ignore){}
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */

    /* **************************************************************************************
     * Class/Interface/Enum
     */

    private static class TransferAttachment implements CompletionHandler<byte[], Void>{
        public final Consumer completed;
        public final Consumer failed;

        public TransferAttachment(Consumer completed, Consumer failed){
            this.completed = completed;
            this.failed = failed;
        }

        @Override
        public void completed(byte[] result, Void attachment) {
            this.completed.accept(result);
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            this.failed.accept(exc);
        }
    }


    /**
     * ConnectAttachment
     */
    private static class ConnectAttachment{
        public final Runnable completion;
        public final Consumer failed;
        public final Runnable disconnect;

        public ConnectAttachment(Runnable completion, Consumer failed, Runnable disconnect){
            this.completion = completion;
            this.failed = failed;
            this.disconnect = disconnect;
        }
    }
}
