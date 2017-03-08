import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PeerInterface extends Remote {
    void startSendingMoney() throws RemoteException;
}