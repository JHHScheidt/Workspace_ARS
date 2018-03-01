package com.project.simulation;

public class Vehicle {

    public double x, y;
    public double r;
    public double theta;

    public double speedLeft, speedRight; // m/s

    public double[] sensorLocations;
    public double sensorRange;
    public Sensor[] sensors;

    public Vehicle(double x, double y, double r, double sensorRange, double... sensorLocations) {
        this(x, y, r);

        this.sensorLocations = sensorLocations;
        this.sensorRange = sensorRange;
        this.sensors = new Sensor[sensorLocations.length];
        for (int i = 0; i < this.sensors.length; i++) {
            this.sensors[i] = new Sensor(0, 0, 0, 0);
        }
        this.updateSensors();
    }

    public Vehicle(double x, double y, double r) {
        this.x = x;
        this.y = y;
        this.r = r;

        this.speedLeft = 11;
        this.speedRight = 10;
    }

    public void updateSensors() {
        double sensorAngle;
        for (int i = 0; i < this.sensors.length; i++) {
            sensorAngle = this.sensorLocations[i];
            Line sensor = this.sensors[i];
            sensor.x1 = this.x;
            sensor.x2 = this.x + Math.cos(sensorAngle) * (this.sensorRange + this.r);
            sensor.y1 = this.y;
            sensor.y2 = this.y + Math.sin(sensorAngle) * (this.sensorRange + this.r);
        }
    }
}
