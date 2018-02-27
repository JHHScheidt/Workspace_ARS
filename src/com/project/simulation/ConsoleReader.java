package com.project.simulation;

import java.util.Scanner;

public class ConsoleReader implements Runnable {

    private Simulator simulator;
    private Scanner scanner;

    private boolean running;

    public ConsoleReader(Simulator simulator) {
        this.scanner = new Scanner(System.in);
        this.simulator = simulator;
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
        this.scanner.close();
    }

    @Override
    public void run() {
        String input;
        while (this.running) {
            input = this.scanner.nextLine();

            if (input.equals("stop")) {
                this.stop();
                this.simulator.stop();
            }
        }
    }
}
