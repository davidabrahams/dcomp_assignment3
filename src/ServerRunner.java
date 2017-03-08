import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerRunner {
    public static void main (String[] argv) {
        try {
            System.setProperty("java.rmi.server.hostname", argv[0]);
            Registry reg = LocateRegistry.createRegistry(1099);
            Server server = new Server(reg);
            reg.rebind("server", server);
            System.out.println("[System] Server Remote Object is ready.");
        }

catch (Exception e) {
    System.out.println("[System] Server failed: " + e);
    e.printStackTrace();
}
    }
}