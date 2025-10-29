// image-puzzle.js

window.ImagePuzzle = {
    image: null,
    tiles: [],
    tileSize: 0,
    boardSize: 0,
    isReady: false,

    /**
     * Initializes the image puzzle with an image file and board size.
     * @param {File} file The image file from the input.
     * @param {number} size The board size (e.g., 3 for 3x3).
     * @param {function} callback A function to call when the image is processed.
     */
    init: function(file, size, callback) {
        this.boardSize = size;
        this.isReady = false;
        const reader = new FileReader();

        reader.onload = (e) => {
            this.image = new Image();
            this.image.onload = () => {
                this.tileSize = this.image.width / this.boardSize;
                this.createTiles();
                this.isReady = true;
                if (callback) {
                    callback();
                }
            };
            this.image.src = e.target.result;
        };

        reader.readAsDataURL(file);
    },

    /**
     * Slices the image into tiles and stores them as canvas elements.
     */
    createTiles: function() {
        this.tiles = [];
        const tileWidth = this.image.width / this.boardSize;
        const tileHeight = this.image.height / this.boardSize;

        for (let i = 0; i < this.boardSize; i++) {
            for (let j = 0; j < this.boardSize; j++) {
                const tileCanvas = document.createElement('canvas');
                tileCanvas.width = tileWidth;
                tileCanvas.height = tileHeight;
                const ctx = tileCanvas.getContext('2d');
                
                // Draw the image portion onto the canvas
                ctx.drawImage(
                    this.image,
                    j * tileWidth,  // source x
                    i * tileHeight, // source y
                    tileWidth,      // source width
                    tileHeight,     // source height
                    0,              // destination x
                    0,              // destination y
                    tileWidth,      // destination width
                    tileHeight      // destination height
                );
                
                // The tile number corresponds to the puzzle board number (1 to N*N-1)
                const tileNumber = i * this.boardSize + j + 1;
                // Vẽ số lên tile (trừ ô trống)
                if (tileNumber < this.boardSize * this.boardSize) {
                    ctx.font = `${Math.floor(tileHeight / 2)}px Arial`;
                    ctx.fillStyle = 'rgba(0,0,0,0.7)';
                    ctx.textAlign = 'center';
                    ctx.textBaseline = 'middle';
                    ctx.lineWidth = 4;
                    // Viền trắng cho dễ nhìn
                    ctx.strokeStyle = 'white';
                    ctx.strokeText(tileNumber, tileWidth / 2, tileHeight / 2);
                    ctx.fillText(tileNumber, tileWidth / 2, tileHeight / 2);
                }
                this.tiles[tileNumber] = tileCanvas;
            }
        }
    },

    /**
     * Gets the canvas for a specific tile number.
     * @param {number} tileNumber The number on the puzzle board.
     * @returns {HTMLCanvasElement|null} The canvas element for the tile or null.
     */
    getTile: function(tileNumber) {
        if (tileNumber > 0 && tileNumber < this.tiles.length) {
            return this.tiles[tileNumber];
        }
        return null;
    }
}; 