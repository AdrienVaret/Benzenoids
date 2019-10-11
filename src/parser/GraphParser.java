package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import graphs.UndirGraph;
import graphs.UndirPonderateGraph;
import utils.Couple;

public class GraphParser {

	private static boolean isCommentary(String [] splittedLine) {
		if (splittedLine[0].equals("c"))
			return true;
		return false;
	}
	
	public static UndirGraph parseUndirectedGraph(String path) {
		
		try {
			
			int nbNodes = 0, nbEdges = 0, nbHexagons = 0, maxVertexId = 0;
			
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line;
			boolean firstLine = true;
			
			ArrayList<String> edgesString = new ArrayList<String>();
			ArrayList<String> hexagonsString = new ArrayList<String>();
			ArrayList<ArrayList<Integer>> edgeMatrix = new ArrayList<ArrayList<Integer>>();
			
			int [] activeNodes = null;
			
			int idEdge = 0;
			
			while ((line = r.readLine()) != null) {
				
				String [] splittedLine = line.split(" ");
				
				if (!isCommentary(splittedLine)) {
					
					if (firstLine) {
						firstLine = false;
						nbNodes = Integer.parseInt(splittedLine[2]);
						nbEdges = Integer.parseInt(splittedLine[3]);
						nbHexagons = Integer.parseInt(splittedLine[4]);
						maxVertexId = Integer.parseInt(splittedLine[5]);
						
						activeNodes = new int[maxVertexId];
						
						//for (int i = 0 ; i < nbNodes ; i++) {
						for (int i = 0 ; i < maxVertexId ; i++) { 
							edgeMatrix.add(new ArrayList<Integer>());
						}
					}
					
					else {
						//If we are reading edges
						if (idEdge < nbEdges) {
							edgesString.add(line);
						
							int u = Integer.parseInt(splittedLine[1]) - 1;
							int v = Integer.parseInt(splittedLine[2]) - 1;
						
							activeNodes[u] = 1;
							activeNodes[v] = 1;
							
							edgeMatrix.get(u).add(idEdge);
							edgeMatrix.get(v).add(idEdge);
						
							idEdge ++;
						} 
						
						//If we are reading hexagons
						else {
							hexagonsString.add(line);
						}
					}
				}
				
			}
			
			r.close();
			return new UndirGraph(nbNodes, nbEdges, nbHexagons, edgeMatrix, edgesString, hexagonsString, activeNodes);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public static UndirPonderateGraph parseUndirectedPonderateGraph(String path) {
		
		try {
			
			int nbNodes = 0, nbEdges = 0, maxVertexId = 0;
			
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line;
			boolean firstLine = true;
			
			ArrayList<ArrayList<Couple<Integer>>> edgeNodes = new ArrayList<ArrayList<Couple<Integer>>>();
			
			int nbAddedEdges = 0;
			
			while ((line = r.readLine()) != null) {
				
				String [] splittedLine = line.split(" ");
				
				if (!isCommentary(splittedLine)) {
					
					if (firstLine) {
						firstLine = false;
						nbNodes = Integer.parseInt(splittedLine[2]);
						nbEdges = Integer.parseInt(splittedLine[3]);
						
						if (splittedLine.length >= 6) {
							maxVertexId = Integer.parseInt(splittedLine[5]);
						} else {
							maxVertexId = nbNodes;
						}
						
						//for (int i = 0 ; i < nbNodes ; i++) {
						for (int i = 0 ; i < maxVertexId ; i++) {
							edgeNodes.add(new ArrayList<Couple<Integer>>());
						}
					}
					
					else if (nbAddedEdges < nbEdges){
						int u = Integer.parseInt(splittedLine[1]) - 1;
						int v = Integer.parseInt(splittedLine[2]) - 1;
						int w = Integer.parseInt(splittedLine[3]);
						
						edgeNodes.get(u).add(new Couple<Integer>(v, w));
						edgeNodes.get(v).add(new Couple<Integer>(u, w));

						nbAddedEdges ++;
					}
				}
				
			}
			
			r.close();
			return new UndirPonderateGraph(nbNodes, nbEdges, edgeNodes);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public static void exportSolutionToPonderateGraph(String path, UndirGraph initialGraph, int [] edgesValues) {
		
		int nbNodes = initialGraph.getNbNodes();
		int nbEdges = initialGraph.getNbEdges();
		int nbHexagons = initialGraph.getNbHexagons();
		int maxVertexId = initialGraph.getEdgeMatrix().size();
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(path)));
			
			w.write("c " + path + "\n");
			w.write("p DIMACS " + nbNodes + " " + nbEdges + " " + nbHexagons + " " + maxVertexId + "\n");
			
			for (int i = 0 ; i < nbEdges ; i++) {
				String edge = initialGraph.getEdgesString().get(i);
				w.write(edge + " " + edgesValues[i] + "\n");
			}
			
			for (int i = 0 ; i < nbHexagons ; i++) {
				w.write(initialGraph.getHexagonsString().get(i) + "\n");
			}
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
