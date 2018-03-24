package com.project.simulation.entity;

import com.project.Controller;
import com.project.simulation.Pose;

public class Beacon {

	public static final double MAX_RAD = 2 * Math.PI;

	private static int nextId = 0;

	public double x, y;
	public int id;

	public double distanceToVehicle;
	public double angleToVehicle;

	public Beacon(double x, double y) {
		this.x = x;
		this.y = y;

		this.id = nextId++;
	}

	public void update(Pose pose) {
		this.distanceToVehicle = Math.sqrt(Math.pow(this.x - pose.x, 2) + Math.pow(this.y - pose.y, 2)) + Controller.nextGaussian(0.1);
		this.angleToVehicle = ((Math.atan2(this.y - pose.y, this.x - pose.x) - pose.theta + MAX_RAD) + Controller.nextGaussian(0.08)) % MAX_RAD;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Beacon) {
			Beacon otherBeacon = (Beacon) other;
			if (otherBeacon.x == this.x && otherBeacon.y == this.y)
				return true;
			else return false;
		} else return false;
	}

	@Override
	public String toString() {
		return "[Beacon: " + this.id + " at x=" + this.x + ", y=" + this.y + "]";
	}
}
