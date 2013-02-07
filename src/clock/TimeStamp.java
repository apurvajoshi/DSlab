package clock;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class TimeStamp implements Serializable {

	private static final long serialVersionUID = -4236758036778851620L;
	private ArrayList<Integer> count;
	
	public TimeStamp() {
		count = new ArrayList<Integer>();
	}

	public TimeStamp(TimeStamp t) {
		count = new ArrayList<Integer>();
		for (int i = 0; i < t.getCount().size(); i++) {
			this.count.add(t.getCount().get(i));
		}
	}
		
	public ArrayList<Integer> getCount() {
		return count;
	}

	public void AddCount() {
		this.count.add(0);
	}
	
	public void setCount(int index, int value) {
		this.count.set(index, value);
	}
	
	public int compare(TimeStamp ts1) {
		if (this.getCount().equals(ts1.getCount())) {
			return 0;
		}
		
		Iterator<Integer> it1 = this.count.iterator();
		Iterator<Integer> it2 = ts1.getCount().iterator();
		
		while (it1.hasNext()) {
			if (it1.next() > it2.next()) {
				while (it1.hasNext()) {
					if (it1.next() < it2.next()) {
						return 3;
					}
					
				}
				return 2;				
			}			
		}		
		return 1;		
	}
}
