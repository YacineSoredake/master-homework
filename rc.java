import java.io.*;
import java.util.Scanner;

public class rc {
    private int[] S = new int[256];
    private int i, j;

    public rc(byte[] key) {
        keySchedulingAlgorithm(key);
    }

    private void keySchedulingAlgorithm(byte[] key) {
        int keyLength = key.length;
        for (i = 0; i < 256; i++) {
            S[i] = i;
        }
        j = 0;
        for (i = 0; i < 256; i++) {
            j = (j + S[i] + key[i % keyLength]) & 0xFF;
            swap(S, i, j);
        }
        i = j = 0;
    }

    private byte generateKeystreamByte() {
        i = (i + 1) & 0xFF;
        j = (j + S[i]) & 0xFF;
        swap(S, i, j);
        return (byte) S[(S[i] + S[j]) & 0xFF];
    }

    public static byte[] generateStream(byte[] key, int size) {
        rc rc4 = new rc(key);
        byte[] keystream = new byte[size];
        for (int k = 0; k < size; k++) {
            keystream[k] = rc4.generateKeystreamByte();
        }
        return keystream;
    }

    public byte[] encryptDecrypt(byte[] data) {
        byte[] output = new byte[data.length];
        for (int k = 0; k < data.length; k++) {
            output[k] = (byte) (data[k] ^ generateKeystreamByte());
        }
        return output;
    }

    private void swap(int[] S, int a, int b) {
        int temp = S[a];
        S[a] = S[b];
        S[b] = temp;
    }

    public static void encryptFile(String inputFile, String outputFile, byte[] key) throws IOException {
        rc rc4 = new rc(key);
        FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(rc4.encryptDecrypt(buffer), 0, bytesRead);
        }
        fis.close();
        fos.close();
    }

    public static void decryptFile(String inputFile, String outputFile, byte[] key) throws IOException {
        encryptFile(inputFile, outputFile, key);
    }

    public static byte[] hexStringToByteArray(String hex) {
        if (!hex.matches("[0-9A-Fa-f]+")) {  
            throw new IllegalArgumentException("La clé doit être une chaîne hexadécimale (0-9, A-F).");
        }
        if (hex.length() % 2 != 0) { 
            throw new IllegalArgumentException("La clé hexadécimale doit avoir une longueur paire.");
        }
    
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Entrez la clé hexa :");
        String hexKey = scanner.nextLine();
        byte[] key = hexStringToByteArray(hexKey);

        System.out.println("Entrez la taille du flot désiré :");
        int streamSize = scanner.nextInt();

        byte[] keystream = generateStream(key, streamSize);
        System.out.println("Generated stream :");
        for (byte b : keystream) {
            System.out.printf("%02X ", b);
        }
        System.out.println("\n");

        String inputFile = "./input.txt";
        String encryptedFile = "./output.txt";
        String decryptedFile = "./decrypt.txt";

        encryptFile(inputFile, encryptedFile, key);
        System.out.println("Fichier chiffré avec succès !");

        decryptFile(encryptedFile, decryptedFile, key);
        System.out.println("Fichier déchiffré avec succès !");
        
        scanner.close();
    }
}
