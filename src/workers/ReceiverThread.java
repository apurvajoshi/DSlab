package workers;
import manager.MessagePasser;
import manager.MulticastManager;
import java.net.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import model.TimeStampedMessage;

public class ReceiverThread implements Runnable{

    protected Socket clientSocket = null;
	
	private ConcurrentHashMap<String, MulticastManager> holdQueue;

    public ReceiverThread(Socket clientSocket, List<TimeStampedMessage> queue) {

		holdQueue = new ConcurrentHashMap<String, MulticastManager> ();
        this.clientSocket = clientSocket;        
        
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

    	int msgOrder = msg.getTimeStamp().compare(MessagePasser.getInstance().clockService.getTimestamp());
    	
    	if (msg.getKind().compareTo("ack") != 0) { 
    		if (msgOrder == 2) {
        		//New Timestamp
    			if (holdQueue.containsKey(msg.getTimeStamp().toString())) {
    				//Duplicate message, already exists in hashmap
    				sendMulticastAck(msg);
    			}        		
        		//else new message , add to hold queue
        		this.holdQueue.put(msg.getTimeStamp().toString(), new MulticastManager(msg));
        	}
        	else if (msgOrder == 1) {
        		//Old Message    	
        		sendMulticastAck(msg);
        	}    		
    	}
    	else {
    		//Ack
    		// If Ack is <= Current Timestamp or if I am the source, drop it.
    		if (((msgOrder == 0) || (msgOrder == 1)) || (msg.getSrc().equals(MessagePasser.localName))) {
    			//drop message
    			return; 
    		}
    		else {
    			if (holdQueue.contains(msg.getTimeStamp())) {
    				holdQueue.get(msg.getTimeStamp()).setAck(msg.getSrc());
    			}
    			else {
    				this.holdQueue.put(msg.getTimeStamp().toString(), new MulticastManager((TimeStampedMessage)msg.getData()));
    				sendMulticastAck((TimeStampedMessage)msg.getData());
    			}
    		}
    		
    	}	
    }
    
    public void sendMulticastAck(TimeStampedMessage msg)
    {
  	  for(int i = 0; i < MessagePasser.getInstance().nodes.size(); i++)
  	  {
    		TimeStampedMessage m = new TimeStampedMessage(MessagePasser.localName, MessagePasser.getInstance().nodes.get(i).getName(), "ack", msg, msg.getTimeStamp());
    		MessagePasser.getInstance().send(m);
  	  }
    }
}