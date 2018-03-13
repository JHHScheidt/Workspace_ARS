package com.project;

import com.project.algorithm.GeneticAlgorithm;
import com.project.algorithm.Individual;
import com.project.simulation.Simulator;
import com.project.simulation.environment.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;
import java.util.concurrent.*;

public class Controller {

	public static final Random RANDOM = new Random(15);

	private ExecutorService executorService;
	private int threads;


	public Controller(int threads) {
		this.threads = threads;
	}

	public void start() {
		this.executorService = Executors.newFixedThreadPool(this.threads);
	}

	public void stop() {
		this.executorService.shutdown();
	}

	public Future<Double> submit(Callable<Double> callable) {
		return this.executorService.submit(callable);
	}

	public static void main(String[] args) throws Exception {
		Controller controller = new Controller(8);
		controller.start();

		boolean visualRun = true;
		if (visualRun) visualRun(Environment.MAZE_JOSHUA, "res/generation510-best.txt");
		else trainingRun(controller, "res/updated-fitness/generation500-all.txt");

		controller.stop();
	}

	public static void visualRun(Environment environment, String individualPath) throws IOException, ClassNotFoundException {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(individualPath));
            Individual individual = (Individual) in.readObject();

            Simulator simulator = new Simulator(0, true);
            simulator.init(individual, environment, environment.startingLocations[0].x, environment.startingLocations[0].y, 0);
            simulator.run();
	}

	public static void trainingRun(Controller controller, String generationPath) throws ExecutionException, InterruptedException {
		GeneticAlgorithm geneticAlgorithm;
		if (generationPath == null)
			geneticAlgorithm = new GeneticAlgorithm(controller, 100, 12);
		else
			geneticAlgorithm = new GeneticAlgorithm(controller, 100, 12, generationPath);

		geneticAlgorithm.start();
	}
}
