import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Master extends UnicastRemoteObject implements MasterInterface {

    private final int NUM_PEERS = 2;
    private List<NameIP> allProcesses;

    public Master() throws RemoteException {
        allProcesses = new ArrayList<>();
    }

    private PeerInterface getPeer(NameIP nip) throws RemoteException, NotBoundException {
        String IP = nip.ip;
        Registry reg = LocateRegistry.getRegistry(IP, 1099);
        return (PeerInterface) reg.lookup(nip.name);
    }


    @Override
    public void register(String IP, String name) throws RemoteException, NotBoundException, InterruptedException {
        System.out.println("Request to register received for process: " + name);
        allProcesses.add(new NameIP(name, IP));
        if (allProcesses.size() == NUM_PEERS) {
            for (NameIP nip : allProcesses) {
                List<NameIP> peersToSend = allProcesses.stream().filter(temp -> temp != nip).collect(Collectors.toList());
                PeerInterface p = getPeer(nip);
                p.startSendingMoney(peersToSend);
            }
            System.exit(0);
        }
    }

}
