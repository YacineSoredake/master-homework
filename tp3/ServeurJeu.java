package tp3;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ServeurJeu extends UnicastRemoteObject implements JeuInterface {
    private int secret;
    private boolean match = false;

    private Set<String> joueursInscrits = new HashSet<>();
    private Map<String, Long> joueurs = new HashMap<>();
    private Set<String> joueursTimeout = new HashSet<>();

    final long TIMEOUT = 2 * 60 * 1000; 


    public ServeurJeu() throws Exception {
        super();
        this.secret = new Random().nextInt(100) + 1;
        System.out.println("Nombre secret : " + secret);
    }

    private void relancerPartie() {
        this.secret = new Random().nextInt(100) + 1;
        this.match = false;
        this.joueursTimeout.clear();
        System.out.println("Nouvelle partie lancée ! Nouveau nombre secret : " + secret);
    }
    
    @Override
    public synchronized void entrejoueur(String joueur) throws java.rmi.RemoteException {
        joueursInscrits.add(joueur);
        joueurs.put(joueur, System.currentTimeMillis());
        System.out.println("Joueur est entre : " + joueur);
    }

    @Override
    public synchronized String guess(String joueur, int nombre) throws java.rmi.RemoteException {
        long now = System.currentTimeMillis();

        if (joueursTimeout.contains(joueur)) {
            System.out.println("(timeout) : " + joueur);
            return "exclu";
        }

        if (!joueurs.containsKey(joueur)) {
            joueurs.put(joueur, now);
        } else {
            long last = joueurs.get(joueur);
            if (now - last > TIMEOUT) {
                joueursTimeout.add(joueur);
                System.out.println("Joueur " + joueur + " exclu");
                return "Vous avez été exclu.";
            }
            joueurs.put(joueur, now); 
        }

        if (match) {
            return "Partie terminée. Le nombre était : " + secret;
        }

        System.out.println(joueur + " a essayé : " + nombre);

        if (nombre == secret) {
            match = true;
            String messageGagnant = joueur + " a gagné ! Il a deviné le nombre : " + secret;
            System.out.println(messageGagnant);
            relancerPartie();
            return "Gagné ! Une nouvelle partie commence.";
        } else if (nombre < secret) {
            return "Trop petit";
        } else {
            return "Trop grand";
        }
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(2000);
            ServeurJeu serveur = new ServeurJeu();
            registry.rebind("JeuRMI", serveur);
            System.out.println("Serveur RMI lancé sur le port 2000");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
