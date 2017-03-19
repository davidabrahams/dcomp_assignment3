import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

public class PeerRunner {

    public static void main (String[] argv) {
        try {
            String localIP = argv[0];
            String masterIP = argv[1];
            String processName = argv[2];
            System.out.println(localIP + masterIP + processName);
            System.setProperty("java.rmi.server.hostname", localIP);
            Registry localReg = LocateRegistry.createRegistry(1099);
            Registry masterReg = LocateRegistry.getRegistry(masterIP, 1099);
            MasterInterface master = (MasterInterface) masterReg.lookup("master");
            Peer peer = new Peer(processName);
            localReg.rebind(processName, peer);
            master.register(localIP, processName);
            System.out.println("Registered with name " + peer.getName());
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}