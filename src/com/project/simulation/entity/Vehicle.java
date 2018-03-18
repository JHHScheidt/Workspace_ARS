package com.project.simulation.entity;

import com.project.simulation.Pose;
import com.project.simulation.environment.Line;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Joshua, Simon
 */
public class Vehicle {

    public double r; // distance between center of the car and the wheel, ie 1/2 distance between the wheels
    public Pose pose;

    public double speedLeft, speedRight; // m/s

    public double[] sensorLocations; // locations of sensors on the car
    public double sensorRange; // range of the sensors
    public Sensor[] sensors; // sensors on the car
    public double[][] sensorValues; // 2 dimensional because of JAMA library requirements
    public Beacon[] visibleBeacons;

    public double maxSpeed = 1;

    public Queue<Pose> pastPositions;

    public Vehicle(double x, double y, double r, double sensorRange, double... sensorLocations) {
        this(x, y, r);

        this.pastPositions = new LinkedList<>();

        this.sensorLocations = sensorLocations;
        this.sensorRange = sensorRange;
        this.sensors = new Sensor[sensorLocations.length];
        for (int i = 0; i < this.sensors.length; i++) {
            this.sensors[i] = new Sensor(0, 0, 0, 0);
        }

        this.sensorValues = new double[1][sensorLocations.length];

        this.updateSensors();
    }

    public Vehicle(double x, double y, double r) {
        this.pose = new Pose(x,y, 0);
        this.r = r;
    }

    public void updateSensors() {
        double sensorAngle;
        for (int i = 0; i < this.sensors.length; i++) { // update locations for the sensors
            sensorAngle = this.sensorLocations[i] + this.pose.theta;
            Line sensor = this.sensors[i];
            sensor.x1 = this.pose.x + Math.cos(sensorAngle) * this.r;
            sensor.x2 = this.pose.x + Math.cos(sensorAngle) * this.sensorRange;
            sensor.y1 = this.pose.y + Math.sin(sensorAngle) * this.r;
            sensor.y2 = this.pose.y + Math.sin(sensorAngle) * this.sensorRange;
        }
    }

    public void updatePosition(){
        double maxRad = 2 * Math.PI;
        double angle;
        double distance;
        for (Beacon b : visibleBeacons) {
            angle = (Math.atan2(b.y - this.pose.y, b.x - this.pose.x) + maxRad) % maxRad;
            distance = b.distance(this);
        }
    }
}
