package com.project.network;

import Jama.Matrix;

/**
 * @author Joshua, Simon
 */
public class NeuralNetwork {

    // inputWeights = 12, 2
    // recurrentWeights = 2, 2
    // input = 1, 12

    private Matrix input, inputWeights;
    private Matrix state, recurrentWeights;

    public NeuralNetwork(double[][] inputWeights, double[][] recurrentWeights) {
        this.inputWeights = new Matrix(inputWeights);
        this.state = new Matrix(new double[][]{{0.5, 0.5}});
        this.recurrentWeights = new Matrix(recurrentWeights);
    }

    public double[] compute(double[][] inputs) {
//        System.out.println(Arrays.deepToString(inputs));
        this.input = new Matrix(inputs);
        this.state = this.state.times(this.recurrentWeights).plus(this.input.times(this.inputWeights));

        double[] stateArray = this.state.getRowPackedCopy();
        stateArray[0] = Math.atan(stateArray[0]);
        stateArray[1] = Math.atan(stateArray[1]);
        this.state.set(0, 0, stateArray[0]);
        this.state.set(0, 1, stateArray[1]);

        return stateArray;
    }
}
