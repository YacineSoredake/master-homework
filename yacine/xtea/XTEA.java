package yacine.xtea;

import java.util.Arrays;
import java.util.Random;

public class XTEA {
    private static final int NUM_ROUNDS = 32;
    private static final int DELTA = 0x9E3779B9;

    public static byte[] encryptCBC(byte[] data, byte[] key, byte[] iv) {
        data = pad(data);
        byte[] result = new byte[data.length];
        byte[] prevBlock = Arrays.copyOf(iv, iv.length);

        for (int i = 0; i < data.length; i += 8) {
            byte[] block = xor(Arrays.copyOfRange(data, i, i + 8), prevBlock);
            byte[] encrypted = encryptBlock(block, key);
            System.arraycopy(encrypted, 0, result, i, 8);
            prevBlock = encrypted;
        }
        return result;
    }

    public static byte[] decryptCBC(byte[] data, byte[] key, byte[] iv) {
        byte[] result = new byte[data.length];
        byte[] prevBlock = Arrays.copyOf(iv, iv.length);

        for (int i = 0; i < data.length; i += 8) {
            byte[] block = Arrays.copyOfRange(data, i, i + 8);
            byte[] decrypted = decryptBlock(block, key);
            decrypted = xor(decrypted, prevBlock);
            System.arraycopy(decrypted, 0, result, i, 8);
            prevBlock = block;
        }
        return unpad(result);
    }

    public static byte[] encryptCTR(byte[] data, byte[] key, byte[] iv) {
        byte[] result = new byte[data.length];
        byte[] counter = Arrays.copyOf(iv, iv.length);

        for (int i = 0; i < data.length; i += 8) {
            byte[] keystream = encryptBlock(counter, key);
            int blockSize = Math.min(8, data.length - i);
            for (int j = 0; j < blockSize; j++) {
                result[i + j] = (byte) (data[i + j] ^ keystream[j]);
            }
            incrementCounter(counter);
        }
        return result;
    }

    public static byte[] decryptCTR(byte[] data, byte[] key, byte[] iv) {
        return encryptCTR(data, key, iv);
    }

    private static void incrementCounter(byte[] counter) {
        for (int i = counter.length - 1; i >= 0; i--) {
            counter[i]++;
            if (counter[i] != 0) break;
        }
    }

    private static byte[] encryptBlock(byte[] block, byte[] key) {
        if (block.length != 8) {
            throw new IllegalArgumentException("Block size must be exactly 8 bytes.");
        }
        if (key.length != 16) {
            throw new IllegalArgumentException("Key size must be exactly 16 bytes (128 bits).");
        }
    
        int[] v = bytesToInts(block);
        int[] k = bytesToInts(Arrays.copyOf(key, 16));
        int sum = 0;
    
        for (int i = 0; i < NUM_ROUNDS; i++) {
            v[0] += ((v[1] << 4 ^ v[1] >>> 5) + v[1]) ^ (sum + k[sum & 3]);
            sum += DELTA;
            v[1] += ((v[0] << 4 ^ v[0] >>> 5) + v[0]) ^ (sum + k[(sum >>> 11) & 3]);
        }
        return intsToBytes(v);
    }
    

    private static byte[] decryptBlock(byte[] block, byte[] key) {
        int[] v = bytesToInts(block);
        int[] k = bytesToInts(Arrays.copyOf(key, 16));
        int sum = DELTA * NUM_ROUNDS;

        for (int i = 0; i < NUM_ROUNDS; i++) {
            v[1] -= ((v[0] << 4 ^ v[0] >>> 5) + v[0]) ^ (sum + k[(sum >>> 11) & 3]);
            sum -= DELTA;
            v[0] -= ((v[1] << 4 ^ v[1] >>> 5) + v[1]) ^ (sum + k[sum & 3]);
        }
        return intsToBytes(v);
    }

    private static byte[] pad(byte[] data) {
        int padding = 8 - (data.length % 8);
        byte[] result = Arrays.copyOf(data, data.length + padding);
        Arrays.fill(result, data.length, result.length, (byte) padding);
        return result;
    }

    private static byte[] unpad(byte[] data) {
        int padding = data[data.length - 1];
        return Arrays.copyOf(data, data.length - padding);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    private static int[] bytesToInts(byte[] bytes) {
        int[] result = new int[2];
        for (int i = 0; i < 2; i++) {
            result[i] = ((bytes[i * 4] & 0xff) << 24) | ((bytes[i * 4 + 1] & 0xff) << 16) |
                        ((bytes[i * 4 + 2] & 0xff) << 8) | (bytes[i * 4 + 3] & 0xff);
        }
        return result;
    }

    private static byte[] intsToBytes(int[] ints) {
        byte[] result = new byte[8];
        for (int i = 0; i < 2; i++) {
            result[i * 4] = (byte) (ints[i] >>> 24);
            result[i * 4 + 1] = (byte) (ints[i] >>> 16);
            result[i * 4 + 2] = (byte) (ints[i] >>> 8);
            result[i * 4 + 3] = (byte) ints[i];
        }
        return result;
    }
}
