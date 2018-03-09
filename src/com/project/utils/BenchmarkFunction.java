package com.project.utils;

import com.orsoncharts.data.function.Function3D;

/**
 * @author Simon, Rico
 */
public class BenchmarkFunction {

    public static final Function3D ROSENBROCK = (Function3D) (x, z) -> Math.pow(x, 2) + 100 * Math.pow((z - Math.pow(x, 2)), 2);
    public static final Function3D RASTRIGIN = (Function3D) (x, z) -> 20 + (Math.pow(x, 2) - 10 * Math.cos(Math.PI * 2 * x)) + (Math.pow(z, 2) - 10 * Math.cos(Math.PI * 2 * z));

}
