package game;

import constants.Piece;

import java.util.Arrays;

public class GameState {

    private Piece currentPlayer;
    private Piece[][] board;

    /**
     * Holds the game state of the current game
     * @param boardSize - the size of the game board
     */
    public GameState(int boardSize) {
        this.board = new Piece[boardSize][boardSize];

        // Fill the board with nothing
        for (int i = 0; i < boardSize; i++) {
            Arrays.fill(board[i], Piece.NONE);
        }

        // Initial piece placement
        this.board[3][3] = Piece.WHITE;
        this.board[3][4] = Piece.BLACK;
        this.board[4][3] = Piece.BLACK;
        this.board[4][4] = Piece.WHITE;

        currentPlayer = Piece.BLACK;
    }

    /**
     * Gets the current game board.
     *
     * @return The game board
     */
    public Piece[][] getBoard() {
        return this.board;
    }

    /**
     * Gets the current player
     *
     * @return the current player
     */
    public Piece getCurrentPlayer() {
        return this.currentPlayer;
    }

    /**
     * Swaps the current player
     *
     * @return The next player to move
     */
    public Piece nextPlayer() {
        if (currentPlayer.equals(Piece.BLACK)) {
            currentPlayer = Piece.WHITE;
        } else {
            currentPlayer = Piece.BLACK;
        }

        return currentPlayer;
    }
}