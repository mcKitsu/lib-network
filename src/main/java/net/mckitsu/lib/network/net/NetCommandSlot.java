package net.mckitsu.lib.network.net;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public abstract class NetCommandSlot extends NetClientSlot{
    protected abstract void onCommand(Command command, int slotId);

    protected NetCommandSlot(Executor executor) {
        super(0, executor);
        super.event.setOnReceiver(this::onReceiver);
    }

    /* **************************************************************************************
     *  Override Method
     */
    @Override
    public void close() {
    }
    /* **************************************************************************************
     *  Public Method
     */
    public void sendCommand(Command command, int slotID){
        Packet packet = new Packet(command, slotID);
        if(command == Command.UNKNOWN)
            return;

        this.send(packet.sourceData);
    }
    /* **************************************************************************************
     *  Private Method
     */
    private void onReceiver(NetClientSlot netClientSlot){
        while(!super.isEmpty()){
            byte[] data = super.read();
            if(data == null)
                continue;

            Packet packet = new Packet(data);
            this.onCommand(packet.command, packet.slotId);
        }
    }

    /* **************************************************************************************
     *  Class Packet
     */
    private static class Packet{
        public final byte[] sourceData;
        public final Command command;
        public final int slotId;

        public Packet(Command command, int slotID){
            this.sourceData = new byte[8];
            this.command = command;
            this.slotId = slotID;

            ByteBuffer cache = ByteBuffer.wrap(sourceData);
            cache.putInt(command.value);
            cache.putInt(slotID);
        }

        public Packet(byte[] sourceData){
            this.sourceData = sourceData;
            if(sourceData.length == 8){
                ByteBuffer cache = ByteBuffer.wrap(sourceData);
                this.command = Command.getEnum(cache.getInt());
                this.slotId = cache.getInt();
            }else{
                this.command = Command.UNKNOWN;
                this.slotId = 0;
            }
        }
    }

    /* **************************************************************************************
     *  Enum CommandSlot.Command
     */
    public enum Command{
        UNKNOWN(0),
        UNKNOWN_SLOT(0x5D036678),
        ALLOC_SLOT(0x6B5F5F9A),
        ACCEPT_SLOT(0x5CCBD6D3),
        CLOSE_SLOT(0xFD60EC5B);

        private static final Map<Integer, Command> intToTypeMap = new HashMap<>();

        static {
            for (Command type : Command.values()) {
                intToTypeMap.put(type.value, type);
            }
        }

        public final int value;

        public static Command getEnum(int i) {
            Command type = intToTypeMap.get(i);
            if (type == null)

                return Command.UNKNOWN;
            return type;
        }

        Command(int i){
            this.value = i;
        }
    }
}
