package workers;

import java.io.IOException;
import java.util.List;
import manager.MessagePasser;
import model.TimeStampedMessage;

public class MutualExclusionThread extends Thread {

	private List<TimeStampedMessage> threadRcvQueue;

	public MutualExclusionThread(List<TimeStampedMessage> threadRcvQueue) throws IOException {
		this.threadRcvQueue = threadRcvQueue;
	}
	
	public void run() {
		while (true) 
		{
			/* Wait for the threadRcvQueue to get filled */
			while(this.threadRcvQueue.isEmpty());
			
			while(!this.threadRcvQueue.isEmpty())
			{
				TimeStampedMessage m = this.threadRcvQueue.remove(0);
				MessagePasser.getInstance().addToProcessQueue(m);
				while(!MessagePasser.getInstance().processQueue.isEmpty())
				{
					TimeStampedMessage msg = MessagePasser.getInstance().processQueue.remove(0);
					
					
					/* YOUR CODE COMES HERE */
					/* Possibly a function call */
					
					
					MessagePasser.getInstance().clockService.resyncTimeStamp(msg.getTimeStamp());
					MessagePasser.getInstance().rcvQueue.add(msg);
				}
			}
		}
	}
	
}