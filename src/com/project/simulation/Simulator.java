package com.project.simulation;

import com.project.algorithm.Individual;
import com.project.simulation.entity.Vehicle;
import com.project.simulation.entity.Sensor;
import com.project.simulation.environment.Environment;
import com.project.simulation.environment.Line;

import java.util.concurrent.Callable;

public class Simulator implements Callable<Double> {

    private boolean running; // is the simulation running

    private Environment environment; // the environment the vehicle is exploring
    private Vehicle vehicle; // the car

    private long simulationTime;

    public int id;

    public Simulator(int id) {
        int sensors = 12; // here we can change the number of sensors
        double[] sensorLocations = new double[sensors];
        for (int i = 0; i < sensors; i++) {
            sensorLocations[i] = i * (Math.PI * 2) / sensors; // space sensors equally around the car
        }

        this.vehicle = new Vehicle(0.5, 0.5, 0.17, 0.5, sensorLocations);
        this.id = id;
    }

    public void init(Individual individual, Environment environment, double startX, double startY, long simulationTime) {
        this.vehicle.x = startX;
        this.vehicle.y = startY;

        this.environment = environment;
        this.simulationTime = simulationTime;
    }

    public void run() {
        double timePassed = 0;
        double updateInterval = 0.005;

        this.running = true;

        while (timePassed < this.simulationTime) {
            update(updateInterval); // step size for the update
            timePassed += updateInterval;
        }

        this.running = false;
    }

    public void update(double delta) {
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
        for (Sensor sensor : this.vehicle.sensors) {
            minDistanceFound = maxDistancePossible;
            for (Line obstacle : this.environment.obstacles) {
                if (sensor.intersects(obstacle)) {
                    minDistanceFound = Math.pow(sensor.x1 - sensor.xIntersect, 2) + Math.pow(sensor.y1 - sensor.yIntersect, 2);
                }
            }
            sensor.value = minDistanceFound / maxDistancePossible;
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

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public Double call() {
        this.run();

        double fitness = 0;
        for (boolean[] array : this.environment.grid) {
            for (boolean value : array) {
                if (value) fitness += 1;
            }
        }

        return fitness;
    }
}
