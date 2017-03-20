import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Master extends UnicastRemoteObject implements MasterInterface {

    private final int NUM_PEERS = 2;
    private List<NameIP> allProcesses;

    public Master() throws RemoteException {
        allProcesses = new ArrayList<>();
    }

    @Override
    public void register(String IP, String name) throws RemoteException, NotBoundException, InterruptedException {
        System.out.println("Request to register received for process: " + name);
        NameIP newProcess = new NameIP(name, IP);
        allProcesses.add(newProcess);
        if (allProcesses.size() == NUM_PEERS) {
            System.out.println("Limit reached. Starting money sending");
            for (int i = 0; i < allProcesses.size(); i++) {
                PeerInterface p = Util.getPeer(allProcesses.get(i));
                NameIP nextPeer = allProcesses.get((i + 1) % allProcesses.size());
                p.setNextPeer(nextPeer);
            }
        }

        for (NameIP nip : allProcesses)
            if (nip != newProcess) {
                Util.getPeer(nip).addPeer(newProcess);
                Util.getPeer(newProcess).addPeer(nip);
            }
    }

}
