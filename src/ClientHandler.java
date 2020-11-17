import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramSocket;

public class ClientHandler implements Runnable {

    // String directories to files where clients and subjects are stored
    private final String clients = null;
    private final String subjects = null;

    private BufferedReader clientsReader;
    private BufferedReader subjectsReader;

    private DatagramSocket ds;

    String requestType;
/*
    public ClientHandler(Socket socket, String request) {
        ds = socket;
        clientsReader = new BufferedReader(new FileReader(clients));
        subjectsReader = new BufferedReader(new FileReader(subjects));
        requestType = request;
        // read data from files
        clientsReader.readLine();
        subjectsReader.readLine();
    }*/
RSS clientR;
    public void run() {
        System.out.println("Server Listening to Client: " + ds.getInetAddress().toString() + ":" + ds.getPort());

        while (ds.isConnected()) {
            sendResponseForRequest(requestType);
        } // end of while loop
    } // end of run

    // returns response depending on client's request
    private void sendResponseForRequest(String requestType) {
        String request = requestType;

        // check if user already exists
        if (request.equals("REGISTER")) { //all the 10 in the for loops are incorrect. needs to be changed
        	for (int i = 0; i < 10; i++) { //loop through previous registered names
        		if (clientR.gettClienName() != clientsReader.readLine()) {
        			System.out.println("Registration accepted");
        			clientR.setClientStatus("REGISTERED");
        		} else {
        			System.out.println("Registration denied: Invalid username");
        			clientR.setClientStatus("REGISTER-DENIED");
        			//add time penalty before user can retry
        		}
        	}
        }
        if (request.equals("DE-REGISTER")) {
        	for (int i = 0; i < 10; i++) { //loop through previous registered names
            	if (clientR.gettClienName() == clientsReader.readLine()) {
    				System.out.println("Deregistration accepted");
    				clientR.setClientStatus("DE-REGISTER");
            	} else {} //if no name is matched, then request is ignored
        	}
        }
        if (request.equals("UPDATE") {
        	for (int i = 0; i < 10; i++) { //loop through previous registered names
        		if (clientR.gettClienName() == clientsReader.readLine()) {
        			System.out.println("Update confirmed");
        			clientR.setClientStatus("UPDATE-CONFIRMED");
        		} else {
        			System.out.println("Update denied: Name does not exist");
        			clientR.setClientStatus("UPDATE-DENIED");
        		}
        	}
        }
        if (request.equals("SUBJECTS") {
        	for (int i = 0; i < 10; i++) { //loop through previous registered names
        		if (clientR.gettClienName() == clientsReader.readLine()) {
        			System.out.println("Subjects updated");
        			clientR.setClientStatus("SUBJECTS-UPDATED");
        		} else {
        			System.out.println("Subjects rejected: Name or Subject does not exist");
        			clientR.setClientStatus("SUBJECTS-REJECTED");
        		}
        	}
        }
        if (request.equals("PUBLISH") {
        	for (int i = 0; i < 10; i++) { //loop through previous registered names
        		if (clientR.gettClienName() == clientsReader.readLine()) {
        			System.out.println("Message");
        			clientR.setClientStatus("MESSAGE");
        		} else if {
        			System.out.println("Publish denied: Name/Subject does not exist");
        			clientR.setClientStatus("PUBLISH-DENIED");
        		} else {
        			System.out.println("Publish denied: Subject is not in the user's interests");
        			clientR.setClientStatus("PUBLISH-DENIED");
        		}
        	}
        }
    }


} // end of class ClientHandler