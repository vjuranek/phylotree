/** This file is part of TrAP, Tree Averaging Program, which computes medians and means of phylogenetic trees.
    Copyright (C) 2013 Miroslav Bacak, Vojtech Juranek

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package eu.vbrlohu.trap.iotools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import eu.vbrlohu.trap.geodesic.Tree;

public class TrAP {

    public static void main(String[] args) {

        OptParser op = new OptParser(args);
        File inputFile = op.getInputFile();
        
        NewickLexer lexer = new NewickLexer(inputFile);
        NewickParser parser = new NewickParser(lexer);
        
        try {
            parser.entry();
          } catch(Exception e) {}
        
        
        
        
        ArrayList<Tree> mytrees = TrAP.readTreesFromFile(inputFile);
        
        System.out.println();
        if (mytrees.size() < 2) {
            System.out.println("At least two trees needed.");
            System.exit(1);
        }

        switch (op.getQuantity()) {
        case distance:
            computeDistance(mytrees);
            break;
        case median:
            computeMedian(mytrees, op.getMethod(), op.getIterations());
            break;
        case mean:
            computeMean(mytrees, op.getMethod(), op.getIterations());
            break;
        default:
            // this should never happen
            System.out.println("Illegal command line option.\n");
            System.exit(1);
        }

        System.out.println();
        System.out.println("***********");
        System.out.println("* THE END *");
        System.out.println("***********");

    }// end main

    private static void computeDistance(ArrayList<Tree> mytrees) {
        double distance = Tree.totalDist(mytrees.get(0), mytrees.get(1));
        System.out.println("Distance is " + distance);
    }

    private static void computeMedian(ArrayList<Tree> mytrees, Method method, int numberOfIterations) {
        int numberOfTrees = mytrees.size();
        Tree median = null;
        
        double startTime = System.currentTimeMillis();
        switch (method) {
        case cyclic:
            median = Tree.medianCyclic(mytrees, numberOfIterations);
            break;
        case random:
            median = Tree.medianRandom(mytrees, numberOfIterations);
            break;
        default:
            // this should never happen
        }
        double elapsedTime = System.currentTimeMillis() - startTime;

        File outputFile1 = new File(".././outputData/median");
        median.writeToFileNewick(outputFile1);

        try {
            // Create log file + write a copy in standard output
            File logFile = new File(".././outputData/logFile");
            FileWriter fstream = new FileWriter(logFile);
            BufferedWriter out = new BufferedWriter(fstream);

            System.out.println("Median computation successful.\n");
            out.write("Median computation successful." + '\n' + '\n');
            switch (method) {
            case cyclic:
                System.out.println("Used method: Cyclic order");
                out.write("Used method: Cyclic order" + '\n');
                System.out.println("Number of cycles: " + numberOfIterations);
                out.write("Number of cycles: " + numberOfIterations + '\n');
                break;
            case random:
                System.out.println("Used method: Random order");
                out.write("Used method: Random order" + '\n');
                System.out.println("Number of iterations: " + numberOfIterations);
                out.write("Number of iterations: " + numberOfIterations + '\n');
                break;
            default:
                // this should never happen
            }

            System.out.println("Number of trees in the input set: " + numberOfTrees);
            out.write("Number of trees in the input set: " + numberOfTrees + '\n');

            String eTimeMgs = "Elapsed time: " + getPrettyTime(elapsedTime);
            out.write(eTimeMgs + '\n');
            System.out.println(eTimeMgs);

            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    private static void computeMean(ArrayList<Tree> mytrees, Method method, int numberOfIterations) {
        int numberOfTrees = mytrees.size();
        Tree mean = null;
        
        double startTime = System.currentTimeMillis();
        switch (method) {
        case cyclic:
            mean = Tree.meanViaPPACyclic(mytrees, numberOfIterations);
            break;
        case random:
            mean = Tree.meanViaPPARandom(mytrees, numberOfIterations);
            break;
        default:
            // this should never happen
        }
        double elapsedTime = System.currentTimeMillis() - startTime;

        File outputFile2 = new File(".././outputData/mean");
        mean.writeToFileNewick(outputFile2);

        try {
            // Create log file + write a copy in standard output
            File logFile = new File(".././outputData/logFile");
            FileWriter fstream = new FileWriter(logFile);
            BufferedWriter out = new BufferedWriter(fstream);

            System.out.println("Mean computation successful.\n");
            out.write("Mean computation successful." + '\n' + '\n');
            
            switch (method) {
            case cyclic:
                System.out.println("Used method: Cyclic order");
                out.write("Used method: Cyclic order" + '\n');
                System.out.println("Number of cycles: " + numberOfIterations);
                out.write("Number of cycles: " + numberOfIterations + '\n');
                break;
            case random:
                System.out.println("Used method: Random order");
                out.write("Used method: Random order" + '\n');
                System.out.println("Number of iterations: " + numberOfIterations);
                out.write("Number of iterations: " + numberOfIterations + '\n');
                break;
            default:
                // this should never happen
            }

            System.out.println("Number of trees in the input set: " + numberOfTrees);
            out.write("Number of trees in the input set:" + numberOfTrees + '\n');
 
            String eTimeMgs = "Elapsed time: " + getPrettyTime(elapsedTime);
            out.write(eTimeMgs + '\n');
            System.out.println(eTimeMgs);
            
            // Close the output stream
            out.close();
        } catch (Exception e) {// Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    private static String getPrettyTime(double timeInMilis) {
        if (timeInMilis < 60000) {
            return timeInMilis / 1000 + " sec";
        } else {
            return timeInMilis / 60000 + " min";
        }
        
    }
    
    

    // read file with trees, each in the Newick format
    public static ArrayList<Tree> readTreesFromFile(File file) {

        ArrayList<Tree> trees = new ArrayList<Tree>();

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));

            String line;

            while ((line = br.readLine()) != null) {

                if (!line.contains("(")) {
                    continue;
                }

                Tree auxTree = new Tree(line);
                trees.add(auxTree);
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trees;
    }

    // finds the smallest index in a string 'string' after i, which contains a character from a string 'characters', if
    // does not contains any, returns string.length
    public static int nextIndexOf(String string, String characters, int index) {
        int retInd = string.length();
        int auxIndex = -1;

        for (int i = 0; i < characters.length(); i++) {
            auxIndex = string.substring(index + 1).indexOf(characters.charAt(i));
            if ((auxIndex > -1) && (auxIndex + index + 1 < retInd)) {
                retInd = auxIndex + index + 1;
            }
        }
        return retInd;
    }

}
