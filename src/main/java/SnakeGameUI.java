import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.Properties;

/**
 * The SnakeGameUI class is responsible for creating the user interface of the Snake game.
 * It provides options for game settings, manages the game loop, and handles user inputs.
 */
public class SnakeGameUI extends Application {
    private SnakeGameLogic gameLogic;

    private Canvas canvas;
    private GraphicsContext gc;
    private Timeline timeline;
    private Timeline countdownTimeline;

    private static final String SETTINGS_FILE = "snake_settings.properties";
    private static final int MIN_GRID_SIZE = 10; // Minimum grid size in terms of tiles

    /**
     * Starts the JavaFX application and initializes the game settings menu.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        gameLogic = new SnakeGameLogic();
        loadSettings(); // 加载设置
        showSettingsMenu(primaryStage);
    }

    /**
     * Displays the settings menu where players can configure the game before starting.
     *
     * @param primaryStage the primary stage for this application
     */
    private void showSettingsMenu(Stage primaryStage) {
        VBox menu = new VBox();
        menu.setSpacing(10);
        menu.setPadding(new Insets(10));

        // Setting up UI components
        TextField widthField = new TextField(String.valueOf(gameLogic.getWidth()));
        TextField heightField = new TextField(String.valueOf(gameLogic.getHeight()));
        CheckBox borderlessCheckBox = new CheckBox("Borderless Mode");
        borderlessCheckBox.setSelected(gameLogic.isBorderless());

        ColorPicker snake1ColorPicker = new ColorPicker(gameLogic.getSnake1Color());
        ColorPicker snake2ColorPicker = new ColorPicker(gameLogic.getSnake2Color());
        ColorPicker foodColorPicker = new ColorPicker(gameLogic.getFoodColor());
        ColorPicker backgroundColorPicker = new ColorPicker(gameLogic.getBackgroundColor());

        ToggleGroup playerToggleGroup = new ToggleGroup();
        RadioButton onePlayerButton = new RadioButton("One Player");
        onePlayerButton.setToggleGroup(playerToggleGroup);
        onePlayerButton.setSelected(!gameLogic.isTwoPlayerMode());
        RadioButton twoPlayerButton = new RadioButton("Two Players");
        twoPlayerButton.setToggleGroup(playerToggleGroup);
        twoPlayerButton.setSelected(gameLogic.isTwoPlayerMode());

        ChoiceBox<String> difficultyChoiceBox = new ChoiceBox<>();
        difficultyChoiceBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyChoiceBox.setValue("Medium");

        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> {
            try {
                int width = Integer.parseInt(widthField.getText());
                int height = Integer.parseInt(heightField.getText());

                // Check if the grid size is at least 10x10
                if (width < MIN_GRID_SIZE || height < MIN_GRID_SIZE) {
                    showAlert("Invalid Input", "Width and height must be at least " + MIN_GRID_SIZE + " tiles.");
                    return;
                }

                boolean isBorderless = borderlessCheckBox.isSelected();
                Color snake1Color = snake1ColorPicker.getValue();
                Color snake2Color = snake2ColorPicker.getValue();
                Color foodColor = foodColorPicker.getValue();
                Color backgroundColor = backgroundColorPicker.getValue();
                boolean isTwoPlayerMode = twoPlayerButton.isSelected();
                double gameSpeed = switch (difficultyChoiceBox.getValue()) {
                    case "Easy" -> 0.5;
                    case "Medium" -> 1.0;
                    case "Hard" -> 1.5;
                    default -> 1.0;
                };

                gameLogic.updateSettings(width, height, isBorderless, snake1Color, snake2Color, foodColor, backgroundColor, isTwoPlayerMode, gameSpeed);
                saveSettings(); // 保存设置
                startGame(primaryStage);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter valid numbers for width and height.");
            }
        });

        // Add components to the layout
        menu.getChildren().addAll(
                new Label("Map Width:"), widthField,
                new Label("Map Height:"), heightField,
                borderlessCheckBox,
                new Label("Snake 1 Color:"), snake1ColorPicker,
                new Label("Snake 2 Color:"), snake2ColorPicker,
                new Label("Food Color:"), foodColorPicker,
                new Label("Background Color:"), backgroundColorPicker,
                onePlayerButton, twoPlayerButton,
                new Label("Difficulty:"), difficultyChoiceBox,
                startButton);

        // Create a ScrollPane to make the menu scrollable
        ScrollPane scrollPane = new ScrollPane(menu);
        scrollPane.setFitToWidth(true);

        Scene menuScene = new Scene(scrollPane, 400, 600); // Set default window size

