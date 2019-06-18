package game;

import constants.Piece;
import constants.ReversiConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Reversi {

    private static final int BOARD_SIZE = 8;

    private JButton[][] boardButtons;
    private JFrame gui;
    private Image blackPiece;
    private Image whitePiece;

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
        drawBoard();
    }

    /**
     * Draws pieces on the board based on the current Game state
     */
    private void drawBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                switch (state.getBoard()[i][j]) {
                    case BLACK:
                        boardButtons[i][j].setIcon(new ImageIcon((blackPiece)));
                        break;

                    case WHITE:
                        boardButtons[i][j].setIcon(new ImageIcon((whitePiece)));
                        break;

                    case NONE:
                        boardButtons[i][j].setIcon(null);
                }
            }
        }
    }

    /**
     * Attempt to place a piece at the location
     *
     * @param row    - row to place piece at
     * @param column - column to place piece at
     */
    public void attemptMove(int row, int column) {

        // Check every direction for valid move
        boolean validMove = checkDirection(row, column, -1, -1, false);
        validMove = checkDirection(row, column, -1, 0, false) || validMove;
        validMove = checkDirection(row, column, -1, 1, false) || validMove;
        validMove = checkDirection(row, column, 0, 1, false) || validMove;
        validMove = checkDirection(row, column, 1, 1, false) || validMove;
        validMove = checkDirection(row, column, 1, 0, false) || validMove;
        validMove = checkDirection(row, column, 1, -1, false) || validMove;
        validMove = checkDirection(row, column, 0, -1, false) || validMove;

        if (validMove) {
            state.nextPlayer();
        }
        System.out.println("Was it a valid move? " + validMove);


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
    private boolean checkDirection(int row, int col, int rowIncrement, int colIncrement, boolean hitOpposite) {

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

        // If hit opposite and now other color, return true
        if (hitOpposite && state.getBoard()[newRow][newCol].equals(state.getCurrentPlayer())) {
            makeMove(row, col, -1 * rowIncrement, -1 * colIncrement);
            return true;
        }

        return checkDirection(newRow, newCol, rowIncrement, colIncrement, true);

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
        drawBoard();
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

