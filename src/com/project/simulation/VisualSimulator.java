package com.project.simulation;

import com.project.algorithm.Individual;
import com.project.network.NeuralNetwork;
import com.project.simulation.entity.Sensor;
import com.project.simulation.entity.Vehicle;
import com.project.simulation.environment.Environment;
import com.project.simulation.environment.Line;
import com.project.visual.SimulatorDisplay;

import javax.swing.*;

public class VisualSimulator implements  Runnable {

    private boolean running; // is the simulation running
    private SimulatorDisplay display; // GUI

    private NeuralNetwork vehicleNetwork;
    private Vehicle vehicle; // the car

    private Environment environment; // the environment the vehicle is exploring

    public VisualSimulator(Environment environment, Individual individual) {
        int sensors = 12; // here we can change the number of sensors
        double[] sensorLocations = new double[sensors];
        for (int i = 0; i < sensors; i++) {
            sensorLocations[i] = i * (Math.PI * 2) / sensors; // space sensors equally around the car
        }

        this.environment = environment;
        this.vehicleNetwork = new NeuralNetwork(individual.getInputWeights(), individual.getRecurWeights());
        this.vehicle = new Vehicle(2, 2, 0.17, 0.5, sensorLocations);

        this.display = new SimulatorDisplay(this); //setup the GUI

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

        double FPS = 60; // limit gui to 60 fps
        double simulationTime = 0;

        while (this.running) {
            current = System.currentTimeMillis();

            if (current - start > 1000 / FPS) {
                this.display.repaint(); // if visuals are enabled we want to repaint
                update(0.005); // step size for the update
                simulationTime += 0.005;
                start = current;
                System.out.println(simulationTime);
            }
        }
    }

    public void update(double delta) {
        double[] activations = this.vehicleNetwork.compute(this.vehicle.sensorValues);
        this.vehicle.speedLeft = activations[0] * vehicle.maxSpeed;
        this.vehicle.speedRight = activations[1] * vehicle.maxSpeed;

        double newX, newY, newTheta;
        if (this.vehicle.speedLeft == this.vehicle.speedRight) { //just translate car forward in current direction
            double speed = (this.vehicle.speedLeft + this.vehicle.speedRight) / 2;
            newX = vehicle.x + delta * speed * Math.cos(this.vehicle.theta);
            newY = vehicle.y + delta * speed * Math.sin(this.vehicle.theta);
            newTheta = vehicle.theta;
        } else { // calculate the rotation and rotate the car around this point
            double R = this.vehicle.r * (this.vehicle.speedLeft + this.vehicle.speedRight) / (this.vehicle.speedRight - this.vehicle.speedLeft);
            if (this.vehicle.speedRight == - this.vehicle.speedLeft) R = 0;
            if (this.vehicle.speedRight == 0 || this.vehicle.speedLeft == 0) R = this.vehicle.r;

            double ICCX = this.vehicle.x - R * Math.sin(this.vehicle.theta);
            double ICCY = this.vehicle.y + R * Math.cos(this.vehicle.theta);
            double omega = (this.vehicle.speedRight - this.vehicle.speedLeft) / (2 * this.vehicle.r);

            newX = Math.cos(omega * delta) * (this.vehicle.x - ICCX) - (Math.sin(omega * delta) * (this.vehicle.y - ICCY)) + ICCX;
            newY = Math.sin(omega * delta) * (this.vehicle.x - ICCX) + (Math.cos(omega * delta) * (this.vehicle.y - ICCY)) + ICCY;
            newTheta = this.vehicle.theta + omega * delta;
            newTheta %= Math.PI * 2;
        }

        double oldX = this.vehicle.x, oldY = this.vehicle.y;
        this.vehicle.x = newX;
        this.vehicle.y = newY;

        this.vehicle.updateSensors(); // update the sensor data to find obstacles and stuff

        double minDistanceFound;
        double maxDistancePossible = Math.pow(this.vehicle.sensorRange + vehicle.r, 2);
        Sensor[] sensors = this.vehicle.sensors;
        for (int i = 0; i < sensors.length; i++) {
            Sensor sensor = sensors[i];
            minDistanceFound = maxDistancePossible;
            for (Line obstacle : this.environment.obstacles) {
                if (sensor.intersects(obstacle)) {
                    minDistanceFound = Math.pow(sensor.x1 - sensor.xIntersect, 2) + Math.pow(sensor.y1 - sensor.yIntersect, 2);
                }
            }
            sensor.value = minDistanceFound / maxDistancePossible;
            vehicle.sensorValues[0][i] = sensor.value;
        }

        boolean collided = false; // check if car collided with any of the obstacles in the world
        for (Line obstacle : this.environment.obstacles) {
            if (obstacle.intersects(this.vehicle)) {
                collided = true;
                break;
            }
        }

        if (collided) {
            this.vehicle.x = oldX;
            this.vehicle.y = oldY;
        }
        this.vehicle.theta = newTheta;

        // indicate which spot of the environment has been visited
        int environmentX = (int) (this.vehicle.x / this.environment.subdivisionSize);
        int environmentY = (int) (this.vehicle.y / this.environment.subdivisionSize);
        this.environment.grid[environmentX][environmentY] = true;
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

    public boolean isRunning() {
        return this.running;
    }

    public Environment getEnvironment() {
        return this.environment;
    }
}
