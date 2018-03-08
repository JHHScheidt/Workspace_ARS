/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.project.algorithm;

import java.io.Serializable;

/**
 * @author Marciano, Rico
 */
public class Individual implements Serializable {

    private double[][] inputWeights;
    private double[][] recurrentWeights;
    private double fitness;

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
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double[][] getInputWeights() {
        return this.inputWeights;
    }
    
    public double[][] getRecurWeights(){
        return this.recurrentWeights;
    }
    
    public double getFitness() {
        return this.fitness;
    }
}
