import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ClientRunner {
    private static PeerInterface registerClient(Scanner s, ServerInterface server) throws RemoteException {
        while (true) {
            System.out.print("Enter a username -> ");
            String name=s.nextLine().trim();
            PeerInterface client = new Peer(name);
            if (server.register(client)) {
                System.out.println("Successfully registered.");
                return client;
            }
            System.out.print("Username is already taken. ");
        }
    }

    public static void main (String[] argv) {
        try {

            Scanner s=new Scanner(System.in);
            Registry reg = LocateRegistry.getRegistry("10.7.92.44", 1099);
            ServerInterface server = (ServerInterface) reg.lookup("server");
            PeerInterface client = registerClient(s, server);

            boolean quit = false;
            System.out.println();
            System.out.println("Enter 1 to get a list of users.\nEnter 2 for sending a message to a user.\nEnter 3 to quit the application.");
            System.out.println();
            while (!quit) {
                System.out.print("Your command -> ");
                int msg;
                try {
                    msg = Integer.parseInt(s.nextLine().trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                System.out.println();

                switch (msg) {
                    case 1:
                        server.getUsers().forEach(System.out::println);
                        System.out.println();
                        break;
                    case 2:
                        System.out.print("Enter the username of the receiver -> ");
                        String r = s.nextLine().trim();
                        System.out.print("Enter the message: ");
                        String m = s.nextLine().trim();
                        Message sendMsg = new Message(client.getName(), r, m);
                        boolean success = server.sendToServer(sendMsg);
                        if (!success) System.out.println("Unable to sendToClient message.");
                        System.out.println();
                        break;
                    case 3:
                        quit = true;
                        break;
                }
            }
            System.exit(0);
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}