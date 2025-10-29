package com.npuzzle.solver;

import com.npuzzle.model.PuzzleResult;
import com.npuzzle.model.StepDetail;
import com.npuzzle.model.DetailedPuzzleResult;
import java.util.*;
import java.time.Duration;
import java.time.Instant;
import java.io.File;
import java.io.InputStream;

public class NPuzzleSolver {

    private static final long TIME_LIMIT_MS = 300000; // 5 minutes time limit

    // Pattern Database cho 4x4
    private static PatternDatabase pdb1 = null;
    private static PatternDatabase pdb2 = null;
    // Tải PDB từ thư mục resources trong classpath
    private static final String PDB1_PATH = "pdb1.ser";
    private static final String PDB2_PATH = "pdb2.ser";
    static {
        try {
            try (InputStream pdb1Stream = NPuzzleSolver.class.getClassLoader().getResourceAsStream(PDB1_PATH);
                 InputStream pdb2Stream = NPuzzleSolver.class.getClassLoader().getResourceAsStream(PDB2_PATH)) {

                if (pdb1Stream == null || pdb2Stream == null) {
                    // Nếu không tìm thấy tệp PDB trong resources, báo lỗi.
                    // Việc build PDB lúc runtime không được khuyến khích trong môi trường server.
                    // Các file PDB nên được build sẵn và đưa vào trong file JAR.
                    System.err.println("FATAL: Pattern database files (pdb1.ser, pdb2.ser) not found in classpath resources.");
                    throw new RuntimeException("Pattern databases not found.");
                }

                pdb1 = new PatternDatabase(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7)));
                pdb2 = new PatternDatabase(new HashSet<>(Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15)));
                pdb1.loadFromStream(pdb1Stream);
                pdb2.loadFromStream(pdb2Stream);
                System.out.println("Pattern Databases loaded successfully.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing Pattern Databases: " + e.getMessage());
            // Để pdb1 và pdb2 là null, heuristic sẽ chuyển sang dùng Manhattan distance.
        }
    }

    private record Node(int[][] board, int cost, int depth, Node parent, int nodesExploredAtStep) {}

    public PuzzleResult solve(int[][] board) {
        Instant startTime = Instant.now();
        int n = board.length;
        validateBoard(board);
        if (!isSolvable(board)) {
            throw new IllegalArgumentException("This board configuration is not solvable");
        }
        int[][] goal = genGoal(n);
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(nod -> nod.cost + nod.depth));
        Map<String, Integer> visited = new HashMap<>();
        int nodesExplored = 0;
        Node start = new Node(board, heuristic(board), 0, null, 0);
        open.add(start);
        while (!open.isEmpty()) {
            if (Duration.between(startTime, Instant.now()).toMillis() > TIME_LIMIT_MS) {
                throw new IllegalStateException("Time limit exceeded");
            }
            Node current = open.poll();
            nodesExplored++;
            if (Arrays.deepEquals(current.board, goal)) {
                List<int[][]> path = reconstructPath(current);
                long solvingTime = Duration.between(startTime, Instant.now()).toMillis();
                return new PuzzleResult(path, path.size() - 1, nodesExplored, solvingTime);
            }
            String hash = boardToString(current.board);
            if (visited.containsKey(hash) && visited.get(hash) <= current.depth) continue;
            visited.put(hash, current.depth);
            int[] zero = findZero(current.board);
            int zx = zero[0], zy = zero[1];
            int[][] moves = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] move : moves) {
                int nx = zx + move[0], ny = zy + move[1];
                if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                    int[][] newBoard = copy(current.board);
                    newBoard[zx][zy] = newBoard[nx][ny];
                    newBoard[nx][ny] = 0;
                    String newHash = boardToString(newBoard);
                    int newDepth = current.depth + 1;
                    if (!visited.containsKey(newHash) || visited.get(newHash) > newDepth) {
                        open.add(new Node(newBoard, heuristic(newBoard), newDepth, current, nodesExplored));
                    }
                }
            }
        }
        long solvingTime = Duration.between(startTime, Instant.now()).toMillis();
        return new PuzzleResult(new ArrayList<>(), 0, nodesExplored, solvingTime);
    }

    private int[][] genGoal(int n) {
        int[][] goal = new int[n][n];
        int val = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == n - 1 && j == n - 1) goal[i][j] = 0;
                else goal[i][j] = val++;
            }
        }
        return goal;
    }

    private void validateBoard(int[][] board) {
        if (board == null || board.length < 2 || board.length != board[0].length) {
            throw new IllegalArgumentException("Board must be NxN and size >= 2");
        }
        int n = board.length;
        boolean hasZero = false;
        Set<Integer> numbers = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 0) hasZero = true;
                if (board[i][j] < 0 || board[i][j] >= n * n) {
                    throw new IllegalArgumentException("Board must contain numbers from 0 to " + (n * n - 1));
                }
                numbers.add(board[i][j]);
            }
        }
        if (!hasZero) throw new IllegalArgumentException("Board must contain number 0");
        if (numbers.size() != n * n) throw new IllegalArgumentException("Board must contain unique numbers from 0 to " + (n * n - 1));
    }

    public boolean isSolvable(int[][] board) {
        int n = board.length;
        int[] flatBoard = new int[n * n];
        int k = 0;
        int emptyRow = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                flatBoard[k] = board[i][j];
                if (board[i][j] == 0) {
                    emptyRow = n - 1 - i; // từ dưới lên
                }
                k++;
            }
        }
        int inversions = 0;
        for (int i = 0; i < flatBoard.length - 1; i++) {
            for (int j = i + 1; j < flatBoard.length; j++) {
                if (flatBoard[i] != 0 && flatBoard[j] != 0 && flatBoard[i] > flatBoard[j]) {
                    inversions++;
                }
            }
        }
        if (n % 2 == 1) {
            // Odd size (3x3): solvable if inversions even
            return inversions % 2 == 0;
        } else {
            // Even size (4x4): CHUẨN QUỐC TẾ chẵn-chẵn, lẻ-lẻ
            return (emptyRow % 2 == 0) ? (inversions % 2 == 0) : (inversions % 2 == 1);
        }
    }

    private int heuristic(int[][] board) {
        int n = board.length;
        if (n == 4 && pdb1 != null && pdb2 != null) {
            // Sử dụng tổng heuristic từ 2 pattern database
            return pdb1.getHeuristic(board) + pdb2.getHeuristic(board);
        }
        // Mặc định: Manhattan distance
        int h = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != 0) {
                    int val = board[i][j] - 1;
                    int goalX = val / n;
                    int goalY = val % n;
                    h += Math.abs(i - goalX) + Math.abs(j - goalY);
                }
            }
        }
        return h;
    }

    private int[][] copy(int[][] board) {
        int n = board.length;
        int[][] newBoard = new int[n][n];
        for (int i = 0; i < n; i++) newBoard[i] = board[i].clone();
        return newBoard;
    }

    private int[] findZero(int[][] board) {
        int n = board.length;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (board[i][j] == 0)
                    return new int[]{i, j};
        return null;
    }

    private List<int[][]> reconstructPath(Node node) {
        LinkedList<int[][]> path = new LinkedList<>();
        while (node != null) {
            path.addFirst(node.board);
            node = node.parent;
        }
        return path;
    }

    public DetailedPuzzleResult solveWithDetails(int[][] board) {
        Instant startTime = Instant.now();
        int n = board.length;
        validateBoard(board);
        if (!isSolvable(board)) {
            throw new IllegalArgumentException("This board configuration is not solvable");
        }
        
        int[][] goal = genGoal(n);
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(nod -> nod.cost + nod.depth));
        Map<String, Integer> visited = new HashMap<>();
        int nodesExplored = 0;
        Node start = new Node(board, heuristic(board), 0, null, 0);
        open.add(start);
        
        Node solutionNode = null;
        while (!open.isEmpty()) {
            if (Duration.between(startTime, Instant.now()).toMillis() > TIME_LIMIT_MS) {
                throw new IllegalStateException("Time limit exceeded");
            }
            Node current = open.poll();
            nodesExplored++;
            
            if (Arrays.deepEquals(current.board, goal)) {
                solutionNode = current;
                break;
            }
            
            String hash = boardToString(current.board);
            if (visited.containsKey(hash) && visited.get(hash) <= current.depth) continue;
            visited.put(hash, current.depth);
            
            int[] zero = findZero(current.board);
            int zx = zero[0], zy = zero[1];
            int[][] moves = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] move : moves) {
                int nx = zx + move[0], ny = zy + move[1];
                if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                    int[][] newBoard = copy(current.board);
                    newBoard[zx][zy] = newBoard[nx][ny];
                    newBoard[nx][ny] = 0;
                    String newHash = boardToString(newBoard);
                    int newDepth = current.depth + 1;
                    if (!visited.containsKey(newHash) || visited.get(newHash) > newDepth) {
                        open.add(new Node(newBoard, heuristic(newBoard), newDepth, current, nodesExplored));
                    }
                }
            }
        }
        
        if (solutionNode == null) {
            long solvingTime = Duration.between(startTime, Instant.now()).toMillis();
            return new DetailedPuzzleResult(new ArrayList<>(), 0, nodesExplored, solvingTime, 
                                          new HashMap<>(), new HashMap<>());
        }
        
        // Tạo danh sách các bước chi tiết
        List<StepDetail> steps = createDetailedSteps(solutionNode);
        
        // Tính toán thống kê về từng con số
        Map<Integer, Integer> tileMoveCounts = calculateTileMoveCounts(steps);
        Map<Integer, List<StepDetail.Position>> tilePath = calculateTilePaths(steps);
        
        long solvingTime = Duration.between(startTime, Instant.now()).toMillis();
        return new DetailedPuzzleResult(steps, steps.size() - 1, nodesExplored, solvingTime, 
                                      tileMoveCounts, tilePath);
    }

    private List<StepDetail> createDetailedSteps(Node solutionNode) {
        List<StepDetail> steps = new ArrayList<>();
        List<Node> pathNodes = new ArrayList<>();
        
        // Thu thập tất cả các node trong đường đi
        Node current = solutionNode;
        while (current != null) {
            pathNodes.add(0, current);
            current = current.parent;
        }
        
        // Tạo StepDetail cho từng bước
        for (int i = 0; i < pathNodes.size(); i++) {
            Node node = pathNodes.get(i);
            Map<Integer, StepDetail.Position> tilePositions = extractTilePositions(node.board);
            String moveDescription = i == 0 ? "Trạng thái ban đầu" : 
                                   generateMoveDescription(pathNodes.get(i-1).board, node.board);
            
            steps.add(new StepDetail(i, node.board, tilePositions, 
                                   node.nodesExploredAtStep, node.cost, moveDescription));
        }
        
        return steps;
    }

    private Map<Integer, StepDetail.Position> extractTilePositions(int[][] board) {
        Map<Integer, StepDetail.Position> positions = new HashMap<>();
        int n = board.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] != 0) {
                    positions.put(board[i][j], new StepDetail.Position(i, j));
                }
            }
        }
        return positions;
    }

    private String generateMoveDescription(int[][] prevBoard, int[][] currentBoard) {
        int n = prevBoard.length;
        int movedTile = -1;
        String direction = "";
        
        // Tìm con số đã di chuyển
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (prevBoard[i][j] != 0 && currentBoard[i][j] == 0) {
                    // Vị trí cũ của con số đã di chuyển
                    movedTile = prevBoard[i][j];
                    // Tìm vị trí mới
                    for (int ni = 0; ni < n; ni++) {
                        for (int nj = 0; nj < n; nj++) {
                            if (currentBoard[ni][nj] == movedTile) {
                                // Xác định hướng di chuyển
                                if (ni < i) direction = "lên trên";
                                else if (ni > i) direction = "xuống dưới";
                                else if (nj < j) direction = "sang trái";
                                else if (nj > j) direction = "sang phải";
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        return "Di chuyển số " + movedTile + " " + direction;
    }

    private Map<Integer, Integer> calculateTileMoveCounts(List<StepDetail> steps) {
        Map<Integer, Integer> moveCounts = new HashMap<>();
        
        for (int i = 1; i < steps.size(); i++) {
            StepDetail prev = steps.get(i-1);
            StepDetail curr = steps.get(i);
            
            // Tìm con số đã di chuyển
            for (Map.Entry<Integer, StepDetail.Position> entry : prev.getTilePositions().entrySet()) {
                int tile = entry.getKey();
                StepDetail.Position prevPos = entry.getValue();
                StepDetail.Position currPos = curr.getTilePositions().get(tile);
                
                if (prevPos.row != currPos.row || prevPos.col != currPos.col) {
                    moveCounts.put(tile, moveCounts.getOrDefault(tile, 0) + 1);
                }
            }
        }
        
        return moveCounts;
    }

    private Map<Integer, List<StepDetail.Position>> calculateTilePaths(List<StepDetail> steps) {
        Map<Integer, List<StepDetail.Position>> paths = new HashMap<>();
        
        for (StepDetail step : steps) {
            for (Map.Entry<Integer, StepDetail.Position> entry : step.getTilePositions().entrySet()) {
                int tile = entry.getKey();
                StepDetail.Position pos = entry.getValue();
                
                paths.computeIfAbsent(tile, k -> new ArrayList<>()).add(pos);
            }
        }
        
        return paths;
    }

    private String boardToString(int[][] board) {
        int n = board.length;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                sb.append(board[i][j]).append(',');
            }
        }
        return sb.toString();
    }
}