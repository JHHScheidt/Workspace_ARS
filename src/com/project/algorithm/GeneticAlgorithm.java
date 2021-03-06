package com.project.algorithm;

import com.project.Controller;
import com.project.simulation.Simulator;
import com.project.simulation.environment.Environment;

import java.awt.geom.Point2D;
import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Marciano, Rico
 */
public class GeneticAlgorithm {

    private final static double MUTATION_RATE = 0.1;
    private final static int TOURNAMENT_SIZE = 10;
    private final static boolean ELITISM = true;
    private final static int NN_OUTPUT = 2;

    private ArrayList<Integer> indexPool;
    private ArrayList<Integer> usedIndices;
    private ArrayList<Individual> individuals;
    private Individual best, worst, allTimeBest;

    private int generation;

    private Controller controller;

    /**
     * Constructor for the GeneticAlgorithm
     *
     * @param numInd Number of Individuals
     * @param size   This number should hold true to the number of sensors on the vehicle
     */
    public GeneticAlgorithm(Controller controller, int numInd, int size) {
        this.controller = controller;
        this.indexPool = new ArrayList<>();
        this.usedIndices = new ArrayList<>();
        this.init(numInd, size);
    }
    
    public GeneticAlgorithm(Controller controller, int numInd, int size, String file){
        this.controller = controller;
        try{
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            this.individuals = (ArrayList<Individual>) in.readObject();
            this.best = this.individuals.get(0);
            this.worst = this.best;
            this.indexPool = new ArrayList<>();
            for (int i = 0; i < this.individuals.size(); i++)
                this.indexPool.add(i);
            this.usedIndices = new ArrayList<>();
            this.allTimeBest = this.best;
            this.generation = Integer.parseInt(file.replaceAll("[^0-9]",""));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Initialise the full population randomly
     *
     * @param numInd Number of Individuals
     * @param size   This number should hold true to the number of sensors on the vehicle
     */
    public void init(int numInd, int size) {
        this.individuals = new ArrayList<>();
        for (int i = 0; i < numInd; i++) {
            this.indexPool.add(i);
            double[][] tempInput = new double[size][NN_OUTPUT];
            double[][] tempRecur = new double[NN_OUTPUT][NN_OUTPUT];
            for (int j = 0; j < tempInput.length; j++) {
                for (int k = 0; k < tempInput[0].length; k++) {
                    tempInput[j][k] = Controller.RANDOM.nextDouble() * 10 - 5;
                }
            }
            for (int j = 0; j < tempRecur.length; j++) {
                for (int k = 0; k < tempRecur.length; k++) {
                    tempRecur[j][k] = Controller.RANDOM.nextDouble() * 10 - 5;
                }
            }
            this.individuals.add(new Individual(tempInput, tempRecur, 0));
        }

        this.generation = 0;
        this.best = this.individuals.get(0);
        this.allTimeBest = this.best;
        this.worst = this.best;
    }

    public void start() throws ExecutionException, InterruptedException {
        Simulator[] simulators = new Simulator[this.individuals.size()];
        Environment[] environments = new Environment[this.individuals.size()];
        Environment chosenEnvironment = Environment.MAZE_JOSHUA;

        for (int i = 0; i < simulators.length; i++) {
            simulators[i] = new Simulator(i, false);
            environments[i] = chosenEnvironment.clone();
        }

            ArrayList<Future<Double>> futures = new ArrayList<>();
            ArrayList<Simulator> tasks = new ArrayList<>();

            int numberOfRuns = this.generation;
            for (int i = (this.generation>0?this.generation:0); i < numberOfRuns+2001; i++) {
                System.out.println("Starting generation " + this.generation);
                this.worst = this.best;

            for (Individual individual : this.individuals)
                individual.fitness = 0;

            // evaluate
            for (Point2D.Double startingLocation : chosenEnvironment.startingLocations) {
                for (int j = 0; j < simulators.length; j++) {
                    environments[j].reset();
                    simulators[j].init(this.individuals.get(j), environments[j], startingLocation.x, startingLocation.y, 100);
                    futures.add(this.controller.submit(simulators[j]));
                    tasks.add(simulators[j]);
                }

                Future<Double> future;
                while (futures.size() > 0) {
                    for (int j = 0; j < futures.size(); j++) {
                        future = futures.get(j);

                        if (future.isDone()) {
                            futures.remove(j);
                            Simulator completedSimulator = tasks.remove(j);
                            Individual completed = this.individuals.get(completedSimulator.id);
                            completed.fitness += future.get() / chosenEnvironment.startingLocations.length;
                            if (this.best.fitness < completed.fitness)
                                this.best = completed;
                            if (this.worst.fitness > completed.fitness)
                                this.worst = completed;
                        }
                    }

                    Thread.sleep(10);
                }
            }

            if (this.allTimeBest.fitness < this.best.fitness)
                this.allTimeBest = this.best;

            if(i%5 == 0) {
                System.out.println("Current best: " + this.best);
               this.doStatisticsAndStorePopulation();
            }

            // evolvePopulation
            this.evolvePopulation();
            this.generation++;
        }

        this.saveAllTimeBest();
    }

    public void saveAllTimeBest() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("res/all_time_best.txt"));
            out.writeObject(this.allTimeBest);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doStatisticsAndStorePopulation() {
        double mean = 0;
        for (Individual individual : this.individuals)
            mean += individual.fitness;
        mean /= individuals.size();

        double std = 0;
        for (Individual individual : this.individuals)
            std += Math.pow(individual.fitness - mean, 2);
        std /= this.individuals.size() - 1;
        std = Math.sqrt(std);

        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("res/generation" + this.generation + "-all.txt"));
            out.writeObject(this.individuals);
            out.close();
            out = new ObjectOutputStream(new FileOutputStream("res/generation" + this.generation + "-best.txt"));
            out.writeObject(this.best);
            out.close();
            out = new ObjectOutputStream(new FileOutputStream("res/generation" + this.generation + "-worst.txt"));
            out.writeObject(this.worst);
            out.close();

            PrintWriter writer = new PrintWriter(new FileOutputStream("res/statistics.txt", true));
            writer.println(mean + ", " + std);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Evolves the population into a new population it may use ELITISM if assigned
     * It uses tournament selection to create a new population on this new population
     * it will perform mutations this mutation can either be an exchange mutation or random mutation
     */
    public void evolvePopulation() {
        ArrayList<Individual> newPopulation = new ArrayList<>();
        System.out.println("best: " + this.best.fitness);
        System.out.println("worst: " + this.worst.fitness);
        // Keep our best individual
        int elitismOffset = 0;
        if (ELITISM) {
            newPopulation.add(this.best);
            elitismOffset = 1;
        }
        // Loop over the population size and create new individuals with
        // crossover
        for (int i = elitismOffset; i < this.individuals.size(); i++) {
            Individual ind1 = tournamentSelection();
            Individual ind2 = tournamentSelection();
            Individual newInd = new Individual(crossover(ind1.getInputWeights(), ind2.getInputWeights()), crossover(ind1.getRecurWeights(), ind2.getRecurWeights()), 0);
            newPopulation.add(newInd);
        }

        // Mutate population
        double random;
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            random = Controller.RANDOM.nextDouble();
            if (random < MUTATION_RATE) {
                newPopulation.get(i).setInputWeights(randomMutation(newPopulation.get(i).getInputWeights()));
                newPopulation.get(i).setRecurWeights(randomMutation(newPopulation.get(i).getRecurWeights()));
            }
                //            } else if (random < 0.666667) {
//                newPopulation.get(i).setInputWeights(exchangeMutation(newPopulation.get(i).getInputWeights()));
//                newPopulation.get(i).setRecurWeights(exchangeMutation(newPopulation.get(i).getRecurWeights()));
//            }
        }
        this.individuals = newPopulation;
    }

