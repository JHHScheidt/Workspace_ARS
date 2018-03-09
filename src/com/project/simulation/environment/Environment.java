package com.project.simulation.environment;

import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * @author Marciano, Rico, Joshua, Simon
 */
public class Environment {

	public static final Environment ROOM = new Environment(5, 100,
			new Line[] {
			new Line(0, 0, 5, 0),
			new Line(0, 0, 0, 5),
			new Line(5, 0, 5, 5),
			new Line(0, 5, 5, 5) },
			new Point2D.Double(5, 5),
			new Point2D.Double(1, 1),
			new Point2D.Double(4, 4));
	public static final Environment MAZE_JOSHUA = new Environment(5, 100,
			new Line[] {
			new Line(0, 0, 5, 0),
			new Line(0, 0, 0, 5),
			new Line(5, 0, 5, 5),
			new Line(0, 5, 5, 5),
			new Line(4, 5, 4, 1),
			new Line(3, 0, 3, 4),
			new Line(2, 1, 2, 3),
			new Line(2, 4, 2, 5),
			new Line(1, 1, 2, 1),
			new Line(0, 2, 1, 2),
			new Line(1, 3, 1, 4),
			new Line(1, 3, 3, 3) },
			new Point2D.Double(4.5, 0.5),
			new Point2D.Double(2.5, 2.5),
			new Point2D.Double(1.5, 4.5),
			new Point2D.Double(0.5, 0.5),
			new Point2D.Double(4.5, 4.5));
	public static final Environment SPIRAL = new Environment(5, 100,
			new Line[] {
			new Line(0, 0, 5, 0),
			new Line(0, 0, 0, 5),
			new Line(5, 0, 5, 5),
			new Line(0, 5, 5, 5),
			new Line(1, 0, 1, 4),
			new Line(1, 4, 4, 4),
			new Line(4, 4, 4, 1),
			new Line(4, 1, 2, 1),
			new Line(2, 1, 2, 3),
			new Line(2, 3, 3, 3),
			new Line(3, 3, 3, 2) },
			new Point2D.Double(2.5, 2.5),
			new Point2D.Double(0.5, 0.5),
			new Point2D.Double(1.5, 3.5),
			new Point2D.Double(4.5, 0.5));

	public Line[] obstacles; // all obstacles, the walls for example are all in here
	public Point2D.Double[] startingLocations;

	public double subdivisionSize;
	public int[][] grid;
	public double size; // width and height of the room

	private int subdivisions;

	public Environment(double size, int subdivisions, Line[] obstacles, Point2D.Double... startingLocations) {
		this.size = size;

		this.grid = new int[subdivisions][subdivisions];
		this.obstacles = obstacles;

		this.subdivisions = subdivisions;
		this.subdivisionSize = this.size / subdivisions;

		this.startingLocations = startingLocations;
	}

	public void reset() {
		for (int i = 0; i < this.grid.length; i++) {
			for (int j = 0; j < this.grid[i].length; j++) {
				this.grid[i][j] = 0;
			}
		}
	}
	
	public void flip() {
		Line[] flippedObst = Arrays.copyOf(this.obstacles, this.obstacles.length);
		Point2D.Double[] flippedStart = Arrays.copyOf(this.startingLocations, this.startingLocations.length);
		double largestX = 0;
		for(Line l : flippedObst) {
			if(Math.max(l.x1, l.x2) > largestX) largestX = Math.max(l.x1, l.x2);
			l.x1 = -l.x1;
			l.x2 = -l.x2;
		}
		for(Line l : flippedObst) {
			l.x1 += largestX;
			l.x2 += largestX;
		}
		for(Point2D.Double p : flippedStart) {
			p.x = -p.x + largestX;
		}
		this.obstacles = flippedObst;
		this.startingLocations = flippedStart;
	}

	public Environment clone() {
		Line[] lineCopy = Arrays.copyOf(this.obstacles, this.obstacles.length);
		return new Environment(this.size, this.subdivisions, lineCopy, this.startingLocations);
	}
}
