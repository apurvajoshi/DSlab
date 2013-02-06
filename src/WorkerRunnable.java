import java.net.*;
import java.util.List;
import java.io.*;
import clock.ClockService;
import clock.LogicalClock;
import clock.VectorClock;

public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
	private List<TimeStampedMessage> rcvQueue;

    public WorkerRunnable(Socket clientSocket, List<TimeStampedMessage> queue) {
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
        
    	while(true)
    	{
    		try {            	
            	ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());
            	
            	TimeStampedMessage m;
    			try {
    				m = (TimeStampedMessage)is.readObject();
        			if (m != null) {
        				ProcessMulticastMessage(m);
        	        	//add to receive buffer
            			//this.rcvQueue.add(m);
        	        }
    			} catch (ClassNotFoundException e) {    				
    				e.printStackTrace();
    			}
            } catch (IOException e) {
                //report exception somewhere.
                e.printStackTrace();
            }
    	}
    }
    
    public void ProcessMulticastMessage(TimeStampedMessage msg) {
    	msg.
    	
    }
}