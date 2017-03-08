import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface{

    private final int NUM_PEERS = 1;
    private Map<String, PeerInterface> peers;
    private Registry reg;

    public Server(Registry reg) throws RemoteException {
        this.reg = reg;
        peers = new HashMap<>();
    }

    @Override
    public String register(PeerInterface client) throws RemoteException {
        System.out.println("Request to register received.");
        String name = "P" + Integer.toString(peers.size() + 1);
        peers.put(name, client);
        reg.rebind(name, client);

        if (peers.size() == NUM_PEERS) {
            for (PeerInterface p : peers.values())
                p.startSendingMoney();
        }

        return name;
    }

}
