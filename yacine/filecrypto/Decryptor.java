package yacine.filecrypto;

import yacine.blake2.*;
import yacine.xtea.*;

import java.io.*;

public class Decryptor {
    public static void decryptFile(String inputPath, String outputPath, String password) throws IOException {
        byte[] full = readFile(inputPath);

        int mode = full[0];
        byte[] iv = new byte[8];
        System.arraycopy(full, 1, iv, 0, 8);
        byte[] hmac = new byte[32];
        System.arraycopy(full, full.length - 32, hmac, 0, 32);
        byte[] encrypted = new byte[full.length - 1 - 8 - 32];
        System.arraycopy(full, 1 + 8, encrypted, 0, encrypted.length);

        byte[] key = blake2s.hash(password.getBytes());
        key = truncate(key, 16);

        byte[] computedHmac = HMACBlake2s.hmac(key, encrypted);
        if (!java.util.Arrays.equals(hmac, computedHmac)) {
            throw new SecurityException("HMAC verification failed! File is corrupted or wrong password.");
        }

        byte[] decrypted;
        if (mode == 0x00) {
            decrypted = XTEA.decryptCBC(encrypted, key, iv);
        } else {
            decrypted = XTEA.decryptCTR(encrypted, key, iv);
        }

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(decrypted);
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
