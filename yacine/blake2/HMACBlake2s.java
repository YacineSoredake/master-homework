package yacine.blake2;

import java.util.Arrays;

public class HMACBlake2s {
    private static final int BLOCK_SIZE = 64; // 512 bits

    public static byte[] hmac(byte[] key, byte[] message) {
        if (key.length > BLOCK_SIZE) {
            key = blake2s.hash(key);
        }
        if (key.length < BLOCK_SIZE) {
            key = Arrays.copyOf(key, BLOCK_SIZE);
        }

        byte[] o_key_pad = new byte[BLOCK_SIZE];
        byte[] i_key_pad = new byte[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            o_key_pad[i] = (byte) (key[i] ^ 0x5c);
            i_key_pad[i] = (byte) (key[i] ^ 0x36);
        }

        byte[] innerHash = blake2s.hash(concat(i_key_pad, message));
        return blake2s.hash(concat(o_key_pad, innerHash));
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
