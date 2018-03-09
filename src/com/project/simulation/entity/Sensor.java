package com.project.simulation.entity;

import com.project.simulation.environment.Line;

/**
 * @author Joshua, Simon
 */
public class Sensor extends Line {

	public double value;

	public Sensor(double x1, double y1, double x2, double y2) {
		super(x1, y1, x2, y2);
	}
}
