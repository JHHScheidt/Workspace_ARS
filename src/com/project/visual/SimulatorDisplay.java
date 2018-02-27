package com.project.visual;

import com.project.simulation.Simulator;
import com.project.simulation.Vehicle;

import javax.swing.*;
import java.awt.*;

public class SimulatorDisplay extends JPanel {

    private Simulator simulator;

    private double scale = 60; // pixels to meter
    private int xOffset = 50, yOffset = 50; // pixel offset

    public SimulatorDisplay(Simulator simulator) {
        this.simulator = simulator;

        this.setPreferredSize(new Dimension((int) this.scale * 15 + this.xOffset * 2, (int) (this.scale * 15 + this.yOffset * 2)));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Vehicle vehicle = this.simulator.getVehicle();

        g2.drawOval((int) ((vehicle.x - vehicle.r) * this.scale) + this.xOffset, (int) ((vehicle.y - vehicle.r) * this.scale) + this.yOffset, (int) (vehicle.r * 2 * this.scale), (int) (vehicle.r * 2 * this.scale));
        g2.drawLine((int) (vehicle.x * this.scale) + this.xOffset, (int) (vehicle.y * this.scale) + this.yOffset, (int) ((vehicle.x + vehicle.r * Math.cos(vehicle.theta)) * this.scale) + this.xOffset, (int) ((vehicle.y + vehicle.r * Math.sin(vehicle.theta)) * this.scale) + this.yOffset);

        for (Shape shape : this.simulator.getObstacles()) {
            Rectangle bound = shape.getBounds();
            g2.drawRect((int) (bound.x * this.scale + this.xOffset), (int) (bound.y * this.scale + this.yOffset), (int) (bound.width * this.scale), (int) (bound.height * this.scale));
        }
    }
}
