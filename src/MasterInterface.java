import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {

    void register(String IP, String name) throws RemoteException, NotBoundException, InterruptedException;

}
