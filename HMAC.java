import java.util.Arrays;

public class HMAC {
    private static final int BLOCK_SIZE = 64;
    private static final byte IPAD = 0x36;
    private static final byte OPAD = 0x5c;

    public static byte[] hmacBlake2s(byte[] key, byte[] message) {
        if (key.length > BLOCK_SIZE) {
            key = BLAKE2s.hash(key);
        }

        if (key.length < BLOCK_SIZE) {
            byte[] paddedKey = new byte[BLOCK_SIZE];
            System.arraycopy(key, 0, paddedKey, 0, key.length);
            key = paddedKey;
        }

        byte[] kIpad = new byte[BLOCK_SIZE];
        byte[] kOpad = new byte[BLOCK_SIZE];

        for (int i = 0; i < BLOCK_SIZE; i++) {
            kIpad[i] = (byte) (key[i] ^ IPAD);
            kOpad[i] = (byte) (key[i] ^ OPAD);
        }

        BLAKE2s hasher = new BLAKE2s(32);
        hasher.update(kIpad);
        hasher.update(message);
        byte[] innerHash = hasher.digest();

        hasher.update(kOpad);
        hasher.update(innerHash);

        return hasher.digest();
    }

    public static byte[] hmacBlake2s(String key, String message) {
        return hmacBlake2s(key.getBytes(), message.getBytes());
    }

    public static boolean verify(byte[] key, byte[] message, byte[] mac) {
        byte[] calculatedMac = hmacBlake2s(key, message);
        return Arrays.equals(calculatedMac, mac);
    }
}