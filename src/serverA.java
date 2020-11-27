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
	boolean isServing = true;

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
		int serverSocket = 3050;
		DatagramSocket ds = new DatagramSocket(serverSocket);
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
		InetAddress ip = InetAddress.getLocalHost();
		Object inputObject;
		int serverBSocket = 3051;
		String key;
		//InetAddress ip = InetAddress.getByName("8.8.8.8");
		boolean serverRunning = true;

		ClientHandler clientHandler = new ClientHandler(ds);

		while (true) {
			System.out.println("[SERVER] Waiting for a client connection...");

			ServerTimer serverTimer = new ServerTimer(ds);
			Timer timer = new Timer();
			// timer starts
			System.out.println("Server started serving at: " + new Date());
			timer.schedule(serverTimer, 0);

			// timer ends
			//randomTime = r.nextInt(420000); //7 mins = 420000 ms
			Thread.sleep(20000); // in milliseconds
			System.out.println("\nServer stopped serving at: " + new Date());
			String status = "This server is no longer serving, the other server must take over.\n";
			System.out.println(status + "   Storage Content:   \n" + clientHandler.storage);

			// sends Server A's storage to Server B
			ByteArrayOutputStream bStream = new ByteArrayOutputStream();
			ObjectOutput objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(clientHandler.storage);
			objectOutput.close();

			byte[] serializedMessage = bStream.toByteArray();

			DatagramPacket dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, serverBSocket);
			ds.send(dpSend);

			// send Server B's socket and ip to all the clients
			RSS serverB = new RSS(serverBSocket);
			serverB.setClientStatus("CHANGE-SERVER");
			
			bStream = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bStream);
			objectOutput.writeObject(serverB);
			objectOutput.close();

			serializedMessage = bStream.toByteArray();

			String string;
			String clientSocket = null;
			String clientIP = null;

			for (Map.Entry<String, ArrayList<String>> clientInfo : clientHandler.storage.entrySet()) {
				ArrayList<String> currentList = clientInfo.getValue();

				// iterate on the current list
				for (int j = 0; j < currentList.size(); j++) {
					string = currentList.get(0);
					String[] splitInfo = string.split(" ");
					clientIP = splitInfo[2];
					clientSocket = splitInfo[3];
				}
				System.out.println(   " Change-server  in serverA " + clientSocket);
				dpSend = new DatagramPacket(serializedMessage, serializedMessage.length, ip, Integer.parseInt(clientSocket));
				ds.send(dpSend);
			}

			// waiting for Server B to stop serving, and to receive Server B's storage
			while (true) {
				DpReceive = new DatagramPacket(receive, receive.length);

				ds.receive(DpReceive);

				ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receive));

				inputObject = iStream.readObject();
				if (inputObject instanceof HashMap) {
					System.out.println("Storage from Server B:\n" + inputObject); // used for testing

					// TESTING - add contents from Server A's storage to Server B's storage
					for (Map.Entry<String, ArrayList<String>> clientInfo : ((HashMap<String, ArrayList<String>>) inputObject).entrySet()) {
						key = clientInfo.getKey();
						ArrayList<String> currentListData = clientInfo.getValue();
						storage.put(key, currentListData);
						System.out.println("TEST - storage from Server B: " + storage);
					}
					break;
				}
			} // end of while loop
		}
	} // end of main
} // end of class ServerA

class ServerTimer extends TimerTask {
	DatagramSocket datagramSocket = null;
	private static ArrayList<ClientHandler> clients = new ArrayList<>();
	private static ExecutorService pool = Executors.newFixedThreadPool(4); // can increase this # depending on # of clients

	public ServerTimer(DatagramSocket ds) {
		this.datagramSocket = ds;
	}

	@Override
	public void run() {
		// create ClientHandler threads to handle each client
		//while (true) {
		ClientHandler clientThread = null;
		try {
			clientThread = new ClientHandler(datagramSocket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		clients.add(clientThread);
		pool.execute(clientThread);
		//} // end of while loop
		}
} // end of class serverTimer

