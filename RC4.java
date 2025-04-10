import java.io.*;
import java.util.Scanner;

public class RC4 {
    private int[] S = new int[256];
    private int i, j;

    public RC4(byte[] key) {
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

    public byte[] generateStream(int size) {
        byte[] keystream = new byte[size];
        for (int k = 0; k < size; k++) {
            keystream[k] = generateKeystreamByte();
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
        RC4 rc4 = new RC4(key);
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

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the key :");
        String hexKey = scanner.nextLine();
        byte[] key = hexStringToByteArray(hexKey);
        
        String inputFile = "./input.txt";
        String encryptedFile = "./output.txt";
        String decryptedFile = "./decrypt.txt";
        
        encryptFile(inputFile, encryptedFile, key);
        System.out.println("file encrypted succesfully !");
        
        decryptFile(encryptedFile, decryptedFile, key);
        System.out.println("file decrypted succesfully !");
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
    
}