package workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import manager.MessagePasser;
import model.TimeStampedMessage;

public class MutualExclusionThread extends Thread {

	private List<TimeStampedMessage> threadRcvQueue;
	private ArrayList<TimeStampedMessage> mutexQueue;
	private boolean criticalSection;
	private boolean voted;

	public MutualExclusionThread(List<TimeStampedMessage> threadRcvQueue) throws IOException {
		this.threadRcvQueue = threadRcvQueue;
		this.criticalSection = false;
		this.voted = false;
		this.mutexQueue = new ArrayList<TimeStampedMessage>();
	}
	
	public void run() {
		while (true) 
		{
			/* Wait for the threadRcvQueue to get filled */
			while(this.threadRcvQueue.isEmpty());
			
			while(!this.threadRcvQueue.isEmpty())
			{
				TimeStampedMessage m = this.threadRcvQueue.remove(0);
				MessagePasser.getInstance().addToProcessQueue(m); //apply rules
				while(!MessagePasser.getInstance().processQueue.isEmpty())
				{
					TimeStampedMessage msg = MessagePasser.getInstance().processQueue.remove(0);
										
					/* YOUR CODE COMES HERE */
					/* Possibly a function call */
					MessagePasser.getInstance().clockService.resyncTimeStamp(msg.getTimeStamp());
					validateMessage(msg);
										
					/*
					if(MessagePasser.getInstance().clockService.getClass().getSimpleName().equals("LogicalClock"))
					{
						MessagePasser.getInstance().clockService.increment(0);
					}
					else 
					{
						MessagePasser.getInstance();
						MessagePasser.getInstance().clockService.increment(MessagePasser.getInstance().nodes.indexOf(MessagePasser.getInstance().findNodeByName(MessagePasser.localName)));
					}
					*/
					
				}
			}
		}		
	}

	private void validateMessage(TimeStampedMessage msg) {
		if (msg.getKind().equals("mreq")) {
			if (criticalSection || voted) {
				mutexQueue.add(msg);
			}
			else {
				//send(mack);
				voted = true;
			}
			
		}
		else if (msg.getKind().equals("mrel")) {
			if (mutexQueue.size() == 0) {
				voted = false;
			}
			else {
				//send MACK to process with lowest TS in mutexQ
				voted = true;
			}
		}
		else if (msg.getKind().equals("mack")) {
			ackCounter++;
			
		}
		else {
			MessagePasser.getInstance().rcvQueue.add(msg);
		}		
	}
	
}