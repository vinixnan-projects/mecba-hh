//  NSGAII.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package hyperheuristics.algs;

import java.util.logging.Level;
import java.util.logging.Logger;
import jmetal.base.*;
import hyperheuristics.core.AlgorithmHH;
import jmetal.base.operator.comparator.CrowdingComparator;
import jmetal.problems.CITO_CAITO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;


/**
 * Implementation of NSGA-II. This implementation of NSGA-II makes use of a
 * QualityIndicator object to obtained the convergence speed of the algorithm.
 * This version is used in the paper: A.J. Nebro, J.J. Durillo, C.A. Coello
 * Coello, F. Luna, E. Alba "A Study of Convergence Speed in Multi-Objective
 * Metaheuristics." To be presented in: PPSN'08. Dortmund. September 2008.
 */
public class NSGAII extends AlgorithmHH {

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGAII(Problem problem) {
        super(problem);
        this.methodName="NSGAII";
    } // NSGAII

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    @Override
    public SolutionSet execute() throws JMException {
        this.initPopulation();
        // Generations 
        while (evaluations < maxEvaluations) {
            this.executeMethod();
        } // while
        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);
        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);
        return ranking.getSubfront(0);
    } // execute

    @Override
    public void executeMethod() throws JMException {

        // Create the offSpring solutionSet      
        SolutionSet offspringPopulation = new SolutionSet(populationSize);
        Solution[] parents = new Solution[2];
        for (int i = 0; i < (populationSize / 2); i++) {
            if (evaluations < maxEvaluations) {
                //obtain parents
                parents[0] = (Solution) selectionOperator.execute(population,(CITO_CAITO) problem_);
                parents[1] = (Solution) selectionOperator.execute(population,(CITO_CAITO) problem_);
                Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents,(CITO_CAITO) problem_);
                mutationOperator.execute(offSpring[0],(CITO_CAITO) problem_);
                mutationOperator.execute(offSpring[1],(CITO_CAITO) problem_);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                offspringPopulation.add(offSpring[0]);
                offspringPopulation.add(offSpring[1]);
                evaluations += 2;
            } // if                            
        } // for

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet union = ((SolutionSet) population).union(offspringPopulation);

        // Ranking the union
        Ranking ranking = new Ranking(union);

        int remain = populationSize;
        int index = 0;
        SolutionSet front = null;
        population.clear();

        // Obtain the next front
        front = ranking.getSubfront(index);

        while ((remain > 0) && (remain >= front.size())) {
            //Assign crowding distance to individuals
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            //Add the individuals of this front
            for (int k = 0; k < front.size(); k++) {
                population.add(front.get(k));
            } // for

            //Decrement remain
            remain = remain - front.size();

            //Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if        
        } // while

        // Remain is less than front(index).size, insert only the best one
        if (remain > 0) {  // front contains individuals to insert                        
            distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
            front.sort(new CrowdingComparator());
            for (int k = 0; k < remain; k++) {
                population.add(front.get(k));
            } // for

            remain = 0;
        } // if                               
    }

    @Override
    public void initPopulation() {
        distance = new Distance();

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            try {
                newSolution = new Solution((CITO_CAITO) problem_);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                evaluations++;
                population.add(newSolution);
            } //for
            catch (ClassNotFoundException ex) {
                Logger.getLogger(NSGAII.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JMException ex) {
                Logger.getLogger(NSGAII.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
} // NSGA-II
