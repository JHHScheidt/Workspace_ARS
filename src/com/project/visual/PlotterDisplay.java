package com.project.visual;

import com.orsoncharts.Chart3D;
import com.orsoncharts.Chart3DFactory;
import com.orsoncharts.Range;
import com.orsoncharts.axis.ValueAxis3D;
import com.orsoncharts.graphics3d.Dimension3D;
import com.orsoncharts.graphics3d.ViewPoint3D;
import com.orsoncharts.plot.XYZPlot;
import com.orsoncharts.renderer.GradientColorScale;
import com.orsoncharts.renderer.xyz.SurfaceRenderer;

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
