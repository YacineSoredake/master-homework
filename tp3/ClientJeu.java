package tp3;
import java.rmi.Naming;
import java.util.Scanner;

public class ClientJeu {

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Entrez votre nom  : ");
            String joueur = sc.nextLine();
            JeuInterface jeu = (JeuInterface) Naming.lookup("rmi://localhost:2000/JeuRMI");

            jeu.entrejoueur(joueur);
            String réponse;

            do {
                System.out.print("Entrez un nombre (entre 1 - 100) : ");
                int essai = sc.nextInt();
                réponse = jeu.guess(joueur, essai);
                System.out.println("=> " + réponse);
            } while (!réponse.contains("terminée"));

            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
