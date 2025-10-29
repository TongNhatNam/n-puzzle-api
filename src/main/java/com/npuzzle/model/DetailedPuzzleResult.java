package com.npuzzle.model;

import java.util.List;
import java.util.Map;

/**
 * Kết quả chi tiết của việc giải n-puzzle, bao gồm từng bước di chuyển
 */
public class DetailedPuzzleResult {
    private List<StepDetail> steps;
    private int totalSteps;
    private int totalNodesExplored;
    private long solvingTimeMs;
    private Map<Integer, Integer> tileMoveCounts; // Số lần di chuyển của từng con số
    private Map<Integer, List<StepDetail.Position>> tilePath; // Đường đi của từng con số

    public DetailedPuzzleResult(List<StepDetail> steps, int totalSteps, int totalNodesExplored, 
                               long solvingTimeMs, Map<Integer, Integer> tileMoveCounts, 
                               Map<Integer, List<StepDetail.Position>> tilePath) {
        this.steps = steps;
        this.totalSteps = totalSteps;
        this.totalNodesExplored = totalNodesExplored;
        this.solvingTimeMs = solvingTimeMs;
        this.tileMoveCounts = tileMoveCounts;
        this.tilePath = tilePath;
    }

    // Getters and Setters
    public List<StepDetail> getSteps() { return steps; }
    public void setSteps(List<StepDetail> steps) { this.steps = steps; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public int getTotalNodesExplored() { return totalNodesExplored; }
    public void setTotalNodesExplored(int totalNodesExplored) { this.totalNodesExplored = totalNodesExplored; }

    public long getSolvingTimeMs() { return solvingTimeMs; }
    public void setSolvingTimeMs(long solvingTimeMs) { this.solvingTimeMs = solvingTimeMs; }

    public Map<Integer, Integer> getTileMoveCounts() { return tileMoveCounts; }
    public void setTileMoveCounts(Map<Integer, Integer> tileMoveCounts) { this.tileMoveCounts = tileMoveCounts; }

    public Map<Integer, List<StepDetail.Position>> getTilePath() { return tilePath; }
    public void setTilePath(Map<Integer, List<StepDetail.Position>> tilePath) { this.tilePath = tilePath; }
} 