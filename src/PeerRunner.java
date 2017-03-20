import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.Arrays;

public class PeerRunner {

    public static void main (String[] argv) {
        try {
            String localIP = argv[0];
            String masterIP = argv[1];
            String processName = argv[2];
            System.out.println("Local IP " + localIP);
            System.out.println("Master IP " + masterIP);
            System.out.println("Process name " + processName);
            System.setProperty("java.rmi.server.hostname", localIP);
            Registry localReg;
            try {
                localReg = LocateRegistry.createRegistry(1099);
            } catch (ExportException e) {
                System.out.println("Local RMI registry already is running. Using existing registry.");
                localReg = LocateRegistry.getRegistry(localIP, 1099);
            }
            Registry masterReg = LocateRegistry.getRegistry(masterIP, 1099);
            System.out.println(Arrays.toString(masterReg.list()));
            MasterInterface master = (MasterInterface) masterReg.lookup("master");
            Peer peer = new Peer(processName, localIP);
            localReg.rebind(processName, peer);
            System.out.println("Process bound locally.");
            master.register(localIP, processName);
            System.out.println("Registered with name " + peer.getName());
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}