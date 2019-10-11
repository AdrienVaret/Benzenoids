package benzenoids;

import java.util.regex.Pattern;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;

import graphs.UndirGraph;
import parser.GraphParser;

public class KekuleStructureGenerator {

public static String computeOneKekuleStructure(String filename) {
		
		System.out.println("Computing one Kekule's structure of : " + filename);
	
		String [] splittedFilename = filename.split(Pattern.quote("."));
		String outputFileName = splittedFilename[0] + "_structure.graph";
	
		UndirGraph graph = GraphParser.parseUndirectedGraph(filename);
		
		Model model = new Model("Kekule's structure");
		
		
		BoolVar [] edges = new BoolVar[graph.getNbEdges()];
		
		for (int i = 0 ; i < graph.getNbEdges() ; i++) {
			edges[i] = model.boolVar("edge " + (i+1));
		}
		
		for (int i = 0 ; i < graph.getEdgeMatrix().size() ; i++) {
				if (graph.isActive(i)) {
					int nbAdjacentEdges = graph.getEdgeMatrix().get(i).size();
			
					BoolVar [] adjacentEdges = new BoolVar[nbAdjacentEdges];
			
					for (int j = 0 ; j < nbAdjacentEdges ; j++) {
						adjacentEdges[j] = edges[graph.getEdgeMatrix().get(i).get(j)];
					}
			
					model.sum(adjacentEdges, "=", 1).post();
				}
		}
			
		model.getSolver().setSearch(new IntStrategy(edges, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();
		
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			
			int [] edgesValues = new int [graph.getNbEdges()];
			
			for (int j = 0 ; j < graph.getNbEdges() ; j++) {
				edgesValues[j] = solution.getIntVal(edges[j]);
			}
			
			GraphParser.exportSolutionToPonderateGraph(outputFileName, graph, edgesValues);
			
			return outputFileName;
		}
		
		return "invalid_structure";
	}
	
	public static void displayUsage() {
		System.err.println("USAGE : java -jar ${EXEC_NAME} filename");
	}

	public static void main(String [] args) {
		if (args.length == 0)
			displayUsage();
		
		else {
			String filename = args[0];

			String result = computeOneKekuleStructure(filename);
			
			if (result.equals("invalid_structure"))
				System.out.println("invalid structure");
			
			else
				System.out.println("> " + result + " generated");
		}
	}
}
