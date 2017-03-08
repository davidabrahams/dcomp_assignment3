import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class PeerRunner {

    public static void main (String[] argv) {
        try {
            System.setProperty("java.rmi.server.hostname", argv[0]);
            Registry reg = LocateRegistry.getRegistry("10.7.92.44", 1099);
            ServerInterface server = (ServerInterface) reg.lookup("server");
            Peer peer = new Peer(reg);
            peer.setName(server.register(peer));
            System.out.println("Registered with name " + peer.getName());
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}