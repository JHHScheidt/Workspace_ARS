package com.project.simulation.environment;

/**
 * @author Marciano, Rico, Joshua, Simon
 */
public class Environment {

	public double size; // width and height of the room
	public Line[] obstacles; // all obstacles, the walls for example are all in here

	public int[][] grid;
	public double subdivisionSize;

	public Environment(double size, int subdivisions, Line... obstacles) {
		this.size = size;

		this.grid = new int[subdivisions][subdivisions];
		this.obstacles = obstacles;

		this.subdivisionSize = this.size / subdivisions;
	}

	public void reset() {
		for (int i = 0; i < this.grid.length; i++) {
			for (int j = 0; j < this.grid[i].length; j++) {
				this.grid[i][j] = 0;
			}
		}
	}
	
	public Line[] flip(Line[] obstacles) {
		Line[] flipped = obstacles.clone();
		double largestX = 0;
		for(Line l : flipped) {
			if(Math.max(l.x1, l.x2) > largestX) largestX = Math.max(l.x1, l.x2);
			l.x1 = l.x1*-1;
			l.x2 = l.x2*-1;
		}
		for(Line l : flipped) {
			l.x1 += largestX;
			l.x2 += largestX;
		}
		
		return flipped;
	}
}
