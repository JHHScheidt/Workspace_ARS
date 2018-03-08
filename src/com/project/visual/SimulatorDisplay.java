package com.project.visual;

import com.project.simulation.Line;
import com.project.simulation.Simulator;
import com.project.simulation.Vehicle;
import com.project.simulation.VisualSimulator;

import javax.swing.*;
import java.awt.*;

public class SimulatorDisplay extends JPanel {

    private VisualSimulator simulator;

    private double scale = 60; // pixels to meter
    private int xOffset = 50, yOffset = 50; // pixel offset

    public SimulatorDisplay(VisualSimulator simulator) {
        this.simulator = simulator;

        this.setPreferredSize(new Dimension((int) this.scale * 15 + this.xOffset * 2, (int) (this.scale * 15 + this.yOffset * 2)));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // vehicle rendering
        Vehicle vehicle = this.simulator.getVehicle();
        g2.drawOval((int) ((vehicle.x - vehicle.r) * this.scale) + this.xOffset, (int) ((vehicle.y - vehicle.r) * this.scale) + this.yOffset, (int) (vehicle.r * 2 * this.scale), (int) (vehicle.r * 2 * this.scale));

        // vehicle sensor rendering
        for (Line line : vehicle.sensors) {
            g2.drawLine((int) (line.x1 * this.scale + this.xOffset), (int) (line.y1 * this.scale + this.yOffset), (int) (line.x2 * this.scale + this.xOffset), (int) (line.y2 * this.scale + this.yOffset));
        }

        // vehicle direction rencering
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.RED);
        g2.drawLine((int) (vehicle.x * this.scale) + this.xOffset, (int) (vehicle.y * this.scale) + this.yOffset, (int) ((vehicle.x + vehicle.r * Math.cos(vehicle.theta)) * this.scale) + this.xOffset, (int) ((vehicle.y + vehicle.r * Math.sin(vehicle.theta)) * this.scale) + this.yOffset);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));

        // obstacle rendering
        for (Line obstacle : this.simulator.getObstacles()) {
            g2.drawLine((int) (obstacle.x1 * this.scale + this.xOffset), (int) (obstacle.y1 * this.scale + this.yOffset), (int) (obstacle.x2 * this.scale + this.xOffset), (int) (obstacle.y2 * this.scale + this.yOffset));
        }
    }
}
