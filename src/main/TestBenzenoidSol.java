package main;

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class TestBenzenoidSol
{
    static int k = 0;
    
    public static void main(String [] args){
        int i, j, k;
        int dimension = Integer.parseInt(args[0]);
        int taille = dimension * 2 - 1; 
        boolean hexas [][] = new boolean [taille][taille];
        
        Scanner scan = null;
        try {
            scan = new Scanner(new File(args[1]));
        }
        catch(FileNotFoundException x){System.out.println("Pas de fichier"); System.exit(0);}
        
        i = 0;
        j = 0;
        boolean solExists;
        System.out.println("graph G {");
        //Pattern p = Pattern.compile(".*nodes.*");
        Pattern pnode = Pattern.compile("nodes\\[\\d*\\] = \\d");
        while (scan.hasNext())
        {
        	solExists = false;
            String solution = scan.nextLine();
            //System.out.println(solution);
            Matcher m = pnode.matcher(solution);
            while(m.find()) {
            	solExists = true;
            	hexas[i][j] = solution.charAt(m.end() - 1) == '1';
            	//System.out.print("("+i+","+j+")="+hexas[i][j]+ " ");
            	i++;
            	if(i == taille){
            		i = 0;
            		j++;
            	}
            }
            //System.out.println();
            if(solExists)
            	genereGraphe(hexas, taille);
            i = 0;
            j = 0;
        }
        System.out.println("}");
    }
    
    static void genereGraphe(boolean[][] hexas, int taille){
        k = k + 1;
        int i, j;
        int haut, hautdroit, basdroit, bas, basgauche, hautgauche;

        for(j = 0 ; j < taille ; j++)
            for(i = 0 ; i < taille ; i++){
                // numerotation des sommets
                if(j == 0) {
                    haut = k;
                    hautdroit = k + 1;
                    basdroit = k + 2;
                    bas = k + 3;
                    if(i == 0) {
                    	basgauche = k + 4;
                    	hautgauche = k + 5;
                    }
                    else {
                    	basgauche = k - 4;
                    	hautgauche = k - 5;
                    }
                }
                else {// j > 0
                	if(i == 0) {
                		haut = k + 4 - 6 * taille;
                		basgauche = k + 4;
                		hautgauche = k + 5;
                	}
                	else {
                		haut = k + 2 - 6 * (taille + 1);
                		basgauche = k - 4;
                		hautgauche = k + 3 - 6 * (taille + 1);
                	}
                	hautdroit = k + 3 - 6 * taille;
                	basdroit = k + 2;
                	bas = k + 3;
                }

                //dessin de l'hexagone
                if(!hexas[i][j]){
                    System.out.print("{ edge [style=invis];");
                }
                //visibilite des sommets
                if(!hexas[i][j] && (j == 0 || !hexas[i][j-1]) && (j == 0 || i == 0 || !hexas[i-1][j-1])) // h
                    System.out.print("{node [style=invis] " + haut + "}");
                else
                    System.out.print("{node [style=bold] " + haut + "}");
                if(!hexas[i][j] && (j == 0 || !hexas[i][j-1]) && (i == taille - 1 || !hexas[i+1][j])) // hd
                    System.out.print("{node [style=invis] " + hautdroit + "}");
                else
                    System.out.print("{node [style=bold] " + hautdroit + "}");
                if(!hexas[i][j] && (i == taille - 1 || !hexas[i+1][j]) && (i == taille - 1 || j == taille - 1 || !hexas[i+1][j+1])) // bd
                    System.out.print("{node [style=invis] " + basdroit + "}");
                else
                    System.out.print("{node [style=bold] " + basdroit + "}");
                if(!hexas[i][j] && (j == taille - 1 || !hexas[i][j+1]) && (i == taille - 1 || j == taille - 1 || !hexas[i+1][j+1])) // b
                    System.out.print("{node [style=invis] " + bas + "}");
                else
                    System.out.print("{node [style=bold] " + bas + "}");
                if(!hexas[i][j] && (j == taille - 1 || !hexas[i][j+1]) && (i == 0 || !hexas[i-1][j])) // bg
                    System.out.print("{node [style=invis] " + basgauche + "}");
                else
                    System.out.print("{node [style=bold] " + basgauche + "}");
                if(!hexas[i][j] && (i == 0 || !hexas[i-1][j]) && (i == 0 || j == 0 || !hexas[i-1][j-1])) //hg
                    System.out.print("{node [style=invis] " + hautgauche + "}");
                else
                    System.out.print("{node [style=bold] " + hautgauche + "}");
                
                //affichage des aretes
                System.out.print(haut);
                if(j > 0 && hexas[i][j-1])
                    System.out.print("; ");
                else
                    System.out.print(" -- ");//h--hd
                System.out.print(hautdroit + " -- " + basdroit + " -- " + bas + " -- " + basgauche);
                if(i > 0 && hexas[i-1][j])
                    System.out.print("; ");
                else
                    System.out.print(" -- ");//bg--hg
                System.out.print(hautgauche);
                if(j > 0 && i > 0 && hexas[i-1][j-1])
                    System.out.println(";");
                else
                    System.out.println(" -- " + haut + ";");
                if(!hexas[i][j]){
                    System.out.print("}");
                }
                
                k = k + 6;
            }
    }
}
