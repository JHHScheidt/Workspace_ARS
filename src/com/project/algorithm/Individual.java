package com.project.algorithm;

import java.io.Serializable;

/**
 * @author Marciano, Rico
 */
public class Individual implements Serializable {

    private double[][] inputWeights;
    private double[][] recurrentWeights;

    public double fitness;

    public Individual(double[][] inputWeights, double[][] recurrentWeigths, double fitness) {
        this.inputWeights = inputWeights;
        this.recurrentWeights = recurrentWeigths;
        this.fitness = fitness;
    }

    public void setInputWeights(double[][] weights) {
        this.inputWeights = weights;
    }
    
    public void setRecurWeights(double[][] weights){
        this.recurrentWeights = weights;
    }

    public double[][] getInputWeights() {
        return this.inputWeights;
    }
    
    public double[][] getRecurWeights(){
        return this.recurrentWeights;
    }

}
