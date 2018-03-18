package com.project.simulation;

import com.project.algorithm.Individual;
import com.project.network.NeuralNetwork;
import com.project.simulation.entity.Vehicle;
import com.project.simulation.entity.Sensor;
import com.project.simulation.environment.Environment;
import com.project.simulation.environment.Line;
import com.project.visual.SimulatorDisplay;

import javax.swing.*;
import java.util.concurrent.Callable;

/**
 * @author Marciano, Rico, Joshua, Simon
 */
public class Simulator implements Callable<Double> {

    private SimulatorDisplay display; // GUI
    private boolean visuals;

    private double simulationTime;
    private double timePassed;
    private double timeSincePositionStored;

    private Environment environment; // the environment the vehicle is exploring

    private NeuralNetwork vehicleNetwork;
    private Vehicle vehicle; // the car
    private Pose actualVehiclePose;

    private int previousX, previousY;

    public int id;

    public Simulator(int id, boolean visuals) {
        this.visuals = visuals;
        if (this.visuals) {
            this.display = new SimulatorDisplay(this);

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(display);
            frame.pack();
            frame.setVisible(true);
        }
        int sensors = 12; // here we can change the number of sensors
        double[] sensorLocations = new double[sensors];
        for (int i = 0; i < sensors; i++) {
            sensorLocations[i] = i * (Math.PI * 2) / sensors; // space sensors equally around the car
        }

        this.vehicle = new Vehicle(0.5, 0.5, 0.17, 0.5, sensorLocations);
        this.id = id;
    }

    public void init(Individual individual, Environment environment, double startX, double startY, long simulationTime) {
        this.vehicleNetwork = new NeuralNetwork(individual.getInputWeights(), individual.getRecurWeights());

        this.vehicle.pose.x = startX;
        this.vehicle.pose.y = startY;
        
        this.previousX = -1;
        this.previousY = -1;

        this.environment = environment;
        this.simulationTime = simulationTime;

        // sensor initialisation for first update
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
            sensor.value = Math.sqrt(minDistanceFound) / Math.sqrt(maxDistancePossible);
            vehicle.sensorValues[0][i] = sensor.value;
        }
    }

    public void run() {
        this.timePassed = 0;
        double updateInterval = 0.025;

        long start = System.currentTimeMillis();
        long current;
        double FPS = 60; // limit gui to 60 fps


        while (this.timePassed < this.simulationTime || this.visuals) {
            if (this.visuals) {
                current = System.currentTimeMillis();
                if (current - start > 1000 / FPS) {
                    update(0.025); // step size for the update
                    this.display.repaint(); // if visuals are enabled we want to repaint
                    start = current;
                }
            } else {
                update(updateInterval); // step size for the update
            }
        }
    }

    public void update(double delta) {
        this.timePassed += delta;
        this.timeSincePositionStored += delta;

        double[] activations = this.vehicleNetwork.compute(this.vehicle.sensorValues);
        this.vehicle.speedLeft = activations[0] * vehicle.maxSpeed;
        this.vehicle.speedRight = activations[1] * vehicle.maxSpeed;

        this.vehicle.visibleBeacons = this.environment.getVisibleBeacons(this.actualVehiclePose.x, this.actualVehiclePose.y);
        this.vehicle.updatePosition();

        double newX, newY, newTheta;
        if (this.vehicle.speedLeft == this.vehicle.speedRight) { //just translate car forward in current direction
            double speed = (this.vehicle.speedLeft + this.vehicle.speedRight) / 2;
            newX = vehicle.pose.x + delta * speed * Math.cos(this.vehicle.pose.theta);
            newY = vehicle.pose.y + delta * speed * Math.sin(this.vehicle.pose.theta);
            newTheta = vehicle.pose.theta;
        } else { // calculate the rotation and rotate the car around this point
            double R = this.vehicle.r * (this.vehicle.speedLeft + this.vehicle.speedRight) / (this.vehicle.speedRight - this.vehicle.speedLeft);
            if (this.vehicle.speedRight == - this.vehicle.speedLeft) R = 0;
            if (this.vehicle.speedRight == 0 || this.vehicle.speedLeft == 0) R = this.vehicle.r;

            double ICCX = this.vehicle.pose.x - R * Math.sin(this.vehicle.pose.theta);
            double ICCY = this.vehicle.pose.y + R * Math.cos(this.vehicle.pose.theta);
            double omega = (this.vehicle.speedRight - this.vehicle.speedLeft) / (2 * this.vehicle.r);

            newX = Math.cos(omega * delta) * (this.vehicle.pose.x - ICCX) - (Math.sin(omega * delta) * (this.vehicle.pose.y - ICCY)) + ICCX;
            newY = Math.sin(omega * delta) * (this.vehicle.pose.x - ICCX) + (Math.cos(omega * delta) * (this.vehicle.pose.y - ICCY)) + ICCY;
            newTheta = this.vehicle.pose.theta + omega * delta;
            newTheta %= Math.PI * 2;
        }

        double oldX = this.vehicle.pose.x, oldY = this.vehicle.pose.y;
        this.vehicle.pose.x = newX;
        this.vehicle.pose.y = newY;

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
            this.vehicle.pose.x = oldX;
            this.vehicle.pose.y = oldY;
        }
        this.vehicle.pose.theta = newTheta;

        // store vehicle previous position
        if (this.visuals) {
            if (this.timeSincePositionStored > 0.3) {
                this.timeSincePositionStored = 0;
                if (this.vehicle.pastPositions.size() == 25)
                    this.vehicle.pastPositions.poll();
                this.vehicle.pastPositions.offer(new Pose(this.vehicle.pose.x, this.vehicle.pose.y, this.vehicle.pose.theta));
            }
        }

        // indicate which spot of the environment has been visited
        int environmentX = (int) (this.vehicle.pose.x / this.environment.subdivisionSize);
        int environmentY = (int) (this.vehicle.pose.y / this.environment.subdivisionSize);

//        if (environmentX != this.previousX && environmentY != this.previousY) {
            double ble = this.vehicle.r / this.environment.subdivisionSize;
            int ceil = (int) Math.ceil(ble);
            for (int i = environmentX - ceil; i <= environmentX + ceil; i++) {
                for (int j = environmentY - ceil; j <= environmentY + ceil; j++) {
                    if (Math.pow(i - environmentX, 2) + Math.pow(j - environmentY, 2) <= ble*ble) {
                        this.environment.grid[j][i]++;
                    }
                }
            }

//        	this.previousX = environmentX;
//        	this.previousY = environmentY;
//        }
    }

    @Override
    public Double call() {
        this.run();

        double fitness = 0;
        for (int[] array : this.environment.grid) {
            for (int value : array) {
                if (value > 0) fitness += 1;
                if (value > 1) fitness -= 0.3333 * (value - 1);
            }
        }

        return fitness;
    }

    public Vehicle getVehicle() {
        return this.vehicle;
    }

    public Environment getEnvironment() {
        return this.environment;
    }
}
