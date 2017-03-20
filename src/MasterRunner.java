import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class MasterRunner {
    public static void main(String[] argv) {
        try {
            System.out.println(argv[0]);
            System.setProperty("java.rmi.server.hostname", argv[0]);
            Registry reg = LocateRegistry.createRegistry(1099);
            Master master = new Master();
            reg.rebind("master", master);
            System.out.println("[System] Master Remote Object is ready.");
        } catch (Exception e) {
            System.out.println("[System] Master failed: " + e);
            e.printStackTrace();
        }
    }
}