    /**
     * Tournament Selection, tournament size is dictated by a preset value
     * In our base case it is set to 5. Within a random selection of 5 individuals
     * the best is selected and returned
     *
     * @return Best Individual from this tournament
     */
    public Individual tournamentSelection() {
        Individual[] tournament = new Individual[TOURNAMENT_SIZE];
        int selectedIndex;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            selectedIndex = this.indexPool.remove((int) (Controller.RANDOM.nextDouble() * this.indexPool.size()));
            this.usedIndices.add(selectedIndex);
            tournament[i] = this.individuals.get(selectedIndex);
        }

        while (this.usedIndices.size() > 0) {
            this.indexPool.add(this.usedIndices.remove(0));
        }

        int bestIndex = 0;
        for (int i = 1; i < tournament.length; i++) {
            if (tournament[i].fitness > tournament[bestIndex].fitness) {
                bestIndex = i;
            }
        }
        return tournament[bestIndex];
    }

    /**
     * Exchange Mutation which give a MUTATION_RATE will exchange 2 indices in the Individual weight
     *
     * @param ind The values to be taken into consideration
     * @return 2D double array which holds the changed or unchanged ind variable
     */
    public double[][] exchangeMutation(double[][] ind) {
        int[] index = {-1, -1};
        for (int i = 0; i < ind.length; i++) {
            index[0] = -1;
            index[1] = -1;
            for (int j = 0; j < ind[i].length; j++) {
                if (Controller.RANDOM.nextDouble() < MUTATION_RATE) {
                    if (index[0] == -1) {
                        index[0] = j;
                    } else if (index[1] == -1) {
                        index[1] = j;
                    } else {
                        break;
                    }
                }
            }
            if (index[0] != -1 && index[1] != -1) {
                double temp = ind[i][index[0]];
                ind[i][index[0]] = ind[i][index[1]];
                ind[i][index[1]] = temp;
            }
        }
        return ind;
    }

    /**
     * Checks each index and decides on a MUTATION_RATE if the value is changed
     * The number it is changed with lies in a range of -0.5 to 0.5
     *
     * @param ind Double array to be mutated
     * @return 2D double array which holds the mutated ind variable
     */
    public double[][] randomMutation(double[][] ind) {
        int mutations = 0;

        while (mutations++ < 5) {
            int i = (int)(Controller.RANDOM.nextDouble() * ind.length);
            int j = (int)(Controller.RANDOM.nextDouble() * ind[0].length);
            ind[i][j] += Controller.RANDOM.nextDouble() * 5 - 2.5;
        }

        return ind;
    }

    /**
     * Makes a crossover between two pairs of 2D arrays
     * It will always split through the middle and combine the first half of ind1
     * and the second hafl of ind2
     *
     * @param ind1 First array to be merged
     * @param ind2 Second array to be merged
     * @return New Array with the merged elements
     */
    public double[][] crossover(double[][] ind1, double[][] ind2) {
        double[][] newInd = new double[ind2.length][ind2[0].length];
        int crossoverPoint = (int)(Controller.RANDOM.nextDouble() * ind1.length);
        for (int i = 0; i < ind1.length; i++) {
            if (i < crossoverPoint){
                System.arraycopy(ind1[i], 0, newInd[i], 0, ind1[i].length);
            }else{
                System.arraycopy(ind2[i], 0, newInd[i], 0, ind2[i].length);
            }
        }

        return newInd;
    }
}
