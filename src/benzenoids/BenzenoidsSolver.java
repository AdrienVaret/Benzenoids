package benzenoids;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.impl.IntervalIntVarImpl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.DirectedGraphVar;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import graphs.DirectedEdge;
import graphs.UndirGraph;
import graphs.UndirPonderateGraph;
import parser.GraphParser;
import utils.Couple;

import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;

public class BenzenoidsSolver {

	//Edges's direction constants
	public static final int HIGH_RIGHT = 0;
	public static final int RIGHT = 1;
	public static final int LOW_RIGHT = 2;
	public static final int LOW_LEFT = 3;
	public static final int LEFT = 4;
	public static final int HIGHT_LEFT = 5;
	
	//Solutions lists
	private static ArrayList<Solution> molecules = new ArrayList<Solution>();
	
	//Problem's attributes
	public static int dimension;
	public static int taille;
	public static int nbHexa;
	public static boolean nbHexaLimite;
	
	private static BoolVar[] h;
	
	private static int[] cycles;
	
	public static void generateMolecules() {
		GraphModel model = new GraphModel("Benzenoides");

		UndirectedGraph GUB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);
		UndirectedGraph GLB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);

		int i, j;
		for(j = 0; j < dimension; j++) {
			for(i = 0; i < dimension; i++) {
				GUB.addNode(Utils.getHexagonId(i,j));
				GUB.addNode(Utils.getHexagonId(dimension - 1 + i,dimension - 1 + j));
			}
		}
		for(j = 0; j < dimension - 2; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addNode(Utils.getHexagonId(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 2 ; i++) {
				GUB.addNode(Utils.getHexagonId(i+1,dimension - 1 + j+1));
			}
		}
		//		System.out.println(GUB.getNodes());
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < dimension - 1; i++) {
				GUB.addEdge(Utils.getHexagonId(i,j), Utils.getHexagonId(i+1,j));
				GUB.addEdge(Utils.getHexagonId(i,j), Utils.getHexagonId(i,j+1));
				GUB.addEdge(Utils.getHexagonId(i,j), Utils.getHexagonId(i+1,j+1));
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,dimension - 1 + j), Utils.getHexagonId(dimension - 1 + i+1,dimension - 1 + j));
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,dimension - 1 + j), Utils.getHexagonId(dimension - 1 + i,dimension - 1 + j+1));
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,dimension - 1 + j), Utils.getHexagonId(dimension - 1 + i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,j), Utils.getHexagonId(dimension - 1 + i,j + 1));
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,j), Utils.getHexagonId(dimension - 1 + i+1,j+1));
				GUB.addEdge(Utils.getHexagonId(dimension - 1 + i,j+1), Utils.getHexagonId(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 1; i++) {
				GUB.addEdge(Utils.getHexagonId(i,dimension - 1 + j), Utils.getHexagonId(i+1,dimension - 1 + j));
				GUB.addEdge(Utils.getHexagonId(i,dimension - 1 + j), Utils.getHexagonId(i+1,dimension - 1 + j+1));
				GUB.addEdge(Utils.getHexagonId(i+1,dimension - 1 + j), Utils.getHexagonId(i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			GUB.addEdge(Utils.getHexagonId(taille - 1,dimension - 1 + j), Utils.getHexagonId(taille - 1,dimension + j));
			GUB.addEdge(Utils.getHexagonId(dimension - 1 + j,taille - 1), Utils.getHexagonId(dimension + j,taille - 1));
		}

		UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
		model.connected(subgraph).post();

		// Variables =======================================================================================================
		h = model.nodeSetBool(subgraph);
		
		// Contraintes =====================================================================================================

		// au moins un hexa sur le bord du haut
		BoolVar[] bord = new BoolVar[dimension];
		for(i = 0; i < dimension; i++)
			bord[i] = h[Utils.getHexagonId(i, 0)];
		model.addClauses(LogOp.or(bord));

		// au moins un hexa sur le bord de gauche
		for(i = 0; i < dimension; i++)
			bord[i] = h[Utils.getHexagonId(0, i)];
		model.addClauses(LogOp.or(bord));

		// le nb d'hexagones est égal à nbHexa (optionnel)
		if(nbHexaLimite)
			model.sum(h, "=", nbHexa).post();

		/* pas d'hexagone vide cerné d'hexagones pleins : (largeur - 2) * (hauteur - 2) clauses */
		for(j = 1 ; j < taille - 1 ; j++)
			for(i = 1 ; i < taille - 1 ; i++)
				model.addClauses(LogOp.implies(LogOp.and(h[Utils.getHexagonId(i-1,j-1)], h[Utils.getHexagonId(i,j-1)], h[Utils.getHexagonId(i+1,j)], h[Utils.getHexagonId(i+1,j+1)], h[Utils.getHexagonId(i,j+1)], h[Utils.getHexagonId(i-1,j)]), h[Utils.getHexagonId(i,j)]));

		// Symétries ----------------------------------------------------------------------------------------------------
		BoolVar y = model.boolVar();

		BoolVar yp1 = model.boolVar();
		model.addClauses(LogOp.or(y));
		for(j = 0 ; j < dimension - 1; j++)
			for(i = j + 1 ; i < dimension + j ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[Utils.getHexagonId(i,j)], LogOp.nor(h[Utils.getHexagonId(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[Utils.getHexagonId(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[Utils.getHexagonId(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}
	
		for(j = dimension - 1 ; j < taille ; j++)
			for(i =  j + 1; i < taille ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[Utils.getHexagonId(i,j)], LogOp.nor(h[Utils.getHexagonId(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[Utils.getHexagonId(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[Utils.getHexagonId(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}

		
		model.getSolver().setSearch(new IntStrategy(h, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();		
		//solver.showSolutions();

		//Generation des structures moléculaire 
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			molecules.add(solution);
		}
	}
	
	public static String getName(String path) {
		String [] splittedPath = path.split(Pattern.quote("."));
		String [] name = splittedPath[0].split(Pattern.quote("/"));
		return name[name.length-1];
	}
	
	public static ArrayList<String> generateLewisStructures(String path, String outputDirectory) {
		
		UndirGraph graph = GraphParser.parseUndirectedGraph(path);
		Model model = new Model("Lewis Structures");
		
		cycles = new int[graph.getNbEdges() + 1];
		
		String name = getName(path);
		
		BoolVar [] edges = new BoolVar[graph.getNbEdges()];
		
		for (int i = 0 ; i < graph.getNbEdges() ; i++) {
			edges[i] = model.boolVar("edge " + (i+1));
		}
		
		for (int i = 0 ; i < graph.getNbNodes() ; i++) {
			int nbAdjacentEdges = graph.getEdgeMatrix().get(i).size();
			BoolVar [] adjacentEdges = new BoolVar[nbAdjacentEdges];
			
			for (int j = 0 ; j < nbAdjacentEdges ; j++) {
				adjacentEdges[j] = edges[graph.getEdgeMatrix().get(i).get(j)];
			}
			
			model.sum(adjacentEdges, "=", 1).post();
		}
			
		model.getSolver().setSearch(new IntStrategy(edges, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();
		
		ArrayList<String> paths = new ArrayList<String>();
		
		int i = 0;
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			
			int [] edgesValues = new int [graph.getNbEdges()];
			
			for (int j = 0 ; j < graph.getNbEdges() ; j++) {
				edgesValues[j] = solution.getIntVal(edges[j]);
			}
			
			String filename = outputDirectory + "/" + name + "_" + i + ".graph";
			GraphParser.exportSolutionToPonderateGraph(filename, graph, edgesValues);
			paths.add(filename);
			
			i++;
		}
		
		return paths;
	}
	
	public static void exportGraph(DirectedGraphVar g, String directory, String name) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(directory + "/" + name)));
			w.write(g.graphVizExport());
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void computeCycles(String path, String outputDirectory) {
		
		UndirPonderateGraph graph = GraphParser.parseUndirectedPonderateGraph(path);
		
		int nbNode = graph.getNbNodes();
		
		int [] nodesSet = new int[nbNode];
		int [] visitedNodes = new int[nbNode];
		
		int deep = 0;
		int n = 0;
		
		ArrayList<Integer> q = new ArrayList<Integer>();
		q.add(0);
		visitedNodes[0] = 1;
		
		int count = 1;
	
		
		//Récupérer l'ensemble des "atomes étoilés"
		while (n < nbNode / 2) {
			
			int newCount = 0;
			
			for (int i = 0 ; i < count ; i++) {
				
				int u = q.get(0);
				
				if (deep % 2 == 0) {
					nodesSet[u] = 1;
					n ++;
				}
				
				for (Couple<Integer> couple : graph.getNodesMatrix().get(u)) {
					int v = couple.getX();
					
					if (visitedNodes[v] == 0) {
						visitedNodes[v] = 1;
						q.add(v);
						newCount ++;
					}
				}	
				q.remove(0);
			}
			deep ++;
			count = newCount;
		}
		
		//Récupérer l'ensemble des couples d'arêtes alternantes
		ArrayList<DirectedEdge> edges = new ArrayList<DirectedEdge>();
		for (int u = 0 ; u < nbNode ; u++) {
			if (nodesSet[u] == 1) {
				
				for (Couple<Integer> couple : graph.getNodesMatrix().get(u)) { //Couples (sommet, liaison)
					if (couple.getY() == 0) {
						int intermediarNode = couple.getX();
						
						for (Couple<Integer> couple2 : graph.getNodesMatrix().get(intermediarNode)) {
							int v = couple2.getX();
							if (nodesSet[v] == 1 && couple2.getY() == 1) {
								edges.add(new DirectedEdge(u, v, intermediarNode));
							}
						}
					}
				}
				
			}
		}
		
		//Créer le problème
		GraphModel model = new GraphModel("Alternant Cycles");
		
		DirectedGraph GLB = new DirectedGraph(model, nbNode, SetType.BITSET, false);
		DirectedGraph GUB = new DirectedGraph(model, nbNode, SetType.BITSET, false);
		
		for (int i = 0 ; i < nbNode ; i ++) {
			if (nodesSet[i] == 1) {
				GUB.addNode(i);
			}
		}
		
		for (DirectedEdge edge : edges) {
			GUB.addArc(edge.getU(), edge.getV());
		}
		
		DirectedGraphVar g = model.digraphVar("g", GLB, GUB);
		model.stronglyConnected(g).post();
		model.maxOutDegrees(g, 1).post();
		model.arithm(model.nbNodes(g), ">", 1).post();
		
		Solver solver = model.getSolver();
		
		//Résoudre le problème et stocker les résultats
		int i = 0;
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			
			System.out.println(g);
			
			exportGraph(g, outputDirectory, "cycle_" + i + ".dot");
			
			int nbNodesSolution = g.getMandatoryNodes().size() * 2;
			cycles[nbNodesSolution] ++;
			
			i ++;
		}
	}
	
	public static void displayCycles(String directory) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(directory + "/cycles.txt")));
			for (int i = 0 ; i < cycles.length ; i++) {
				if (cycles[i] > 0)
					w.write(cycles[i] + " cycles of " + i + " edges \n");
			}
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void analyzeMolecule(String filename){
		
		String [] splittedPath = filename.split(Pattern.quote("."));
		String [] splittedPath2 = splittedPath[0].split(Pattern.quote("/"));
		
		
		String rootDirectoryName = "";
		for (int i = 0 ; i < splittedPath2.length - 1 ; i++) {
			rootDirectoryName += splittedPath2[i] + "/";
		}
		
		String lewisDirectoryName = rootDirectoryName + "lewis";
		
		File lewisDirectory = new File(lewisDirectoryName);
		lewisDirectory.mkdir();
		
		ArrayList<String> lewisStructures = generateLewisStructures(filename, lewisDirectoryName);
		
		for (String lewisStructure : lewisStructures) {
			String structureDirectoryName = lewisStructure.split(Pattern.quote("."))[0];
			File structureDirectory = new File(structureDirectoryName);
			structureDirectory.mkdir();
			computeCycles(lewisStructure, structureDirectoryName);
		}
		
		displayCycles(rootDirectoryName);
		
	}
	
	public static void displayUsage() {
		System.err.println("USAGE");
		System.exit(1);
	}
	
	public static void main(String [] args) {
		//analyzeMolecule("molecules/benzene/benzene.graph");
		analyzeMolecule("molecules/phenanthrene/phenanthrene.graph");
 	}
}
