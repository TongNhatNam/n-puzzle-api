// NPuzzle4x4.js
window.NPuzzle4x4 = {
    size: 4,
    genInitialBoard: function () {
        return [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12], [13, 14, 15, 0]];
    },
    isSolvable: function (board) {
        // Flatten board
        let flat = [];
        let emptyRow = 0;
        for (let i = 0; i < 4; i++) for (let j = 0; j < 4; j++) {
            flat.push(board[i][j]);
            if (board[i][j] === 0) emptyRow = 3 - i; // dòng trống từ dưới lên (0-based)
        }
        // Đếm nghịch thế
        let inv = 0;
        for (let i = 0; i < 15; i++) for (let j = i + 1; j < 16; j++) {
            if (flat[i] !== 0 && flat[j] !== 0 && flat[i] > flat[j]) inv++;
        }
        return (emptyRow % 2) === (inv % 2);
    },
    shuffleBoard: function (currentBoard, createBoard, resultDiv, solveButton) {
        // Bắt đầu từ trạng thái đã giải
        let board = this.genInitialBoard();
        let zero = [3, 3], lastMove = -1;
        const moves = [[-1, 0], [1, 0], [0, -1], [0, 1]];
        let steps = 15 + Math.floor(Math.random() * 6); // 10 đến 15 bước
        for (let s = 0; s < steps; s++) {
            let possible = [];
            for (let i = 0; i < 4; i++) {
                let nx = zero[0] + moves[i][0], ny = zero[1] + moves[i][1];
                if (nx >= 0 && nx < 4 && ny >= 0 && ny < 4 && i !== (lastMove ^ 1)) possible.push(i);
            }
            if (!possible.length) break;
            let move = possible[Math.floor(Math.random() * possible.length)];
            let nx = zero[0] + moves[move][0], ny = zero[1] + moves[move][1];
            board[zero[0]][zero[1]] = board[nx][ny];
            board[nx][ny] = 0; zero = [nx, ny]; lastMove = move;
        }
        createBoard(board);

        // Luôn mở khoá nút Solve và ẩn thông báo lỗi
        solveButton.disabled = false;
        resultDiv.className = 'result';
        resultDiv.textContent = '';
        return board;
    },
    solvePuzzle: async function (currentBoard, solvePuzzleCommon) {
        await solvePuzzleCommon();
    }
};
// Đảm bảo hàm solvePuzzleDetailed có thể dùng chung cho 4x4
window.solvePuzzleDetailed = window.solvePuzzleDetailed; // Cho phép dùng chung với index.html 