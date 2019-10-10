package benzenoids;

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

	private static int taille;
	private static int dimension;
	private static boolean nbHexaLimite;
	private static int nbHexa;
	
	static int getHexagonID(int x, int y) {
		return x + y * taille;
	}
	
	//arete z de l'hexagone (x,y)
	static int get(int x, int y, int z) {
		//0:hd, 1:d, 2:bd, 3:bg, 4:g, 5:hg
		if(z == 0 && y > 0)
			return (x + (y - 1) * taille) * 6 + 3;
		else if(z == 4 && x > 0)
			return (x - 1 + y * taille) * 6 + 1;
		else if (z == 5 && x > 0 && y > 0)
			return (x - 1 + (y - 1) * taille) * 6 + 2;
		else
			return (x + y * taille) * 6 + z;
	}
	
	public static void generateMolecules() {
		GraphModel model = new GraphModel("Benzenoides");

		// Graphe ---------------------------------------------
		UndirectedGraph GUB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);
		UndirectedGraph GLB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);

		int i, j;
		for(j = 0; j < dimension; j++) {
			for(i = 0; i < dimension; i++) {
				GUB.addNode(getHexagonID(i,j));
				GUB.addNode(getHexagonID(dimension - 1 + i,dimension - 1 + j));
			}
		}
		for(j = 0; j < dimension - 2; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addNode(getHexagonID(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 2 ; i++) {
				GUB.addNode(getHexagonID(i+1,dimension - 1 + j+1));
			}
		}
		
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < dimension - 1; i++) {
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i+1,j));
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i,j+1));
				GUB.addEdge(getHexagonID(i,j), getHexagonID(i+1,j+1));
				GUB.addEdge(getHexagonID(dimension - 1 + i,dimension - 1 + j), getHexagonID(dimension - 1 + i+1,dimension - 1 + j));
				GUB.addEdge(getHexagonID(dimension - 1 + i,dimension - 1 + j), getHexagonID(dimension - 1 + i,dimension - 1 + j+1));
				GUB.addEdge(getHexagonID(dimension - 1 + i,dimension - 1 + j), getHexagonID(dimension - 1 + i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addEdge(getHexagonID(dimension - 1 + i,j), getHexagonID(dimension - 1 + i,j + 1));
				GUB.addEdge(getHexagonID(dimension - 1 + i,j), getHexagonID(dimension - 1 + i+1,j+1));
				GUB.addEdge(getHexagonID(dimension - 1 + i,j+1), getHexagonID(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 1; i++) {
				GUB.addEdge(getHexagonID(i,dimension - 1 + j), getHexagonID(i+1,dimension - 1 + j));
				GUB.addEdge(getHexagonID(i,dimension - 1 + j), getHexagonID(i+1,dimension - 1 + j+1));
				GUB.addEdge(getHexagonID(i+1,dimension - 1 + j), getHexagonID(i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			GUB.addEdge(getHexagonID(taille - 1,dimension - 1 + j), getHexagonID(taille - 1,dimension + j));
			GUB.addEdge(getHexagonID(dimension - 1 + j,taille - 1), getHexagonID(dimension + j,taille - 1));
		}

		UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
		model.connected(subgraph).post();

		// Variables =======================================================================================================
		BoolVar[] h = model.nodeSetBool(subgraph);
		
		// Contraintes =====================================================================================================

		// au moins un hexa sur le bord du haut
		BoolVar[] bord = new BoolVar[dimension];
		for(i = 0; i < dimension; i++)
			bord[i] = h[getHexagonID(i, 0)];
		model.addClauses(LogOp.or(bord));

		// au moins un hexa sur le bord de gauche
		for(i = 0; i < dimension; i++)
			bord[i] = h[getHexagonID(0, i)];
		model.addClauses(LogOp.or(bord));

		// le nb d'hexagones est égal à nbHexa (optionnel)
		if(nbHexaLimite)
			model.sum(h, "=", nbHexa).post();

		/* pas d'hexagone vide cerné d'hexagones pleins : (largeur - 2) * (hauteur - 2) clauses */
		for(j = 1 ; j < taille - 1 ; j++)
			for(i = 1 ; i < taille - 1 ; i++)
				model.addClauses(LogOp.implies(LogOp.and(h[getHexagonID(i-1,j-1)], h[getHexagonID(i,j-1)], h[getHexagonID(i+1,j)], h[getHexagonID(i+1,j+1)], h[getHexagonID(i,j+1)], h[getHexagonID(i-1,j)]), h[getHexagonID(i,j)]));

		// Symétries ----------------------------------------------------------------------------------------------------
		BoolVar y = model.boolVar();

		BoolVar yp1 = model.boolVar();
		// (1) miroir diag
		model.addClauses(LogOp.or(y));
		for(j = 0 ; j < dimension - 1; j++)
			for(i = j + 1 ; i < dimension + j ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[getHexagonID(i,j)], LogOp.nor(h[getHexagonID(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[getHexagonID(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[getHexagonID(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}
		
		for(j = dimension - 1 ; j < taille ; j++)
			for(i =  j + 1; i < taille ; i++) {
				model.addClauses(LogOp.or(LogOp.nor(y), h[getHexagonID(i,j)], LogOp.nor(h[getHexagonID(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[getHexagonID(i,j)]));
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[getHexagonID(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}

		
		model.getSolver().setSearch(new IntStrategy(h, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();		
	
		//Generation des structures moléculaire 
		Stack<Solution> solutionStack = new Stack<Solution>();
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			System.out.println(solution);
			System.out.println("\n\n" + subgraph);
			SolutionConverter converter = new SolutionConverter(solution, dimension, taille);
			solutionStack.push(solution);
		}
		
		System.out.println(solutionStack.size());
	}
	
	public static void displayUsage() {
		System.err.println("USAGE : java -jar $EXEC_NAME nbCrowns [nbHexagons]");
	}
	
	public static void main(String [] args) {
		
		dimension = Integer.parseInt(args[0]);
		taille = dimension * 2 - 1;
		nbHexaLimite = args.length > 1;
		if(nbHexaLimite)
			nbHexa = Integer.parseInt(args[1]);
		else
			nbHexa = 0;

		generateMolecules();
		
	}
	
}
