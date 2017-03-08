import java.rmi.*;

public interface ClientInterface extends Remote{
    String getName() throws RemoteException;
    void sendToClient(Message msg) throws RemoteException;
}