package com.project.simulation.entity;

import Jama.Matrix;
import com.project.Controller;
import com.project.simulation.Pose;
import com.project.simulation.Simulator;

import java.util.ArrayList;

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
        this.Mu = new Matrix(new double[][]{
                {this.pose.x},
                {this.pose.y},
                {this.pose.theta}});
        this.Sigma = new Matrix(new double[][]{
                {Controller.nextGaussian(0.1), Controller.nextGaussian(0.1), Controller.nextGaussian(0.1)},
                {Controller.nextGaussian(0.1), Controller.nextGaussian(0.1), Controller.nextGaussian(0.1)},
                {Controller.nextGaussian(0.1), Controller.nextGaussian(0.1), Controller.nextGaussian(0.1)}});
        double cSquared = 0.04;
        this.R = new Matrix(new double[][]{
                {cSquared, 0, 0},
                {0, cSquared, 0},
                {0, 0, cSquared}});
        this.Q = new Matrix(new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, cSquared}});
        this.A = Matrix.identity(3, 3);
        this.C = Matrix.identity(3, 3);
        this.I = Matrix.identity(3, 3);
//        this.X = new Matrix(new double[][] {{this.pose.x},{this.pose.y}, {this.pose.theta}});
//        this.E = new Matrix(new double[][] {{0.01, 0, 0}, {0, 0.01, 0}, {0, 0, 0.01}});
        this.B = new Matrix(new double[][]{
                {0, 0},
                {0, 0},
                {0, 0}});
    }

    public void predictPosition(double dt) {
        // assumes you can see at least 3 beacons

        double prevX = this.pose.x;
        double prevY = this.pose.y;
        double prevTheta = this.pose.theta;

        // kalman shit
        double deltaTheta = (this.pose.theta - prevTheta);
        if (deltaTheta > Math.PI) deltaTheta -= Math.PI * 2;
        else if (deltaTheta < -Math.PI) deltaTheta += Math.PI * 2;

        double velocity = Math.sqrt(Math.pow(this.pose.y - prevY, 2) + Math.pow(this.pose.x - prevX, 2)) / dt;
        double angularVelocity = deltaTheta / dt;

        Matrix ut = new Matrix(new double[][] {
                {velocity},
                {angularVelocity}});

        this.B.set(0, 0, dt * Math.cos(this.pose.theta));
        this.B.set(1, 0, dt * Math.sin(this.pose.theta));
        this.B.set(2, 1, dt);

        // prediction
        Matrix MuPredict = this.A.times(this.Mu).plus(this.B.times(ut));
        Matrix SigmaPredict = this.A.times(this.Sigma).times(this.A.transpose()).plus(this.R);

        // correction
        int combinations = 0;
        double xObservation = 0, yObservation = 0;
        for (int i = 0; i < this.visibleBeacons.size(); i++) {
            for (int j = i + 1; j < this.visibleBeacons.size(); j++) {
                for (int k = j + 1; k < this.visibleBeacons.size(); k++) {
                    combinations++;
                    this.triangulate(this.visibleBeacons.get(i).angleToVehicle, this.visibleBeacons.get(j).angleToVehicle, this.visibleBeacons.get(k).angleToVehicle, this.visibleBeacons.get(i).x, this.visibleBeacons.get(i).y, this.visibleBeacons.get(j).x, this.visibleBeacons.get(j).y, this.visibleBeacons.get(k).x, this.visibleBeacons.get(k).y);
                    xObservation += pose.x;
                    yObservation += pose.y;
                }
            }
        }
        xObservation /= combinations;
        yObservation /= combinations;
//        this.pose.theta = (Math.atan2(prevY - pose.y, prevX - pose.x) + Math.PI) % Beacon.MAX_RAD;
        double thetaObservation = 0;
        for (Beacon beacon : this.visibleBeacons) {
            double angleWithXAxis = (2 * Math.PI + Math.atan2(beacon.y - yObservation, beacon.x - xObservation)) % (Math.PI * 2);
            thetaObservation += ((angleWithXAxis - beacon.angleToVehicle) + Math.PI * 2) % (Math.PI * 2);
        }
        thetaObservation /= this.visibleBeacons.size();

        Matrix zt = new Matrix(new double[][]{
                {xObservation},
                {yObservation},
                {thetaObservation}});

        Matrix Kt = SigmaPredict.times(this.C.transpose()).times(this.C.times(SigmaPredict).times(this.C.transpose()).plus(this.Q).inverse());
        this.Mu = MuPredict.plus(Kt.times(zt.minus(this.C.times(MuPredict))));
        this.Sigma = I.minus(Kt.times(this.C)).times(SigmaPredict);
//
//        System.out.println(this.pose.x + " " + this.Mu.get(0, 0));
//        System.out.println(this.pose.y + " " + this.Mu.get(1, 0));
//        System.out.println(this.pose.theta + " " + this.Mu.get(2, 0));
//        System.out.println();

        this.pose.x = this.Mu.get(0, 0);
        this.pose.y = this.Mu.get(1, 0);
        this.pose.theta = this.Mu.get(2, 0);
    }

    public void triangulate(double a1, double a2, double a3, double x1, double y1, double x2, double y2, double x3, double y3) {
        double cot12 = bound(1.0 / Math.tan(a2 - a1));
        double cot23 = bound(1.0 / Math.tan(a3 - a2));
        double cot31 = bound((1.0 - cot12 * cot23) / (cot12 + cot23));

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

        double k31 = (dx3 * dx1) + (dy3 * dy1) + cot31 * ((dy3 * dx1) - (dx3 * dy1));

        double D = (c12x - c23x) * (c23y - c31y) - (c23x - c31x) * (c12y - c23y);

        double invD = 1.0 / D;

        double K = k31 * invD;

        this.pose.x = K * (c12y - c23y) + x2;
        this.pose.y = K * (c23x - c12x) + y2;
    }

    private double bound(double value) {
        if (value > MAX_COT) {
            return MAX_COT;
        } else if (value < -MAX_COT) {
            return -MAX_COT;
        } else {
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

    private double[] estimateDistanceAngle(Beacon b, double x, double y, double angle){
        double hx = Math.cos(angle), hy = Math.sin(angle);
        double dx = b.x - x, dy = b.y - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        dx /= distance;
        dy /= distance;

        double dot = dx*hx + dy*hy;

        double angleWithBeacon = Math.acos(dot);

        double tdx = hy;
        double tdy = -hx;

        if (tdx*dx + tdy*dy > 0){
            angleWithBeacon *= -1;
        }
        return new double[]{distance, angleWithBeacon};
    }

    private double constrain_angle(double radian)
    {
        if (radian < -Math.PI) {
            radian += 2*Math.PI;
        } else if (radian > Math.PI) {
            radian -= 2*Math.PI;
        }

        return radian;
    }

//    double a1 = 0.1;
//    double a2 = 0.1;
//    double a3 = 0.01;
//    double a4 = 0.1;
//    double EPS = 1e-4;
//
//    double v = (this.speedLeft + this.speedRight) / 2;
//    double w = (this.speedRight - this.speedLeft) / (2 * this.r);
//    Matrix muBar;
//
//    Matrix G = new Matrix(3,3);
//    Matrix V = new Matrix(3,2);
//    Matrix M = new Matrix(2,2);
//    Matrix MUBAR = new Matrix(3,1);
//    Matrix SIGMABAR = new Matrix(3,3);
//    Matrix ZHAT = new Matrix(2,1);
//    Matrix Z = new Matrix(2,1);
//    Matrix H = new Matrix(2,3);
//    Matrix S = new Matrix(2,2);
//    Matrix Q = new Matrix(2,2);
//    Matrix K = new Matrix(3,2);
//    Matrix I = new Matrix(3,3);
//
//        G.set(0,0, 1);
//        G.set(1,1, 1);
//        G.set(2,2, 1);
//
//        I.set(0,0, 1);
//        I.set(1,1, 1);
//        I.set(2,2, 1);
//
//        M.set(0,0, Math.pow(a1*Math.abs(v) + a2 * Math.abs(v), 2));
//        M.set(1,1, Math.pow(a3*Math.abs(v) + a4 * Math.abs(v), 2));
//
//    double sinTheta = Math.sin(this.pose.theta);
//    double cosTheta = Math.cos(this.pose.theta);
//
//        if (Math.abs(w) > EPS){
//        double vOverW = v / w;
//        double omegaDeltaT = w * dt;
//        double sinThetaOmegaDt = Math.sin(this.pose.theta + omegaDeltaT);
//
//        double cosThetaOmegaDt = Math.cos(this.pose.theta + omegaDeltaT);
//
//        G.set(0,2, -vOverW * cosTheta + (vOverW * cosThetaOmegaDt));
//        G.set(1,2, -vOverW * sinTheta + (vOverW * sinThetaOmegaDt));
//
//        V.set(0,0,(-sinTheta + sinThetaOmegaDt) / w);
//        V.set(1,0,(cosTheta - cosThetaOmegaDt) / w);
//        V.set(0,1,((v * (sinTheta - sinThetaOmegaDt)) / (w * w)) + ((v * cosThetaOmegaDt * dt) / w));
//        V.set(1,1, (-(v * (cosTheta - cosThetaOmegaDt)) / (w * w)) + ((v * sinThetaOmegaDt * dt) / w));
//        V.set(2,0,0);
//        V.set(2,1,dt);
//
//        MUBAR.set(0,0, this.Mu.get(0,0) - (vOverW*sinTheta) + (vOverW*sinThetaOmegaDt));
//        MUBAR.set(1,0, this.Mu.get(1,0) + (vOverW*cosTheta) - (vOverW*cosThetaOmegaDt));
//        MUBAR.set(2,0, this.Mu.get(2,0) + w*dt);
//    }else {
//        G.set(0, 2, -v * sinTheta * dt);
//        G.set(1, 2, v * cosTheta * dt);
//
//        V.set(0, 0, cosTheta * dt);
//        V.set(1, 0, sinTheta * dt);
//        V.set(0, 1, -v * sinTheta * dt * dt * 0.5);
//        V.set(1, 1, v * cosTheta * dt * dt * 0.5);
//        V.set(2, 0, 0);
//        V.set(2, 1, dt);
//
//        MUBAR.set(0, 0, this.Mu.get(0, 0) + v * cosTheta * dt);
//        MUBAR.set(1, 0, this.Mu.get(1, 0) + v * sinTheta * dt);
//        MUBAR.set(2, 0, this.Mu.get(2, 0));
//    }
//    SIGMABAR = G.times(this.Sigma).times(G.transpose()).plus(V.times(M).times(V.transpose()));
//
//        for(int i = 0; i < this.visibleBeacons.size(); i++){
//        Beacon b = this.visibleBeacons.get(i);
//        Z.set(0,0, b.distanceToVehicle);
//        Z.set(1,0, b.angleToVehicle);
//
//        if (Math.abs(b.distanceToVehicle) > EPS){
//            double[] distAngle = estimateDistanceAngle(b, Mu.get(0,0), Mu.get(1,0), Mu.get(2,0));
//            ZHAT.set(0,0, distAngle[0]);
//            ZHAT.set(1,0, distAngle[1]);
//
//            H.set(0,0, -(b.x - Mu.get(0,0))/distAngle[0]);
//            H.set(0,1, -(b.y - Mu.get(1,0))/distAngle[0]);
//            H.set(0,2, 0);
//
//            H.set(1,0, (b.y - Mu.get(1,0))/(distAngle[0]*distAngle[0]));
//            H.set(1,1, -(b.x - Mu.get(0,0))/(distAngle[0]*distAngle[0]));
//            H.set(1,2, -1);
//
//            Q.set(0,0, Math.pow(b.distanceToVehicle * 0.1, 2));
//            Q.set(1,1, Math.pow(0.02, 2));
//
//            S = H.times(SIGMABAR).times(H.transpose()).plus(Q);
//
//            K = SIGMABAR.times(H.transpose()).times(S.inverse());
//            MUBAR = MUBAR.plus(K.times(Z.minus(ZHAT)));
//            SIGMABAR = (I.minus(K.times(H))).times(SIGMABAR);
//
//            MUBAR.set(2,0, constrain_angle(MUBAR.get(2,0)));
//        }
//
//        this.Mu = MUBAR;
//        this.Sigma = SIGMABAR;
//    }
//
////        double vOverW = v / w;
////        double omegaDeltaT = w * dt;
////        G = new Matrix(new double[][]{
////                {1, 0, -vOverW * Math.cos(this.pose.theta) + (vOverW * Math.cos(this.pose.theta + omegaDeltaT))},
////                {0, 1, -vOverW * Math.sin(this.pose.theta) + (vOverW * Math.sin(this.pose.theta + omegaDeltaT))},
////                {0, 0, 1}});
////
////        double sinThetaOmegaDt = Math.sin(this.pose.theta + omegaDeltaT);
////        double sinTheta = Math.sin(this.pose.theta);
////
////        double cosThetaOmegaDt = Math.cos(this.pose.theta + omegaDeltaT);
////        double cosTheta = Math.cos(this.pose.theta);
////
////        V = new Matrix(new double[][]{
////                {(-sinTheta + sinThetaOmegaDt) / w, ((v * (sinTheta - sinThetaOmegaDt)) / (w * w)) + ((v * cosThetaOmegaDt * dt) / w)},
////                {(cosTheta - cosThetaOmegaDt) / w, (-(v * (cosTheta - cosThetaOmegaDt)) / (w * w)) + ((v * sinThetaOmegaDt * dt) / w)},
////                {0, dt}});
////
////        if (w == 0) {
////            muBar = this.Mu.plus(new Matrix(new double[][]{
////                    {this.pose.x + dt * v * Math.cos(this.pose.theta)},
////                    {this.pose.y + dt * v * Math.sin(this.pose.theta)},
////                    {this.pose.theta}}));
////        } else {
////            Matrix odometry = new Matrix(new double[][]{
////                    {-vOverW * Math.sin(this.pose.theta) + (vOverW * Math.sin(this.pose.theta + omegaDeltaT))},
////                    {vOverW * Math.cos(this.pose.theta) - (vOverW * Math.cos(this.pose.theta + omegaDeltaT))},
////                    {omegaDeltaT}});
////            muBar = this.Mu.plus(odometry);
////        }
////
////
////        Matrix sigmaBar = G.times(this.Sigma).times(G.transpose()).plus(this.R);
////        Matrix[] Ks = new Matrix[this.visibleBeacons.size()];
////        Matrix[] Hs = new Matrix[this.visibleBeacons.size()];
////        Matrix[] zHats = new Matrix[this.visibleBeacons.size()];
////        Matrix delta;
////        double q, sqrtQ;
////
////
////        for (int i = 0; i < this.visibleBeacons.size(); i++) {
////            Beacon beacon = this.visibleBeacons.get(i);
////            delta = new Matrix(new double[][]{
////                    {beacon.x - this.Mu.get(0, 0)},
////                    {beacon.y - this.Mu.get(1, 0)}});
////            q = delta.transpose().times(delta).get(0, 0);
////            sqrtQ = Math.sqrt(q);
////            zHats[i] = new Matrix(new double[][]{
////                    {sqrtQ},
////                    {Math.atan2(delta.get(1, 0), delta.get(0, 0)) - muBar.get(2, 0)},
////                    {beacon.id}});
//////            Hs[i] = new Matrix(new double[][] {
//////                    {sqrtQ * delta.get(0, 0), -sqrtQ * delta.get(1, 0), 0},
//////                    {delta.get(1, 0), delta.get(0, 0), -1},
//////                    {0, 0, 0}}).times(1/q);
////            Hs[i] = new Matrix(new double[][]{
////                    {-delta.get(0, 0) / sqrtQ, -delta.get(1, 0) / sqrtQ, 0},
////                    {delta.get(1, 0) / q, -delta.get(0, 0) / q, -1},
////                    {0, 0, 0}});
////            Ks[i] = sigmaBar.times(Hs[i].transpose()).times(Hs[i].times(sigmaBar).times(Hs[i].transpose()).plus(this.Q).inverse());
////        }
////
////        Matrix KSummed = new Matrix(new double[][]{
////                {0},
////                {0},
////                {0}});
////        Matrix KHSummed = new Matrix(new double[][]{
////                {0, 0, 0},
////                {0, 0, 0},
////                {0, 0, 0}});
////        for (int i = 0; i < this.visibleBeacons.size(); i++) {
////            Matrix zDifference = new Matrix(new double[][]{
////                    {this.visibleBeacons.get(i).distanceToVehicle},
////                    {this.visibleBeacons.get(i).angleToVehicle},
////                    {this.visibleBeacons.get(i).id}}).minus(zHats[i]);
////            KSummed.plusEquals(Ks[i].times(zDifference));
////            printMatrix(Ks[i]);
////
////            KHSummed.plusEquals(Ks[i].times(Hs[i]));
////        }
////
//////        System.out.println("yo");
//////        this.printMatrix(this.Mu);
////        this.Sigma = this.I.minus(KHSummed).times(sigmaBar);
////        this.Mu = muBar.plus(KSummed);
//////        this.printMatrix(this.Mu);
//////        System.out.println("no");

}
