import java.util.List;

public class LoggerRunnable implements Runnable{

	MessagePasser m;
	private List<TimeStampedMessage> mq;
	
    public LoggerRunnable(MessagePasser m, List<TimeStampedMessage> mq) {
    	this.m = m;
    	this.mq = mq;
    }

    public void run() {
        
    	while(true)
    	{
    		mq.add(m.receive());
    	}
    	
    }
}