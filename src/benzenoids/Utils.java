package benzenoids;

import java.awt.Point;

public class Utils {

	public static int getHexagonId(int x, int y) {
		return x + y * BenzenoidsSolver.taille;
	}
	
	public static int getEdgeId(int x, int y, int edgeBoard) {
		
		if(edgeBoard == BenzenoidsSolver.HIGH_RIGHT && y > 0)
			return (x + (y - 1) * BenzenoidsSolver.taille) * 6 + 3;
		
		else if(edgeBoard == BenzenoidsSolver.LEFT && x > 0)
			return (x - 1 + y * BenzenoidsSolver.taille) * 6 + 1;
		
		else if (edgeBoard == BenzenoidsSolver.HIGHT_LEFT && x > 0 && y > 0)
			return (x - 1 + (y - 1) * BenzenoidsSolver.taille) * 6 + 2;
		
		else
			return (x + y * BenzenoidsSolver.taille) * 6 + edgeBoard;
	}
	
	public static Point getHexagonCoordinates(int index) {
		
		int y = (int) Math.floor(index / BenzenoidsSolver.taille);
		int x = index - (BenzenoidsSolver.taille * y);
		
		return new Point(x, y);
	}
}

