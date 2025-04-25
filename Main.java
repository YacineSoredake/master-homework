import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public class XTEAa {
    private static final int DELTA = 0x9E3779B9;
    private static final int ROUNDS = 32;
    private int[] key;

    public enum Mode {
        ECB, CBC, OFB, CTR
    }

    public XTEAa(byte[] keyBytes) {
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

    private static int[] bytesToBlock(byte[] bytes, int offset) {
        return new int[] {
            ((bytes[offset] & 0xFF) << 24) | ((bytes[offset+1] & 0xFF) << 16) |
            ((bytes[offset+2] & 0xFF) << 8) | (bytes[offset+3] & 0xFF),
            ((bytes[offset+4] & 0xFF) << 24) | ((bytes[offset+5] & 0xFF) << 16) |
            ((bytes[offset+6] & 0xFF) << 8) | (bytes[offset+7] & 0xFF)
        };
    }

    private static void blockToBytes(int[] block, byte[] bytes, int offset) {
        for (int i = 0; i < 2; i++) {
            bytes[offset + i*4    ] = (byte)(block[i] >>> 24);
            bytes[offset + i*4 + 1] = (byte)(block[i] >>> 16);
            bytes[offset + i*4 + 2] = (byte)(block[i] >>> 8);
            bytes[offset + i*4 + 3] = (byte)(block[i]);
        }
    }

    private static byte[] xorBlocks(byte[] a, byte[] b) {
        byte[] result = new byte[8];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public void encryptFile(String inputPath, String outputPath, Mode mode, byte[] iv) throws IOException {
        byte[] data = Files.readAllBytes(Path.of(inputPath));
        int paddedLength = (data.length + 7) / 8 * 8;
        byte[] padded = Arrays.copyOf(data, paddedLength);
        byte[] result = new byte[padded.length];
        byte[] prevCipher = iv;
        byte[] counter = iv.clone();

        for (int i = 0; i < padded.length; i += 8) {
            byte[] block = Arrays.copyOfRange(padded, i, i + 8);
            byte[] toEncrypt;

            switch (mode) {
                case ECB -> {
                    int[] intBlock = bytesToBlock(block, 0);
                    encrypt(intBlock);
                    blockToBytes(intBlock, result, i);
                }
                case CBC -> {
                    byte[] xored = xorBlocks(block, prevCipher);
                    int[] intBlock = bytesToBlock(xored, 0);
                    encrypt(intBlock);
                    blockToBytes(intBlock, result, i);
                    prevCipher = Arrays.copyOfRange(result, i, i + 8);
                }
                case OFB -> {
                    int[] intIV = bytesToBlock(prevCipher, 0);
                    encrypt(intIV);
                    blockToBytes(intIV, prevCipher, 0);
                    byte[] xored = xorBlocks(block, prevCipher);
                    System.arraycopy(xored, 0, result, i, 8);
                }
                case CTR -> {
                    int[] intCounter = bytesToBlock(counter, 0);
                    encrypt(intCounter);
                    byte[] keystream = new byte[8];
                    blockToBytes(intCounter, keystream, 0);
                    byte[] xored = xorBlocks(block, keystream);
                    System.arraycopy(xored, 0, result, i, 8);
                    incrementCounter(counter);
                }
            }
        }

        Files.write(Path.of(outputPath), result);
    }

    public void decryptFile(String inputPath, String outputPath, Mode mode, byte[] iv) throws IOException {
        byte[] encrypted = Files.readAllBytes(Path.of(inputPath));
        byte[] result = new byte[encrypted.length];
        byte[] prevCipher = iv;
        byte[] counter = iv.clone();

        for (int i = 0; i < encrypted.length; i += 8) {
            byte[] block = Arrays.copyOfRange(encrypted, i, i + 8);
            byte[] plainBlock;

            switch (mode) {
                case ECB -> {
                    int[] intBlock = bytesToBlock(block, 0);
                    decrypt(intBlock);
                    blockToBytes(intBlock, result, i);
                }
                case CBC -> {
                    int[] intBlock = bytesToBlock(block, 0);
                    decrypt(intBlock);
                    byte[] temp = new byte[8];
                    blockToBytes(intBlock, temp, 0);
                    plainBlock = xorBlocks(temp, prevCipher);
                    System.arraycopy(plainBlock, 0, result, i, 8);
                    prevCipher = block;
                }
                case OFB -> {
                    int[] intIV = bytesToBlock(prevCipher, 0);
                    encrypt(intIV);
                    blockToBytes(intIV, prevCipher, 0);
                    plainBlock = xorBlocks(block, prevCipher);
                    System.arraycopy(plainBlock, 0, result, i, 8);
                }
                case CTR -> {
                    int[] intCounter = bytesToBlock(counter, 0);
                    encrypt(intCounter);
                    byte[] keystream = new byte[8];
                    blockToBytes(intCounter, keystream, 0);
                    plainBlock = xorBlocks(block, keystream);
                    System.arraycopy(plainBlock, 0, result, i, 8);
                    incrementCounter(counter);
                }
            }
        }

        Files.write(Path.of(outputPath), result);
    }

    private static void incrementCounter(byte[] counter) {
        for (int i = counter.length - 1; i >= 0; i--) {
            counter[i]++;
            if (counter[i] != 0) break;
        }
    }

    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }}
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Tester la fonction de hachage BLAKE2s");
        System.out.println("2. Tester la fonction HMAC");
        System.out.println("3. chiffrement de fichiers avec authentification");
        System.out.print("Choisissez une option (1-2): ");

        int choice = scanner.nextInt();
        scanner.nextLine();
        try {
            switch (choice) {
                case 1:
                    testBlake2s(scanner);
                    break;
                case 2:
                    testHmac(scanner);
                    break;
                case 3:
                    chifFichier(scanner);
                    break;
                default:
                    System.out.println("Option invalide.");
            }
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testBlake2s(Scanner scanner) {
        System.out.print("Entrez le texte à hacher: ");
        String input = scanner.nextLine();

        byte[] hash = BLAKE2s.hash(input);

        System.out.println("Haché BLAKE2s : " + BLAKE2s.bytesToHex(hash));
    }

    private static void testHmac(Scanner scanner) {
        System.out.print("Entrez la clé: ");
        String key = scanner.nextLine();

        System.out.print("Entrez le message: ");
        String message = scanner.nextLine();

        byte[] hmac = HMAC.hmacBlake2s(key, message);

        System.out.println("HMAC-BLAKE2s: " + BLAKE2s.bytesToHex(hmac));
    }

    private static void chifFichier(Scanner scanner) {
        
    }
}
