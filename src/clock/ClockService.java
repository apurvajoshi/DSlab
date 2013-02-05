package clock;
public abstract class ClockService {
	
	protected TimeStamp timestamp;

	public ClockService() {
		timestamp = new TimeStamp();
	}
	
	public String toString() {
		return "This is the class named: \""+ this.getClass().getSimpleName()+"\"";
	}
	
	public TimeStamp getTimestamp() {
		return timestamp;
	}
	
	public abstract void setTimestamp();
	
	public void increment(int index) {
		int count = this.timestamp.getCount().get(index);
		count++;
		//System.out.println("Count  = " + count);
		this.timestamp.setCount(index, count);
	}
	
	public abstract void resyncTimeStamp(TimeStamp ts);
}
