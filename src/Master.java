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

    @Override
    public void register(String IP, String name) throws RemoteException, NotBoundException, InterruptedException {
        System.out.println("Request to register received for process: " + name);
        allProcesses.add(new NameIP(name, IP));
        if (allProcesses.size() == NUM_PEERS) {
            for (int i = 0; i < allProcesses.size(); i++) {
                NameIP nip = allProcesses.get(i);
                List<NameIP> peersToSend = allProcesses.stream().filter(temp -> temp != nip).collect(Collectors.toList());
                PeerInterface p = Util.getPeer(nip);
                NameIP nextPeer = allProcesses.get((i + 1) % allProcesses.size());
                p.setNextPeer(nextPeer);
                p.startSendingMoney(peersToSend);
            }
            System.exit(0);
        }
    }

}
