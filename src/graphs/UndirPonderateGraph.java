package graphs;

import java.util.ArrayList;

import utils.Couple;

public class UndirPonderateGraph {

	private int nbNodes, nbEdges;
	private ArrayList<ArrayList<Couple<Integer>>> nodesMatrix;
	
	public UndirPonderateGraph(int nbNodes, int nbEdges, ArrayList<ArrayList<Couple<Integer>>> nodesMatrix) {
		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.nodesMatrix = nodesMatrix;
	}

	public int getNbNodes() {
		return nbNodes;
	}

	public int getNbEdges() {
		return nbEdges;
	}

	public ArrayList<ArrayList<Couple<Integer>>> getNodesMatrix() {
		return nodesMatrix;
	}

}
