package tp3;
import java.rmi.Naming;
import java.util.Scanner;

public class ClientJeu {

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Entrez votre nom de joueur : ");
            String joueur = sc.nextLine();

            JeuInterface jeu = (JeuInterface) Naming.lookup("rmi://localhost/JeuRMI");

            jeu.enregistrerJoueur(joueur);
            String réponse;

            do {
                System.out.print("Entrez un nombre (entre 1 et 100) : ");
                int essai = sc.nextInt();
                réponse = jeu.deviner(joueur, essai);
                System.out.println("=> " + réponse);
            } while (!réponse.startsWith("Gagné") && !réponse.contains("terminée"));

            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
