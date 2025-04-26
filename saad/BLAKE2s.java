package saad;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BLAKE2s {
    private static final int BLOCK_SIZE = 64;
    private static final int HASH_SIZE = 32;

    private static final int[] IV = {
            0x6A09E667, 0xBB67AE85, 0x3C6EF372, 0xA54FF53A,
            0x510E527F, 0x9B05688C, 0x1F83D9AB, 0x5BE0CD19
    };

    private static final byte[] SIGMA = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
            14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3,
            11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4,
            7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8,
            9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13,
            2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9,
            12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11,
            13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10,
            6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5,
            10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0
    };

    private int[] h = new int[8];
    private int[] t = new int[2];
    private int[] f = new int[2];
    private byte[] buffer = new byte[BLOCK_SIZE];
    private int bufferPos = 0;

    public BLAKE2s(int outlen) {
        System.arraycopy(IV, 0, h, 0, 8);
        h[0] ^= 0x01010000 ^ outlen;
    }

    public void reset() {
        System.arraycopy(IV, 0, h, 0, 8);
        h[0] ^= 0x01010000 ^ HASH_SIZE;

        t[0] = 0;
        t[1] = 0;
        f[0] = 0;
        f[1] = 0;

        Arrays.fill(buffer, (byte) 0);
        bufferPos = 0;
    }

    private void G(int[] v, int a, int b, int c, int d, int x, int y) {
        v[a] = v[a] + v[b] + x;
        v[d] = Integer.rotateRight(v[d] ^ v[a], 16);
        v[c] = v[c] + v[d];
        v[b] = Integer.rotateRight(v[b] ^ v[c], 12);
        v[a] = v[a] + v[b] + y;
        v[d] = Integer.rotateRight(v[d] ^ v[a], 8);
        v[c] = v[c] + v[d];
        v[b] = Integer.rotateRight(v[b] ^ v[c], 7);
    }

    private void compress(byte[] block) {
        int[] v = new int[16];
        int[] m = new int[16];

        ByteBuffer.wrap(block).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(m);

        System.arraycopy(h, 0, v, 0, 8);
        System.arraycopy(IV, 0, v, 8, 8);

        v[12] ^= t[0];
        v[13] ^= t[1];

        if (f[0] != 0) {
            v[14] = ~v[14];
        }

        for (int i = 0; i < 10; i++) {
            int s0 = SIGMA[(i * 16)];
            int s1 = SIGMA[(i * 16) + 1];
            int s2 = SIGMA[(i * 16) + 2];
            int s3 = SIGMA[(i * 16) + 3];
            int s4 = SIGMA[(i * 16) + 4];
            int s5 = SIGMA[(i * 16) + 5];
            int s6 = SIGMA[(i * 16) + 6];
            int s7 = SIGMA[(i * 16) + 7];
            int s8 = SIGMA[(i * 16) + 8];
            int s9 = SIGMA[(i * 16) + 9];
            int s10 = SIGMA[(i * 16) + 10];
            int s11 = SIGMA[(i * 16) + 11];
            int s12 = SIGMA[(i * 16) + 12];
            int s13 = SIGMA[(i * 16) + 13];
            int s14 = SIGMA[(i * 16) + 14];
            int s15 = SIGMA[(i * 16) + 15];

            G(v, 0, 4, 8, 12, m[s0], m[s1]);
            G(v, 1, 5, 9, 13, m[s2], m[s3]);
            G(v, 2, 6, 10, 14, m[s4], m[s5]);
            G(v, 3, 7, 11, 15, m[s6], m[s7]);

            G(v, 0, 5, 10, 15, m[s8], m[s9]);
            G(v, 1, 6, 11, 12, m[s10], m[s11]);
            G(v, 2, 7, 8, 13, m[s12], m[s13]);
            G(v, 3, 4, 9, 14, m[s14], m[s15]);
        }

        for (int i = 0; i < 8; i++) {
            h[i] ^= v[i] ^ v[i + 8];
        }
    }

    public void update(byte[] input, int offset, int length) {
        int i = offset;

        if (bufferPos > 0) {
            int remainingSpace = BLOCK_SIZE - bufferPos;
            int toFill = Math.min(remainingSpace, length);

            System.arraycopy(input, i, buffer, bufferPos, toFill);
            bufferPos += toFill;
            i += toFill;
            length -= toFill;

            if (bufferPos == BLOCK_SIZE) {
                t[0] += BLOCK_SIZE;
                if (t[0] < BLOCK_SIZE) {
                    t[1]++;
                }

                compress(buffer);
                bufferPos = 0;
            }
        }

        while (length >= BLOCK_SIZE) {
            t[0] += BLOCK_SIZE;
            if (t[0] < BLOCK_SIZE) {
                t[1]++;
            }

            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(input, i, block, 0, BLOCK_SIZE);
            compress(block);

            i += BLOCK_SIZE;
            length -= BLOCK_SIZE;
        }

        if (length > 0) {
            System.arraycopy(input, i, buffer, bufferPos, length);
            bufferPos += length;
        }
    }

    public void update(byte[] input) {
        update(input, 0, input.length);
    }

    public void update(String input) {
        update(input.getBytes());
    }
    public byte[] digest() {
        f[0] = -1;
        Arrays.fill(buffer, bufferPos, BLOCK_SIZE, (byte) 0);
        t[0] += bufferPos;
        if (t[0] < bufferPos) {
            t[1]++;
        }
        compress(buffer);
        byte[] result = new byte[HASH_SIZE];
        ByteBuffer bb = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 8; i++) {
            bb.putInt(h[i]);
        }

        reset();

        return result;
    }
    public static byte[] hash(byte[] input) {
        BLAKE2s hasher = new BLAKE2s(HASH_SIZE);
        hasher.update(input);
        return hasher.digest();
    }
    public static byte[] hash(String input) {
        return hash(input.getBytes());
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}