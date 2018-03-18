package com.project.simulation.entity;

import com.project.simulation.Pose;
import com.project.simulation.environment.Line;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Joshua, Simon
 */
public class Vehicle {

    private static final double MAX_COT = 100000000;

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

    public void updatePosition() {
        double maxRad = 2 * Math.PI;
        double angle;
        double distance;
        for (Beacon b : visibleBeacons) {
            angle = (Math.atan2(b.y - this.pose.y, b.x - this.pose.x) + maxRad) % maxRad;
            distance = b.distance(this);
            // TODO FIX THIS
        }
    }

    // There are 3 cases, maybe we need those
    public void triangulate(double a1, double a2, double a3, double x1, double y1, double x2, double y2, double x3, double y3){
        double cot12 = bound(1.0 / Math.tan(a2 - a1));
        double cot23 = bound(1.0 / Math.tan(a3 - a2));
        double cot31 = bound(( 1.0 - cot12 * cot23 ) / ( cot12 + cot23 ));

        double dx1 = x1 - x2;
        double dy1 = y1 - y2;

        double dx3 = x2 - x2;
        double dy3 = y3 - y2;

        double c12x = dx1 + cot12 * dy1;
        double c12y = dy1 - cot12 * dx1;

        double c23x = dx3 - cot23 * dy3;
        double c23y = dy3 + cot23 * dx3;

        double c31x = (dx3 + dx1) + cot31 * (dy3 - dy1) ;
        double c31y = (dy3 + dy1) - cot31 * (dx3 - dx1) ;
        double k31 = (dx3 * dx1) + (dy3 * dy1) + cot31 * ( (dy3 * dx1) - (dx3 * dy1) ) ;

        double D = (c12x - c23x) * (c23y - c31y) - (c23x - c31x) * (c12y - c23y) ;
        double invD = 1.0 / D ;
        double K = k31 * invD ;

        this.pose.x = K * (c12y - c23y) + x2 ;
        this.pose.y = K * (c23x - c12x) + y2 ;

//        double Q = Math.abs( invD ) ;
    }

    private double bound(double value){
        if (value > MAX_COT){
            return MAX_COT;
        }else if (value < -MAX_COT){
            return -MAX_COT;
        }else{
            return value;
        }
    }
}
