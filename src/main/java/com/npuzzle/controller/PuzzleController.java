package com.npuzzle.controller;

import com.npuzzle.model.PuzzleRequest;
import com.npuzzle.model.PuzzleResult;
import com.npuzzle.model.DetailedPuzzleResult;
import com.npuzzle.solver.NPuzzleSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling N-puzzle solving requests
 */
@RestController
@RequestMapping("/api")
public class PuzzleController {
    private static final Logger logger = LoggerFactory.getLogger(PuzzleController.class);

    /**
     * Solves an N-puzzle
     * @param request The puzzle board to solve
     * @return The solution path and statistics
     */
    @PostMapping("/solve")
    public ResponseEntity<?> solvePuzzle(@Valid @RequestBody PuzzleRequest request) {
        System.out.println("DEBUG: Board nhận được: " + Arrays.deepToString(request.getBoard()));
        logger.info("Received solve request for board: {}", Arrays.deepToString(request.getBoard()));
        
        NPuzzleSolver solver = new NPuzzleSolver();
        try {
            if (request == null || request.getBoard() == null) {
                logger.warn("Invalid request: board is null");
                return ResponseEntity.badRequest().body("Invalid request: board is required");
            }
            
            PuzzleResult result = solver.solve(request.getBoard());
            
            if (result.getSolution() == null || result.getSolution().isEmpty()) {
                String message = "Không tìm thấy lời giải. ";
                if (!solver.isSolvable(request.getBoard())) {
                    message += "Bảng không thể giải được (unsolvable).";
                } else {
                    message += "Bảng quá phức tạp hoặc vượt quá thời gian cho phép.";
                }
                logger.info("No solution found for board: {} - {}", Arrays.deepToString(request.getBoard()), message);
                Map<String, Object> response = new HashMap<>();
                response.put("solution", null);
                response.put("steps", 0);
                response.put("nodesExplored", result.getNodesExplored());
                response.put("solvingTimeMs", result.getSolvingTimeMs());
                response.put("error", message);
                return ResponseEntity.ok().body(response);
            }
            
            logger.info("Solution found in {}ms with {} steps and {} nodes explored", 
                result.getSolvingTimeMs(), result.getSteps(), result.getNodesExplored());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid input: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error solving puzzle", ex);
            return ResponseEntity.internalServerError().body("An error occurred while solving the puzzle: " + ex.getMessage());
        }
    }

    /**
     * Solves an N-puzzle with detailed step-by-step information
     * @param request The puzzle board to solve
     * @return Detailed solution with step-by-step information and tile statistics
     */
    @PostMapping("/solve-detailed")
    public ResponseEntity<?> solvePuzzleDetailed(@Valid @RequestBody PuzzleRequest request) {
        System.out.println("DEBUG: Detailed solve request for board: " + Arrays.deepToString(request.getBoard()));
        logger.info("Received detailed solve request for board: {}", Arrays.deepToString(request.getBoard()));
        
        NPuzzleSolver solver = new NPuzzleSolver();
        try {
            if (request == null || request.getBoard() == null) {
                logger.warn("Invalid request: board is null");
                return ResponseEntity.badRequest().body("Invalid request: board is required");
            }
            
            DetailedPuzzleResult result = solver.solveWithDetails(request.getBoard());
            
            if (result.getSteps() == null || result.getSteps().isEmpty()) {
                String message = "Không tìm thấy lời giải. ";
                if (!solver.isSolvable(request.getBoard())) {
                    message += "Bảng không thể giải được (unsolvable).";
                } else {
                    message += "Bảng quá phức tạp hoặc vượt quá thời gian cho phép.";
                }
                logger.info("No detailed solution found for board: {} - {}", Arrays.deepToString(request.getBoard()), message);
                Map<String, Object> response = new HashMap<>();
                response.put("steps", null);
                response.put("totalSteps", 0);
                response.put("totalNodesExplored", result.getTotalNodesExplored());
                response.put("solvingTimeMs", result.getSolvingTimeMs());
                response.put("tileMoveCounts", new HashMap<>());
                response.put("tilePath", new HashMap<>());
                response.put("error", message);
                return ResponseEntity.ok().body(response);
            }
            
            logger.info("Detailed solution found in {}ms with {} steps and {} nodes explored", 
                result.getSolvingTimeMs(), result.getTotalSteps(), result.getTotalNodesExplored());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid input: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            logger.error("Error solving puzzle with details", ex);
            return ResponseEntity.internalServerError().body("An error occurred while solving the puzzle: " + ex.getMessage());
        }
    }
}