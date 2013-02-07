package workers;
import java.net.*;
import java.util.List;
import java.io.*;

import model.TimeStampedMessage;

public class ReceiverThread implements Runnable{

    protected Socket clientSocket = null;	
	private List<TimeStampedMessage> rcvQueue;

    public ReceiverThread(Socket clientSocket, List<TimeStampedMessage> queue) {
        this.clientSocket = clientSocket;        
        this.rcvQueue = queue;

        try {
			this.clientSocket.setKeepAlive(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void run() {
    	ObjectInputStream is = null;
    	try {
			while (true) {
				is = new ObjectInputStream(clientSocket.getInputStream());
				TimeStampedMessage m;
				m = (TimeStampedMessage) is.readObject();
				if (m != null) {
					this.rcvQueue.add(m);
				}
			}
    	} catch (Exception e) {
			try {
	    		is.close();
				clientSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}
    		//e.printStackTrace();
		}
    }
}