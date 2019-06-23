package game;

import constants.Piece;
import constants.ReversiConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class Reversi {

    private static final int BOARD_SIZE = 8;

    private JButton[][] boardButtons;
    private JFrame gui;
    private Image blackPiece;
    private Image whitePiece;

    private GameState state;

    private int moveTotalNodes = 0;
    private int movePrunedNodes = 0;
    private int gameTotalNodes = 0;
    private int gamePrunedNodes = 0;

    /**
     * Sets up the game and begins execution
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

        // Reset the number of nodes visited/pruned for the game
        gamePrunedNodes = 0;
        gameTotalNodes = 0;

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
    void attemptMove(int row, int column) {

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
        int validMoves = markValidMoves(state.getBoard(), state.getCurrentPlayer());
        drawBoard();

        // If we need to swap players because the current new player doesn't have a valid move,
        // then call finishTurn again and return from this finishTurn() call
        if (checkFinished(validMoves)) {
            finishTurn();
            return;
        }


        // Only AI Move
        if(ReversiConstants.NUMBER_OF_AI >= 2 || (ReversiConstants.NUMBER_OF_AI == 1 && state.isComputerPlayer())) {
            // Reset number of visited nodes/pruned nodes for every move the ai makes
            moveTotalNodes = 0;
            movePrunedNodes = 0;
            Move bestMove;

            // Create two different minimax functions for tweaking parameters if we're using 2 AI/Computers to play
            if(ReversiConstants.NUMBER_OF_AI >= 2) {
                if(state.getCurrentPlayer().equals(Piece.WHITE)) {
                    bestMove = minimax(0, true, state.getBoard(), state.getCurrentPlayer(), Integer.MIN_VALUE, Integer.MAX_VALUE);
                } else {
                    bestMove = minimaxTwo(0, true, state.getBoard(), state.getCurrentPlayer(), Integer.MIN_VALUE, Integer.MAX_VALUE);
                }
            } else {
                bestMove = minimax(0, true, state.getBoard(), state.getCurrentPlayer(), Integer.MIN_VALUE, Integer.MAX_VALUE);
            }

            System.out.println("Minimax score for player: " + state.getCurrentPlayer() + " and current board is: " + bestMove.getScore() + " row: " + bestMove.getRow() + " column: " + bestMove.getColumn());
            System.out.println("Total number of nodes: " + moveTotalNodes + " Total number of nodes pruned: " + movePrunedNodes);

            // Now that we have the minimax, attempt the move and finish turn
            this.attemptMove(bestMove.getRow(), bestMove.getColumn());
        }
    }

    /**
     * Checks if there is a valid move at every board location.
     * Called after a player makes a move
     */
    private int markValidMoves(Piece[][] board, Piece player) {
        int validMoves = 0;
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
//                    System.out.println(i + ", " + j + " is a valid move");
                    board[i][j] = Piece.POSSIBLE_MOVE;
                    validMoves += 1;
                }
            }
        }
