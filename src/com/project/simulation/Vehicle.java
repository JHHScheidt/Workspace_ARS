package com.project.simulation;

public class Vehicle {

    public double x, y;
    public double r;
    public double theta;

    public double speedLeft, speedRight; // m/s

    public double[] sensorLocations;
    public double sensorRange;

    public Vehicle(double x, double y, double r, double sensorRange, double... sensorLocations) {
        this(x, y, r);

        this.sensorLocations = sensorLocations;
        this.sensorRange = sensorRange;
    }

    public Vehicle(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;

        this.speedLeft = 1.1;
        this.speedRight = 1.0;
    }
}
