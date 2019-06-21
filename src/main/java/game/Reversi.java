package game;

import constants.Piece;
import constants.ReversiConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;


public class Reversi {

    private static final int BOARD_SIZE = 8;

    private JButton[][] boardButtons;
    private JFrame gui;
    private Image blackPiece;
    private Image whitePiece;
    private int validMoves;

    private GameState state;

    /**
     * Sets up the game and beings execution
     */
    public Reversi() {
        gui = new JFrame();
        gui.setTitle("Reversi");
        gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        gui.setSize(new Dimension(700, 700));
        gui.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            blackPiece = ImageIO.read(Reversi.class.getResource("/black_piece.png"));
            whitePiece = ImageIO.read(Reversi.class.getResource("/white_piece.png"));
        } catch (Exception e) {
            System.out.println("Failed to initialize the board.  Exiting.");
            System.exit(2);
        }

        // Scale the image to fit in the grid
        blackPiece = blackPiece.getScaledInstance(85, 85, Image.SCALE_SMOOTH);
        whitePiece = whitePiece.getScaledInstance(85, 85, Image.SCALE_SMOOTH);


        // Create a new game
        newGame();

        gui.setVisible(true);

    }

    /**
     * Re-initialize the game state
     */
    private void newGame() {

        System.out.println("Starting new game");

        // Initialize the Board
        gui.getContentPane().removeAll();
        gui.repaint();
        this.boardButtons = new JButton[BOARD_SIZE][BOARD_SIZE];
        state = new GameState(BOARD_SIZE);

        MovementListener listener = new MovementListener(this);

        // Initialize all buttons
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardButtons[i][j] = new JButton();
                boardButtons[i][j].setOpaque(true);
                boardButtons[i][j].setBackground(Color.GREEN);

                // Save row column properties (note: top left corner is 0,0 on the board)
                boardButtons[i][j].putClientProperty(ReversiConstants.ROW, i);
                boardButtons[i][j].putClientProperty(ReversiConstants.COLUMN, j);

                // Add action listener to the JButtons
                boardButtons[i][j].addActionListener(listener);

                // Add the button to the board
                gui.add(boardButtons[i][j]);
            }
        }

        finishTurn();
    }

    /**
     * Draws pieces on the board based on the current Game state
     */
    private void drawBoard() {
        gui.setVisible(false);

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                switch (state.getBoard()[i][j]) {
                    case BLACK:
                        boardButtons[i][j].setIcon(new ImageIcon((blackPiece)));
                        boardButtons[i][j].setBackground(Color.GREEN);
                        break;

                    case WHITE:
                        boardButtons[i][j].setIcon(new ImageIcon((whitePiece)));
                        boardButtons[i][j].setBackground(Color.GREEN);
                        break;

                    case POSSIBLE_MOVE:
                        boardButtons[i][j].setBackground(Color.yellow);
                        break;

                    case NONE:
                        boardButtons[i][j].setIcon(null);
                        boardButtons[i][j].setBackground(Color.GREEN);
                        break;
                }
            }
        }
        gui.setVisible(true);
    }

    /**
     * Attempt to place a piece at the location
     *
     * @param row    - row to place piece at
     * @param column - column to place piece at
     */
    public void attemptMove(int row, int column) {


        // Check to see if it's the AI's turn.
        if (state.isComputerPlayer()) {
            return;
        }

        // Make sure that the location we click on is a potential valid move
        if (!state.getBoard()[row][column].equals(Piece.POSSIBLE_MOVE)) {
            return;
        }

        // Check every direction for valid move
        boolean validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, -1, -1, false, false);
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, -1, 0, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, -1, 1, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, 0, 1, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, 1, 1, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, 1, 0, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, 1, -1, false, false) || validMove;
        validMove = checkDirection(state.getBoard(), state.getCurrentPlayer(), row, column, 0, -1, false, false) || validMove;

        // Finish the player's turn if the move is over
        if (validMove) {
            finishTurn();
        }
    }

    /**
     * Called on each valid move or initial start of the board.
     * Changes the player, clears the possible moves,
     * marks valid moves in yellow, draws the board, and then finally checks if the game is over
     */
    private void finishTurn() {
        state.changePlayer();
        clearValidMoves(state.getBoard());
        markValidMoves(state.getBoard(), state.getCurrentPlayer());
        drawBoard();
        checkFinished();

        // AI Move turn
        if(state.isComputerPlayer()) {
            System.out.println("Minimax for this board state is: " + minimax(0, true, state.getBoard(), state.getCurrentPlayer()));
        }
    }

    /**
     * Checks if there is a valid move at every board location.
     * Called after a player makes a move
     */
    private void markValidMoves(Piece[][] board, Piece player) {
        validMoves = 0;
        boolean validMove;

        // Loop through every board location
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {

                // If this piece is not none, continue on
                if (!board[i][j].equals(Piece.NONE)) {
                    continue;
                }

                // Check every direction for valid
                validMove = checkDirection(board, player, i, j, -1, -1, false, true);
                validMove = validMove || checkDirection(board, player, i, j, -1, 0, false, true);
                validMove = validMove || checkDirection(board, player, i, j, -1, 1, false, true);
                validMove = validMove || checkDirection(board, player, i, j, 0, 1, false, true);
                validMove = validMove || checkDirection(board, player, i, j, 1, 1, false, true);
                validMove = validMove || checkDirection(board, player, i, j, 1, 0, false, true);
                validMove = validMove || checkDirection(board, player, i, j, 1, -1, false, true);
                validMove = validMove || checkDirection(board, player, i, j, 0, -1, false, true);

                if (validMove) {
                    System.out.println(i + ", " + j + " is a valid move");
                    board[i][j] = Piece.POSSIBLE_MOVE;
                    validMoves += 1;
                }
            }
        }
        System.out.println("There are " + validMoves + " valid moves");

    }

    private void checkFinished() {

        // If the number of valid moves is zero, the game is over
        if (validMoves == 0) {
            System.out.println("No valid moves left.  Game over");

            int blackPieces = 0, whitePieces = 0;

            // Count the number of pieces
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    switch (state.getBoard()[i][j]) {
                        case BLACK:
                            blackPieces++;
                            break;
                        case WHITE:
                            whitePieces++;
                            break;
                    }
                }
            }

            String message;
            if (blackPieces > whitePieces) {
                message = "Black wins: " + blackPieces + " to " + whitePieces;
            } else {
                message = "White wins: " + whitePieces + " to " + blackPieces;
            }

            message += " click OK or close to play a new game";

            JOptionPane.showMessageDialog(gui, message);
            newGame();
        }
    }

    /**
     * Clears out any possible moves on the game board.
     * Called when a player makes a move
     */
    private void clearValidMoves(Piece[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    board[i][j] = Piece.NONE;
                }
            }
        }
    }

    /**
     * Checks if there was a valid move in a direction
     *
     * @param row          - the current row
     * @param col          - the current column
     * @param rowIncrement - the direction were're checking row increment
     * @param colIncrement - the direction were're checking row increment
     * @param hitOpposite  - whether we've hit the opposite piece yet
     * @return - boolean whether or not it was a valid move
     */
    private boolean checkDirection(Piece[][] board, Piece player, int row, int col, int rowIncrement, int colIncrement, boolean hitOpposite, boolean testMove) {

        // Check if out of bounds
        if (row + rowIncrement == BOARD_SIZE ||
                row + rowIncrement < 0 ||
                col + colIncrement == BOARD_SIZE ||
                col + colIncrement < 0) {
            return false;
        }

        int newRow = row + rowIncrement;
        int newCol = col + colIncrement;

        // Check for opposite color next to immediate
        if (!hitOpposite && !board[newRow][newCol].equals(getOpposite(player))) {
            return false;
        }

        // If hit opposite color, then check if the next color is our
        if (hitOpposite) {
            if (board[newRow][newCol].equals(player)) {
                if (!testMove) {
                    makeMove(board, player, row, col, -1 * rowIncrement, -1 * colIncrement);
                }
                return true;
                // Make sure if we continue searching, we saw another opponent's piece
            } else if (!board[newRow][newCol].equals(getOpposite(player))) {
                return false;
            }
        }

        return checkDirection(board, player, newRow, newCol, rowIncrement, colIncrement, true, testMove);

    }

    /**
     * There was a valid move clicked, so make that move
     *
     * @param board        - the current game board
     * @param row          - the current row in consideration
     * @param col          - the current column in consideration
     * @param rowIncrement - backtrack row increment value
     * @param colIncrement - backtrack column increment value
     */
    private void makeMove(Piece[][] board, Piece player, int row, int col, int rowIncrement, int colIncrement) {
        while (board[row][col].equals(getOpposite(player))) {
            board[row][col] = player;
            row = row + rowIncrement;
            col = col + colIncrement;
        }
        board[row][col] = player;
    }


    /**
     * Gets the opposite of the passed in piece
     *
     * @param piece - the piece to get the opposite of
     * @return the opposite piece of the passed im param
     */
    private Piece getOpposite(Piece piece) {
        return piece.equals(Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
    }

    /**
     * Recursive minimax function
     *
     * @param depth - current depth of the tree
     * @param isMax - if we're maximizing the tree currently
     * @param board - the board object
     * @param player - the current moving player for the given board object
     * @return - an integer representing the minimax output
     */
    private int minimax(int depth, boolean isMax, Piece[][] board, Piece player) {
        if (depth == ReversiConstants.MINIMAX_DEPTH) {
            return staticEvaluation_CountPieces(board);
        }

        ArrayList<Piece[][]> children = new ArrayList<>();

        // Find children (valid moves) of the current board object
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    Piece[][] child = board.clone();

                    // Go through every possible direction and make the move on the board
                    checkDirection(child, player, i, j, -1, -1, false, false);
                    checkDirection(child, player, i, j, -1, 0, false, false);
                    checkDirection(child, player, i, j, -1, 1, false, false);
                    checkDirection(child, player, i, j, 0, 1, false, false);
                    checkDirection(child, player, i, j, 1, 1, false, false);
                    checkDirection(child, player, i, j, 1, 0, false, false);
                    checkDirection(child, player, i, j, 1, -1, false, false);
                    checkDirection(child, player, i, j, 0, -1, false, false);


                    // Clear, then mark the valid moves on the new child with the new player
                    clearValidMoves(child);
                    markValidMoves(child, getOpposite(player));

                    // Add to List of children
                    children.add(child);

                }
            }
        }

        // Switch player
        final Piece nextPlayer = getOpposite(player);

        // Maximize/minimize functions for every player
        if (isMax) {
            return Collections.max(children.stream().map(child -> minimax(depth + 1, false, child, nextPlayer)).collect(Collectors.toList()));
        } else {
            return Collections.min(children.stream().map(child -> minimax(depth + 1, true, child, nextPlayer)).collect(Collectors.toList()));
        }
    }


    /**
     * Static evaluation function which counts the pieces of the current player vs the other player's pieces
     *
     * @param board - the board
     * @return (total current player pieces - total opposite player pieces)
     */
    private int staticEvaluation_CountPieces(Piece[][] board) {
        int score = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(state.getCurrentPlayer())) {
                    score++;
                } else if(board[i][j].equals(getOpposite(state.getCurrentPlayer()))) {
                    score--;
                }
            }
        }

        return score;
    }
}

