package yacine.filecrypto;
import yacine.blake2.*;
import yacine.xtea.*;
import java.io.*;
import java.util.Random;

public class Encryptor {
    public static void encryptFile(String inputPath, String outputPath, String password, boolean useCBC) throws IOException {
        byte[] data = readFile(inputPath);
        byte[] key = blake2s.hash(password.getBytes());
        key = truncate(key, 16); // XTEA = 128 bits
        byte[] iv = new byte[8];
        new Random().nextBytes(iv);

        byte[] encrypted;
        if (useCBC) {
            encrypted = XTEA.encryptCBC(data, key, iv);
        } else {
            encrypted = XTEA.encryptCTR(data, key, iv);
        }

        byte[] hmac = HMACBlake2s.hmac(key, encrypted);

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(useCBC ? 0x00 : 0x01);
            fos.write(iv);
            fos.write(encrypted);
            fos.write(hmac);
        }
    }

    private static byte[] readFile(String path) throws IOException {
        return java.nio.file.Files.readAllBytes(new File(path).toPath());
    }

    private static byte[] truncate(byte[] data, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, 0, result, 0, length);
        return result;
    }
}
