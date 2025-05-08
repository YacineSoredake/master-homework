package tp3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface JeuInterface extends Remote {
    String guess(String joueur, int nombre) throws RemoteException;
    void entrejoueur(String joueur) throws RemoteException;
}
