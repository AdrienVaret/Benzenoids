package utils;

public class Cycle implements Comparable<Cycle>{

	private int [] edges;
	private int size;
	
	public Cycle(int [] edges, int size) {
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
	public int compareTo(Cycle o) {
		if (size < o.getSize())
			return -1;
		else if (size == o.getSize())
			return 0;
		else 
			return 1;
	}
	
	@Override
	public String toString() {
		String toString = "[";
		for (int i = 0 ; i < edges.length ; i++)
			toString += edges[i] + ", ";
		toString += "] (" + size + ")";
		return toString;
	}

}
