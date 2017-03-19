import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerInterface extends Remote {
    void startSendingMoney(List<NameIP> peers) throws RemoteException;
    void receiveMoney(int m) throws RemoteException;
    void setNextPeer(NameIP nip) throws RemoteException;
    void receiveMessage(NameIP nip, boolean alreadyElected) throws RemoteException, NotBoundException;
}