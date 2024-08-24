import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The SnakeGameLogic class contains the core logic for the Snake game.
 * It manages the game state, including the positions of the snakes, food, and scores.
 */
public class SnakeGameLogic {
    public static final int TILE_SIZE = 20;
    private int width = 20;
    private int height = 20;
    private long lastUpdateTime = 0; // Records the last time the direction was updated
    private static final long DIRECTION_CHANGE_DELAY = 100; // Minimum interval time for direction changes in milliseconds

    private List<Point> snake1 = new ArrayList<>();
    private Direction direction1 = Direction.RIGHT;
    private boolean gameOver1 = false;
    private int score1 = 0;

    private List<Point> snake2 = new ArrayList<>();
    private Direction direction2 = Direction.LEFT;
    private boolean gameOver2 = false;
    private int score2 = 0;

    private Point food;
    private boolean isTwoPlayerMode = false;
    private boolean isBorderless = false;

    private Color snake1Color = Color.GREEN;
    private Color snake2Color = Color.BLUE;
    private Color foodColor = Color.RED;
    private Color backgroundColor = Color.BLACK;

    private double gameSpeed = 1.0; // Game speed, 1.0 is normal speed

    /**
     * Represents the direction in which the snake is moving.
     */
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    /**
     * Represents a point on the game board with x and y coordinates.
     */
    public static class Point {
        int x, y;

        /**
         * Constructs a Point with the specified coordinates.
         *
         * @param x the x-coordinate
         * @param y the y-coordinate
         */
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Updates the game settings with the specified parameters.
     *
     * @param width            the width of the game board
     * @param height           the height of the game board
     * @param isBorderless     whether the game is in borderless mode
     * @param snake1Color      the color of snake 1
     * @param snake2Color      the color of snake 2
     * @param foodColor        the color of the food
     * @param backgroundColor  the background color of the game board
     * @param isTwoPlayerMode  whether the game is in two-player mode
     * @param gameSpeed        the speed of the game
     */
    public void updateSettings(int width, int height, boolean isBorderless, Color snake1Color, Color snake2Color,
                               Color foodColor, Color backgroundColor, boolean isTwoPlayerMode, double gameSpeed) {
        this.width = width;
        this.height = height;
        this.isBorderless = isBorderless;
        this.snake1Color = snake1Color;
        this.snake2Color = snake2Color;
        this.foodColor = foodColor;
        this.backgroundColor = backgroundColor;
        this.isTwoPlayerMode = isTwoPlayerMode;
        this.gameSpeed = gameSpeed;
    }

    /**
     * Resets the game state, including the positions of the snakes and food.
     * Resets scores and directions for both snakes.
     */
    public void resetGame() {
        snake1.clear();
        snake1.add(new Point(width / 4, height / 2));
        direction1 = Direction.RIGHT;
        gameOver1 = false;
        score1 = 0;

        if (isTwoPlayerMode) {
            snake2.clear();
            snake2.add(new Point(3 * width / 4, height / 2));
            direction2 = Direction.LEFT;
            gameOver2 = false;
            score2 = 0;
        }

        spawnFood();
    }

    /**
     * Updates the game state for each frame of the game loop.
     * Handles snake movement and collision detection.
     */
    public void run() {
        if (gameOver1 || (isTwoPlayerMode && gameOver2)) {
            return;
        }

        moveSnake(snake1, direction1);
        checkCollisions(snake1);

        if (isTwoPlayerMode) {
            moveSnake(snake2, direction2);
            checkCollisions(snake2);
            checkInterSnakeCollisions(); // Checks collisions between the two snakes
        }
    }

