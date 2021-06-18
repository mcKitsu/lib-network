package net.mckitsu.lib.network;

import net.mckitsu.lib.network.util.EncryptAes;
import net.mckitsu.lib.network.util.EncryptRsa;
import java.nio.channels.CompletionHandler;


public abstract class Handshake {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private EncryptRsa encryptRsa;
    private EncryptAes encryptAes;

    /* **************************************************************************************
     *  Abstract method <Public>
     */
    public abstract void action(Network network, CompletionHandler<EncryptAes, Void> handler);



    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    protected Handshake(){
    }



    /* **************************************************************************************
     *  Public Method
     */

    /* **************************************************************************************
     *  Public Method <Override>
     */

    /* **************************************************************************************
     *  Public Method <Static>
     */

    /* **************************************************************************************
     *  Protected Method
     */
    protected void doCompleted(CompletionHandler<EncryptAes, Void> handler, EncryptAes encryptAes){
        try {
            handler.completed(encryptAes, null);
        }catch (Throwable ignore){}
    }


    protected void doFailed(CompletionHandler<EncryptAes, Void> handler, Throwable e){
        try {
            handler.failed(e, null);
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

    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}
