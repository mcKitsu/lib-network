package net.mckitsu.lib.network.net;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NetClientPacket {
    public final int slotID;
    public final byte[] sourceData;

    protected NetClientPacket(int slotID, byte[] data){
        this.slotID = slotID;
        this.sourceData = new byte[data.length+4];
        ByteBuffer cache = ByteBuffer.wrap(this.sourceData);
        cache.putInt(slotID);
        cache.put(data);
    }

    protected NetClientPacket(byte[] sourceData){
        this.sourceData = sourceData;
        ByteBuffer cache = ByteBuffer.wrap(this.sourceData);
        this.slotID = cache.getInt();
    }

    public byte[] getData(){
        byte[] result = new byte[sourceData.length-4];
        System.arraycopy(sourceData, 4, result, 0, (sourceData.length-4));
        return result;
    }

    @Override
    public String toString(){
        return "NetClientPacket{" +
                "slotID=" + this.slotID +
                ", sourceData=" + Arrays.toString(this.sourceData) +
                '}';
    }
}
