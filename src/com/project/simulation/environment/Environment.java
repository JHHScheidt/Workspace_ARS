package com.project.simulation.environment;

public class Environment {

	public double size; // width and height of the room
	public Line[] obstacles; // all obstacles, the walls for example are all in here

	public boolean[][] grid;
	public double subdivisionSize;

	public Environment(double size, int subdivisions, Line... obstacles) {
		this.size = size;

		this.grid = new boolean[subdivisions][subdivisions];
		this.obstacles = obstacles;

		this.subdivisionSize = this.size / subdivisions;
	}

	public void reset() {
		for (int i = 0; i < this.grid.length; i++) {
			for (int j = 0; j < this.grid[i].length; j++) {
				this.grid[i][j] = false;
			}
		}
	}
}
