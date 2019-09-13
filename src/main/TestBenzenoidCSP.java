package main;

import org.chocosolver.solver.*;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.*;
import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.GraphVar;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;

public class TestBenzenoidCSP {
    
    private static int taille;
    private static int nbCouronne;
    
    
    static int hexa(int x, int y) {
        return x + y * taille;
    }
    
	public static void main(String[] args) {
        nbCouronne = Integer.parseInt(args[0]);
        taille = nbCouronne * 2 - 1;
        boolean nbHexaLimite = args.length > 1;
        int nbHexa;
        if(nbHexaLimite)
        	nbHexa = Integer.parseInt(args[1]);
        else
        	nbHexa = 0;
        int nb_hexa = 1 + 3 * (nbCouronne - 1) * nbCouronne ; // hexa_i_j

		GraphModel model = new GraphModel("Benzenoides");
        
		// Graphe ---------------------------------------------
		UndirectedGraph GUB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);
		UndirectedGraph GLB = new UndirectedGraph(model, taille * taille, SetType.BITSET, false);

		int i, j;
		for(j = 0; j < nbCouronne; j++) {
			for(i = 0; i < nbCouronne; i++) {
				GUB.addNode(hexa(i,j));
				GUB.addNode(hexa(nbCouronne - 1 + i,nbCouronne - 1 + j));
			}
		}
		for(j = 0; j < nbCouronne - 2; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addNode(hexa(nbCouronne - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < nbCouronne - 1; j++) {
			for(i = j; i < nbCouronne - 2 ; i++) {
				GUB.addNode(hexa(i+1,nbCouronne - 1 + j+1));
			}
		}
		System.out.println(GUB.getNodes());
		for(j = 0; j < nbCouronne - 1; j++) {
			for(i = 0; i < nbCouronne - 1; i++) {
				GUB.addEdge(hexa(i,j), hexa(i+1,j));
				GUB.addEdge(hexa(i,j), hexa(i,j+1));
				GUB.addEdge(hexa(i,j), hexa(i+1,j+1));
				GUB.addEdge(hexa(nbCouronne - 1 + i,nbCouronne - 1 + j), hexa(nbCouronne - 1 + i+1,nbCouronne - 1 + j));
				GUB.addEdge(hexa(nbCouronne - 1 + i,nbCouronne - 1 + j), hexa(nbCouronne - 1 + i,nbCouronne - 1 + j+1));
				GUB.addEdge(hexa(nbCouronne - 1 + i,nbCouronne - 1 + j), hexa(nbCouronne - 1 + i+1,nbCouronne - 1 + j+1));
			}
		}
		for(j = 0; j < nbCouronne - 1; j++) {
			for(i = 0; i < j + 1; i++) {
				GUB.addEdge(hexa(nbCouronne - 1 + i,j), hexa(nbCouronne - 1 + i,j + 1));
				GUB.addEdge(hexa(nbCouronne - 1 + i,j), hexa(nbCouronne - 1 + i+1,j+1));
				GUB.addEdge(hexa(nbCouronne - 1 + i,j+1), hexa(nbCouronne - 1 + i+1,j+1));
			}
		}
		for(j = 0; j < nbCouronne - 1; j++) {
			for(i = j; i < nbCouronne - 1; i++) {
				GUB.addEdge(hexa(i,nbCouronne - 1 + j), hexa(i+1,nbCouronne - 1 + j));
				GUB.addEdge(hexa(i,nbCouronne - 1 + j), hexa(i+1,nbCouronne - 1 + j+1));
				GUB.addEdge(hexa(i+1,nbCouronne - 1 + j), hexa(i+1,nbCouronne - 1 + j+1));
			}
		}
		for(j = 0; j < nbCouronne - 1; j++) {
			GUB.addEdge(hexa(taille - 1,nbCouronne - 1 + j), hexa(taille - 1,nbCouronne + j));
			GUB.addEdge(hexa(nbCouronne - 1 + j,taille - 1), hexa(nbCouronne + j,taille - 1));
		}
  		//for(int e:GUB.getNodes())
		//	System.out.println(e + ":" + GUB.getNeighOf(e));
//		System.exit(0);
		
  		

  		//UndirectedGraphVar gub = model.graphVar("gr", GUB, GUB);
  		//System.out.println(gub.graphVizExport());
		
		UndirectedGraphVar subgraph = model.graphVar("g", GLB, GUB);
		//Connexe
		model.connected(subgraph).post();

		// Variables ==============================================
		// tableau bool (existence des noeuds)
		BoolVar[] b = model.nodeSetBool(subgraph);

        // Contraintes ============================================
		BoolVar[] bord = new BoolVar[nbCouronne];
		
		// au moins un hexa sur le bord du haut
		for(i = 0; i < nbCouronne; i++)
			bord[i] = b[hexa(i, 0)];
		//OU sur les variables bool du tableau bord
		model.addClauses(LogOp.or(bord));
		
		
		// au moins un hexa sur le bord de gauche
		for(i = 0; i < nbCouronne; i++)
			bord[i] = b[hexa(0, i)];
		model.addClauses(LogOp.or(bord));
		
		
		// le nb d'hexagones est égal à nbHexa (optionnel)
		if(nbHexaLimite)
			model.sum(b, "=", nbHexa).post();
       
        /* pas d'hexagone vide cerné d'hexagones pleins : (largeur - 2) * (hauteur - 2) clauses */
        for(j = 1 ; j < taille - 1 ; j++)
            for(i = 1 ; i < taille - 1 ; i++)
                model.addClauses(LogOp.implies(LogOp.and(b[hexa(i-1,j-1)], b[hexa(i,j-1)], b[hexa(i+1,j)], b[hexa(i+1,j+1)], b[hexa(i,j+1)], b[hexa(i-1,j)]), b[hexa(i,j)]));

        // Symétries -------------------------------------------
        BoolVar y = model.boolVar();
        BoolVar yp1 = model.boolVar();
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < dimension - 1; j++)
//            for(i = 0 ; i < dimension + j ; i++) {
//                System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
//        System.out.println("#");
//        for(j = dimension - 1 ; j < taille ; j++)
//            for(i =  j - dimension + 1; i < taille ; i++) {
//                System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
        // (1) miroir diag
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < dimension - 1; j++)
//            for(i = j + 1 ; i < dimension + j ; i++) {
//                //System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
//        //System.out.println("#");
//        for(j = dimension - 1 ; j < taille ; j++)
//            for(i =  j + 1; i < taille ; i++) {
//                //System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
        // miroir diag
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < dimension - 1; j++)
//            for(i = j + 1 ; i < dimension + j ; i++) {
//                System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
//        System.out.println("#");
//        for(j = dimension - 1 ; j < taille ; j++)
//            for(i =  j + 1; i < taille ; i++) {
//                //System.out.println("-" + y.getName() + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//                if(j != dimension - 2 || i != dimension + j){
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                    //System.out.println((y.getName()+1) + " -" + y.getName() + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), LogOp.nor(b[hexa(j,i)])));
//                }
//                y = yp1;
//                yp1 = model.boolVar();
//            }
	
