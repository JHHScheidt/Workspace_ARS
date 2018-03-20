package com.project.simulation.entity;

import com.project.Controller;
import com.project.simulation.Pose;
import com.project.simulation.environment.Line;

import java.util.*;

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
    public ArrayList<Beacon> visibleBeacons;

    public double maxSpeed = 1;

    public Vehicle(double x, double y, double r, double sensorRange, double... sensorLocations) {
        this(x, y, r);

        this.sensorLocations = sensorLocations;
        this.sensorRange = sensorRange;
        this.sensors = new Sensor[sensorLocations.length];
        for (int i = 0; i < this.sensors.length; i++) {
            this.sensors[i] = new Sensor(0, 0, 0, 0);
        }

        this.sensorValues = new double[1][sensorLocations.length];
    }

    public Vehicle(double x, double y, double r) {
        this.pose = new Pose(x, y, 0);
        this.r = r;
    }

    public void predictPosition() {
        // assumes you can see at least 3 beacons
        Beacon[] selectedBeacons = new Beacon[3];
        for (int i = 0; i < 3; i++)
            selectedBeacons[i] = this.visibleBeacons.remove((int) (Controller.RANDOM.nextDouble() * this.visibleBeacons.size()));

        double prevX = this.pose.x;
        double prevY = this.pose.y;
        this.triangulate(selectedBeacons[0].angleToVehicle, selectedBeacons[1].angleToVehicle, selectedBeacons[2].angleToVehicle, selectedBeacons[0].x, selectedBeacons[0].y, selectedBeacons[1].x, selectedBeacons[1].y, selectedBeacons[2].x, selectedBeacons[2].y);

        this.pose.theta = (Math.atan2(prevY - pose.y, prevX - pose.x) + Math.PI) % Beacon.MAX_RAD;

//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//        double maxRad = 2 * Math.PI;
//        double angle;
//        double distance;
//
//        // assumes you can see at least 3 beacons
//        Beacon[] selectedBeacons = new Beacon[3];
//        for (int i = 0; i < 3; i++)
//            selectedBeacons[i] = this.visibleBeacons.remove((int) (Controller.RANDOM.nextDouble() * this.visibleBeacons.size()));
//
//        double[][] intersectionPoints = new double[selectedBeacons.length][4];
//        for (int i = 0; i < selectedBeacons.length; i++) {
//            Beacon one = selectedBeacons[i];
//            Beacon two = selectedBeacons[(i + 1) % selectedBeacons.length];
//
//            double d = Math.sqrt(Math.pow(one.x - two.x, 2) + Math.pow(one.y - two.y, 2));
//            double a = (one.distanceToVehicle * one.distanceToVehicle - two.distanceToVehicle * two.distanceToVehicle + d * d) / (2 * d);
//            double h = one.distanceToVehicle * one.distanceToVehicle - a * a;
//
//            double x = (one.x + a * (two.x - one.x) / d);
//            double y = (one.y + a * (two.y - one.y) / d);
//
//            intersectionPoints[i][0] = x + h * (two.y - one.y) / d;
//            intersectionPoints[i][1] = y - h * (two.x - one.x) / d;
//
//            intersectionPoints[i][2] = x - h * (two.y - one.y) / d;
//            intersectionPoints[i][3] = y + h * (two.x - one.x) / d;
//        }
//
//        int[] selectedPoints = new int[3];
//        double shortertestDistance = 10000;
    }

    public void triangulate(double a1, double a2, double a3, double x1, double y1, double x2, double y2, double x3, double y3){
        double cot12 = bound(1.0 / Math.tan(a2 - a1));
        double cot23 = bound(1.0 / Math.tan(a3 - a2));
        double cot31 = bound(( 1.0 - cot12 * cot23 ) / ( cot12 + cot23 ));

        double dx1 = x1 - x2;
        double dy1 = y1 - y2;

        double dx3 = x3 - x2;
        double dy3 = y3 - y2;

        double c12x = dx1 + cot12 * dy1;
        double c12y = dy1 - cot12 * dx1;

        double c23x = dx3 - cot23 * dy3;
        double c23y = dy3 + cot23 * dx3;

        double c31x = (dx3 + dx1) + cot31 * (dy3 - dy1);
        double c31y = (dy3 + dy1) - cot31 * (dx3 - dx1);

        double k31 = (dx3 * dx1) + (dy3 * dy1) + cot31 * ( (dy3 * dx1) - (dx3 * dy1) );

        double D = (c12x - c23x) * (c23y - c31y) - (c23x - c31x) * (c12y - c23y);

        double invD = 1.0 / D;

        double K = k31 * invD;

        this.pose.x = K * (c12y - c23y) + x2 ;
        this.pose.y = K * (c23x - c12x) + y2 ;
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
