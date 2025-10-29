package com.npuzzle.model;

import java.util.Map;
import java.util.List;

/**
 * Model chứa thông tin chi tiết về từng bước di chuyển trong quá trình giải n-puzzle
 */
public class StepDetail {
    private int stepNumber;
    private int[][] board;
    private Map<Integer, Position> tilePositions; // Vị trí của từng con số
    private int nodesExploredAtStep;
    private int heuristicValue;
    private String moveDescription; // Mô tả bước di chuyển (ví dụ: "Di chuyển số 5 lên trên")

    public static class Position {
        public int row;
        public int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() { return row; }
        public void setRow(int row) { this.row = row; }
        public int getCol() { return col; }
        public void setCol(int col) { this.col = col; }
    }

    public StepDetail(int stepNumber, int[][] board, Map<Integer, Position> tilePositions, 
                     int nodesExploredAtStep, int heuristicValue, String moveDescription) {
        this.stepNumber = stepNumber;
        this.board = board;
        this.tilePositions = tilePositions;
        this.nodesExploredAtStep = nodesExploredAtStep;
        this.heuristicValue = heuristicValue;
        this.moveDescription = moveDescription;
    }

    // Getters and Setters
    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public int[][] getBoard() { return board; }
    public void setBoard(int[][] board) { this.board = board; }

    public Map<Integer, Position> getTilePositions() { return tilePositions; }
    public void setTilePositions(Map<Integer, Position> tilePositions) { this.tilePositions = tilePositions; }

    public int getNodesExploredAtStep() { return nodesExploredAtStep; }
    public void setNodesExploredAtStep(int nodesExploredAtStep) { this.nodesExploredAtStep = nodesExploredAtStep; }

    public int getHeuristicValue() { return heuristicValue; }
    public void setHeuristicValue(int heuristicValue) { this.heuristicValue = heuristicValue; }

    public String getMoveDescription() { return moveDescription; }
    public void setMoveDescription(String moveDescription) { this.moveDescription = moveDescription; }
} 