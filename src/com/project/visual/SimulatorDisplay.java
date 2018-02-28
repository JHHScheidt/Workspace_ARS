package com.project.visual;

import com.project.simulation.Line;
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

        // vehicle rendering
        Vehicle vehicle = this.simulator.getVehicle();

        g2.drawOval((int) ((vehicle.x - vehicle.r) * this.scale) + this.xOffset, (int) ((vehicle.y - vehicle.r) * this.scale) + this.yOffset, (int) (vehicle.r * 2 * this.scale), (int) (vehicle.r * 2 * this.scale));
        g2.drawLine((int) (vehicle.x * this.scale) + this.xOffset, (int) (vehicle.y * this.scale) + this.yOffset, (int) ((vehicle.x + vehicle.r * Math.cos(vehicle.theta)) * this.scale) + this.xOffset, (int) ((vehicle.y + vehicle.r * Math.sin(vehicle.theta)) * this.scale) + this.yOffset);
        for (double sensorAngle : vehicle.sensorLocations) {
            double startX = (vehicle.x + Math.cos(sensorAngle) * vehicle.r);
            double startY = (vehicle.y + Math.sin(sensorAngle) * vehicle.r);
            g2.drawLine((int) (startX * this.scale + this.xOffset),
                    (int) (startY * this.scale + this.yOffset),
                    (int) ((startX + Math.cos(sensorAngle) * vehicle.sensorRange) * this.scale + this.xOffset),
                    (int) ((startY + Math.sin(sensorAngle) * vehicle.sensorRange) * this.scale + this.yOffset));
        }

        // obstacle rendering
        for (Line obstacle : this.simulator.getObstacles()) {
            g2.drawLine((int) (obstacle.x1 * this.scale + this.xOffset), (int) (obstacle.y1 * this.scale + this.yOffset), (int) (obstacle.x2 * this.scale + this.xOffset), (int) (obstacle.y2 * this.scale + this.yOffset));
        }
    }
}
