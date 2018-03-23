package com.project.simulation.entity;

import Jama.Matrix;
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

    private Matrix Mu, Sigma;
    private Matrix A, B, C, Q, R;
    private Matrix I;

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
        this.speedLeft = -0.1;
        this.Mu = new Matrix(new double[][] {
				{this.pose.x},
				{this.pose.y},
                {this.pose.theta}});
        this.Sigma = new Matrix(new double[][] {
                {Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian()},
                {Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian()},
                {Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian(), Controller.RANDOM.nextGaussian()}});
        double cSquared = 0.04;
        this.R = new Matrix(new double[][] {
                {cSquared, 0, 0},
                {0, cSquared, 0},
                {0, 0, cSquared}});
        this.Q = new Matrix(new double[][] {
                {cSquared, 0, 0.002},
                {0.002, cSquared, 0.001},
                {0, 0, cSquared}});
        this.A = Matrix.identity(3, 3);
        this.C = Matrix.identity(3, 3);
        this.I = Matrix.identity(3, 3);
//        this.X = new Matrix(new double[][] {{this.pose.x},{this.pose.y}, {this.pose.theta}});
//        this.E = new Matrix(new double[][] {{0.01, 0, 0}, {0, 0.01, 0}, {0, 0, 0.01}});
        this.B = new Matrix(new double[][] {
                {0, 0},
                {0, 0},
                {0, 0}});
    }

    public void predictPosition(double deltaT) {
        double velocity = (this.speedLeft + this.speedRight) / 2;
        double angularVelocity = (this.speedRight - this.speedLeft) / (2 * this.r);
        Matrix muBar;
        if (angularVelocity == 0) {
            muBar = new Matrix(new double[][] {
                    {this.pose.x + deltaT * velocity * Math.cos(this.pose.theta)},
                    {this.pose.y + deltaT * velocity * Math.sin(this.pose.theta)},
                    {this.pose.theta}});
        } else {
            Matrix odometry = new Matrix(new double[][]{
                    {-velocity / angularVelocity * Math.sin(this.pose.theta) + (velocity / angularVelocity * Math.sin(this.pose.theta + angularVelocity * deltaT))},
                    {velocity / angularVelocity * Math.cos(this.pose.theta) - (velocity / angularVelocity * Math.cos(this.pose.theta + angularVelocity * deltaT))},
                    {angularVelocity * deltaT}});
            muBar = this.Mu.plus(odometry);
        }
        Matrix G = new Matrix(new double[][] {
                {1, 0, velocity/angularVelocity*Math.cos(this.pose.theta) - (velocity/angularVelocity*Math.cos(this.pose.theta + angularVelocity*deltaT))},
                {0, 1, velocity/angularVelocity*Math.sin(this.pose.theta) - (velocity/angularVelocity*Math.sin(this.pose.theta + angularVelocity*deltaT))},
                {0, 0, 1}});
        Matrix sigmaBar = G.times(this.Sigma).times(G.transpose()).plus(this.R);
        Matrix[] Ks = new Matrix[this.visibleBeacons.size()];
        Matrix[] Hs = new Matrix[this.visibleBeacons.size()];
        Matrix[] zHats = new Matrix[this.visibleBeacons.size()];
        Matrix delta;
        double q, sqrtQ;
        for (int i = 0; i < this.visibleBeacons.size(); i++) {
            Beacon beacon = this.visibleBeacons.get(i);
            delta = new Matrix(new double[][] {
                    {beacon.x - this.Mu.get(0, 0)},
                    {beacon.y - this.Mu.get(1, 0)}});
            q = delta.transpose().times(delta).get(0, 0);
            sqrtQ = Math.sqrt(q);
            zHats[i] = new Matrix(new double[][] {
                    {sqrtQ},
                    {Math.atan2(delta.get(1, 0), delta.get(0, 0)) - muBar.get(2, 0)},
                    {beacon.id}});
            Hs[i] = new Matrix(new double[][] {
                    {sqrtQ * delta.get(0, 0), -sqrtQ * delta.get(1, 0), 0},
                    {delta.get(1, 0), delta.get(0, 0), -1},
                    {0, 0, 0}}).times(1/q);
            Ks[i] = sigmaBar.times(Hs[i].transpose()).times(Hs[i].times(sigmaBar).times(Hs[i].transpose()).plus(this.Q).inverse());
        }

        Matrix KSummed = new Matrix(new double[][] {
                {0},
                {0},
                {0}});
        Matrix KHSummed = new Matrix(new double[][] {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}});
        for (int i = 0; i < this.visibleBeacons.size(); i++) {
            KSummed.plusEquals(Ks[i].times(new Matrix(new double[][] {
                            {this.visibleBeacons.get(i).distanceToVehicle},
                            {this.visibleBeacons.get(i).angleToVehicle},
                            {this.visibleBeacons.get(i).id}}).minus(zHats[i])));
            KHSummed.plusEquals(Ks[i].times(Hs[i]));
        }

