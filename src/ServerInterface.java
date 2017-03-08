import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {

    String register (PeerInterface client) throws RemoteException;

}
