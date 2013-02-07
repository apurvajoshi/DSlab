package clock;

public class VectorClock extends ClockService{
	
	int noOfNodes;
	
	public VectorClock(int nodes) {
		super();
		this.noOfNodes = nodes;
	}

	@Override
	public synchronized void setTimestamp() {
		for(int i=0; i < this.noOfNodes; i++)
			this.timestamp.AddCount();		
	}

	@Override
	public synchronized void resyncTimeStamp(TimeStamp ts) {
		for(int i=0; i < this.noOfNodes; i++) {
			int max = Math.max(this.timestamp.getCount().get(i), ts.getCount().get(i));
			this.timestamp.setCount(i, max);
		}
	}
}
