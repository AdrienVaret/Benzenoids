package graphs;

public class DirectedEdge {

	private int u, v, intermediarNode;

	public DirectedEdge(int u, int v, int intermediarNode) {
		this.u = u;
		this.v = v;
		this.intermediarNode = intermediarNode;
	}

	public int getU() {
		return u;
	}

	public int getV() {
		return v;
	}

	public int getIntermediarNode() {
		return intermediarNode;
	}
	
	@Override
	public String toString() {
		return u + " -> " + v + " (" + intermediarNode + ")"; 
	}
}
