package yacine.filecrypto;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("1- Encrypt file");
        System.out.println("2- Decrypt file");
        System.out.print("Choose option: ");
        int option = sc.nextInt();
        sc.nextLine();

        try {
            if (option == 1) {
                System.out.print("Input file path: ");
                String input = sc.nextLine();
                System.out.print("Output file path: ");
                String output = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();
                System.out.print("Mode (CBC/CTR): ");
                String mode = sc.nextLine().toUpperCase();
                boolean useCBC = mode.equals("CBC");
                Encryptor.encryptFile(input, output, password, useCBC);
                System.out.println("File encrypted successfully.");
            } else if (option == 2) {
                System.out.print("Input file path: ");
                String input = sc.nextLine();
                System.out.print("Output file path: ");
                String output = sc.nextLine();
                System.out.print("Password: ");
                String password = sc.nextLine();
                Decryptor.decryptFile(input, output, password);
                System.out.println("File decrypted successfully.");
            } else {
                System.out.println("Invalid option.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Operation failed: " + e.getMessage());
        }

        sc.close();
    }
}
