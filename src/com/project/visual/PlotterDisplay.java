package com.project.visual;

import com.orsoncharts.Chart3D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PlotterDisplay extends JPanel {

    private Chart3D chart;

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.chart == null) return;

        Graphics2D g2 = (Graphics2D) g;
        this.chart.draw(g2, new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));
    }

    public void setChart(Chart3D chart) {
        this.chart = chart;
    }
}
