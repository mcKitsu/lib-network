package net.mckitsu.lib.network.local;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class TcpClientPacketHeader {
    /* **************************************************************************************
     *  Variable <Public>
     */

    /* **************************************************************************************
     *  Variable <Protected>
     */

    /* **************************************************************************************
     *  Variable <Private>
     */
    private final byte[] array;
    private final ByteBuffer byteBuffer;
    private int number;
    private int length;
    private int checksum;

    /* **************************************************************************************
     *  Abstract method <Public>
     */

    /* **************************************************************************************
     *  Abstract method <Protected>
     */

    /* **************************************************************************************
     *  Construct Method
     */
    public TcpClientPacketHeader(){
        this.array = new byte[12];
        this.byteBuffer = ByteBuffer.wrap(this.array);
        this.bufferWrite(0, 0, 0);
    }


    public TcpClientPacketHeader(int number, int checksum, int length){
        this();
        this.bufferWrite(number, checksum, length);
    }


    public TcpClientPacketHeader(byte[] data){
        this.array = Arrays.copyOf(data, 12);
        this.byteBuffer = ByteBuffer.wrap(this.array);
    }


    /* **************************************************************************************
     *  Public Method
     */

    /*----------------------------------------
     *  clear
     *----------------------------------------*/
    public void clear(){
        this.length = 0;
        this.number = 0;
        this.checksum = 0;
        Arrays.fill(this.array, (byte)0);
    }


    /*----------------------------------------
     *  setData
     *----------------------------------------*/
    public void setArray(byte[] data){
        try {
            ByteBuffer b = ByteBuffer.wrap(data);
            int number = b.getInt();
            int checksum = b.getInt();
            int length = b.getInt();
            this.bufferWrite(number, checksum, length);
        }catch (Throwable exc){
            this.bufferWrite(0, 0, 0);
        }
    }


    /*----------------------------------------
     *  setLength
     *----------------------------------------*/
    public void setLength(int length){
        this.bufferWrite(this.number, this.checksum, length);
    }


    /*----------------------------------------
     *  setNumber
     *----------------------------------------*/
    public void setNumber(int number){
        this.bufferWrite(number, this.checksum, this.length);
    }


    /*----------------------------------------
     *  setNumber
     *----------------------------------------*/
    public void setChecksum(int checksum){
        this.bufferWrite(this.number, checksum,this.length);
    }


    /*----------------------------------------
     *  setAll
     *----------------------------------------*/
    public void setAll(int number, int checksum, int length){
        this.bufferWrite(number, checksum, length);
    }


    /*----------------------------------------
     *  getNumber
     *----------------------------------------*/
    public int getNumber(){
        return this.number;
    }


    /*----------------------------------------
     *  getLength
     *----------------------------------------*/
    public int getLength(){
        return this.length;
    }


    /*----------------------------------------
     *  getChecksum
     *----------------------------------------*/
    public int getChecksum(){
        return this.checksum;
    }


    /*----------------------------------------
     *  array
     *----------------------------------------*/
    public byte[] array(){
        return this.array;
    }


    /* **************************************************************************************
     *  Public Method <Override>
     */
    @Override
    public String toString(){
        return String.format("PacketHeader[number=%d length=%d checksum=%d]", this.number, this.length, this.checksum);
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
    /*----------------------------------------
     *  bufferWrite
     *----------------------------------------*/
    private void bufferWrite(int number, int checksum, int length){
        this.byteBuffer.clear();
        this.number = number;
        this.checksum = checksum;
        this.length = length;
        this.byteBuffer.putInt(number);
        this.byteBuffer.putInt(checksum);
        this.byteBuffer.putInt(length);
    }


    /* **************************************************************************************
     *  Private Method <Override>
     */

    /* **************************************************************************************
     *  Private Method <Static>
     */
}
