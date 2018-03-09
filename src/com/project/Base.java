package com.project;

import com.project.algorithm.GeneticAlgorithm;
import com.project.algorithm.Individual;
import com.project.simulation.VisualSimulator;
import com.project.simulation.environment.Environment;
import com.project.simulation.environment.Line;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ExecutionException;

/**
 * @author Marciano, Rico, Joshua, Simon
 */
public class Base {

    public Base() {
//        chart = Chart3DFactory.createSurfaceChart(
//                "SurfaceRendererDemo1",
//                "y = cos(x) * sin(z)",
//                function, "X", "Y", "Z");
//        chart.setViewPoint(new ViewPoint3D(0, Math.PI, 50,  - Math.PI / 2));
//        XYZPlot plot = (XYZPlot) chart.getPlot();
//        plot.setDimensions(new Dimension3D(10, 5, 10));
//        ValueAxis3D xAxis = plot.getXAxis();
//        xAxis.setRange(-2, 2);
//        ValueAxis3D zAxis = plot.getZAxis();
//        zAxis.setRange(-1, 3);
//        SurfaceRenderer renderer = (SurfaceRenderer) plot.getRenderer();
//        renderer.setDrawFaceOutlines(false);
//        renderer.setColorScale(new GradientColorScale(new Range(0, 2500),
//                Color.BLUE, Color.RED));
    }

    public static void main(String[] args) {
//        try {
//            ObjectInputStream in = new ObjectInputStream(new FileInputStream("res/generation41-best.txt"));
//            Individual individual = (Individual) in.readObject();
////            ArrayList<Individual> generation = (ArrayList<Individual>) in.readObject();
//
//            Environment environment = Environment.MAZE_JOSHUA;
//            environment.flip();
//
//            VisualSimulator simulator = new VisualSimulator(environment, individual, environment.startingLocations[0]);
////            VisualSimulator simulator = new VisualSimulator(environment, generation.get(0));
//            simulator.start();
//            simulator.run();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }


        try {
            GeneticAlgorithm algorithm = new GeneticAlgorithm(100, 12);
//            GeneticAlgorithm algorithm = new GeneticAlgorithm(100, 12, "res/maze/generation900-all.txt");
            algorithm.start();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}