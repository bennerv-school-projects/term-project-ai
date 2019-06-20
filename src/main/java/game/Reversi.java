package game;

import constants.Piece;
import constants.ReversiConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;

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

        // draw the board
        markValidMoves();
        drawBoard();
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

        if(!state.getBoard()[row][column].equals(Piece.POSSIBLE_MOVE)) {
            return;
        }

        // Check every direction for valid move
        boolean validMove = checkDirection(row, column, -1, -1, false, false);
        validMove = checkDirection(row, column, -1, 0, false, false) || validMove;
        validMove = checkDirection(row, column, -1, 1, false, false) || validMove;
        validMove = checkDirection(row, column, 0, 1, false, false) || validMove;
        validMove = checkDirection(row, column, 1, 1, false, false) || validMove;
        validMove = checkDirection(row, column, 1, 0, false, false) || validMove;
        validMove = checkDirection(row, column, 1, -1, false, false) || validMove;
        validMove = checkDirection(row, column, 0, -1, false, false) || validMove;

        // Do checks if the move was valid
        if (validMove) {
            state.nextPlayer();
            clearPossibleMoves();
            markValidMoves();
            drawBoard();
            checkFinished();
        }
    }

    /**
     * Checks if there is a valid move at every board location.
     * Called after a player makes a move
     */
    private void markValidMoves() {
        validMoves = 0;
        boolean validMove;

        // Loop through every board location
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {

                // If this piece is not none, continue on
                if(!state.getBoard()[i][j].equals(Piece.NONE)) {
                    continue;
                }

                // Check every direction for valid
                validMove = checkDirection(i, j, -1, -1, false, true);
                validMove = validMove || checkDirection(i, j, -1, 0, false, true);
                validMove = validMove || checkDirection(i, j, -1, 1, false, true);
                validMove = validMove || checkDirection(i, j, 0, 1, false, true);
                validMove = validMove || checkDirection(i, j, 1, 1, false, true);
                validMove = validMove || checkDirection(i, j, 1, 0, false, true);
                validMove = validMove || checkDirection(i, j, 1, -1, false, true);
                validMove = validMove || checkDirection(i, j, 0, -1, false, true);

                if(validMove) {
                    state.getBoard()[i][j] = Piece.POSSIBLE_MOVE;
                    validMoves += 1;
                }
            }
        }
    }

    private void checkFinished() {

        // If the number of valid moves is zero, the game is over
        if(validMoves == 0) {

            System.out.println("No valid moves left.  Game over");

            int blackPieces = 0, whitePieces = 0;

            // Count the number of pieces
            for(int i = 0; i < BOARD_SIZE; i++) {
                for(int j = 0; j < BOARD_SIZE; j++) {
                    switch(state.getBoard()[i][j]) {
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
            if(blackPieces > whitePieces) {
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
    private void clearPossibleMoves() {
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if(state.getBoard()[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    state.getBoard()[i][j] = Piece.NONE;
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
    private boolean checkDirection(int row, int col, int rowIncrement, int colIncrement, boolean hitOpposite, boolean testMove) {

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
        if (!hitOpposite && !state.getBoard()[newRow][newCol].equals(getOpposite(state.getCurrentPlayer()))) {
            return false;
        }

        // If hit opposite color, then check if the next color is our
        if (hitOpposite) {
            if(state.getBoard()[newRow][newCol].equals(state.getCurrentPlayer())) {
                if(!testMove) {
                    makeMove(row, col, -1 * rowIncrement, -1 * colIncrement);
                }
                return true;
                // Make sure if we continue searching, we saw another opponent's piece
            } else if(!state.getBoard()[newRow][newCol].equals(getOpposite(state.getCurrentPlayer()))) {
                return false;
            }
        }

        return checkDirection(newRow, newCol, rowIncrement, colIncrement, true, testMove);

    }

    /**
     * There was a valid move clicked, so make that move
     *
     * @param row          - the current row in consideration
     * @param col          - the current column in consideration
     * @param rowIncrement - backtrack row increment value
     * @param colIncrement - backtrack column increment value
     */
    private void makeMove(int row, int col, int rowIncrement, int colIncrement) {
        while (state.getBoard()[row][col].equals(getOpposite(state.getCurrentPlayer()))) {
            state.getBoard()[row][col] = state.getCurrentPlayer();
            row = row + rowIncrement;
            col = col + colIncrement;
        }
        state.getBoard()[row][col] = state.getCurrentPlayer();
    }


    /**
     * Gets the opposite of the passed in piece
     *
     * @param piece - the piece to get the opposite of
     * @return
     */
    private Piece getOpposite(Piece piece) {
        return piece.equals(Piece.BLACK) ? Piece.WHITE : Piece.BLACK;
    }
}

