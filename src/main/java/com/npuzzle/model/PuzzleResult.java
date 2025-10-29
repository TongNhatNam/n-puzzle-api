package com.npuzzle.model;

import java.util.List;

public class PuzzleResult {
    private List<int[][]> solution;
    private int steps;
    private int nodesExplored;
    private long solvingTimeMs;

    public PuzzleResult(List<int[][]> solution, int steps, int nodesExplored, long solvingTimeMs) {
        this.solution = solution;
        this.steps = steps;
        this.nodesExplored = nodesExplored;
        this.solvingTimeMs = solvingTimeMs;
    }

    public List<int[][]> getSolution() {
        return solution;
    }

    public void setSolution(List<int[][]> solution) {
        this.solution = solution;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getNodesExplored() {
        return nodesExplored;
    }

    public void setNodesExplored(int nodesExplored) {
        this.nodesExplored = nodesExplored;
    }

    public long getSolvingTimeMs() {
        return solvingTimeMs;
    }

    public void setSolvingTimeMs(long solvingTimeMs) {
        this.solvingTimeMs = solvingTimeMs;
    }
}