package utils;

public class CoupleCycle implements Comparable<CoupleCycle>{

	private int index;
	private int size;
	
	public CoupleCycle(int index, int size) {
		this.index = index;
		this.size = size;
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getSize() {
		return size;
	}

	@Override
	public int compareTo(CoupleCycle o) {
		if (size < o.getSize())
			return -1;
		else if (size == o.getSize())
			return 0;
		else 
			return 1;
	}

}
