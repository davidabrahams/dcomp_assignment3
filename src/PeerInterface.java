import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerInterface extends Remote {
    void receiveMoney(NameIP sender, int m) throws RemoteException;
    void setNextPeer(NameIP nip) throws RemoteException;
    void electionMessage(NameIP nip, boolean alreadyElected) throws RemoteException, NotBoundException;
    void receiveMarker(NameIP sender) throws RemoteException, NotBoundException;
    SnapshotState endSnapShot() throws RemoteException;
    void startSnapShot() throws RemoteException;
    void addPeer(NameIP peer) throws RemoteException;
    void start() throws InterruptedException, RemoteException, NotBoundException;
}