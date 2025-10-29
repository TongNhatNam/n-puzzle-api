package com.npuzzle.solver;

import java.io.*;
import java.util.*;

public class PatternDatabase implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Set<Integer> patternTiles;
    private final Map<String, Integer> pdb;

    public PatternDatabase(Set<Integer> patternTiles) {
        this.patternTiles = patternTiles;
        this.pdb = new HashMap<>();
    }

    // Sinh PDB bằng BFS từ trạng thái đích
    public void build() {
        Queue<int[][]> queue = new LinkedList<>();
        Map<String, Integer> visited = new HashMap<>();
        int[][] goal = genGoal();
        queue.add(goal);
        visited.put(serializePattern(goal), 0);
        while (!queue.isEmpty()) {
            int[][] state = queue.poll();
            int depth = visited.get(serializePattern(state));
            int[] zero = findZero(state);
            int zx = zero[0], zy = zero[1];
            int[][] moves = {{-1,0},{1,0},{0,-1},{0,1}};
            for (int[] move : moves) {
                int nx = zx + move[0], ny = zy + move[1];
                if (nx >= 0 && nx < 4 && ny >= 0 && ny < 4) {
                    int[][] newBoard = copy(state);
                    newBoard[zx][zy] = newBoard[nx][ny];
                    newBoard[nx][ny] = 0;
                    // Chỉ di chuyển nếu ô bị di chuyển thuộc pattern
                    if (!patternTiles.contains(newBoard[zx][zy]) && newBoard[zx][zy] != 0) continue;
                    String key = serializePattern(newBoard);
                    if (!visited.containsKey(key)) {
                        visited.put(key, depth + 1);
                        queue.add(newBoard);
                    }
                }
            }
        }
        pdb.clear();
        pdb.putAll(visited);
    }

    // Lưu PDB ra file
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(pdb);
        }
    }

    // Nạp PDB từ file
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Map<String, Integer> loaded = (Map<String, Integer>) ois.readObject();
            pdb.clear();
            pdb.putAll(loaded);
        }
    }

    // Nạp PDB từ một Input Stream
    @SuppressWarnings("unchecked")
    public void loadFromStream(InputStream stream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(stream)) {
            Map<String, Integer> loaded = (Map<String, Integer>) ois.readObject();
            pdb.clear();
            pdb.putAll(loaded);
        }
    }

    // Lấy heuristic cho một board
    public int getHeuristic(int[][] board) {
        String key = serializePattern(board);
        return pdb.getOrDefault(key, 0);
    }

    // Tạo trạng thái đích cho 4x4
    private int[][] genGoal() {
        int[][] goal = new int[4][4];
        int val = 1;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 3 && j == 3) goal[i][j] = 0;
                else goal[i][j] = val++;
            }
        }
        return goal;
    }

    // Serialize pattern: chỉ lưu vị trí các ô thuộc pattern, các ô khác là X
    private String serializePattern(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int v = board[i][j];
                if (patternTiles.contains(v) || v == 0) sb.append(v).append(',');
                else sb.append('X').append(',');
            }
        }
        return sb.toString();
    }

    private int[] findZero(int[][] board) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (board[i][j] == 0)
                    return new int[]{i, j};
        return null;
    }

    private int[][] copy(int[][] board) {
        int[][] newBoard = new int[4][4];
        for (int i = 0; i < 4; i++) newBoard[i] = board[i].clone();
        return newBoard;
    }
} 