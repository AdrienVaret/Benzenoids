package graphs;

import java.util.ArrayList;

public class UndirGraph {

	private int nbNodes, nbEdges, nbHexagons;
	
	private ArrayList<ArrayList<Integer>> edgeMatrix;
	
	private ArrayList<String> edgesString;
	private ArrayList<String> hexagonsString;
	
	private int [] activeNodes;
	
	public UndirGraph(int nbNodes, int nbEdges, int nbHexagons, ArrayList<ArrayList<Integer>> edgeMatrix, ArrayList<String> edgesString,
			          ArrayList<String> hexagonsString) {
		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.edgeMatrix = edgeMatrix;
		this.edgesString = edgesString;
		this.hexagonsString = hexagonsString;
		activeNodes = null;
	}
	
	public UndirGraph(int nbNodes, int nbEdges, int nbHexagons, ArrayList<ArrayList<Integer>> edgeMatrix, ArrayList<String> edgesString,
	          ArrayList<String> hexagonsString, int [] activeNodes) {
		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.edgeMatrix = edgeMatrix;
		this.edgesString = edgesString;
		this.hexagonsString = hexagonsString;
		this.activeNodes = activeNodes;
	}
	
	public int getNbNodes() {
		return nbNodes;
	}
	
	public int getNbEdges() {
		return nbEdges;
	}
	
	public int getNbHexagons() {
		return nbHexagons;
	}
	
	public ArrayList<ArrayList<Integer>> getEdgeMatrix() {
		return edgeMatrix;
	}
	
	public ArrayList<String> getEdgesString() {
		return edgesString;
	}
	
	public ArrayList<String> getHexagonsString() {
		return hexagonsString;
	}
	
	public boolean isActive(int node) {
		return (activeNodes[node] == 1);
	}
}
