package tp3;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.rmi.Naming;


public class ServeurJeu extends UnicastRemoteObject implements JeuInterface {
    private int secret;
    private boolean trouve = false;
    private Set<String> joueurs = new HashSet<>();
    public ServeurJeu() throws Exception {
        super();
        this.secret = new Random().nextInt(100) + 1;
        System.out.println("Nombre secret choisi : " + secret);
    }

    @Override
    public synchronized void enregistrerJoueur(String joueur) throws java.rmi.RemoteException {
        joueurs.add(joueur);
        System.out.println("Joueur inscrit : " + joueur);
    }

    @Override
    public synchronized String deviner(String joueur, int nombre) throws java.rmi.RemoteException {
        if (trouve) {
            return "Partie déjà terminée. Le gagnant a été annoncé.";
        }

        System.out.println("[" + joueur + "] a essayé : " + nombre);

        if (nombre == secret) {
            trouve = true;
            String messageGagnant = joueur + " a trouvé le bon nombre : " + secret + " !";
            System.out.println(messageGagnant);
            return "Gagné ! Tu as trouvé le bon nombre ";
        } else if (nombre < secret) {
            return "Trop petit ! Essaie encore.";
        } else {
            return "Trop grand ! Essaie encore.";
        }
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(2000);
            ServeurJeu serveur = new ServeurJeu();
            Naming.rebind("JeuRMI", serveur);
            System.out.println("Serveur RMI en ligne...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
