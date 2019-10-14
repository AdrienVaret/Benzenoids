package benzenoids;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

public class BenzenoidsGenerator {
	
	private static int diameter;
	private static int nbCrowns;
	private static boolean nbHexaLimited;
	private static int nbHexa;
	
	private static int nbMolecules;
	
	static int getHexagonID(int x, int y) {
		return x + y * diameter;
	}
	
	public static void exportSolution(ArrayList<Solution> solutions, String outputFileName) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFileName)));
			
			for (Solution solution : solutions) {
				w.write(solution.toString() + "\n");
			}
			
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//arete z de l'hexagone (x,y)
	static int get(int x, int y, int z) {
		//0:hd, 1:d, 2:bd, 3:bg, 4:g, 5:hg
		if(z == 0 && y > 0)
			return (x + (y - 1) * diameter) * 6 + 3;
		else if(z == 4 && x > 0)
			return (x - 1 + y * diameter) * 6 + 1;
		else if (z == 5 && x > 0 && y > 0)
			return (x - 1 + (y - 1) * diameter) * 6 + 2;
		else
			return (x + y * diameter) * 6 + z;
	}
	
	public static void generateMolecules() {
		
		nbMolecules = 0;
		
		String directoryName = "molecules/" + nbCrowns + "_crowns/";
		
		File directory1 = new File("molecules");
		directory1.mkdir();
		
		File directory = new File(directoryName);
		directory.mkdir();
		
		GraphModel model = new GraphModel("Benzenoides");

		// Graphe ---------------------------------------------
		UndirectedGraph GUB = new UndirectedGraph(model, diameter * diameter, SetType.BITSET, false);
		UndirectedGraph GLB = new UndirectedGraph(model, diameter * diameter, SetType.BITSET, false);

		int i, j;
		for(j = 0; j < nbCrowns; j++) {
			for(i = 0; i < nbCrowns; i++) {
				GUB.addNode(getHexagonID(i,j));
				GUB.addNode(getHexagonID(nbCrowns - 1 + i,nbCrowns - 1 + j));
			}
		}
		for(j = 0; j < nbCrowns - 2; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addNode(getHexagonID(nbCrowns - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < nbCrowns - 1; j++) {
			for(i = j; i < nbCrowns - 2 ; i++) {
				GUB.addNode(getHexagonID(i+1,nbCrowns - 1 + j+1));
			}
		}
		
		for(j = 0; j < nbCrowns - 1; j++) {
			for(i = 0; i < nbCrowns - 1; i++) {
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i+1,j));
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i,j+1));
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i+1,j+1));
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,nbCrowns - 1 + j), getHexagonID(nbCrowns - 1 + i+1,nbCrowns - 1 + j));
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,nbCrowns - 1 + j), getHexagonID(nbCrowns - 1 + i,nbCrowns - 1 + j+1));
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,nbCrowns - 1 + j), getHexagonID(nbCrowns - 1 + i+1,nbCrowns - 1 + j+1));
			}
		}
		for(j = 0; j < nbCrowns - 1; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,j), getHexagonID(nbCrowns - 1 + i,j + 1));
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,j), getHexagonID(nbCrowns - 1 + i+1,j+1));
				GUB.addEdge(getHexagonID(nbCrowns - 1 + i,j+1), getHexagonID(nbCrowns - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < nbCrowns - 1; j++) {
			for(i = j; i < nbCrowns - 1; i++) {
				GUB.addEdge(getHexagonID(i,nbCrowns - 1 + j), getHexagonID(i+1,nbCrowns - 1 + j));
				GUB.addEdge(getHexagonID(i,nbCrowns - 1 + j), getHexagonID(i+1,nbCrowns - 1 + j+1));
				GUB.addEdge(getHexagonID(i+1,nbCrowns - 1 + j), getHexagonID(i+1,nbCrowns - 1 + j+1));
			}
		}
		for(j = 0; j < nbCrowns - 1; j++) {
			GUB.addEdge(getHexagonID(diameter - 1,nbCrowns - 1 + j), getHexagonID(diameter - 1,nbCrowns + j));
			GUB.addEdge(getHexagonID(nbCrowns - 1 + j,diameter - 1), getHexagonID(nbCrowns + j,diameter - 1));
		}

		UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
		model.connected(subgraph).post();

		// Variables =======================================================================================================
		BoolVar[] h = model.nodeSetBool(subgraph);
		
		// Contraintes =====================================================================================================

		// au moins un hexa sur le bord du haut
		BoolVar[] bord = new BoolVar[nbCrowns];
		for(i = 0; i < nbCrowns; i++)
			bord[i] = h[getHexagonID(i, 0)];
		model.addClauses(LogOp.or(bord));

		// au moins un hexa sur le bord de gauche
		for(i = 0; i < nbCrowns; i++)
			bord[i] = h[getHexagonID(0, i)];
		model.addClauses(LogOp.or(bord));

		// le nb d'hexagones est égal à nbHexa (optionnel)
		if(nbHexaLimited)
			model.sum(h, "=", nbHexa).post();

		/* pas d'hexagone vide cerné d'hexagones pleins : (largeur - 2) * (hauteur - 2) clauses */
		for(j = 1 ; j < diameter - 1 ; j++)
			for(i = 1 ; i < diameter - 1 ; i++)
				model.addClauses(LogOp.implies(LogOp.and(h[getHexagonID(i-1,j-1)], h[getHexagonID(i,j-1)], h[getHexagonID(i+1,j)], h[getHexagonID(i+1,j+1)], h[getHexagonID(i,j+1)], h[getHexagonID(i-1,j)]), h[getHexagonID(i,j)]));

		// Symétries ----------------------------------------------------------------------------------------------------
		BoolVar y = model.boolVar();

		BoolVar yp1 = model.boolVar();
		// (1) miroir diag
		model.addClauses(LogOp.or(y));
		for(j = 0 ; j < nbCrowns - 1; j++)
			for(i = j + 1 ; i < nbCrowns + j ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[getHexagonID(i,j)], LogOp.nor(h[getHexagonID(j,i)])));
				if(j != nbCrowns - 2 || i != nbCrowns + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[getHexagonID(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[getHexagonID(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}
		
		for(j = nbCrowns - 1 ; j < diameter ; j++)
			for(i =  j + 1; i < diameter ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[getHexagonID(i,j)], LogOp.nor(h[getHexagonID(j,i)])));
				if(j != nbCrowns - 2 || i != nbCrowns + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[getHexagonID(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[getHexagonID(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}

		
		model.getSolver().setSearch(new IntStrategy(h, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();		
	
		//Generation des structures moléculaires 
		ArrayList<Solution> solutions = new ArrayList<Solution>();
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			String outputFileName = directoryName + "molecule_" + nbMolecules;
			@SuppressWarnings("unused")
			SolutionConverter converter = new SolutionConverter(solution, nbCrowns, diameter, outputFileName);
			nbMolecules ++;
			
			solutions.add(solution);
		}
		
		exportSolution(solutions, nbCrowns + "_crowns.graph");
		System.out.println(solutions.size() + " molecules generateds");
	}
	
	public static void displayUsage() {
		System.err.println("USAGE : java -jar $EXEC_NAME nbCrowns [nbHexagons]");
	}
	
	public static void main(String [] args) {
		
		nbCrowns = Integer.parseInt(args[0]);
		diameter = nbCrowns * 2 - 1;

		nbHexaLimited = args.length > 1;
		if(nbHexaLimited)
			nbHexa = Integer.parseInt(args[1]);
		else
			nbHexa = 0;

		System.out.print("Generating all molecules in " + nbCrowns + " crowns tiling");
		if (nbHexa != 0) {
			System.out.println(" with " + nbHexa + " hexagons...");
		} else {
			System.out.println("...");
		}
		
		generateMolecules();
		
	}
	
}
