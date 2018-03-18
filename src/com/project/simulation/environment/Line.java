package com.project.simulation.environment;

import com.project.simulation.entity.Vehicle;

/**
 * @author Joshua, Simon
 */
public class Line {

	public double xIntersect, yIntersect; // The intersection is put into these variables if it exists

	public double x1, x2; // x-coordinates of the line
	public double y1, y2; // y-coordinates of the line

	public Line(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public boolean intersects(Line other) {
		double s1_x, s1_y, s2_x, s2_y;
		s1_x = this.x2 - this.x1;
		s1_y = this.y2 - this.y1;
		s2_x = other.x2 - other.x1;
		s2_y = other.y2 - other.y1;

		double s, t;
		s = (-s1_y * (this.x1 - other.x1) + s1_x * (this.y1 - other.y1)) / (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (this.y1 - other.y1) - s2_y * (this.x1 - other.x1)) / (-s2_x * s1_y + s1_x * s2_y);
		if (s >= 0 && s <= 1 && t >= 0 && t <= 1) { //check if the intersection exists and is within the line segment
			// Collision detected
			this.xIntersect = this.x1 + (t * s1_x);
			this.yIntersect = this.y1 + (t * s1_y);
			return true;
		} else return false;
	}

	public boolean intersects(Vehicle vehicle) {

		double baX = this.x2 - this.x1;
		double baY = this.y2 - this.y1;
		double caX = vehicle.pose.x - this.x1;
		double caY = vehicle.pose.y - this.y1;

		double a = baX * baX + baY * baY;
		double bBy2 = baX * caX + baY * caY;
		double c = caX * caX + caY * caY - vehicle.r * vehicle.r;

		double pBy2 = bBy2 / a;
		double q = c / a;

		double disc = pBy2 * pBy2 - q;
		if (disc < 0) return false;

		double tmpSqrt = Math.sqrt(disc);
		double abScalingFactor1 = -pBy2 + tmpSqrt;
		double abScalingFactor2 = -pBy2 - tmpSqrt;

		double intersectXOne = this.x1 - baX * abScalingFactor1;
		double intersectYOne = this.y1 - baY * abScalingFactor1;

		double minXBound, maxXBound;
		double minYBound, maxYBound;

		minXBound = Math.min(this.x1, this.x2);
		maxXBound = (minXBound == this.x1) ? this.x2 : this.x1;
		minYBound = Math.min(this.y1, this.y2);
		maxYBound = (minYBound == this.y1) ? this.y2 : this.y1;

		if (disc == 0) {
            if ((intersectXOne >= minXBound && intersectXOne <= maxXBound) && (intersectYOne >= minYBound && intersectYOne <= maxYBound)) return true;
            return false;
        } else {
            double intersectXTwo = this.x1 - baX * abScalingFactor2;
            double intersectYTwo = this.y1 - baY * abScalingFactor2;

            if ((intersectXOne >= minXBound && intersectXOne <= maxXBound) && (intersectYOne >= minYBound && intersectYOne <= maxYBound)) return true;
            if ((intersectXTwo >= minXBound && intersectXTwo <= maxXBound) && (intersectYTwo >= minYBound && intersectYTwo <= maxYBound)) return true;
            return false;
        }
	}
	
	public Line clone() {
		return new Line(this.x1, this.y1, this.x2, this.y2);
	}
}
