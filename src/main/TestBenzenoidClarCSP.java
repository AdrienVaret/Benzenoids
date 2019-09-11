package main;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.*;
import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import static org.chocosolver.solver.search.strategy.Search.*;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.search.strategy.selectors.variables.*;

import java.util.ArrayList;
import java.util.Stack;

public class TestBenzenoidClarCSP {

	private static int taille;
	private static int dimension;


	static int xy2i(int x, int y) {
		return x + y * taille;
	}
	
	//arete z de l'hexagone (x,y)
	static int xyc2i(int x, int y, int z) {
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

	public static void main(String[] args) {
		ArrayList<Solution> sols = new ArrayList<Solution>();
		dimension = Integer.parseInt(args[0]);
		taille = dimension * 2 - 1;
		boolean nbHexaLimite = args.length > 1;
		//        boolean nbRondLimite = args.length > 2;
		int nbHexa;
		//        int nbRond;
		if(nbHexaLimite)
			nbHexa = Integer.parseInt(args[1]);
		else
			nbHexa = 0;
		//int nb_hexa = 1 + 3 * (dimension - 1) * dimension ; // hexa_i_j
		//        if(nbRondLimite)
		//        	nbRond = Integer.parseInt(args[2]);
		//        else
		//        	nbRond = 0;

		GraphModel model = new GraphModel("Benzenoides");

		// Graphe ---------------------------------------------
		UndirectedGraph GUB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);
		UndirectedGraph GLB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);

