import net.mckitsu.lib.util.encrypt.AES;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Main {
    public static void main(String[] args){
        try {
            new Main().run();
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }


    public void run() throws NoSuchAlgorithmException, InvalidKeyException {
            AES aes = new AES(generateKeyAES());
            byte[] src = "Hello".getBytes(StandardCharsets.UTF_8);
            ByteBuffer byteBufferSrc = ByteBuffer.wrap(src);
            ByteBuffer byteBufferDst = ByteBuffer.allocate(32);

            byteBufferDst.putInt(0);
            writeLength(byteBufferDst, aes.encrypt(byteBufferSrc, byteBufferDst));

            System.out.println("position: " + byteBufferDst.position());
            byteBufferDst.flip();
            System.out.println(byteBufferDst.remaining());
            System.out.println(byteBufferDst.getInt());
            byte[] data = new byte[4];
            byteBufferDst.get(data);
            System.out.println(byteToString(data));
            System.out.println(byteBufferDst.remaining());



    }

    private String byteToString(byte[] data){
        StringBuilder stringBuilder = new StringBuilder();
        for(byte d : data){
            stringBuilder.append(String.format("0x%02X ", d));
        }
        return stringBuilder.toString();
    }

    protected SecretKey generateKeyAES() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128,new SecureRandom());
        return keyGen.generateKey();
    }

    protected void writeLength(ByteBuffer src, int length){
        if(src.array().length<=4)
            return;

        src.array()[0] = (byte)(length >>> 24);
        src.array()[1] = (byte)(length >>> 16);
        src.array()[2] = (byte)(length >>> 8);
        src.array()[3] = (byte)(length);
    }
}
