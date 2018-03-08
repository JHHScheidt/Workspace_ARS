package com.project.simulation;

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
}