//        // miroir hg
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < taille ; j++)
//        	for(i = j + 1 ; i < taille - j ; i++) {
//        		//System.out.println("-" + y + " " + hexa(i,j) + " -" + hexa(j,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,i)])));
//        		if(j != taille - 1 || i != taille - j - 1){
//        			//System.out.println((y+1) + " -" + y + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//                	//System.out.println((y+1) + " -" + y + " -" + hexa(j,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y),  LogOp.nor(b[hexa(j,i)])));
//                	//**System.out.println("-" + (y+1) + " " + y + " 0");
//        			//**System.out.println("-" + (y+1) + " -" + hexa(i,j) + " " + hexa(j,i) + " 0");
//        		}
//                y = yp1;
//                yp1 = model.boolVar();
//        	}
//        // miroir bg
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < taille - 1; j++)
//        	for(i = 0 ; i < (taille - j) / 2 ; i++) {
//        		//System.out.println("-" + y + " " + hexa(i,j) + " -" + hexa(taille-i-j-1,j) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(taille-i-j-1,j)])));
//        		if(j != taille - 2 || i != (taille - j) / 2 - 1){
//        			//System.out.println((y+1) + " -" + y + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//        			//System.out.println((y+1) + " -" + y + " -" + hexa(taille-i-j-1,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y),  LogOp.nor(b[hexa(taille-i-j-1,j)])));
//        			//**System.out.println("-" + (y+1) + " " + y + " 0");
//        			//**System.out.println("-" + (y+1) + " -" + hexa(i,j) + " " + hexa(taille-i-j-1,j) + " 0");
//        		}
//                y = yp1;
//                yp1 = model.boolVar();
//        	}
//        // rot 240
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < taille ; j++)
//        	for(i = 0 ; i < taille - j; i++) {
//        		//System.out.println("-" + y + " " + hexa(i,j) + " -" + hexa(j,taille-i-j-1) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(j,taille-i-j-1)])));
//        		if(j != taille - 1 || i != taille - j - 1){
//        			//System.out.println((y+1) + " -" + y + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//        			//System.out.println((y+1) + " -" + y + " -" + hexa(j,taille-i-j-1) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y),  LogOp.nor(b[hexa(j,taille-i-j-1)])));
//        			//**System.out.println("-" + (y+1) + " " + y + " 0");
//        			//**System.out.println("-" + (y+1) + " -" + hexa(i,j) + " " + hexa(j,taille-i-j-1) + " 0");
//        		}
//                y = yp1;
//                yp1 = model.boolVar();
//        	}
//        // rot 120
//        model.addClauses(LogOp.or(y));
//        for(j = 0 ; j < taille ; j++)
//        	for(i = 0 ; i < taille - j ; i++) {
//        		//System.out.println("-" + y + " " + hexa(i,j) + " -" + hexa(taille-i-j-1,i) + " 0");
//                model.addClauses(LogOp.or(LogOp.nor(y), b[hexa(i,j)], LogOp.nor(b[hexa(taille-i-j-1,i)])));
//        		if(j != taille - 1 || i != taille - j - 1){
//        			//System.out.println((y+1) + " -" + y + " " + hexa(i,j) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(i,j)]));
//        			//System.out.println((y+1) + " -" + y + " -" + hexa(taille-i-j-1,i) + " 0");
//                	model.addClauses(LogOp.or(yp1, LogOp.nor(y), b[hexa(taille-i-j-1,i)]));
//        			//**System.out.println("-" + (y+1) + " " + y + " 0");
//        			//**System.out.println("-" + (y+1) + " -" + hexa(i,j) + " " + hexa(taille-i-j-1,i) + " 0");
//        		}
//                y = yp1;
//                yp1 = model.boolVar();
//        	}


		// Resolution ========================================================
        
        model.getSolver().setSearch(new IntStrategy(b, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();		
		
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			System.out.println(solution);
		}
		
		//solver.showSolutions();
		//solver.findAllSolutions();
	}
}
