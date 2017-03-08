import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerInterface extends Remote {
    void startSendingMoney(List<String> peers) throws RemoteException;
    void receiveMoney(int m) throws RemoteException;

}