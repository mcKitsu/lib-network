import java.nio.ByteBuffer;

public class Main {
    public static void main(String[] args){
        /*
        CRC32 crc32 = new CRC32();
        byte[] src = "Hello".getBytes(StandardCharsets.US_ASCII);
        crc32.update(src);
        long result = crc32.getValue();
        int resultInt = (int) result;
        System.out.format("0x%016X",result);
         */

        ByteBuffer byteBuffer = ByteBuffer.allocate(12);
        System.out.println(byteBuffer);
        byteBuffer.putInt(123);
        System.out.println(byteBuffer);
        byteBuffer.flip();
        System.out.println(byteBuffer);
        System.out.println(byteBuffer.getInt());
    }
}