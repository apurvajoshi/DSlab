package workers;
import manager.MessagePasser;
import manager.MulticastManager;
import clock.TimeStamp;
import java.net.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import model.TimeStampedMessage;

public class ReceiverThread implements Runnable{

    protected Socket clientSocket = null;	
	private ConcurrentHashMap<String, MulticastManager> holdQueue;

    public ReceiverThread(Socket clientSocket) {

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
			
			if (msg.getSrc().equals(MessagePasser.localName)) {
	    		if(!this.holdQueue.contains(msg.getTimeStamp().getCount().toString()))
	    			this.holdQueue.put(msg.getTimeStamp().getCount().toString(), new MulticastManager(msg));
	    		
	    		return;
	    	}
			
    		if (msgOrder == 2) {
        		//New Timestamp
    			if (holdQueue.containsKey(msg.getTimeStamp().getCount().toString())) {
    				//Duplicate message, already exists in hashmap
    				System.out.println("Duplicate message already exists in hashmap = " + msg.getTimeStamp().getCount().toString());
    				sendMulticastAck(msg);
    			}  
    			else {
    				//else new message , add to hold queue
    				System.out.println("New Message received = " + msg.getTimeStamp().getCount().toString());
            		this.holdQueue.put(msg.getTimeStamp().getCount().toString(), new MulticastManager(msg));	
    			}        		
        	}
        	else if ((msgOrder == 1) || (msgOrder == 0) || (msg.getKind().equals("replay"))) {
        		//Old Message
        		System.out.println("Old Message received" + msg.getTimeStamp().getCount().toString() +"  msgorder = " + msgOrder + "(1, 0 = Already Processed)");
        		sendMulticastAck(msg);
        	}    		
    	}
    	else {
    		//Ack
    		// If Ack is <= Current Timestamp or if I am the source, drop it.
    		if (((msgOrder == 0) || (msgOrder == 1)) || (msg.getSrc().equals(MessagePasser.localName))) {
    			//drop message
    			System.out.println("Drop ACK - old / equal or mine");
    			return; 
    		}
    		else {
    			if (holdQueue.contains(msg.getTimeStamp())) {
    				// New ACK but in hold queue
    				System.out.println("New ACK in hold queue, so update ACK ");
    				holdQueue.get(msg.getTimeStamp()).setAck(msg.getSrc());
    				if (holdQueue.get(msg.getTimeStamp().getCount().toString()).ifAllAckReceived())
    				{
    					
    					while(addThreadRcvQueue() == 1);
    				}
    					
    			}
    			else {
    				this.holdQueue.put(msg.getTimeStamp().getCount().toString(), new MulticastManager((TimeStampedMessage)msg.getData()));
    				System.out.println("New Message received through ACK ");
    				holdQueue.get(msg.getTimeStamp()).setAck(msg.getSrc());
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
    
    public int addThreadRcvQueue() {
    	for (Entry<String, MulticastManager> entry : holdQueue.entrySet()) {
    		if (entry.getValue().ifAllAckReceived()) {
    			if (entry.getValue().getMessage().getSrc().equals(MessagePasser.localName)) {
    				holdQueue.remove(entry.getKey());
    			}
    			else {
    				if (inorder(entry.getValue().getMessage())) {
        				TimeStampedMessage m = holdQueue.remove(entry.getKey()).getMessage(); 
        				MessagePasser.getInstance().threadRcvQueue.add(m);
        				return 1;
    				}
    				
    			}
    		}
    	}
		return 0;
    }
    
    public boolean inorder(TimeStampedMessage m) {
    	//inorder
    	TimeStamp localTS = MessagePasser.getInstance().clockService.getTimestamp();
    	TimeStamp mesgTS = m.getTimeStamp();
    	
    	int mesgIdx = MessagePasser.getInstance().nodes.indexOf(MessagePasser.getInstance().findNodeByName(m.getSrc()));
    	
		if (mesgTS.getCount().get(mesgIdx) != (localTS.getCount().get(mesgIdx) + 1)) {
			return false;
		}
		
		for (int i = 0; i < mesgTS.getCount().size(); i++) {
			if (i != mesgIdx) {
				if (mesgTS.getCount().get(i) > localTS.getCount().get(i)) {
					return false;
				}
			}
		}
		
		return true;		
    }
}