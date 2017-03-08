import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ClientRunner {

    public static void main (String[] argv) {
        try {
            System.setProperty("java.rmi.server.hostname", argv[0]);
            Registry reg = LocateRegistry.getRegistry("10.7.92.44", 1099);
            ServerInterface server = (ServerInterface) reg.lookup("server");
            Peer peer = new Peer(reg);
            peer.setName(server.register(peer));
            reg.rebind(peer.getName(), peer);
            System.exit(0);
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}