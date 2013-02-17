package workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import manager.MessagePasser;
import model.Node;
import model.TimeStampedMessage;

public class MutualExclusionThread extends Thread {

	private List<TimeStampedMessage> threadRcvQueue;
	private ArrayList<TimeStampedMessage> mutexQueue;
	private boolean criticalSection;
	private boolean voted;
	private int ackCounter; 

	public MutualExclusionThread(List<TimeStampedMessage> threadRcvQueue) throws IOException {
		this.threadRcvQueue = threadRcvQueue;
		this.criticalSection = false;
		this.voted = false;
		this.mutexQueue = new ArrayList<TimeStampedMessage>();
		this.ackCounter = 0;
	}
	
	public void run() {
		while (true) 
		{
			/* Wait for the threadRcvQueue to get filled */
			while(this.threadRcvQueue.isEmpty());
			
			while(!this.threadRcvQueue.isEmpty())
			{
				TimeStampedMessage m = this.threadRcvQueue.remove(0);
				System.out.println("Received message");
				MessagePasser.getInstance().addToProcessQueue(m); //apply rules
				while(!MessagePasser.getInstance().processQueue.isEmpty())
				{
					TimeStampedMessage msg = MessagePasser.getInstance().processQueue.remove(0);
										
					validateMessage(msg);
					
					MessagePasser.getInstance().clockService.resyncTimeStamp(msg.getTimeStamp());
					MessagePasser.getInstance().rcvQueue.add(msg);
				}
			}
		}		
	}

	private void validateMessage(TimeStampedMessage msg) {
		System.out.println("Validating Message");
		if (msg.getKind().equals("mreq")) {
			if (criticalSection || voted) {
				System.out.println("got mreq");
				mutexQueue.add(msg);
			}
			else {
				System.out.println("Not in critical section or voted = false, sending ACK");
				sendMAck(msg);
				voted = true;
			}
		}
		else if (msg.getKind().equals("mrel")) {
			if (mutexQueue.size() == 0) {
				voted = false;
			}
			else {
				//send MACK to process with lowest TS in mutexQ
				
				/* Returns the message with minimum timestamp */
				TimeStampedMessage m = Collections.min(this.mutexQueue, new Comparator<TimeStampedMessage>() {
					public int compare(TimeStampedMessage  m1, TimeStampedMessage m2) {
                        return m2.getTimeStamp().getCount().get(0).compareTo(m1.getTimeStamp().getCount().get(0));
                    }
				});
				sendMAck(m);
				this.mutexQueue.remove(m);
				voted = true;
			}
		}
		else if (msg.getKind().equals("mack")) {
			// Update count for a message with a given id
			Node localNode = MessagePasser.getInstance().findNodeByName(MessagePasser.localName);
			
			this.ackCounter++;
			if(this.ackCounter == localNode.getProcessGroup().size())
			{				
				/* Returns the message with minimum timestamp */
				TimeStampedMessage m = Collections.min(MessagePasser.getInstance().meSendQueue, new Comparator<TimeStampedMessage>() {
					public int compare(TimeStampedMessage  m1, TimeStampedMessage m2) {
                        return m2.getTimeStamp().getCount().get(0).compareTo(m1.getTimeStamp().getCount().get(0));
                    }
				});
				
				/* Enter critical Section */
				System.out.println("Entering critical Section.");
				this.criticalSection = true;
				MessagePasser.getInstance().meSendQueue.remove(m);
				ackCounter = 0;
			}
		}
	}
	
	
	private void sendMAck(TimeStampedMessage msg) {
		System.out.println("Sending ACK");
		TimeStampedMessage m = new TimeStampedMessage(MessagePasser.localName, msg.getSrc(), "mack", msg, msg.getTimeStamp());
		//MessagePasser.getInstance().send(m);
		MessagePasser.getInstance().sendAfterRuleCheck(m);
	}

	
}