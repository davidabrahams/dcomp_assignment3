import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Util {

    public static PeerInterface getPeer(NameIP nip) throws RemoteException, NotBoundException {
        String IP = nip.ip;
        Registry reg = LocateRegistry.getRegistry(IP, 1099);
        return (PeerInterface) reg.lookup(nip.name);
    }
}
