import java.util.*;
import java.math.BigInteger;

public class HillCipherBigInt {
    static final BigInteger MOD = BigInteger.valueOf(27);
    static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ ";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = 3;
        BigInteger[][] keyMatrix = generateInvertibleMatrix(n);

        System.out.println("Matrice clé générée :");
        printMatrix(keyMatrix);

        while (true) {
            System.out.println("\n1- Chiffrer un message");
            System.out.println("2- Déchiffrer un message");
            System.out.println("0- Quitter");
            System.out.print("Choisissez une option : ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                System.out.println("Fin du programme.");
                break;
            }

            if (choice == 1) {
                System.out.print("Entrez le message à chiffrer : ");
                String message = scanner.nextLine().toUpperCase();
                String encrypted = encryptMessage(message, keyMatrix);
                System.out.println("Message chiffré : " + encrypted);
            } else if (choice == 2) {
                System.out.print("Entrez le message chiffré : ");
                String encrypted = scanner.nextLine().toUpperCase();
                BigInteger[][] inverseMatrix = invertMatrix(keyMatrix);
                if (inverseMatrix == null) {
                    System.out.println("Erreur : Matrice non inversible.");
                    continue;
                }
                String decrypted = decryptMessage(encrypted, inverseMatrix);
                System.out.println("Message déchiffré : " + decrypted);
            } else {
                System.out.println("Option invalide !");
            }
        }
        scanner.close();
    }

    public static void printMatrix(BigInteger[][] matrix) {
        for (BigInteger[] row : matrix) {
            for (BigInteger num : row) {
                System.out.printf("%3d ", num);
            }
            System.out.println();
        }
    }
    

    public static BigInteger determinantMod(BigInteger[][] matrix, BigInteger mod) {
        int n = matrix.length;
        if (n != 3) {
            throw new IllegalArgumentException("La fonction determinantMod ne supporte que les matrices 3x3.");
        }
        return matrix[0][0].multiply(matrix[1][1].multiply(matrix[2][2])
                .subtract(matrix[1][2].multiply(matrix[2][1])))
                .subtract(matrix[0][1].multiply(matrix[1][0].multiply(matrix[2][2])
                .subtract(matrix[1][2].multiply(matrix[2][0]))))
                .add(matrix[0][2].multiply(matrix[1][0].multiply(matrix[2][1])
                .subtract(matrix[1][1].multiply(matrix[2][0]))))
                .mod(mod);
    }
    

    public static BigInteger[][] cofactorMatrix(BigInteger[][] matrix) {
        int n = matrix.length;
        BigInteger[][] cofactors = new BigInteger[n][n];
    
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                BigInteger[][] minor = getMinor(matrix, i, j);
                BigInteger determinant = determinantMod(minor, MOD);
                cofactors[i][j] = determinant.multiply(BigInteger.valueOf((i + j) % 2 == 0 ? 1 : -1)).mod(MOD);
            }
        }
        return cofactors;
    }

    public static BigInteger[][] getMinor(BigInteger[][] matrix, int row, int col) {
        int n = matrix.length;
        BigInteger[][] minor = new BigInteger[n - 1][n - 1];
        int r = 0, c;
    
        for (int i = 0; i < n; i++) {
            if (i == row) continue;
            c = 0;
            for (int j = 0; j < n; j++) {
                if (j == col) continue;
                minor[r][c] = matrix[i][j];
                c++;
            }
            r++;
        }
        return minor;
    }

    public static BigInteger[][] invertMatrix(BigInteger[][] matrix) {
        int n = matrix.length;
        if (n != 3) {
            System.out.println("Erreur : La matrice fournie n'est pas 3x3.");
            return null;
        }
    
        BigInteger det = determinantMod(matrix, MOD);
        if (det.equals(BigInteger.ZERO) || !det.gcd(MOD).equals(BigInteger.ONE)) {
            System.out.println("Erreur : Matrice non inversible.");
            return null;
        }
    
        BigInteger detInverse = det.modInverse(MOD);
        BigInteger[][] cofactors = cofactorMatrix(matrix);
        BigInteger[][] adjugate = transposeMatrix(cofactors);
    
        BigInteger[][] inverse = new BigInteger[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverse[i][j] = adjugate[i][j].multiply(detInverse).mod(MOD);
            }
        }
        return inverse;
    }
    
    public static BigInteger[][] generateInvertibleMatrix(int n) {
        if (n != 3) {
            throw new IllegalArgumentException("Seules les matrices 3x3 sont supportées.");
        }
        Random random = new Random();
        BigInteger[][] matrix;
        BigInteger det;
    
        do {
            matrix = new BigInteger[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrix[i][j] = BigInteger.valueOf(random.nextInt(27));
                }
            }
            det = determinantMod(matrix, MOD);
        } while (det.equals(BigInteger.ZERO) || !det.gcd(MOD).equals(BigInteger.ONE));
    
        return matrix;
    }
    

    public static BigInteger[][] transposeMatrix(BigInteger[][] matrix) {
        int n = matrix.length;
        BigInteger[][] transposed = new BigInteger[n][n];
    
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }
        return transposed;
    }
    

    public static String encryptMessage(String message, BigInteger[][] keyMatrix) {
        int n = keyMatrix.length;
        message = normalizeMessage(message, n);
        BigInteger[] messageVector = toNumericVector(message);
        BigInteger[] encryptedVector = multiplyMatrixVector(keyMatrix, messageVector);
        return toAlphabetString(encryptedVector);
    }

    public static String decryptMessage(String encrypted, BigInteger[][] inverseMatrix) {
        BigInteger[] encryptedVector = toNumericVector(encrypted);
        BigInteger[] decryptedVector = multiplyMatrixVector(inverseMatrix, encryptedVector);
        return toAlphabetString(decryptedVector);
    }

    public static String normalizeMessage(String message, int n) {
        StringBuilder normalized = new StringBuilder(message);
        while (normalized.length() % n != 0) {
            normalized.append(" ");
        }
        return normalized.toString();
    }

    public static BigInteger[] toNumericVector(String text) {
        List<BigInteger> numericList = new ArrayList<>();
        for (char c : text.toCharArray()) {
            numericList.add(BigInteger.valueOf(ALPHABET.indexOf(c)));
        }
        return numericList.toArray(new BigInteger[0]);
    }
    
    

    public static String toAlphabetString(BigInteger[] numbers) {
        StringBuilder result = new StringBuilder();
        for (BigInteger num : numbers) {
            result.append(ALPHABET.charAt(num.mod(MOD).intValue()));
        }
        return result.toString().trim();
    }

    public static BigInteger[] multiplyMatrixVector(BigInteger[][] matrix, BigInteger[] vector) {
        int n = matrix.length;
        BigInteger[] result = new BigInteger[vector.length];
        Arrays.fill(result, BigInteger.ZERO);

        for (int i = 0; i < vector.length; i += n) {
            for (int j = 0; j < n; j++) {
                BigInteger sum = BigInteger.ZERO;
                for (int k = 0; k < n; k++) {
                    sum = sum.add(matrix[j][k].multiply(vector[i + k]));
                }
                result[i + j] = sum.mod(MOD).add(MOD).mod(MOD);
            }
        }
        return result;
    }
}