import java.rmi.*;

public class ServerRunner {
    public static void main (String[] argv) {
        try {
            Server server = new Server();
            Naming.rebind("rmi://10.7.92.44/ABC", server);
            System.out.println("[System] Server Remote Object is ready.");
        }

catch (Exception e) {
    System.out.println("[System] Server failed: " + e);
    e.printStackTrace();
}
    }
}