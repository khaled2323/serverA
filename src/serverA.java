import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class serverA {
	static HashMap<String, ArrayList<String>> storage = new HashMap<String, ArrayList<String>>();
	private static boolean isServing = true;
	private static int serverSocket = 3050;
	private static int serverBSocket = 3051;

	public static StringBuilder data(byte[] a) {
		// TODO Auto-generated method stub
		if(a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		
		while(a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}
	
	public static void main(String [] args) throws IOException, InterruptedException, ClassNotFoundException {
		DatagramSocket ds = new DatagramSocket(serverSocket);
		ByteArrayOutputStream bStream;
		ObjectOutput objectOutput;
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
		InetAddress ip = InetAddress.getLocalHost();
		Object inputObject;
		String key;
		//InetAddress ip = InetAddress.getByName("8.8.8.8");

		//ClientHandler clientHandler = new ClientHandler(ds);

		while (true) {
			System.out.println("[SERVER] Waiting for a client connection...");

			ServerTimer serverTimer = new ServerTimer(ds, serverBSocket);
			Timer timer = new Timer();
			// timer starts
			System.out.println("Server started serving at: " + new Date());
			timer.schedule(serverTimer, 0);

			// timer ends
			//randomTime = r.nextInt(420000); //7 mins = 420000 ms
			Thread.sleep(15000); // in milliseconds
			System.out.println("\nServer stopped serving at: " + new Date());
			isServing = false;
			String status = "This server is no longer serving, the other server must take over.\n";
			System.out.println(status + "   Storage Content:   \n" + ClientHandler.storage);

			// tell Server B to take over
			RSS serverB = new RSS(serverBSocket);
			serverB.setClientStatus("CHANGE-SERVER");

			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverB);
			objectOutput.close();

			byte[] serializedMessage = bStream.toByteArray();
			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, serverBSocket);
			ds.send(dpSend);

			// send Server B's socket and ip to all the clients
			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverB);
			objectOutput.close();

			serializedMessage = bStream.toByteArray();

			String string;
			String clientSocket = null;
			String clientIP = null;

			// iterates through storage to get all the clients' ip and sockets
			for (Map.Entry<String, ArrayList<String>> clientInfo : ClientHandler.storage.entrySet()) {
				ArrayList<String> currentList = clientInfo.getValue();

				// iterate on the current list
				for (int j = 0; j < currentList.size(); j++) {
					string = currentList.get(0);
					String[] splitInfo = string.split(" ");
					clientIP = splitInfo[2];
					clientSocket = splitInfo[3];
				}
				dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, Integer.parseInt(clientSocket));
				ds.send(dpSend);
			}

			// waiting for Server B to stop serving, then receives Server B's storage
			while (!isServing) {
				ClientHandler clientHandler = new ClientHandler(ds, serverBSocket);
				clientHandler.run();
				isServing = clientHandler.getIsServing();
			} // end of inner while loop
		} // end of outer while loop
	} // end of main
} // end of class ServerA

class ServerTimer extends TimerTask {
	DatagramSocket datagramSocket = null;
	int serverBSocket;
	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static ExecutorService pool = Executors.newFixedThreadPool(4); // can increase this # depending on # of clients
	private volatile boolean exit = false;

	public ServerTimer(DatagramSocket ds, int serverBSocket) {
		this.datagramSocket = ds;
		this.serverBSocket = serverBSocket;
	}

	@Override
	public void run() {
		// create ClientHandler threads to handle each client
		//while (!exit) {
		ClientHandler clientThread = null;
		try {
			clientThread = new ClientHandler(datagramSocket, serverBSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clients.add(clientThread);
		pool.execute(clientThread);
			//} // end of while loop
		} // end of run

	public void stop() {
		exit = true;
	}
} // end of class serverTimer

