package com.project.simulation;

public class Vehicle {

    public double x, y; // x, y location of the car
    public double r; // distance between center of the car and the wheel, ie 1/2 distance between the wheels
    public double theta; // rotation of the car

    public double speedLeft, speedRight; // m/s

    public double[] sensorLocations; // locations of sensors on the car
    public double sensorRange; // range of the sensors
    public Sensor[] sensors; // sensors on the car

    public double maxSpeed = 1;

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

        this.speedLeft = 1;
        this.speedRight = -1;
    }

    public void updateSensors() {
        double sensorAngle;
        for (int i = 0; i < this.sensors.length; i++) { // update locations for the sensors
            sensorAngle = this.sensorLocations[i] + this.theta;
            Line sensor = this.sensors[i];
            sensor.x1 = this.x;
            sensor.x2 = this.x + Math.cos(sensorAngle) * (this.sensorRange + this.r);
            sensor.y1 = this.y;
            sensor.y2 = this.y + Math.sin(sensorAngle) * (this.sensorRange + this.r);
        }
    }
}