//        System.out.println("There are " + validMoves + " valid moves");
        return validMoves;

    }

    /**
     * Check if the game is finished and display a box showing the score if it is.
     * <p>
     * A game is defined as being over if all pieces are played or neither player can make any move.
     * If one player is unable to move, the game is not over as long as the other player can make a
     * valid move.
     *
     * @param validMoves - the number of valid moves left on the board for the current player
     * @return - returns true if the players need to be switched as there is no valid move
     * for the current player
     */
    private boolean checkFinished(int validMoves) {

        // If the number of valid moves is zero for the current player, check the other player
        if (validMoves == 0) {

            Piece[][] boardCopy = makeCopy(state.getBoard());
            int otherPlayerValidMoves = markValidMoves(boardCopy, getOpposite(state.getCurrentPlayer()));

            // If both players have zero moves left, then the game is over
            if (otherPlayerValidMoves == 0) {

                System.out.println("No valid moves left.  Game over");
                System.out.println("Number of total nodes: " + gameTotalNodes + " number of pruned nodes: " + gamePrunedNodes);

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
                if (blackPieces == whitePieces) {
                    message = "Tie game!  32-32.";
                } else if (blackPieces > whitePieces) {
                    message = "Black wins " + blackPieces + " to " + whitePieces;
                } else {
                    message = "White wins: " + whitePieces + " to " + blackPieces;
                }

                message += " click OK or close to play a new game";

                JOptionPane.showMessageDialog(gui, message);
                newGame();
            } else {
                System.out.println("No valid moves for the current player.  Swapping players");
                return true;
            }
        }
        return false;
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
    private boolean checkDirection(Piece[][] board, Piece player, int row, int col, int rowIncrement,
                                   int colIncrement, boolean hitOpposite, boolean testMove) {

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
     * @param depth  - current depth of the tree
     * @param isMax  - if we're maximizing the tree currently
     * @param board  - the board object
     * @param player - the current moving player for the given board object
     * @param alpha  - the alpha score for alpha-beta pruning
     * @param beta   - the beta scroe for alpha-beta pruning
     * @return - an integer representing the minimax output
     */
    private Move minimax(int depth, boolean isMax, Piece[][] board, Piece player, int alpha, int beta) {

        // If we've reached out depth, then return the static evaluation function
        if (depth == ReversiConstants.MINIMAX_DEPTH_PLAYER_ONE) {
            return new Move(staticEvaluation(board), -1, -1);
        }

        ArrayList<Move> children = new ArrayList<>();

        // Find children (valid moves) of the current board object
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    Piece[][] child = makeCopy(board);

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

                    Move validBoardMove;
                    if (isMax) {
                        validBoardMove = new Move(board, Integer.MIN_VALUE, i, j);
                    } else {
                        validBoardMove = new Move(board, Integer.MAX_VALUE, i, j);
                    }


                    // Add to List of children
                    children.add(validBoardMove);

                }
            }
        }

        // Default move values
        Move bestMove;

        if (isMax) {
            bestMove = new Move(Integer.MIN_VALUE, -1, -1);
        } else {
            bestMove = new Move(Integer.MAX_VALUE, -1, -1);
        }

        // Switch player
        Piece nextPlayer = getOpposite(player);
        moveTotalNodes += children.size();
        gameTotalNodes += children.size();

        // Go through every valid board move
        for (int i = 0; i < children.size(); i++) {
            Move childBoardMove = children.get(i);

            // Maximize / minimize as necessary
            if (isMax) {
                Move move = minimax(depth + 1, false, childBoardMove.getBoard(), nextPlayer, alpha, beta);
                if (move.getScore() > bestMove.getScore()) {
                    bestMove.setRow(childBoardMove.getRow());
                    bestMove.setColumn(childBoardMove.getColumn());
                    bestMove.setScore(move.getScore());
                    alpha = Math.max(alpha, bestMove.getScore());
                }
            } else {
                Move move = minimax(depth + 1, true, childBoardMove.getBoard(), nextPlayer, alpha, beta);
                if (move.getScore() < bestMove.getScore()) {
                    bestMove.setRow(childBoardMove.getRow());
                    bestMove.setColumn(childBoardMove.getColumn());
                    bestMove.setScore(move.getScore());
                    beta = Math.min(beta, bestMove.getScore());
                }
            }

            // Alpha beta pruning
            if (beta <= alpha) {
                movePrunedNodes += (children.size() - 1 - i);
                gamePrunedNodes += (children.size() - 1 - i);
                break;
            }
        }

        return bestMove;

    }


    /**
     * Recursive minimax function - uses a different static evaluation compared to the other minimax
     *
     * @param depth  - current depth of the tree
     * @param isMax  - if we're maximizing the tree currently
     * @param board  - the board object
     * @param player - the current moving player for the given board object
     * @param alpha  - the alpha score for alpha-beta pruning
     * @param beta   - the beta scroe for alpha-beta pruning
     * @return - an integer representing the minimax output
     */
    private Move minimaxTwo(int depth, boolean isMax, Piece[][] board, Piece player, int alpha, int beta) {

        // If we've reached out depth, then return the static evaluation function
        if (depth == ReversiConstants.MINIMAX_DEPTH_PLAYER_TWO) {
            return new Move(staticEvaluationTwo(board), -1, -1);
        }

        ArrayList<Move> children = new ArrayList<>();

        // Find children (valid moves) of the current board object
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    Piece[][] child = makeCopy(board);

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

                    Move validBoardMove;
                    if (isMax) {
                        validBoardMove = new Move(board, Integer.MIN_VALUE, i, j);
                    } else {
                        validBoardMove = new Move(board, Integer.MAX_VALUE, i, j);
                    }


                    // Add to List of children
                    children.add(validBoardMove);

                }
            }
        }

        // Default move values
        Move bestMove;

        if (isMax) {
            bestMove = new Move(Integer.MIN_VALUE, -1, -1);
        } else {
            bestMove = new Move(Integer.MAX_VALUE, -1, -1);
        }

        // Switch player
        Piece nextPlayer = getOpposite(player);
        moveTotalNodes += children.size();
        gameTotalNodes += children.size();

        // Go through every valid board move
        for (int i = 0; i < children.size(); i++) {
            Move childBoardMove = children.get(i);

            // Maximize / minimize as necessary
            if (isMax) {
                Move move = minimaxTwo(depth + 1, false, childBoardMove.getBoard(), nextPlayer, alpha, beta);
                if (move.getScore() > bestMove.getScore()) {
                    bestMove.setRow(childBoardMove.getRow());
                    bestMove.setColumn(childBoardMove.getColumn());
                    bestMove.setScore(move.getScore());
                    alpha = Math.max(alpha, bestMove.getScore());
                }
            } else {
                Move move = minimaxTwo(depth + 1, true, childBoardMove.getBoard(), nextPlayer, alpha, beta);
                if (move.getScore() < bestMove.getScore()) {
                    bestMove.setRow(childBoardMove.getRow());
                    bestMove.setColumn(childBoardMove.getColumn());
                    bestMove.setScore(move.getScore());
                    beta = Math.min(beta, bestMove.getScore());
                }
            }

            // Alpha beta pruning
            if (beta <= alpha) {
                movePrunedNodes += (children.size() - 1 - i);
                gamePrunedNodes += (children.size() - 1 - i);
                break;
            }
        }

        return bestMove;

    }

    /**
     * Make a copy of the game board
     *
     * @param board - the board to copy
     * @return - a copy of the board passed in
     */
    private Piece[][] makeCopy(Piece[][] board) {
        Piece[][] newBoard = new Piece[board.length][board.length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                newBoard[i][j] = board[i][j];
            }
        }
        return newBoard;
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
                } else if (board[i][j].equals(getOpposite(state.getCurrentPlayer()))) {
                    score--;
                }
            }
        }
        return score;
    }

    /**
     * Static evaluation function which counts the number of corners a player has compared to the other player
     *
     * @param board - the board
     * @return (total corners held - total corners held by opponent)
     */
    private int staticEvaluation_CheckCorners(Piece[][] board) {
        int score = 0;

        // Top left corner
        if(board[0][0].equals(state.getCurrentPlayer())) {
            score++;
        } else if(board[0][0].equals(getOpposite(state.getCurrentPlayer()))) {
            score--;
        }

        // Top right corner
        if(board[0][board.length - 1].equals(state.getCurrentPlayer())) {
            score++;
        } else if(board[0][board.length - 1].equals(getOpposite(state.getCurrentPlayer()))) {
            score--;
        }

        // Bottom left corner
        if(board[board.length - 1][0].equals(state.getCurrentPlayer())) {
            score++;
        } else if(board[board.length - 1][0].equals(getOpposite(state.getCurrentPlayer()))) {
            score--;
        }

        // Bottom right corner
        if(board[board.length - 1][board.length - 1].equals(state.getCurrentPlayer())) {
            score++;
        } else if(board[board.length - 1][board.length - 1].equals(getOpposite(state.getCurrentPlayer()))) {
            score--;
        }

        return score;
    }

    /**
     * Static evaluation function for how many potential moves a player has based on current board state.
     * Note: There is a disadvantage.  Since we don't have the previous move, we aren't measuring
     * the 100% correct potential moves of the opponent board.  We're just utilizing the same board to measure
     *
     * @param board - the current board
     * @return - (potential moves of current player - potential moves of opponent)
     */
    private int staticEvaluation_CheckPotentialMoves(Piece[][] board) {
        // Create an opponent board (same board)
        Piece[][] opponentBoard = makeCopy(board);
        clearValidMoves(opponentBoard);
        markValidMoves(opponentBoard, getOpposite(state.getCurrentPlayer()));

        int score = 0;
        for(int i = 0; i < board.length; i++) {
            for(int j = 0; j < board.length; j++) {

                // Tally based on current player mobility
                if(board[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    score++;
                }

                // Tally based on other player mobility
                if(opponentBoard[i][j].equals(Piece.POSSIBLE_MOVE)) {
                    score--;
                }
            }
        }

        return score;
    }

    private int staticEvaluation(Piece[][] board) {
        return (100 * staticEvaluation_CheckCorners(board)) +
                (10 * staticEvaluation_CheckPotentialMoves(board)) +
                staticEvaluation_CountPieces(board);
    }

    private int staticEvaluationTwo(Piece[][] board) {
        return staticEvaluation_CheckCorners(board) +
                staticEvaluation_CheckPotentialMoves(board) +
                staticEvaluation_CountPieces(board);
    }
}

