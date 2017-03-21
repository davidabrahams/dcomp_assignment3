import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class PeerRunner {

    private static PeerInterface peer;

    public static void start() throws InterruptedException, RemoteException, NotBoundException {
        int count = 0;
        peer.sendMessage(new Message(Message.ELECTION));
        while (true) {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5, 10));
            peer.clearMessagesAndMakeTransaction();
            if (peer.isLeader()) {
                if (count==10) {
                    peer.sendMessage(new Message(Message.MARKER));
                    count = 0;
                } else
                    count++;
            }
        }
    }

    public static void main (String[] argv) {
        try {
            Scanner sc = new Scanner(System.in);
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
            peer = new Peer(processName, localIP);
            localReg.rebind(processName, peer);
            System.out.println("Process bound locally.");
            master.register(localIP, processName);
            System.out.println("Registered with name " + processName);
            System.out.println("Press Enter to start sending money -> ");
            sc.nextLine();
            start();
        } catch (Exception e) {
            System.out.println("[System] Peer failed: " + e);
            e.printStackTrace();
        }
    }
}