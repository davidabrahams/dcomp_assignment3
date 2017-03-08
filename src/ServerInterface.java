import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {

    boolean register (ClientInterface client)throws RemoteException;
    List<String> getUsers ()throws RemoteException;
    boolean sendToServer(Message m)throws RemoteException;

}