    /**
     * Moves the specified snake in the current direction and handles collisions and food consumption.
     *
     * @param snake     the list of points representing the snake's body
     * @param direction the direction in which the snake is moving
     */
    private void moveSnake(List<Point> snake, Direction direction) {
        Point head = snake.get(0);
        Point newHead = switch (direction) {
            case UP -> new Point(head.x, head.y - 1);
            case DOWN -> new Point(head.x, head.y + 1);
            case LEFT -> new Point(head.x - 1, head.y);
            case RIGHT -> new Point(head.x + 1, head.y);
        };

        // Handle borderless mode
        if (isBorderless) {
            if (newHead.x < 0) newHead.x = width - 1;
            if (newHead.y < 0) newHead.y = height - 1;
            if (newHead.x >= width) newHead.x = 0;
            if (newHead.y >= height) newHead.y = 0;
        }

        snake.add(0, newHead);
        if (newHead.x == food.x && newHead.y == food.y) {
            if (snake == snake1) {
                score1++;
            } else {
                score2++;
            }
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    /**
     * Checks for collisions between the specified snake and the game boundaries or itself.
     *
     * @param snake the list of points representing the snake's body
     */
    private void checkCollisions(List<Point> snake) {
        Point head = snake.get(0);

        if (!isBorderless && (head.x < 0 || head.y < 0 || head.x >= width || head.y >= height)) {
            if (snake == snake1) {
                gameOver1 = true;
            } else {
                gameOver2 = true;
            }
        }

        for (int i = 1; i < snake.size(); i++) {
            if (head.x == snake.get(i).x && head.y == snake.get(i).y) {
                if (snake == snake1) {
                    gameOver1 = true;
                } else {
                    gameOver2 = true;
                }
                break;
            }
        }
    }

    /**
     * Checks for collisions between the two snakes in two-player mode.
     */
    private void checkInterSnakeCollisions() {
        // Check if the head of snake 1 collides with the body of snake 2
        Point head1 = snake1.get(0);
        for (Point point : snake2) {
            if (head1.x == point.x && head1.y == point.y) {
                gameOver1 = true;
                break;
            }
        }

        // Check if the head of snake 2 collides with the body of snake 1
        Point head2 = snake2.get(0);
        for (Point point : snake1) {
            if (head2.x == point.x && head2.y == point.y) {
                gameOver2 = true;
                break;
            }
        }
    }

    /**
     * Spawns food at a random location on the game board.
     * Ensures that food does not spawn on the snakes' bodies.
     */
    private void spawnFood() {
        Random random = new Random();
        boolean validPosition;
        do {
            validPosition = true;
            food = new Point(random.nextInt(width), random.nextInt(height));
            for (Point point : snake1) {
                if (food.x == point.x && food.y == point.y) {
                    validPosition = false;
                    break;
                }
            }
            if (isTwoPlayerMode) {
                for (Point point : snake2) {
                    if (food.x == point.x && food.y == point.y) {
                        validPosition = false;
                        break;
                    }
                }
            }
        } while (!validPosition);
    }

    /**
     * Updates the direction of the snake based on the player's input, with a delay to prevent rapid changes.
     *
     * @param code the key code of the player's input
     */
    public void updateDirection(KeyCode code) {
        if (System.currentTimeMillis() - lastUpdateTime > DIRECTION_CHANGE_DELAY) {  // Check the time interval for direction updates
            switch (code) {
                case UP -> {
                    if (direction1 != Direction.DOWN) direction1 = Direction.UP;
                }
                case DOWN -> {
                    if (direction1 != Direction.UP) direction1 = Direction.DOWN;
                }
                case LEFT -> {
                    if (direction1 != Direction.RIGHT) direction1 = Direction.LEFT;
                }
                case RIGHT -> {
                    if (direction1 != Direction.LEFT) direction1 = Direction.RIGHT;
                }
                case W -> {
                    if (isTwoPlayerMode && direction2 != Direction.DOWN) direction2 = Direction.UP;
                }
                case S -> {
                    if (isTwoPlayerMode && direction2 != Direction.UP) direction2 = Direction.DOWN;
                }
                case A -> {
                    if (isTwoPlayerMode && direction2 != Direction.RIGHT) direction2 = Direction.LEFT;
                }
                case D -> {
                    if (isTwoPlayerMode && direction2 != Direction.LEFT) direction2 = Direction.RIGHT;
                }
            }
            lastUpdateTime = System.currentTimeMillis();  // Update the last time the direction changed
        }
    }

    // Getters for various game properties

    /**
     * Gets the width of the game board.
     *
     * @return the width of the game board
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the game board.
     *
     * @return the height of the game board
     */
    public int getHeight() {
        return height;
    }

    /**
     * Checks if the game is in borderless mode.
     *
     * @return true if the game is in borderless mode, false otherwise
     */
    public boolean isBorderless() {
        return isBorderless;
    }

    /**
     * Checks if the game is in two-player mode.
     *
     * @return true if the game is in two-player mode, false otherwise
     */
    public boolean isTwoPlayerMode() {
        return isTwoPlayerMode;
    }

    /**
     * Gets the color of snake 1.
     *
     * @return the color of snake 1
     */
    public Color getSnake1Color() {
        return snake1Color;
    }

    /**
     * Gets the color of snake 2.
     *
     * @return the color of snake 2
     */
    public Color getSnake2Color() {
        return snake2Color;
    }

    /**
     * Gets the color of the food.
     *
     * @return the color of the food
     */
    public Color getFoodColor() {
        return foodColor;
    }

    /**
     * Gets the background color of the game board.
     *
     * @return the background color of the game board
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Gets the speed of the game.
     *
     * @return the speed of the game
     */
    public double getGameSpeed() {
        return gameSpeed;
    }

    /**
     * Gets the list of points representing snake 1's body.
     *
     * @return the list of points for snake 1
     */
    public List<Point> getSnake1() {
        return snake1;
    }

    /**
     * Gets the list of points representing snake 2's body.
     *
     * @return the list of points for snake 2
     */
    public List<Point> getSnake2() {
        return snake2;
    }

    /**
     * Gets the current direction of snake 1.
     *
     * @return the direction of snake 1
     */
    public Direction getDirection1() {
        return direction1;
    }

    /**
     * Gets the current direction of snake 2.
     *
     * @return the direction of snake 2
     */
    public Direction getDirection2() {
        return direction2;
    }

    /**
     * Gets the current position of the food.
     *
     * @return the position of the food
     */
    public Point getFood() {
        return food;
    }

    /**
     * Gets the score of snake 1.
     *
     * @return the score of snake 1
     */
    public int getScore1() {
        return score1;
    }

    /**
     * Gets the score of snake 2.
     *
     * @return the score of snake 2
     */
    public int getScore2() {
        return score2;
    }

    /**
     * Checks if snake 1 has reached a game over state.
     *
     * @return true if snake 1 is in a game over state, false otherwise
     */
    public boolean isGameOver1() {
        return gameOver1;
    }

    /**
     * Checks if snake 2 has reached a game over state.
     *
     * @return true if snake 2 is in a game over state, false otherwise
     */
    public boolean isGameOver2() {
        return gameOver2;
    }
}
