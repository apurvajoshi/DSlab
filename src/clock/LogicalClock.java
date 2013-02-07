package clock;

public class LogicalClock extends ClockService {
	
	public LogicalClock() {
		super();
	}

	@Override
	public synchronized void setTimestamp() {
		this.timestamp.AddCount();
	}
	
	public synchronized void resyncTimeStamp(TimeStamp ts) {
		int max = Math.max(this.timestamp.getCount().get(0), ts.getCount().get(0));
		this.timestamp.setCount(0, max);
	}
	
}
