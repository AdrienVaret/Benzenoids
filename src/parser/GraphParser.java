package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.chocosolver.solver.Solution;

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
			
			int nbNodes = 0, nbEdges = 0;
			
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line;
			boolean firstLine = true;
			
			ArrayList<String> edgesString = new ArrayList<String>();
			ArrayList<ArrayList<Integer>> edgeMatrix = new ArrayList<ArrayList<Integer>>();
			int idEdge = 0;
			
			while ((line = r.readLine()) != null) {
				
				String [] splittedLine = line.split(" ");
				
				if (!isCommentary(splittedLine)) {
					
					if (firstLine) {
						firstLine = false;
						nbNodes = Integer.parseInt(splittedLine[2]);
						nbEdges = Integer.parseInt(splittedLine[3]);
						
						for (int i = 0 ; i < nbNodes ; i++) {
							edgeMatrix.add(new ArrayList<Integer>());
						}
					}
					
					else {
						edgesString.add(line);
						
						int u = Integer.parseInt(splittedLine[1]) - 1;
						int v = Integer.parseInt(splittedLine[2]) - 1;
						
						edgeMatrix.get(u).add(idEdge);
						edgeMatrix.get(v).add(idEdge);
						
						idEdge ++;
					}
				}
				
			}
			
			r.close();
			return new UndirGraph(nbNodes, nbEdges, edgeMatrix, edgesString);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return null;
	}
	
	public static UndirPonderateGraph parseUndirectedPonderateGraph(String path) {
		
		try {
			
			int nbNodes = 0, nbEdges = 0;
			
			BufferedReader r = new BufferedReader(new FileReader(new File(path)));
			String line;
			boolean firstLine = true;
			
			ArrayList<ArrayList<Couple<Integer>>> edgeNodes = new ArrayList<ArrayList<Couple<Integer>>>();
			
			while ((line = r.readLine()) != null) {
				
				String [] splittedLine = line.split(" ");
				
				if (!isCommentary(splittedLine)) {
					
					if (firstLine) {
						firstLine = false;
						nbNodes = Integer.parseInt(splittedLine[2]);
						nbEdges = Integer.parseInt(splittedLine[3]);
						
						for (int i = 0 ; i < nbNodes ; i++) {
							edgeNodes.add(new ArrayList<Couple<Integer>>());
						}
					}
					
					else {
						int u = Integer.parseInt(splittedLine[1]) - 1;
						int v = Integer.parseInt(splittedLine[2]) - 1;
						int w = Integer.parseInt(splittedLine[3]);
						
						edgeNodes.get(u).add(new Couple<Integer>(v, w));
						edgeNodes.get(v).add(new Couple<Integer>(u, w));

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
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(path)));
			
			w.write("c " + path + "\n");
			w.write("p DIMACS " + nbNodes + " " + nbEdges + "\n");
			
			for (int i = 0 ; i < nbEdges ; i++) {
				String edge = initialGraph.getEdgesString().get(i);
				w.write(edge + " " + edgesValues[i] + "\n");
			}
			
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
