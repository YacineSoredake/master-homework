import java.util.Scanner;

public class mRC4 {
    private int[] S = new int[256];
    private int i = 0, j = 0;

    public mRC4(byte[] clé) {
        keySchedule(clé);
    }

    private void keySchedule(byte[] clé) {
        int longueur_clé = clé.length;
        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }
        
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + (clé[i % longueur_clé] & 0xFF)) % 256;
            échangee(S, i, j);
        }
    }

    private void échangee(int[] S, int i, int j) {
        int temp = S[i];
        S[i] = S[j];
        S[j] = temp;
    }

    public byte[] generateStream(int taille) {
        byte[] flot = new byte[taille];
        for (int k = 0; k < taille; k++) {
            i = (i + 1) % 256;
            j = (j + S[i]) % 256;
            échangee(S, i, j);
            flot[k] = (byte) S[(S[i] + S[j]) % 256];
        }
        return flot;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez la taille de la clé  : ");
        int keySize = scanner.nextInt();
        if (keySize != 16 && keySize != 32) {
            System.out.println("Taille invalide !");
            return;
        }
        byte[] key = new byte[keySize];
        System.out.println("Entrez la clé octet par octet :");
        for (int i = 0; i < keySize; i++) {
            key[i] = (byte) scanner.nextInt();
        }
        
        System.out.print("taille du flot à générer : ");
        int taille = scanner.nextInt();
        
        RC4 rc4 = new RC4(key);
        byte[] stream = rc4.generateStream(taille);
        
        System.out.println("Flot généré :");
        for (byte b : stream) {
            System.out.printf("%02X ", b);
        }
        scanner.close();
    }
}