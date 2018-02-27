package com.project.simulation;

import com.project.visual.SimulatorDisplay;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

public class Simulator implements  Runnable {

    private boolean running;
    private boolean visuals;
    private SimulatorDisplay display;

    private Vehicle vehicle;
    private Shape[] obstacles;
    private Ellipse2D.Double vehicleShape;

    public Simulator(boolean visuals) {
        this.visuals = visuals;

        int sensors = 12;
        double[] sensorLocations = new double[sensors];
        for (int i = 0; i < sensors; i++) {
            sensorLocations[i] = i * (Math.PI * 2) / sensors;
        }

        this.vehicle = new Vehicle(5, 5, 0.2, sensors);
        this.vehicleShape = new Ellipse2D.Double(4.8,4.8,0.4, 0.4);
        this.obstacles = new Shape[]{new Line2D.Double(0, 0, 15, 0), //top
                                    new Line2D.Double(0, 0, 0, 15), //left
                                    new Line2D.Double(15, 0, 15, 15), //right
                                    new Line2D.Double(0, 15, 15, 15)}; //bottom
        this.display = new SimulatorDisplay(this);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(display);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long current;

        double FPS = 60;

        while (this.running) {
            current = System.currentTimeMillis();

            if (current - start > 1000 / FPS) {
                if (this.visuals) this.display.repaint();
                start = current;
            }

            update(0.000001);
        }
    }

    public void update(double delta) {

        double newX, newY, newTheta;
        if (this.vehicle.speedLeft == this.vehicle.speedRight) {
            double speed = (this.vehicle.speedLeft + this.vehicle.speedRight) / 2;
            newX = vehicle.x + delta * speed * Math.cos(this.vehicle.theta);
            newY = vehicle.y + delta * speed * Math.sin(this.vehicle.theta);
            newTheta = vehicle.theta;
        } else {
            double R = this.vehicle.r * (this.vehicle.speedLeft + this.vehicle.speedRight) / (this.vehicle.speedRight - this.vehicle.speedLeft);
            if (this.vehicle.speedRight == - this.vehicle.speedLeft) R = 0;
            if (this.vehicle.speedRight == 0 || this.vehicle.speedLeft == 0) R = this.vehicle.r;

            double ICCX = this.vehicle.x - R * Math.sin(this.vehicle.theta);
            double ICCY = this.vehicle.y + R * Math.cos(this.vehicle.theta);
            double omega = (this.vehicle.speedRight - this.vehicle.speedLeft) / (2 * this.vehicle.r);

            newX = Math.cos(omega * delta) * (this.vehicle.x - ICCX) - (Math.sin(omega * delta) * (this.vehicle.y - ICCY)) + ICCX;
            newY = Math.sin(omega * delta) * (this.vehicle.x - ICCX) + (Math.cos(omega * delta) * (this.vehicle.y - ICCY)) + ICCY;
            newTheta = vehicle.theta + omega * delta;
            newTheta %= Math.PI * 2;
        }

        this.vehicleShape.setFrame(newX - vehicle.r, newY - vehicle.r, vehicle.r * 2, vehicle.r * 2);

        boolean collided = false;
        for (Shape shape : this.obstacles)
            if (shape.intersects(vehicleShape.getBounds2D())) {
                collided = true;
                System.out.println(" fucking shit kut hoere zooi");
                break;
            }

        if (collided) this.running = false;
        else {
            vehicle.x = newX;
            vehicle.y = newY;
            vehicle.theta = newTheta;
        }
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public Shape[] getObstacles() {
        return this.obstacles;
    }
}
