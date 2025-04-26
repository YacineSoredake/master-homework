import java.math.BigInteger;
import java.util.*;

public class HillCiphe {
    private static final BigInteger MOD = BigInteger.valueOf(27);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Chiffrer un message");
        System.out.println("2. Déchiffrer un message");
        System.out.print("Choisissez une option: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Entrez le message: ");
        String message = scanner.nextLine().toUpperCase().replaceAll("[^A-Z]", "");

        BigInteger[][] keyMatrix = generateInvertibleMatrix(2);
        System.out.println("Matrice générée:");
        printMatrix(keyMatrix);

        if (choice == 1) {
            String encrypted = encrypt(message, keyMatrix);
            System.out.println("Message chiffré: " + encrypted);
        } else if (choice == 2) {
            BigInteger[][] inverseKeyMatrix = invertMatrixModulo(keyMatrix, MOD);
            if (inverseKeyMatrix == null) {
                System.out.println("Impossible d'inverser la matrice.");
            } else {
                String decrypted = decrypt(message, inverseKeyMatrix);
                System.out.println("Message déchiffré: " + decrypted);
            }
        }
    }

    public static BigInteger[][] generateInvertibleMatrix(int size) {
        Random rand = new Random();
        BigInteger[][] matrix;
        do {
            matrix = new BigInteger[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    matrix[i][j] = BigInteger.valueOf(rand.nextInt(2^1024));
                }
            }
        } while (det(matrix, MOD).equals(BigInteger.ZERO));
        return matrix;
    }

    public static String encrypt(String message, BigInteger[][] key) {
        return processMessage(message, key);
    }

    public static String decrypt(String message, BigInteger[][] inverseKey) {
        return processMessage(message, inverseKey);
    }

    private static String processMessage(String message, BigInteger[][] matrix) {
        int size = matrix.length;
        StringBuilder result = new StringBuilder();
        BigInteger[] vector = new BigInteger[size];

        for (int i = 0; i < message.length(); i += size) {
            for (int j = 0; j < size; j++) {
                vector[j] = (i + j < message.length()) ? BigInteger.valueOf(message.charAt(i + j) - 'A') : BigInteger.ZERO;
            }
            BigInteger[] transformed = multiplyMatrixVector(matrix, vector);
            for (BigInteger val : transformed) {
                result.append((char) ('A' + val.mod(MOD).intValue()));
            }
        }
        return result.toString();
    }

    private static BigInteger[] multiplyMatrixVector(BigInteger[][] matrix, BigInteger[] vector) {
        int size = matrix.length;
        BigInteger[] result = new BigInteger[size];
        Arrays.fill(result, BigInteger.ZERO);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i] = result[i].add(matrix[i][j].multiply(vector[j]));
            }
            result[i] = result[i].mod(MOD);
        }
        return result;
    }

    private static BigInteger det(BigInteger[][] matrix, BigInteger mod) {
        return matrix[0][0].multiply(matrix[1][1]).subtract(matrix[0][1].multiply(matrix[1][0])).mod(mod);
    }

    private static BigInteger[][] invertMatrixModulo(BigInteger[][] matrix, BigInteger mod) {
        BigInteger determinant = det(matrix, mod);
        if (determinant.equals(BigInteger.ZERO)) return null;
        BigInteger invDet = determinant.modInverse(mod);
        if (invDet == null) return null;

        BigInteger[][] inverse = new BigInteger[2][2];
        inverse[0][0] = matrix[1][1].multiply(invDet).mod(mod);
        inverse[0][1] = matrix[0][1].negate().multiply(invDet).mod(mod);
        inverse[1][0] = matrix[1][0].negate().multiply(invDet).mod(mod);
        inverse[1][1] = matrix[0][0].multiply(invDet).mod(mod);

        return inverse;
    }

    private static void printMatrix(BigInteger[][] matrix) {
        for (BigInteger[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}