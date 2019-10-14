package benzenoids;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.chocosolver.solver.Solution;

public class SolutionConverter {

	private String solution;
	@SuppressWarnings("unused")
	private int nbCrowns;
	private int diameter;
	
	private int [][] edges;
	private int [] nodes;
	private ArrayList<Integer> nodesSolution;
	private ArrayList<ArrayList<Integer>> hexagons;
	
	private int nbNodes;
	private int nbEdges;
	private int nbHexagons;
	private int maxVertexId = -1;
	
	public SolutionConverter(String solution, int nbCrowns, int size, String outputFileName) {
		this.solution = solution;
		this.nbCrowns = nbCrowns;
		this.diameter = size;
		
		nodesSolution = new ArrayList<Integer>();
		hexagons = new ArrayList<ArrayList<Integer>>();
		edges = new int[6 * size * size][6 * size * size];
		nodes = new int[6 * size * size];
		
		readSolution();
		createMolecule();
		exportToDimacs(outputFileName + ".graph");
		System.out.println("\t> " + outputFileName + ".graph" + " generated.");
	}
	
	public void readSolution() {
		
		String toString = solution;
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
	
	public int countEdges() {
		int nbEdges = 0;
		
		for (int i = 0 ; i < edges.length ; i++) {
			for (int j = i + 1 ; j < edges[i].length ; j++) {
				if (edges[i][j] == 1) nbEdges ++;
			}
		}
		
		return nbEdges;
	}
	
	public void updateGraph(int vertex1, int vertex2) {
		//System.out.println(vertex1 + " -- " + vertex2);
		edges[vertex1][vertex2] = 1;
		edges[vertex2][vertex1] = 1;
		nodes[vertex1] = 1;
		nodes[vertex2] = 1;
	}
	
	public int max(int H, int HD, int BD, int B, int BG, int HG) {
		
		int [] array = new int [6];
		array[0] = H;
		array[1] = HD;
		array[2] = BD;
		array[3] = B;
		array[4] = BG;
		array[5] = HG;
		
		int max = -1;
		
		for (int i = 0 ; i < array.length ; i++) {
			if (array[i] > max) max = array[i];
		}
		
		return max;
	}
	
	public void createMolecule() {
		
		int H, HD, BD, B, BG, HG;
		
		int k = 0;
		
		for (int j = 0 ; j < diameter ; j++) {
			for (int i = 0 ; i < diameter ; i++) {
				
				if (j == 0) {
					H = k;
					HD = k + 1;
					BD = k + 2;
					B = k + 3;
					
					if (i == 0) {
						BG = k + 4;
						HG = k + 5;
					} else {
						BG = k - 4;
						HG = k - 5;
					}
				}
				
				else {
					
					if (i == 0) {
						H = k + 4 - 6 * diameter;
						BG = k + 4;
			            HG = k + 5;
					}
					
					else {
						H = k + 2 - 6 * (diameter + 1);
			            BG = k - 4;
			            HG = k + 3 - 6 * (diameter + 1);
					}
					
					HD = k + 3 - 6 * diameter;
			        BD = k + 2;
			        B = k + 3;
				}
				
				if (nodesSolution.get(xy2i(i, j, diameter)) == 1) {
					
					ArrayList<Integer> hexagon = new ArrayList<Integer>();
					hexagon.add(H);
					hexagon.add(HD);
					hexagon.add(BD);
					hexagon.add(B);
					hexagon.add(BG);
					hexagon.add(HG);
					hexagons.add(hexagon);
					
					int max = max(H, HD, BD, B, BG, HG);
					if (max > maxVertexId)
						maxVertexId = max;
					
					int vertex1, vertex2;
					
					if (H < HD) {
						vertex1 = H;
						vertex2 = HD;
					} else {
						vertex2 = H;
						vertex1 = HD;
					}
					
					updateGraph(vertex1, vertex2);
					
					if (HD < BD) {
						vertex1 = HD;
						vertex2 = BD;
					} else {
						vertex2 = HD;
						vertex1 = BD;
					}
					
					updateGraph(vertex1, vertex2);
					
					if (BD < B) {
						vertex1 = BD;
						vertex2 = B;
					} else {
						vertex2 = BD;
						vertex1 = B;
					}
					
					updateGraph(vertex1, vertex2);
					
					if (B < BG) {
						vertex1 = B;
						vertex2 = BG;
					} else {
						vertex2 = B;
						vertex1 = BG;
					}
					
					updateGraph(vertex1, vertex2);
					
					if (BG < HG) {
						vertex1 = BG;
						vertex2 = HG;
					} else {
						vertex2 = BG;
						vertex1 = HG;
					}
					
					updateGraph(vertex1, vertex2);
					
					if (HG < H) {
						vertex1 = HG;
						vertex2 = H;
					} else {
						vertex2 = HG;
						vertex1 = H;
					}

					updateGraph(vertex1, vertex2);
				}
				k = k + 6;
			}
		}
		
		nbNodes = countNodes();
		nbEdges = countEdges();
		nbHexagons = hexagons.size();
	}
	
	public void exportToDimacs(String outputFileName) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFileName)));
			
			w.write("p DIMACS " + nbNodes + " " + nbEdges + " " + nbHexagons + " " + (maxVertexId+1) + "\n");
			
			for (int i = 0 ; i < edges.length ; i++) {
				for (int j = i + 1 ; j < edges[i].length ; j ++) {
					if (edges[i][j] == 1) {
						w.write("e " + (i+1) + " " + (j+1) + "\n");
					}
				}
			}
			
			for (ArrayList<Integer> hexagon : hexagons) {
				w.write("h ");
				for (Integer i : hexagon) {
					w.write((i+1) + " ");
				}
				w.write("\n");
			}
			
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readSolutions(String inputFileName, int nbCrowns) {
		
		int diameter = 2 * nbCrowns - 1;
		
		try {
			
			File file = new File(nbCrowns + "_crowns");
			file.mkdir();
			
			BufferedReader r = new BufferedReader(new FileReader(new File(inputFileName)));
			String line;
			
			int i = 0;
			
			while ((line = r.readLine()) != null) {
				String outputFileName = nbCrowns + "_crowns_" + i + ".graph";
				@SuppressWarnings("unused")
				SolutionConverter converter = new SolutionConverter(line, nbCrowns, diameter, nbCrowns + "_crowns/" + outputFileName);
				i ++;
			}
			
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void displayUsage() {
		System.err.println("USAGE : java -jar ${EXEC_NAME} input nbCrowns");
	}
	
	public static void main(String [] args) {
		
		if (args.length < 2) {
			displayUsage();
			System.exit(1);
		}
		
		String inputFileName = args[0];
		int nbCrowns = Integer.parseInt(args[1]);
		
		readSolutions(inputFileName, nbCrowns);
			
		
	}
	
}
