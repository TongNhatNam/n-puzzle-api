package com.npuzzle.model;

import jakarta.validation.constraints.NotNull;

/**
 * Request model for solving an N-puzzle
 */
public class PuzzleRequest {
    @NotNull(message = "Board cannot be null")
    private int[][] board;

    /**
     * Gets the puzzle board
     * @return 3x3 array representing the puzzle board
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Sets the puzzle board
     * @param board 3x3 array representing the puzzle board
     */
    public void setBoard(int[][] board) {
        this.board = board;
    }
}