//        System.out.println("yo");
//        this.printMatrix(this.Mu);
        this.Sigma = this.I.minus(KHSummed).times(sigmaBar);
        this.Mu = muBar.plus(KSummed);
//        this.printMatrix(this.Mu);
//        System.out.println("no");



        // assumes you can see at least 3 beacons

//        double prevX = this.pose.x;
//        double prevY = this.pose.y;
//        double prevTheta = this.pose.theta;
//        this.triangulate(this.visibleBeacons.get(0).angleToVehicle, this.visibleBeacons.get(1).angleToVehicle, this.visibleBeacons.get(2).angleToVehicle, this.visibleBeacons.get(0).x, this.visibleBeacons.get(0).y, this.visibleBeacons.get(1).x, this.visibleBeacons.get(1).y, this.visibleBeacons.get(2).x, this.visibleBeacons.get(2).y);
////        this.pose.x += Controller.nextGaussian(0.25);
////        this.pose.y += Controller.nextGaussian(0.25);
////        this.pose.theta += Controller.nextGaussian(0.1);
//
////        this.pose.theta = (Math.atan2(prevY - pose.y, prevX - pose.x) + Math.PI) % Beacon.MAX_RAD;
//
//        // kalman shit
//        double deltaTheta = (this.pose.theta - prevTheta);
//        if (deltaTheta > Math.PI) deltaTheta -= Math.PI * 2;
//        else if (delta < -Math.PI) delta += Math.PI * 2;
//
//        double velocity = Math.sqrt(Math.pow(this.pose.y - prevY, 2) + Math.pow(this.pose.x - prevX, 2)) / delta;
//        double angularVelocity = deltaTheta / delta;
//
//        Matrix zt = new Matrix(new double[][]{
//                {this.pose.x},
//                {this.pose.y},
//                {this.pose.theta}});
//        Matrix ut = new Matrix(new double[][] {
//                {velocity},
//                {angularVelocity}});
//
//        this.B.set(0, 0, delta * Math.cos(this.pose.theta));
//        this.B.set(1, 0, delta * Math.sin(this.pose.theta));
//        this.B.set(2, 1, delta);
//
//        Matrix MuPredict = this.A.times(this.Mu).plus(this.B.times(ut));
//        Matrix SigmaPredict = this.A.times(this.Sigma).times(this.A.transpose()).plus(this.R);
//
//        Matrix Kt = SigmaPredict.times(this.C.transpose()).times(this.C.times(SigmaPredict).times(this.C.transpose()).plus(this.Q).inverse());
//        this.Mu = MuPredict.plus(Kt.times(zt.minus(this.C.times(MuPredict))));
//        this.Sigma = I.minus(Kt.times(this.C)).times(SigmaPredict);

//        System.out.println(this.pose.x + " " + this.Mu.get(0, 0));
//        System.out.println(this.pose.y + " " + this.Mu.get(1, 0));
//        System.out.println(this.pose.theta + " " + this.Mu.get(2, 0));
//        System.out.println();

        this.pose.x = this.Mu.get(0, 0);
        this.pose.y = this.Mu.get(1, 0);
        this.pose.theta = this.Mu.get(2, 0);
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

    public void setMotorInput(double left, double right) {
        this.speedLeft = (left * this.maxSpeed) + Controller.nextGaussian(0.1);
        this.speedRight = (right * this.maxSpeed) + Controller.nextGaussian(0.1);
    }

    private void printMatrix(Matrix matrix) {
        for (int i = 0; i < matrix.getRowDimension(); i++) {
            for (int j = 0; j < matrix.getColumnDimension(); j++) {
                System.out.print(matrix.get(i, j) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}
