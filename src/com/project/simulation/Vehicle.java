package com.project.simulation;

public class Vehicle {

    public double x, y;
    public double r;
    public double theta;

    public double speedLeft, speedRight; // m/s

    public double[] sensorLocations;

    public Vehicle(double x, double y, double r, double... sensorLocations) {
        this.x = x;
        this.y = y;
        this.r = r;

        this.speedLeft = 1.1;
        this.speedRight = 1.0;

        this.sensorLocations = sensorLocations;

        this.theta = Math.PI / 4;
    }
}
