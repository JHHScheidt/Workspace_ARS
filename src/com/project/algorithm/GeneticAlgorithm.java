package com.project.algorithm;

import java.util.ArrayList;
import java.util.Arrays;

public class GeneticAlgorithm {

    private final static double mutationRate = 0.1;
    private final static int tournamentSize = 5;
    private final static boolean elitism = true;
    private final static int nnOutput = 2;
    private ArrayList<Individual> individuals;
    private Individual best;

    public GeneticAlgorithm(int numInd, int size) {
        this.init(numInd, size);
    }

    public void init(int numInd, int size) {
        individuals = new ArrayList<>();
        for (int i = 0; i < numInd; i++) {
            double[][] tempInput = new double[size][nnOutput];
            double[][] tempRecur = new double[nnOutput][nnOutput];
            for (int j = 0; j < tempInput.length; j++) {
                for(int k = 0; k < tempInput[0].length; k++){
                    tempInput[j][k] = Math.random();
                }
            }
            for(int j = 0; j < tempRecur.length; j++){
                for(int k = 0; k < tempRecur.length; k++){
                    tempRecur[j][k] = Math.random();
                }
            }
            individuals.add(new Individual(tempInput, tempRecur, 0));
        }
    }

    public void evolvePopulation() {
        ArrayList<Individual> newPopulation = new ArrayList<>();

        // Keep our best individual
        int elitismOffset = 0;
        if (elitism) {
            newPopulation.add(best);
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
        for (int i = elitismOffset; i < newPopulation.size(); i++) {
            if (Math.random() >= 0.5) {
                newPopulation.get(i).setInputWeights(randomMutation(newPopulation.get(i).getInputWeights()));
                newPopulation.get(i).setRecurWeights(randomMutation(newPopulation.get(i).getRecurWeights()));
            } else {
                newPopulation.get(i).setInputWeights(exchangeMutation(newPopulation.get(i).getInputWeights()));
                newPopulation.get(i).setRecurWeights(exchangeMutation(newPopulation.get(i).getRecurWeights()));
            }
        }
        this.individuals = newPopulation;
    }

    public Individual tournamentSelection() {
        Individual best;
        Individual[] tournament = new Individual[tournamentSize];
        for (int i = 0; i < tournamentSize; i++) {
            tournament[i] = this.individuals.get((int) Math.random() * this.individuals.size());
        }
        int bestIndex = 0;
        for (int i = 1; i < tournament.length; i++) {
            if (tournament[i].getFitness() > tournament[bestIndex].getFitness()) {
                bestIndex = i;
            }
        }
        return tournament[bestIndex];
    }

    public double[][] exchangeMutation(double[][] ind) {
        int[] index = {-1, -1};
        for(int i = 0; i < ind.length; i++){
            index[0] = -1;
            index[1] = -1;
            for (int j = 0; j < ind[i].length; j++) {
                if (Math.random() <= mutationRate) {
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

    public double[][] randomMutation(double[][] ind) {
        for(int i = 0; i < ind.length; i++){
            for (int j = 0; j < ind[i].length; j++) {
                if (Math.random() <= mutationRate) {
                    ind[i][j] += Math.random() - 0.5;
                }
            }
        }
        return ind;
    }

    public double[][] crossover(double[][] ind1, double[][] ind2) {
        double[][] newInd = Arrays.copyOf(ind2, ind2.length);
        for(int i = 0; i < ind1.length; i++){
            for (int j = 0; j < ind1[0].length / 2; j++) {
                newInd[i][j] = ind1[i][j];
            }
        }
        return newInd;
    }
}
