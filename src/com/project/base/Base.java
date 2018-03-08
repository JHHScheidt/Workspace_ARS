package com.project.base;

import com.project.network.NeuralNetwork;
import com.project.simulation.Simulator;
import com.project.simulation.VisualSimulator;

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
        VisualSimulator simulator = new VisualSimulator();
        simulator.start();
        simulator.run();
    }
}