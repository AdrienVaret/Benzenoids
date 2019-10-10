package benzenoids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.chocosolver.solver.Solution;

public class SolutionConverter {

	private Solution solution;
	private int nbCrowns;
	private int size;
	
	private int [][] edges;
	private int [] nodes;
	private ArrayList<Integer> nodesSolution;
	private ArrayList<ArrayList<Integer>> hexagons;
	
	public SolutionConverter(Solution solution, int nbCrowns, int size) {
		this.solution = solution;
		this.nbCrowns = nbCrowns;
		this.size = size;
		
		nodesSolution = new ArrayList<Integer>();
		hexagons = new ArrayList<ArrayList<Integer>>();
		edges = new int[6 * size * size][6 * size * size];
		nodes = new int[6 * size * size];
		
		readSolution();
		createMolecule();
	}
	
	public void readSolution() {
		
		String toString = solution.toString();
		String [] subString1 = toString.split(Pattern.quote(": "));
		
		String toString2 = subString1[1];
		String [] subString2 = toString2.split(Pattern.quote(", not"));
		
		String toString3 = subString2[0];
		String [] subString3 = toString3.split(Pattern.quote(", "));
		
		for (int i = 0 ; i < subString3.length ; i++) {
			String str = subString3[i];
			String [] tabStr = str.split(Pattern.quote("="));
			int nodeIsPresent = Integer.parseInt(tabStr[1]);
			nodesSolution.add(nodeIsPresent);
		}
		
	}
	


	
	public int xy2i(int x, int y, int taille) {
		return x + y * taille;
	}
	
	public int countNodes() {
		int nbNodes = 0;
		for (int i = 0 ; i < nodes.length ; i++) {
			if (nodes[i] == 1)
				nbNodes ++;
		}
		return nbNodes;
	}
	
	public void createMolecule() {
		int haut, hautdroit, basdroit, bas, basgauche, hautgauche;
		
		int k = 0;
		
		for (int j = 0 ; j < size ; j++) {
			for (int i = 0 ; i < size ; i++) {
				
				if (j == 0) {
					haut = k;
					hautdroit = k + 1;
					basdroit = k + 2;
					bas = k + 3;
					
					if (i == 0) {
						basgauche = k + 4;
						hautgauche = k + 5;
					} else {
						basgauche = k - 4;
						hautgauche = k - 5;
					}
				}
				
				else {
					
					if (i == 0) {
						haut = k + 4 - 6 * size;
						basgauche = k + 4;
			            hautgauche = k + 5;
					}
					
					else {
						haut = k + 2 - 6 * (size + 1);
			            basgauche = k - 4;
			            hautgauche = k + 3 - 6 * (size + 1);
					}
					
					hautdroit = k + 3 - 6 * size;
			        basdroit = k + 2;
			        bas = k + 3;
				}
				
				if (nodesSolution.get(xy2i(i, j, size)) == 1) {
					
					ArrayList<Integer> hexagon = new ArrayList<Integer>();
					hexagon.add(haut);
					hexagon.add(hautdroit);
					hexagon.add(basdroit);
					hexagon.add(bas);
					hexagon.add(basgauche);
					hexagon.add(hautgauche);
					hexagons.add(hexagon);
					
					int vertex1, vertex2;
					
					if (haut < hautdroit) {
						vertex1 = haut;
						vertex2 = hautdroit;
						//System.out.println(haut + " -- " + hautdroit);
					} else {
						vertex2 = haut;
						vertex1 = hautdroit;
						//System.out.println(hautdroit + " -- " + haut);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
					if (hautdroit < basdroit) {
						vertex1 = hautdroit;
						vertex2 = basdroit;
						//System.out.println(hautdroit + " -- " + basdroit);
					} else {
						vertex2 = hautdroit;
						vertex1 = basdroit;
						//System.out.println(basdroit + " -- " + hautdroit);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
					if (basdroit < bas) {
						vertex1 = basdroit;
						vertex2 = bas;
						//System.out.println(basdroit + " -- " + bas);
					} else {
						vertex2 = basdroit;
						vertex1 = bas;
						//System.out.println(bas + " -- " + basdroit);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
					if (bas < basgauche) {
						vertex1 = bas;
						vertex2 = basgauche;
						//System.out.println(bas + " -- " + basgauche);
					} else {
						vertex2 = bas;
						vertex1 = basgauche;
						//System.out.println(basgauche + " -- " + bas);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
					if (basgauche < hautgauche) {
						vertex1 = basgauche;
						vertex2 = hautgauche;
						//System.out.println(basgauche + " -- " + hautgauche);
					} else {
						vertex2 = basgauche;
						vertex1 = hautgauche;
						//System.out.println(hautgauche + " -- " + basgauche);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
					if (hautgauche < haut) {
						vertex1 = hautgauche;
						vertex2 = haut;
						//System.out.println(hautgauche + " -- " + haut);
					} else {
						vertex2 = hautgauche;
						vertex1 = haut;
						//System.out.println(haut + " -- " + hautgauche);
					}
					
					System.out.println(vertex1 + " -- " + vertex2);
					edges[vertex1][vertex2] = 1;
					edges[vertex2][vertex1] = 1;
					nodes[vertex1] = 1;
					nodes[vertex2] = 1;
					
				}
				k = k + 6;
			}
		}
		int nbNodes = countNodes();
		System.out.println("");
	}
}
