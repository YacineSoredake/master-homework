package tp3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JeuInterface extends Remote {
    String deviner(String joueur, int nombre) throws RemoteException;
    void enregistrerJoueur(String joueur) throws RemoteException;
}
