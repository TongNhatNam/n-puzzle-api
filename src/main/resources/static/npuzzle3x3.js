// NPuzzle3x3.js
window.NPuzzle3x3 = {
    size: 3,
    genInitialBoard: function () {
        return [[1, 2, 3], [4, 5, 6], [7, 8, 0]];
    },
    isSolvable: function (board) {
        let flat = [];
        let emptyRow = 0;
        for (let i = 0; i < 3; i++) for (let j = 0; j < 3; j++) {
            flat.push(board[i][j]);
            if (board[i][j] === 0) emptyRow = 2 - i;
        }
        let inv = 0;
        for (let i = 0; i < 8; i++) for (let j = i + 1; j < 9; j++) {
            if (flat[i] && flat[j] && flat[i] > flat[j]) inv++;
        }
        return inv % 2 === 0;
    },
    shuffleBoard: function (currentBoard, createBoard, resultDiv, solveButton) {
        let board = this.genInitialBoard();
        let zero = [2, 2], lastMove = -1;
        const moves = [[-1, 0], [1, 0], [0, -1], [0, 1]];
        let steps = 15 + Math.floor(Math.random() * 6); // 10 đến 15 bước
        for (let s = 0; s < steps; s++) {
            let possible = [];
            for (let i = 0; i < 4; i++) {
                let nx = zero[0] + moves[i][0], ny = zero[1] + moves[i][1];
                if (nx >= 0 && nx < 3 && ny >= 0 && ny < 3 && i !== (lastMove ^ 1)) possible.push(i);
            }
            if (!possible.length) break;
            let move = possible[Math.floor(Math.random() * possible.length)];
            let nx = zero[0] + moves[move][0], ny = zero[1] + moves[move][1];
            board[zero[0]][zero[1]] = board[nx][ny];
            board[nx][ny] = 0; zero = [nx, ny]; lastMove = move;
        }
        createBoard(board);
        if (!this.isSolvable(board)) {
            resultDiv.className = 'result show';
            resultDiv.innerHTML = '<strong>Board không hợp lệ: Không thể giải được theo quy tắc nghịch thế</strong>';
            solveButton.disabled = true;
        } else {
            resultDiv.className = 'result';
            resultDiv.textContent = '';
            solveButton.disabled = false;
        }
        return board;
    },
    solvePuzzle: async function (currentBoard, solvePuzzleCommon) {
        await solvePuzzleCommon();
    }
};

// Hàm gọi API chi tiết và hiển thị modal
window.solvePuzzleDetailed = async function (board) {
    const detailedButton = document.getElementById('detailedSolveButton');
    const modal = document.getElementById('detailedModal');
    const stepsContainer = document.getElementById('detailedStepsContainer');
    const closeBtn = document.getElementById('closeDetailedModal');
    detailedButton.disabled = true;
    stepsContainer.innerHTML = 'Đang giải...';
    modal.style.display = 'flex';
    try {
        const response = await fetch('/api/solve-detailed', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ board })
        });
        if (!response.ok) throw new Error('Lỗi máy chủ');
        const data = await response.json();
        if (!data.steps || !data.steps.length) {
            stepsContainer.innerHTML = `<strong>${data.error || 'Không tìm thấy lời giải.'}</strong>`;
        } else {
            let html = `<div><strong>Tổng bước:</strong> ${data.totalSteps} | <strong>Tổng node:</strong> ${data.totalNodesExplored} | <strong>Thời gian:</strong> ${data.solvingTimeMs}ms</div>`;
            html += '<ol>';
            data.steps.forEach(step => {
                html += `<li><div><strong>Bước ${step.stepNumber}:</strong> ${step.moveDescription}<br/>`;
                html += '<table style="border-collapse:collapse;">';
                step.board.forEach(row => {
                    html += '<tr>' + row.map(cell => `<td style="border:1px solid #ccc;padding:4px 8px;">${cell === 0 ? '' : cell}</td>`).join('') + '</tr>';
                });
                html += '</table>';
                html += `<div style="font-size:12px;">Heuristic: ${step.heuristicValue}, Node explored: ${step.nodesExploredAtStep}</div>`;
                html += '</div></li>';
            });
            html += '</ol>';
            // Thống kê từng con số
            html += '<h3>Thống kê số lần di chuyển của từng con số:</h3><ul>';
            Object.entries(data.tileMoveCounts || {}).forEach(([tile, count]) => {
                html += `<li>Số ${tile}: ${count} lần</li>`;
            });
            html += '</ul>';
            stepsContainer.innerHTML = html;
        }
    } catch (e) {
        stepsContainer.innerHTML = `<strong>Lỗi: ${e.message}</strong>`;
    } finally {
        detailedButton.disabled = false;
    }
    closeBtn.onclick = () => { modal.style.display = 'none'; };
    modal.onclick = (e) => { if (e.target === modal) modal.style.display = 'none'; };
}; 