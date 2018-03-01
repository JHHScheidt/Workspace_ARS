package com.project.simulation;

import com.project.visual.SimulatorDisplay;

import javax.swing.*;
import java.util.Arrays;

public class Simulator implements  Runnable {

    private boolean running;
    private boolean visuals;
    private SimulatorDisplay display;

    private Vehicle vehicle;
    private Line[] obstacles;

    public Simulator(boolean visuals) {
        this.visuals = visuals;

        int sensors = 12;
        double[] sensorLocations = new double[sensors];
        for (int i = 0; i < sensors; i++) {
            sensorLocations[i] = i * (Math.PI * 2) / sensors;
        }

        this.vehicle = new Vehicle(5, 5, 0.2, 0.5, sensorLocations);
        this.obstacles = new Line[]{new Line(0, 0, 15, 0), //top
                                    new Line(0, 0, 0, 15), //left
                                    new Line(15, 0, 15, 15), //right
                                    new Line(0, 15, 15, 15)}; //bottom
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
                update(0.00166666);
                start = current;
            }

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

        this.vehicle.updateSensors();

        double minDistanceFound;
        double maxDistancePossible = Math.pow(this.vehicle.sensorRange + vehicle.r, 2);
        for (Sensor sensor : this.vehicle.sensors) {
            minDistanceFound = maxDistancePossible;
            for (Line obstacle : this.obstacles) {
                if (sensor.intersects(obstacle)) {
                    minDistanceFound = Math.pow(sensor.x1 - sensor.xIntersect, 2) + Math.pow(sensor.y1 - sensor.yIntersect, 2);
                }
            }
            sensor.value = minDistanceFound / maxDistancePossible;

            System.out.print(sensor.value + " ");
        }
        System.out.println();

        boolean collided = false;
        for (Line obstacle : this.obstacles) {
            if (obstacle.intersects(this.vehicle)) {
                collided = true;
                break;
            }
        }

        if (collided) this.running = false;
        else {
            this.vehicle.x = newX;
            this.vehicle.y = newY;
            this.vehicle.theta = newTheta;
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

    public Line[] getObstacles() {
        return this.obstacles;
    }
}