		int i, j;
		for(j = 0; j < dimension; j++) {
			for(i = 0; i < dimension; i++) {
				GUB.addNode(xy2i(i,j));
				GUB.addNode(xy2i(dimension - 1 + i,dimension - 1 + j));
			}
		}
		for(j = 0; j < dimension - 2; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addNode(xy2i(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 2 ; i++) {
				GUB.addNode(xy2i(i+1,dimension - 1 + j+1));
			}
		}
		//		System.out.println(GUB.getNodes());
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < dimension - 1; i++) {
				GUB.addEdge(xy2i(i,j), xy2i(i+1,j));
				GUB.addEdge(xy2i(i,j), xy2i(i,j+1));
				GUB.addEdge(xy2i(i,j), xy2i(i+1,j+1));
				GUB.addEdge(xy2i(dimension - 1 + i,dimension - 1 + j), xy2i(dimension - 1 + i+1,dimension - 1 + j));
				GUB.addEdge(xy2i(dimension - 1 + i,dimension - 1 + j), xy2i(dimension - 1 + i,dimension - 1 + j+1));
				GUB.addEdge(xy2i(dimension - 1 + i,dimension - 1 + j), xy2i(dimension - 1 + i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addEdge(xy2i(dimension - 1 + i,j), xy2i(dimension - 1 + i,j + 1));
				GUB.addEdge(xy2i(dimension - 1 + i,j), xy2i(dimension - 1 + i+1,j+1));
				GUB.addEdge(xy2i(dimension - 1 + i,j+1), xy2i(dimension - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			for(i = j; i < dimension - 1; i++) {
				GUB.addEdge(xy2i(i,dimension - 1 + j), xy2i(i+1,dimension - 1 + j));
				GUB.addEdge(xy2i(i,dimension - 1 + j), xy2i(i+1,dimension - 1 + j+1));
				GUB.addEdge(xy2i(i+1,dimension - 1 + j), xy2i(i+1,dimension - 1 + j+1));
			}
		}
		for(j = 0; j < dimension - 1; j++) {
			GUB.addEdge(xy2i(taille - 1,dimension - 1 + j), xy2i(taille - 1,dimension + j));
			GUB.addEdge(xy2i(dimension - 1 + j,taille - 1), xy2i(dimension + j,taille - 1));
		}

		UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
		model.connected(subgraph).post();

		// Variables =======================================================================================================
		BoolVar[] h = model.nodeSetBool(subgraph);
		
		// Contraintes =====================================================================================================

		// au moins un hexa sur le bord du haut
		BoolVar[] bord = new BoolVar[dimension];
		for(i = 0; i < dimension; i++)
			bord[i] = h[xy2i(i, 0)];
		model.addClauses(LogOp.or(bord));

		// au moins un hexa sur le bord de gauche
		for(i = 0; i < dimension; i++)
			bord[i] = h[xy2i(0, i)];
		model.addClauses(LogOp.or(bord));

		// le nb d'hexagones est égal à nbHexa (optionnel)
		if(nbHexaLimite)
			model.sum(h, "=", nbHexa).post();

		/* pas d'hexagone vide cerné d'hexagones pleins : (largeur - 2) * (hauteur - 2) clauses */
		for(j = 1 ; j < taille - 1 ; j++)
			for(i = 1 ; i < taille - 1 ; i++)
				model.addClauses(LogOp.implies(LogOp.and(h[xy2i(i-1,j-1)], h[xy2i(i,j-1)], h[xy2i(i+1,j)], h[xy2i(i+1,j+1)], h[xy2i(i,j+1)], h[xy2i(i-1,j)]), h[xy2i(i,j)]));

		// Symétries ----------------------------------------------------------------------------------------------------
		BoolVar y = model.boolVar();

		BoolVar yp1 = model.boolVar();
		// (1) miroir diag
		model.addClauses(LogOp.or(y));
		for(j = 0 ; j < dimension - 1; j++)
			for(i = j + 1 ; i < dimension + j ; i++) {
				//System.out.println("-" + y.getName() + " " + xy2i(i,j) + " -" + xy2i(j,i) + " 0");
				model.addClauses(LogOp.or(LogOp.nor(y), h[xy2i(i,j)], LogOp.nor(h[xy2i(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					//System.out.println((y.getName()+1) + " -" + y.getName() + " " + xy2i(i,j) + " 0");
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[xy2i(i,j)]));
					//System.out.println((y.getName()+1) + " -" + y.getName() + " -" + xy2i(j,i) + " 0");
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[xy2i(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}
		//System.out.println("#");
		for(j = dimension - 1 ; j < taille ; j++)
			for(i =  j + 1; i < taille ; i++) {
				//System.out.println("-" + y.getName() + " " + xy2i(i,j) + " -" + xy2i(j,i) + " 0");
				model.addClauses(LogOp.or(LogOp.nor(y), h[xy2i(i,j)], LogOp.nor(h[xy2i(j,i)])));
				if(j != dimension - 2 || i != dimension + j){
					//System.out.println((y.getName()+1) + " -" + y.getName() + " " + xy2i(i,j) + " 0");
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), h[xy2i(i,j)]));
					//System.out.println((y.getName()+1) + " -" + y.getName() + " -" + xy2i(j,i) + " 0");
					model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(h[xy2i(j,i)])));
				}
				y = yp1;
				yp1 = model.boolVar();
			}

		
		model.getSolver().setSearch(new IntStrategy(h, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();		
		//solver.showSolutions();

		//Generation des structures moléculaire 
		Stack<Solution> solutionStack = new Stack<Solution>();
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			solutionStack.push(solution);
		}
		
		System.out.println(solutionStack.size());
		
		//rond + doubles liaisons
		for(Solution solution: solutionStack) {
			Model clarModel = new Model("Clar");
			
			//hexa = h
			BoolVar [] hexa = new BoolVar[h.length];
			for(i = 0 ; i < hexa.length ; i++) {
				hexa[i] = clarModel.boolVar("nodes[" + i + "]");
				clarModel.arithm(hexa[i], "=", solution.getIntVal(h[i])).post();
			}
			
			
			BoolVar[] ronds = new BoolVar[taille * taille];
			for(i = 0 ; i < ronds.length ; i++)
				ronds[i] = clarModel.boolVar("rond" + i);
			
			
			BoolVar[] liaisons = new BoolVar[taille * taille * 6];
			for(i = 0 ; i < liaisons.length ; i++) {
				liaisons[i] = clarModel.boolVar("liaison" + i / 6 + ":" + i % 6);
				if(i != xyc2i((i/6) % taille, i / (taille * 6), i % 6))
					clarModel.arithm(liaisons[i], "=", 0).post();
			}

			//max rond = somme des ronds, OBJ: maximiser (max rond = 0, pas de ronds)
			//IntVar maxRonds = clarModel.intVar("MaxRonds", 0, nbHexa);
			//clarModel.sum(ronds,  "=", maxRonds).post();
			clarModel.sum(ronds, "=", 0).post();
			//clarModel.setObjective(Model.MAXIMIZE, maxRonds);

			
			// rond implique hexa
			for(j = 0 ; j < taille ; j++)
				for(i = 0 ; i < taille ; i++)
					clarModel.addClauses(LogOp.implies(ronds[xy2i(i,j)], hexa[xy2i(i,j)]));         	


			// Condition d'existence d'une double liaison (i = abscisse, j = ord)
			
			//haut droit
			for(j = 1; j <  taille; j++) 
				for(i = 0; i < taille; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,0)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i,j-1)])));
			for(i = 0; i < taille; i++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,0,0)], hexa[xy2i(i,0)]));
			
			//droit
			for(j = 0; j <  taille; j++) 
				for(i = 0; i < taille - 1; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,1)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i+1,j)])));
			for(j = 0; j < taille; j++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(taille - 1,j,1)], hexa[xy2i(taille - 1,j)]));
			
			//bas droit
			for(j = 0; j <  taille - 1; j++) 
				for(i = 0; i < taille - 1; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,2)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i+1,j+1)])));
			for(i = 0; i < taille; i++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,taille - 1,2)], hexa[xy2i(i,taille - 1)]));
			for(j = 0; j < taille - 1; j++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(taille - 1,j,2)], hexa[xy2i(taille - 1,j)]));
			
			
			//bas gauche
			for(j = 0; j <  taille - 1; j++) 
				for(i = 0; i < taille; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,3)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i,j+1)])));
			for(i = 0; i < taille; i++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,taille - 1,3)], hexa[xy2i(i,taille - 1)]));
			
			//gauche
			for(j = 0; j <  taille; j++) 
				for(i = 1; i < taille; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,4)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i-1,j)])));
			for(j = 0; j < taille; j++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(0,j,4)], hexa[xy2i(0,j)]));
			
			//haut gauche
			for(j = 1; j <  taille ; j++) 
				for(i = 1; i < taille ; i++) 
					clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,j,5)], LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i-1,j-1)])));
			for(i = 0; i < taille ; i++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(i,0,5)], hexa[xy2i(i,0)]));
			for(j = 1; j < taille ; j++)
				clarModel.addClauses(LogOp.implies(liaisons[xyc2i(0,j,5)], hexa[xy2i(0,j)]));


			// contraintes ronds-doubles liaisons
			BoolVar [] somme6 = new BoolVar[6];
			BoolVar [] somme5 = new BoolVar[5];
			BoolVar [] somme3 = new BoolVar[3];

			for(j = 0; j < taille - 1; j++) {
				for(i = 0; i < taille - 1; i++) {
					somme6[0] = ronds[xy2i(i,j)]; somme6[1] = ronds[xy2i(i,j+1)]; somme6[2] = ronds[xy2i(i+1,j+1)];
					somme6[3] = liaisons[xyc2i(i,j,3)]; somme6[4] = liaisons[xyc2i(i,j,2)]; somme6[5] = liaisons[xyc2i(i,j+1,1)];
					clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i,j+1)], hexa[xy2i(i+1,j+1)]), clarModel.sum(somme6, "=", 1).reify()));					
					somme6[0] = ronds[xy2i(i,j)]; somme6[1] = ronds[xy2i(i+1,j)]; somme6[2] = ronds[xy2i(i+1,j+1)];
					somme6[3] = liaisons[xyc2i(i,j,1)]; somme6[4] = liaisons[xyc2i(i,j,2)]; somme6[5] = liaisons[xyc2i(i+1,j,3)];
					clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(i,j)], hexa[xy2i(i+1,j)], hexa[xy2i(i+1,j+1)]), clarModel.sum(somme6, "=", 1).reify()));					
				}
			}

			for(i = 0; i < taille ; i++) {
				somme3[0] = ronds[xy2i(i,0)]; somme3[1] = liaisons[xyc2i(i,0,0)]; somme3[2] = liaisons[xyc2i(i,0,5)];
				clarModel.addClauses(LogOp.implies(hexa[xy2i(i,0)], clarModel.sum(somme3, "=", 1).reify()));
			}

			for(i = 0; i < taille ; i++) {
				somme3[0] = ronds[xy2i(i,taille - 1)]; somme3[1] = liaisons[xyc2i(i,taille - 1,2)]; somme3[2] = liaisons[xyc2i(i,taille - 1,3)];
				clarModel.addClauses(LogOp.implies(hexa[xy2i(i, taille - 1)], clarModel.sum(somme3, "=", 1).reify()));
			}
			for(j = 0; j < taille ; j++) {
				somme3[0] = ronds[xy2i(0,j)]; somme3[1] = liaisons[xyc2i(0,j,4)]; somme3[2] = liaisons[xyc2i(0,j,5)];
				clarModel.addClauses(LogOp.implies(hexa[xy2i(0, j)], clarModel.sum(somme3, "=", 1).reify()));
			}
			for(j = 0; j < taille ; j++) {
				somme3[0] = ronds[xy2i(taille - 1,j)]; somme3[1] = liaisons[xyc2i(taille - 1,j,1)]; somme3[2] = liaisons[xyc2i(taille - 1,j,2)];
				clarModel.addClauses(LogOp.implies(hexa[xy2i(taille - 1, j)], clarModel.sum(somme3, "=", 1).reify()));
			}
			for(i = 0; i < taille - 1; i++) {
				somme5[0] = ronds[xy2i(i,0)]; somme5[1] = ronds[xy2i(i+1,0)];
				somme5[2] = liaisons[xyc2i(i,0,0)]; somme5[3] = liaisons[xyc2i(i,0,1)]; somme5[4] = liaisons[xyc2i(i+1,0,5)];
				clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(i,0)], hexa[xy2i(i+1,0)]), clarModel.sum(somme5, "=", 1).reify()));					
			}
			for(i = 0; i < taille - 1; i++) {
				somme5[0] = ronds[xy2i(i,taille - 1)]; somme5[1] = ronds[xy2i(i+1,taille - 1)];
				somme5[2] = liaisons[xyc2i(i,taille - 1,1)]; somme5[3] = liaisons[xyc2i(i,taille - 1,2)]; somme5[4] = liaisons[xyc2i(i+1,taille - 1,3)];
				clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(i,taille - 1)], hexa[xy2i(i+1,taille - 1)]), clarModel.sum(somme5, "=", 1).reify()));					
			}
			for(j = 0; j < taille - 1 ; j++) {
				somme5[0] = ronds[xy2i(0,j)]; somme5[1] = ronds[xy2i(0,j+1)];
				somme5[2] = liaisons[xyc2i(0,j,3)]; somme5[3] = liaisons[xyc2i(0,j,4)]; somme5[4] = liaisons[xyc2i(0,j+1,5)];
				clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(0,j)], hexa[xy2i(0,j+1)]), clarModel.sum(somme5, "=", 1).reify()));					
			}
			for(j = 0; j < taille - 1; j++) {
				somme5[0] = ronds[xy2i(taille - 1,j)]; somme5[1] = ronds[xy2i(taille - 1,j+1)];
				somme5[2] = liaisons[xyc2i(taille - 1,j,2)]; somme5[3] = liaisons[xyc2i(taille - 1,j,3)]; somme5[4] = liaisons[xyc2i(taille - 1,j+1,1)];
				clarModel.addClauses(LogOp.implies(LogOp.or(hexa[xy2i(taille - 1,j)], hexa[xy2i(taille - 1,j+1)]), clarModel.sum(somme5, "=", 1).reify()));					
			}
			somme3[0] = ronds[xy2i(0,taille - 1)]; somme3[1] = liaisons[xyc2i(0,taille - 1,3)]; somme3[2] = liaisons[xyc2i(0,taille - 1,4)];
			clarModel.addClauses(LogOp.implies(hexa[xy2i(0, taille - 1)], clarModel.sum(somme3, "=", 1).reify()));

			somme3[0] = ronds[xy2i(taille - 1,0)]; somme3[1] = liaisons[xyc2i(taille - 1,j,0)]; somme3[2] = liaisons[xyc2i(taille - 1,0,1)];
			clarModel.addClauses(LogOp.implies(hexa[xy2i(taille - 1,0)], clarModel.sum(somme3, "=", 1).reify()));

			Solver clarSolver = clarModel.getSolver();		
			//clarSolver.showSolutions();
			
			
			
			boolean found = false;
			Solution clarSolution = new Solution(clarModel);
			while(clarSolver.solve()) {
				found = true;
				clarSolution.record();
				sols.add(clarSolution);
			}
			//System.out.print("\t");
			
			System.out.println(sols.size());
			System.out.print("");
			//if(found)
				//System.out.println(clarSolution);
			//else
			//	System.out.println(solution);
		}
		
		System.out.println(sols.size());
		
		//solver.findAllSolutions();
//		System.out.println("Fini");
	}
}
