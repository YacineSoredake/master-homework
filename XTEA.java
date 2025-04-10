import java.io.*;
import java.util.Arrays;

public class XTEA {
    private static final int DELTA = 0x9E3779B9;
    private static final int ROUNDS = 32;
    private int[] key;

    public XTEA(byte[] keyBytes) {
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("La clé doit faire 16 octets (128 bits)");
        }
        this.key = new int[4];
        for (int i = 0; i < 4; i++) {
            this.key[i] = ((keyBytes[i * 4] & 0xFF) << 24) | ((keyBytes[i * 4 + 1] & 0xFF) << 16) |
                          ((keyBytes[i * 4 + 2] & 0xFF) << 8) | (keyBytes[i * 4 + 3] & 0xFF);
        }
    }

    public void encrypt(int[] v) {
        int v0 = v[0], v1 = v[1], sum = 0;
        for (int i = 0; i < ROUNDS; i++) {
            v0 += ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + key[sum & 3]);
            sum += DELTA;
            v1 += ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + key[(sum >>> 11) & 3]);
        }
        v[0] = v0;
        v[1] = v1;
    }

    public void decrypt(int[] v) {
        int v0 = v[0], v1 = v[1], sum = DELTA * ROUNDS;
        for (int i = 0; i < ROUNDS; i++) {
            v1 -= ((v0 << 4 ^ v0 >>> 5) + v0) ^ (sum + key[(sum >>> 11) & 3]);
            sum -= DELTA;
            v0 -= ((v1 << 4 ^ v1 >>> 5) + v1) ^ (sum + key[sum & 3]);
        }
        v[0] = v0;
        v[1] = v1;
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static void main(String[] args) {
        byte[] key = hexStringToByteArray("0123456789ABCDEF0123456789ABCDEF"); 
        int[] block = {0x12345678, 0x9ABCDEF0};
        XTEA xtea = new XTEA(key);
        System.out.println("Bloc avant chiffrement: " + Arrays.toString(block));
        xtea.encrypt(block);
        System.out.println("Bloc chiffré: " + Arrays.toString(block));
        xtea.decrypt(block);
        System.out.println("Bloc déchiffré: " + Arrays.toString(block));
    }
}
