package graphs;

import java.util.ArrayList;

public class UndirGraph {

	private int nbNodes, nbEdges;
	
	private ArrayList<ArrayList<Integer>> edgeMatrix;
	private ArrayList<String> edgesString;
	
	public UndirGraph(int nbNodes, int nbEdges, ArrayList<ArrayList<Integer>> edgeMatrix, ArrayList<String> edgesString) {
		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.edgeMatrix = edgeMatrix;
		this.edgesString = edgesString;
	}
	
	public int getNbNodes() {
		return nbNodes;
	}
	
	public int getNbEdges() {
		return nbEdges;
	}
	
	public ArrayList<ArrayList<Integer>> getEdgeMatrix() {
		return edgeMatrix;
	}
	
	public ArrayList<String> getEdgesString() {
		return edgesString;
	}
	
}