        primaryStage.setTitle("Snake Game Settings");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    /**
     * Starts the game by initializing the canvas and setting up the game loop.
     *
     * @param primaryStage the primary stage for this application
     */
    private void startGame(Stage primaryStage) {
        int canvasWidth = gameLogic.getWidth() * SnakeGameLogic.TILE_SIZE;
        int canvasHeight = gameLogic.getHeight() * SnakeGameLogic.TILE_SIZE;

        // Adjust canvas size if not in borderless mode
        if (!gameLogic.isBorderless()) {
            canvasWidth += 2 * SnakeGameLogic.TILE_SIZE;
            canvasHeight += 2 * SnakeGameLogic.TILE_SIZE;
        }

        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D();

        // 将动画的帧速率增加，使游戏更流畅
        timeline = new Timeline(new KeyFrame(Duration.millis(300 / gameLogic.getGameSpeed()), e -> run()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            gameLogic.updateDirection(event.getCode());
        });

        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(scene);
        gameLogic.resetGame();
        startCountdown();
    }

    /**
     * Initiates a countdown before the game starts.
     */
    private void startCountdown() {
        int[] countdown = {3};
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            gc.setFill(gameLogic.getBackgroundColor());
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            Color countdownColor = getContrastColor(gameLogic.getBackgroundColor());
            gc.setFill(countdownColor);
            gc.setFont(new Font(50));
            gc.fillText("Starting in: " + countdown[0], canvas.getWidth() / 2.0 - 120, canvas.getHeight() / 2.0);

            drawDirectionArrow(gameLogic.getSnake1().get(0), gameLogic.getDirection1(), gameLogic.getSnake1Color());
            if (gameLogic.isTwoPlayerMode()) {
                drawDirectionArrow(gameLogic.getSnake2().get(0), gameLogic.getDirection2(), gameLogic.getSnake2Color());
            }

            countdown[0]--;

            if (countdown[0] < 0) {
                timeline.play();
                countdownTimeline.stop();
            }
        }));
        countdownTimeline.setCycleCount(4);
        countdownTimeline.play();
    }

    /**
     * The main game loop which updates the game state and renders the graphics.
     */
    private void run() {
        if (gameLogic.isGameOver1() || (gameLogic.isTwoPlayerMode() && gameLogic.isGameOver2())) {
            showGameOver();
            return;
        }

        gameLogic.run();

        gc.setFill(gameLogic.getBackgroundColor());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (!gameLogic.isBorderless()) {
            drawWalls();
        }

        drawFood();
        drawSnake(gameLogic.getSnake1(), gameLogic.getSnake1Color());
        drawDirectionArrow(gameLogic.getSnake1().get(0), gameLogic.getDirection1(), gameLogic.getSnake1Color());

        if (gameLogic.isTwoPlayerMode()) {
            drawSnake(gameLogic.getSnake2(), gameLogic.getSnake2Color());
            drawDirectionArrow(gameLogic.getSnake2().get(0), gameLogic.getDirection2(), gameLogic.getSnake2Color());
        }
    }

    /**
     * Draws the walls around the game area if the game is not in borderless mode.
     * The walls are drawn as black squares on the canvas.
     */
    private void drawWalls() {
        gc.setFill(Color.BLACK);
        // Draw top and bottom walls
        for (int x = 0; x < gameLogic.getWidth() + 2; x++) {
            gc.fillRect(x * SnakeGameLogic.TILE_SIZE, 0, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
            gc.fillRect(x * SnakeGameLogic.TILE_SIZE, (gameLogic.getHeight() + 1) * SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
        }
        // Draw left and right walls
        for (int y = 0; y < gameLogic.getHeight() + 2; y++) {
            gc.fillRect(0, y * SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
            gc.fillRect((gameLogic.getWidth() + 1) * SnakeGameLogic.TILE_SIZE, y * SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
        }
    }

    /**
     * Draws the food on the canvas.
     */
    private void drawFood() {
        SnakeGameLogic.Point food = gameLogic.getFood();
        int offset = gameLogic.isBorderless() ? 0 : SnakeGameLogic.TILE_SIZE; // Adjust for walls if not borderless
        gc.setFill(gameLogic.getFoodColor());
        gc.fillRect((food.x + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE, (food.y + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
    }

    /**
     * Draws the snake on the canvas.
     *
     * @param snake the list of points representing the snake's body
     * @param color the color of the snake
     */
    private void drawSnake(java.util.List<SnakeGameLogic.Point> snake, Color color) {
        gc.setFill(color);
        int offset = gameLogic.isBorderless() ? 0 : SnakeGameLogic.TILE_SIZE; // Adjust for walls if not borderless
        for (SnakeGameLogic.Point point : snake) {
            gc.fillRect((point.x + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE, (point.y + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE, SnakeGameLogic.TILE_SIZE);
        }
    }

    /**
     * Draws an arrow indicating the direction of the snake's head.
     *
     * @param head      the position of the snake's head
     * @param direction the direction the snake is moving
     * @param color     the color of the arrow
     */
    private void drawDirectionArrow(SnakeGameLogic.Point head, SnakeGameLogic.Direction direction, Color color) {
        double offset = gameLogic.isBorderless() ? 0 : SnakeGameLogic.TILE_SIZE; // Adjust for walls if not borderless
        double x = (head.x + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE + SnakeGameLogic.TILE_SIZE / 2.0;
        double y = (head.y + (gameLogic.isBorderless() ? 0 : 1)) * SnakeGameLogic.TILE_SIZE + SnakeGameLogic.TILE_SIZE / 2.0;
        gc.setFill(color);

        switch (direction) {
            case UP -> gc.fillArc(x - 5, y - 10, 10, 10, 0, 180, ArcType.ROUND);
            case DOWN -> gc.fillArc(x - 5, y, 10, 10, 180, 180, ArcType.ROUND);
            case LEFT -> gc.fillArc(x - 10, y - 5, 10, 10, 90, 180, ArcType.ROUND);
            case RIGHT -> gc.fillArc(x, y - 5, 10, 10, 270, 180, ArcType.ROUND);
        }
    }

    /**
     * Displays the game over screen and shows the scores.
     */
    private void showGameOver() {
        timeline.stop();
        gc.setFill(Color.RED);
        gc.setFont(new Font(30));
        gc.fillText("Game Over", canvas.getWidth() / 2.0 - 70, canvas.getHeight() / 2.0 - 20);
        gc.fillText("Score 1: " + gameLogic.getScore1(), canvas.getWidth() / 2.0 - 70, canvas.getHeight() / 2.0 + 20);
        if (gameLogic.isTwoPlayerMode()) {
            gc.fillText("Score 2: " + gameLogic.getScore2(), canvas.getWidth() / 2.0 - 70, canvas.getHeight() / 2.0 + 60);
        }

        Button backButton = new Button("Back to Menu");
        backButton.setLayoutX(canvas.getWidth() / 2.0 - 50);
        backButton.setLayoutY(canvas.getHeight() / 2.0 + 100);
        backButton.setOnAction(e -> showSettingsMenu((Stage) backButton.getScene().getWindow()));

        Pane root = (Pane) canvas.getParent();
        root.getChildren().add(backButton);
    }

    /**
     * Displays an alert with the specified title and message.
     *
     * @param title   the title of the alert
     * @param message the message of the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Saves the current game settings to a properties file.
     */
    private void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("width", String.valueOf(gameLogic.getWidth()));
        properties.setProperty("height", String.valueOf(gameLogic.getHeight()));
        properties.setProperty("isBorderless", String.valueOf(gameLogic.isBorderless()));
        properties.setProperty("isTwoPlayerMode", String.valueOf(gameLogic.isTwoPlayerMode()));
        properties.setProperty("snake1Color", gameLogic.getSnake1Color().toString());
        properties.setProperty("snake2Color", gameLogic.getSnake2Color().toString());
        properties.setProperty("foodColor", gameLogic.getFoodColor().toString());
        properties.setProperty("backgroundColor", gameLogic.getBackgroundColor().toString());

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            showAlert("Error", "Failed to save settings: " + e.getMessage());
        }
    }

    /**
     * Loads the game settings from a properties file.
     */
    private void loadSettings() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
            properties.load(input);
            int width = Integer.parseInt(properties.getProperty("width", "20"));
            int height = Integer.parseInt(properties.getProperty("height", "20"));
            boolean isBorderless = Boolean.parseBoolean(properties.getProperty("isBorderless", "false"));
            boolean isTwoPlayerMode = Boolean.parseBoolean(properties.getProperty("isTwoPlayerMode", "false"));
            Color snake1Color = Color.valueOf(properties.getProperty("snake1Color", Color.GREEN.toString()));
            Color snake2Color = Color.valueOf(properties.getProperty("snake2Color", Color.BLUE.toString()));
            Color foodColor = Color.valueOf(properties.getProperty("foodColor", Color.RED.toString()));
            Color backgroundColor = Color.valueOf(properties.getProperty("backgroundColor", Color.BLACK.toString()));

            gameLogic.updateSettings(width, height, isBorderless, snake1Color, snake2Color, foodColor, backgroundColor, isTwoPlayerMode, 1.0);
        } catch (IOException e) {
            // 默认设置不需要处理
        }
    }

    /**
     * Calculates a contrasting color for the given color.
     *
     * @param color the color to calculate the contrast for
     * @return the contrasting color
     */
    private Color getContrastColor(Color color) {
        double brightness = (color.getRed() * 0.299 + color.getGreen() * 0.587 + color.getBlue() * 0.114);
        return brightness > 0.5 ? Color.BLACK : Color.WHITE;
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
