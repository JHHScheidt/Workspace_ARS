package com.project.visual;

import com.project.simulation.Pose;
import com.project.simulation.Simulator;
import com.project.simulation.environment.Line;
import com.project.simulation.entity.Vehicle;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;

/**
 * @author Simon, Rico
 */
public class SimulatorDisplay extends JPanel {

    private Simulator simulator;

    private double scale = 200; // pixels to meter
    private int xOffset = 50, yOffset = 50; // pixel offset

    private Color transparantFill;

    public SimulatorDisplay(Simulator simulator) {
        this.simulator = simulator;

        this.setPreferredSize(new Dimension((int) this.scale * 5 + this.xOffset * 2, (int) (this.scale * 5 + this.yOffset * 2)));
        this.transparantFill = new Color(255, 0, 0, 50);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Vehicle vehicle = this.simulator.getVehicle();
        // render previous vehicle positions
        for (Iterator<Pose> it = vehicle.pastPositions.iterator(); it.hasNext(); ) {
            Pose pose = it.next();

            g2.setColor(this.transparantFill);
            g2.fillOval((int) ((pose.x - vehicle.r) * this.scale) + this.xOffset, (int) ((pose.y - vehicle.r) * this.scale) + this.yOffset, (int) (vehicle.r * 2 * this.scale), (int) (vehicle.r * 2 * this.scale));

            g2.setColor(Color.RED.darker());
            g2.drawLine((int) (pose.x * this.scale) + this.xOffset, (int) (pose.y * this.scale) + this.yOffset, (int) ((pose.x + vehicle.r * Math.cos(pose.theta)) * this.scale) + this.xOffset, (int) ((pose.y + vehicle.r * Math.sin(pose.theta)) * this.scale) + this.yOffset);
        }

        // vehicle rendering
        g2.setColor(Color.BLUE);
        g2.fillOval((int) ((vehicle.x - vehicle.r) * this.scale) + this.xOffset, (int) ((vehicle.y - vehicle.r) * this.scale) + this.yOffset, (int) (vehicle.r * 2 * this.scale), (int) (vehicle.r * 2 * this.scale));

        g2.setColor(Color.BLACK);
        // vehicle sensor rendering
        for (Line line : vehicle.sensors) {
            g2.drawLine((int) (line.x1 * this.scale + this.xOffset), (int) (line.y1 * this.scale + this.yOffset), (int) (line.x2 * this.scale + this.xOffset), (int) (line.y2 * this.scale + this.yOffset));
        }

        // vehicle direction rendering
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.RED);
        g2.drawLine((int) (vehicle.x * this.scale) + this.xOffset, (int) (vehicle.y * this.scale) + this.yOffset, (int) ((vehicle.x + vehicle.r * Math.cos(vehicle.theta)) * this.scale) + this.xOffset, (int) ((vehicle.y + vehicle.r * Math.sin(vehicle.theta)) * this.scale) + this.yOffset);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1));

        // obstacle rendering
        for (Line obstacle : this.simulator.getEnvironment().obstacles) {
            g2.drawLine((int) (obstacle.x1 * this.scale + this.xOffset), (int) (obstacle.y1 * this.scale + this.yOffset), (int) (obstacle.x2 * this.scale + this.xOffset), (int) (obstacle.y2 * this.scale + this.yOffset));
        }
    }
}
