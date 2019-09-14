package utils;

public class CoupleCycle implements Comparable<CoupleCycle>{

	private int [] edges;
	private int size;
	
	public CoupleCycle(int [] edges, int size) {
		this.edges = edges;
		this.size = size;
	}
	
	public int [] getEdges() {
		return edges;
	}
	
	public int getEdge(int index) {
		return edges[index];
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
