import clock.TimeStamp;


public class TimeStampedMessage extends Message {

	private static final long serialVersionUID = 8564595233200877226L;

	private TimeStamp timeStamp;
	
	public TimeStampedMessage(String src, String dest, String kind, Object data, TimeStamp timeStamp) {
		super(src, dest, kind, data);
		this.timeStamp = timeStamp;
	}

	public TimeStamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(TimeStamp timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@Override
	public TimeStampedMessage clone()
	{
		TimeStampedMessage msg = new TimeStampedMessage(this.getSrc(), this.getDest(), this.getKind(), this.getData(), this.getTimeStamp());
		msg.setId(this.getId());
		return msg;
	}
	
	public TimeStampedMessage cloneWithUpdatedTimeStamp(TimeStamp ts)
	{
		TimeStampedMessage msg = new TimeStampedMessage(this.getSrc(), this.getDest(), this.getKind(), this.getData(), ts);
		msg.setId(this.getId());
		return msg;
	}

}
