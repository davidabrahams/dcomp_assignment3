import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PeerInterface extends Remote {
    void setNextPeer(NameIP nip) throws RemoteException;
    SnapshotState endSnapShot() throws RemoteException;
    void startSnapShot() throws RemoteException;
    void addPeer(NameIP peer) throws RemoteException;
    void clearMessagesAndMakeTransaction() throws RemoteException, NotBoundException;
    boolean isLeader() throws RemoteException;
    void sendMessage(Message m) throws RemoteException;
}