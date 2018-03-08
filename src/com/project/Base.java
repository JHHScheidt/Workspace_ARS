package com.project;

import com.project.algorithm.GeneticAlgorithm;
import com.project.algorithm.Individual;
import com.project.simulation.Simulator;
import com.project.simulation.VisualSimulator;
import com.project.simulation.environment.Environment;
import com.project.simulation.environment.Line;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
//            ObjectInputStream in = new ObjectInputStream(new FileInputStream("res/generation49-best.txt"));
//            Individual individual = (Individual) in.readObject();
////            ArrayList<Individual> generation = (ArrayList<Individual>) in.readObject();
//
//            Line[] obstacles = new Line[]{new Line(0, 0, 5, 0), //top wall
//                    new Line(0, 0, 0, 5), //left wall
//                    new Line(1, 2.5, 4, 2.5), //left wall
//                    new Line(5, 0, 5, 5), //right wall
//                    new Line(0, 5, 5, 5)}; //bottom wall
//
//            Environment environment = new Environment(5, 100, obstacles);
//
//            VisualSimulator simulator = new VisualSimulator(environment, individual);
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
            algorithm.start();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}