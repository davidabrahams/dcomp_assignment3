import java.rmi.*;

public interface PeerInterface extends Remote{
    String getName() throws RemoteException;
    void sendToClient(Message msg) throws RemoteException;
}