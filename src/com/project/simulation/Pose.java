package com.project.simulation;

public class Pose {

	public double x, y, theta;

	public Pose(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}

	@Override
	public String toString() {
		return "[Pose x = " + this.x + ", y = " + this.y + ", theta = " + this.theta + "]";
	}
}
