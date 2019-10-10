package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Synthesis {

	public static void computeReasonanceEnergy(String inputFilename, String outputFilename) {
		
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File(inputFilename)));
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFilename)));
			
			boolean firstLine = true;
			double [] R = new double[5];
			int [] nbCycles = new int[5];
			int nbStructures;
			String line = null;
			int indexMolecule = 1;
			
			while ((line = r.readLine()) != null) {
				String [] splittedLine = line.split(" ");
				
				if (!splittedLine[0].equals("c")) {
					
					if (firstLine) {
						firstLine = false;
						for (int i = 0 ; i < R.length ; i++) {
							R[i] = Double.parseDouble(splittedLine[i]);
							w.write("R" + (i+1) + " = " + R[i]);
							if (i < R.length - 1)
								w.write(", ");
						}
						w.write("\n");
					}
					
					else {
						nbStructures = Integer.parseInt(splittedLine[0]);
						for (int i = 1 ; i < splittedLine.length ; i++) {
							nbCycles[i-1] = Integer.parseInt(splittedLine[i]);
						}
						
						double energy = 0;
						w.write("Molecule " + indexMolecule + " ");
						w.write("(");
						for (int i = 0 ; i < R.length ; i++) {
							energy += ((double)nbCycles[i]) * R[i];
							w.write("(" + nbCycles[i] + "* R" + (i+1) + ")");
							if (i < R.length-1)
								w.write(" + ");
						}
						energy = energy / nbStructures;
						w.write(") / " + nbStructures + " = " + energy + "\n");
						
						indexMolecule ++;
					}
				}
			}
			
			r.close();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) {
		computeReasonanceEnergy("cycles_conjugues.txt", "synthese.txt");
	}
}
