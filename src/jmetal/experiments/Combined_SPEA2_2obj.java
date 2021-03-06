package jmetal.experiments;

import hyperheuristics.algs.SPEA2;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.SolutionSet;
import jmetal.base.operator.crossover.CrossoverFactory;
import jmetal.base.operator.mutation.MutationFactory;
import jmetal.base.operator.selection.SelectionFactory;
import jmetal.problems.Combined2Objetives;
import jmetal.util.JMException;

public class Combined_SPEA2_2obj {

    //  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --
    public static void main(String[] args) throws FileNotFoundException, IOException, JMException, ClassNotFoundException {

        String[] softwares;
        int runsNumber, populationSize, archiveSize, maxEvaluations;
        String crossoverName, mutationName;

        if (args.length == 7) {
            populationSize = Integer.parseInt(args[0]);
            archiveSize = Integer.parseInt(args[1]);
            maxEvaluations = Integer.parseInt(args[2]);
            crossoverName = args[3];
            mutationName = args[4];
            runsNumber = Integer.parseInt(args[5]);
            softwares = new String[1];
            softwares[0] = args[6];
        } else if (args.length == 6) {
            populationSize = Integer.parseInt(args[0]);
            archiveSize = Integer.parseInt(args[1]);
            maxEvaluations = Integer.parseInt(args[2]);
            crossoverName = args[3];
            mutationName = args[4];
            runsNumber = Integer.parseInt(args[5]);
            softwares = new String[5];
            softwares[0] = "OO_MyBatis";
            softwares[1] = "OA_AJHsqldb";
            softwares[2] = "OO_BCEL";
            softwares[3] = "OA_AJHotDraw";
            softwares[4] = "OA_HealthWatcher";
        } else {
            softwares = new String[5];
            softwares[0] = "OO_MyBatis";
            softwares[1] = "OA_AJHsqldb";
            softwares[2] = "OO_BCEL";
            softwares[3] = "OA_AJHotDraw";
            softwares[4] = "OA_HealthWatcher";
            runsNumber = 30;
            populationSize = 300;
            archiveSize = 300;
            maxEvaluations = 60000;
            crossoverName = "PMXCrossover";
            mutationName = "SimpleInsertionMutation";
        }

        double crossoverProbability = 0.95;
        double[] mutations = new double[3];
        mutations[0] = 0.05;
        mutations[1] = 0.08;
        mutations[2] = 0.04;
        for (double mutationProbability : mutations) {

            for (String filename : softwares) {

                String context = "_Comb_2obj";

                File directory = new File("resultado/spea2/" + filename + context);
                if (!directory.exists()) {
                    if (!directory.mkdir()) {
                        System.exit(0);
                    }
                }

                Combined2Objetives problem = new Combined2Objetives("problemas/" + filename + ".txt");
                Algorithm algorithm = new SPEA2(problem);
                SolutionSet todasRuns = new SolutionSet();
                Operator crossover;
                Operator mutation;
                Operator selection;

                // Crossover
                crossover = CrossoverFactory.getCrossoverOperator(crossoverName);
                crossover.setParameter("probability", crossoverProbability);
                // Mutation
                mutation = MutationFactory.getMutationOperator(mutationName);// SwapMutation
                mutation.setParameter("probability", mutationProbability);
                // Selection
                selection = SelectionFactory.getSelectionOperator("BinaryTournament");
                // Algorithm params
                algorithm.setInputParameter("populationSize", populationSize);
                algorithm.setInputParameter("maxEvaluations", maxEvaluations);
                algorithm.setInputParameter("archiveSize", archiveSize);
                algorithm.addOperator("crossover", crossover);
                algorithm.addOperator("mutation", mutation);
                algorithm.addOperator("selection", selection);

                System.out.println("\n================ SPEA2 ================");
                System.out.println("Software: " + filename);
                System.out.println("Context: " + context);
                System.out.println("Params:");
                System.out.println("\tPop -> " + populationSize);
                System.out.println("\tArq -> " + archiveSize);
                System.out.println("\tMaxEva -> " + maxEvaluations);
                System.out.println("\tCrossover: " + crossoverName);
                System.out.println("\tCross -> " + crossoverProbability);
                System.out.println("\tMutation: " + mutationName);
                System.out.println("\tMuta -> " + mutationProbability);
                System.out.println("Number of elements: " + problem.numberOfElements_);

                long heapSize = Runtime.getRuntime().totalMemory();
                heapSize = (heapSize / 1024) / 1024;
                System.out.println("Heap Size: " + heapSize + "Mb\n");

                for (int runs = 0; runs < runsNumber; runs++) {
                    // Execute the Algorithm
                    long initTime = System.currentTimeMillis();
                    SolutionSet resultFront = algorithm.execute();
                    long estimatedTime = System.currentTimeMillis() - initTime;
                    System.out.println("Iruns: " + runs + "\tTotal time: " + estimatedTime);

                    resultFront = problem.removeDominadas(resultFront);
                    resultFront = problem.removeRepetidas(resultFront);

                    resultFront.printObjectivesToFile("resultado/spea2_" + mutationProbability + "/" + filename + context + "/FUN_spea2" + "-" + filename + "-" + runs + ".NaoDominadas");
                //resultFront.printVariablesToFile("resultado/spea2/" + filename + context + "/VAR_spea2" + "-" + filename + "-" + runs + ".NaoDominadas");

                    //armazena as solucoes de todas runs
                    todasRuns = todasRuns.union(resultFront);
                }

                todasRuns = problem.removeDominadas(todasRuns);
                todasRuns = problem.removeRepetidas(todasRuns);
                todasRuns.printObjectivesToFile("resultado/spea2_" + mutationProbability + "/" + filename + context + "/All_FUN_spea2" + "-" + filename);
            //todasRuns.printVariablesToFile("resultado/spea2/" + filename + context + "/All_VAR_spea2" + "-" + filename);

            //grava arquivo juntando funcoes e variaveis
                //gravaCompleto(todasRuns, "TodasRuns-Completo_spea2");
            }
        }
    }
    //  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --  --
}
