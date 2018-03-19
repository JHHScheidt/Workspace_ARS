package com.project.simulation.entity;

import com.project.Controller;
import com.project.simulation.Pose;

public class Beacon {

	private static int nextId = 0;

	private static final double MEAN = 0;
	private static final double STD = 0;

	public double x, y;
	public int id;

	public double distanceToVehicle;

	public Beacon(double x, double y) {
		this.x = x;
		this.y = y;

		this.id = nextId++;
	}

	public void updateDistance(Pose pose) {
		this.distanceToVehicle = Math.sqrt(Math.pow(this.x - pose.x, 2) + Math.pow(this.y - pose.y, 2)) + (Controller.RANDOM.nextGaussian() * STD + MEAN);
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
