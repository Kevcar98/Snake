import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;
import javax.swing.*;

// Represents a food item in the game
class Food {
    Point position;// Position of the food on the board
    int type; // Type of the food which determines its color

    // Constructor to initialize the food item
    public Food(Point position, int type) {
        this.position = position;
        this.type = type;
    }
}

// Represents a score entry with a user name and score
class Score {
    String userName; // User name
    int score; // Score

    // Constructor to initialize score with a username and score value
    public Score(String userName, int score) {
        this.userName = userName;
        this.score = score;
    }

    // Getter for username
    public String getUserName() {
        return userName;
    }

    // Getter for score
    public int getScore() {
        return score;
    }
}

// Main class for the Snake game
public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    // Instance variables
    private ArrayList<Point> snake;
    private ArrayList<Food> foodPoints;
    private String direction;
    private Timer timer;
    private final int TILE_SIZE = 20;
    private int WIDTH = 400; // Default width
    private int HEIGHT = 400; // Default height
    private int speed = 100; // Initial speed
    private boolean isPaused = false;

    // Constructor to initialize the game
    public SnakeGame() {
        int[] userInput = getUserCustomSize(); // Get custom board size from user
        if (userInput != null) { // If user input is valid
            WIDTH = userInput[0];
            HEIGHT = userInput[1];
        }
        customBoardSize(WIDTH, HEIGHT); // Set custom board size
    }

    private void customBoardSize(int width, int height) {
        this.setPreferredSize(new Dimension(width, height)); // Set the preferred size of the panel
        this.setBackground(Color.BLACK); // Set the background color of the panel
        this.setFocusable(true); // Set the panel to be focusable
        this.addKeyListener(this); // Add key listener to the panel
        initGame(); // Initialize the game
    }
    
    // Method to get custom board size from the user
    private int[] getUserCustomSize() {
        try {
            // prompt user for custom board size
            String widthStr = JOptionPane.showInputDialog(this, "Enter the width for the board:", "Custom Board Size", JOptionPane.QUESTION_MESSAGE);
            String heightStr = JOptionPane.showInputDialog(this, "Enter the height for the board:", "Custom Board Size", JOptionPane.QUESTION_MESSAGE);
            // Parse the user input to get the width and height of the board as integers
            int width = Integer.parseInt(widthStr);
            int height = Integer.parseInt(heightStr);
            return new int[]{width, height}; // Return the width and height as an array
        } catch (NumberFormatException e) {
            // Display an error message if the input is invalid
            JOptionPane.showMessageDialog(this, "Using Default 400x400 grid as no valid input was detected!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null; // Returns null if an exception occurs
    }

    // Method to initialize the game 
    private void initGame() {
        snake = new ArrayList<>(); // Initialize the snake
        snake.add(new Point(5, 5)); // Add the initial position of the snake
        direction = "RIGHT"; // Set the initial direction of the snake
        foodPoints = new ArrayList<>(); // Initialize the food points
        placeFood(); // Place the food on the board
        adjustSpeed(); // Adjust the speed of the game
        timer = new Timer(speed, this); // Initialize the timer for the game
        timer.start(); // Start the timer
    }

    private void placeFood() {
        Random rand = new Random();
        while (foodPoints.size() < 5) { // Example: Max 5 food items on the screen
            int x = rand.nextInt(WIDTH / TILE_SIZE); // Random x position
            int y = rand.nextInt(HEIGHT / TILE_SIZE);  // Random y position
            int typeOfFood = rand.nextInt(10); // Determine the type for each food item
            Food newFood = new Food(new Point(x, y), typeOfFood); // Create a new food item
            // Ensure the food is not placed on the snake or on top of another food item
            boolean exists = foodPoints.stream().anyMatch(f -> f.position.equals(newFood.position));
            if (!snake.contains(newFood.position) && !exists) { // If the food is not on the snake or on top of another food item
                foodPoints.add(newFood);
            }
        }
        updateRedFoods(); // Ensure there are at most 3 red foods on the board
    }
    
    // Updates the type of red foods if there are more than 3
    private void updateRedFoods() {
        List<Food> redFoods = foodPoints.stream()
                                         .filter(f -> f.type == 7 || f.type == 8) // Filter red foods
                                         .collect(Collectors.toList());
        if (redFoods.size() > 3) { // If there are more than 3 red foods
            Collections.shuffle(redFoods); // Shuffle to randomly pick which red foods to change
            redFoods.subList(3, redFoods.size()).forEach(f -> f.type = generateNonRedType()); // Update types of excess red foods
        }
    }
    
    // Generates a random type for non-red foods
    private int generateNonRedType() {
        Random rand = new Random();
        int type;
        do {
            type = rand.nextInt(10); // Generate a new type
        } while (type == 7 || type == 8); // Ensure it's not red
        return type;
    }

    // Method to paint the components of the game
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the super class method
        drawSnake(g); // Draw the snake
        drawFood(g); // Draw the food
        drawScore(g); // Draw the score
    }

    // Method to draw the snake on the board
    private void drawSnake(Graphics g) {
        g.setColor(Color.GREEN); // Set the color of the snake
        for (Point p : snake) { // Iterate over each segment of the snake
            g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE); // Draw the snake segment
        }
    }

    // Method to draw the score on the board
    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE); // Set the color of the score
        int x = WIDTH - 60; // Adjust this value to position the score from the right edge
        int y = HEIGHT - 10; // Adjust this value to position the score from the bottom edge
        g.drawString("Score: " + (snake.size() - 1), x, y); // Draw the score on the board
    }

    // Method to draw the food on the board
    private void drawFood(Graphics g) {
        for (Food food : foodPoints) { // Iterate over each food item
            switch (food.type) { // Set the color of the food based on its type
                case 7:
                case 8:
                    g.setColor(Color.RED); // Red for types 7 and 8 (bad food)
                    break;
                case 9:
                    g.setColor(Color.YELLOW); // Yellow for type 9 (special food)
                    break;
                default:
                    g.setColor(Color.GREEN); // Green for all other types
                    break;
            }
            // Draw the food item on the board
            g.fillRect(food.position.x * TILE_SIZE, food.position.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    // Method to handle the action performed by the timer
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) { // Check if the game is not paused
            move(); // Move the snake
            checkCollision(); // Check for collision
            eatSelf(); // Check if the snake eats itself
            repaint(); // Repaint the board
        }
    }

    // Method to move the snake
    private void move() {
        if (snake.isEmpty()) return; // Return if the snake is empty
        Point head = snake.get(0); // Get the head of the snake
        Point newPoint = new Point(head.x, head.y); // Create a new point for the next position
        switch (direction) { // Move the snake based on the direction
            case "UP":
                newPoint.y -= 1; // Move up
                break;
            case "DOWN":
                newPoint.y += 1; // Move down
                break;
            case "LEFT":
                newPoint.x -= 1; // Move left
                break;
            case "RIGHT":
                newPoint.x += 1; // Move right
                break;
        }
        snake.add(0, newPoint); // Add the new point to the snake
        boolean foodEaten = false; // Flag to check if food is eaten
        for (Iterator<Food> iterator = foodPoints.iterator(); iterator.hasNext(); ) { // Iterate over each food item
            Food food = iterator.next(); // Get the food item
            if (newPoint.equals(food.position)) { // Check if the snake eats the food
                iterator.remove(); // Remove the eaten food
                foodEaten = true;
                // Handle special food effects here
                if (food.type == 9) { // Special food that gives extra points
                    Point lastSegment = snake.get(snake.size() - 1); // Get the last segment of the snake
                    for (int i = 1; i <= 5; i++) { // Add 5 new segments to the snake
                        Point newSegment = new Point(lastSegment.x, lastSegment.y); // Create a new segment
                        switch (direction) {
                            case "UP":
                                newSegment.y += i; // Move up
                                break;
                            case "DOWN":
                                newSegment.y -= i; // Move down
                                break;
                            case "LEFT":
                                newSegment.x += i; // Move left
                                break;
                            case "RIGHT":
                                newSegment.x -= i; // Move right
                                break;
                        }
                        // Ensure new segments are within bounds and adjust if necessary
                        newSegment.x = (newSegment.x + WIDTH / TILE_SIZE) % (WIDTH / TILE_SIZE);
                        newSegment.y = (newSegment.y + HEIGHT / TILE_SIZE) % (HEIGHT / TILE_SIZE);
                        snake.add(newSegment);
                    }
                } else if (food.type == 8 || food.type == 7) { // Bad food that reduces snake size
                    if (snake.size() > 1) {
                        //half snake size
                        int halfSnake = snake.size() / 2;
                        snake.subList(halfSnake, snake.size()).clear();
                    }
                }
                break;
            }
        }
        if (!foodEaten && snake.size() > 1) { // If no food was eaten and the snake has more than one segment
            snake.remove(snake.size() - 1); // Only remove the last segment if no food was eaten
        }
        // Check if one food has been eaten
        if (foodEaten) {
            placeFood(); // Place new food and refresh food type
        }
        adjustSpeed(); // Adjust the speed of the game
    }

    // Method to check if the snake eats itself
    private void eatSelf() { 
        Point head = snake.get(0); // Get the head of the snake
        for (int i = 1; i < snake.size(); i++) { // Iterate over each segment of the snake
            if (head.equals(snake.get(i))) { // Check if the head collides with any other segment
                // Collision detected, cut the snake from this segment onwards
                snake.subList(i, snake.size()).clear();
                return;
            }
        }
    }

    // Method to check for collision with the walls
    private void checkCollision() {
        Point head = snake.get(0); // Get the head of the snake
        if (head.x < 0 || head.x >= WIDTH / TILE_SIZE || head.y < 0 || head.y >= HEIGHT / TILE_SIZE) { // Check if the head collides with the walls
            timer.stop();
            // Display game over message
            JOptionPane.showMessageDialog(this, "Game Over!  Your Score: " + (snake.size() - 1), "Game Over", JOptionPane.INFORMATION_MESSAGE);
            
            // Prompt for user name
            String userName = JOptionPane.showInputDialog(this, "Enter your name to register your score:", "High Score", JOptionPane.QUESTION_MESSAGE);
            if (userName != null && !userName.trim().isEmpty()) {
                // Save the high score
                updateSaveScore(userName, snake.size() - 1);
            }            
            getHighScore(); // Assuming this method displays high scores
            initGame(); // Restart the game
        }
    }

    // Method to update the high scores in the file
    private void updateSaveScore(String userName, int score) {
        try {
            File file = new File("score.txt"); // Open the score file
            if (!file.exists()) {
                file.createNewFile(); // Create the file if it doesn't exist
            }
            FileWriter writer = new FileWriter(file, true); // Open the file for writing
            writer.write(userName + ": " + score + "\n"); // Write the user name and score to the file
            writer.close(); // Close the file
        } catch (IOException e) { // Handle file IO exceptions
            e.printStackTrace();
        }
    }

    // Method to get the high scores from the file
    private void getHighScore() {
        List<Score> scores = new ArrayList<>(); // Initialize a list to store scores
        try {
            File file = new File("score.txt"); // Open the score file
            Scanner scanner = new Scanner(file); // Create a scanner to read the file
            while (scanner.hasNextLine()) { // Read each line of the file
                String line = scanner.nextLine(); // Get the line
                String[] parts = line.split(": "); // Split the line into parts
                if (parts.length == 2) { // Check if the line has two parts
                    String userName = parts[0]; // Get the user name
                    int score = Integer.parseInt(parts[1]); // Get the score
                    scores.add(new Score(userName, score)); // Add the score to the list
                }
            }
            scanner.close();
    
            // Sort scores in descending order
            scores.sort((s1, s2) -> s2.getScore() - s1.getScore());
    
            // Keep only top 10 scores
            List<Score> topScores = scores.stream().limit(10).collect(Collectors.toList());
    
            // Build the display string
            StringBuilder displayString = new StringBuilder("Top 10 Scores:\n");
            for (int i = 0; i < topScores.size(); i++) { // Iterate over the top scores
                Score score = topScores.get(i); 
                displayString.append(i + 1).append(". ").append(score.getUserName()).append(": ").append(score.getScore()).append("\n");
            }
    
            // Display the top 10 scores
            JOptionPane.showMessageDialog(this, displayString.toString(), "High Scores", JOptionPane.INFORMATION_MESSAGE);
    
        } catch (FileNotFoundException e) { // Handle file not found exception
            JOptionPane.showMessageDialog(this, "Score file not found.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) { // Handle IO exception
            JOptionPane.showMessageDialog(this, "Error reading score file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to adjust the speed of the game
    private void adjustSpeed() {
        int currentScore = snake.size() - 1; // Get the current score
        if (currentScore % 5 == 0 && currentScore > 0) { // Increase speed every 5 points
            timer.setDelay(Math.max(50, speed - (currentScore / 5 * 10))); // Decrease delay to increase speed
        }
    }

    // Method to handle key pressed events
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            // Handle arrow keys and WASD keys
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
            // Ensure the snake cannot move in the opposite direction
                if (!direction.equals("DOWN")) direction = "UP";
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
            // Ensure the snake cannot move in the opposite direction
                if (!direction.equals("UP")) direction = "DOWN";
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
            // Ensure the snake cannot move in the opposite direction
                if (!direction.equals("RIGHT")) direction = "LEFT";
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
            // Ensure the snake cannot move in the opposite direction
                if (!direction.equals("LEFT")) direction = "RIGHT";
                break;            
            case KeyEvent.VK_P:
                isPaused = !isPaused; // Toggle pause
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);// Exit the game 
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Main method to start the game
    public static void main(String[] args) { 
        JFrame frame = new JFrame("Snake Game"); // Create a new frame
        SnakeGame game = new SnakeGame(); // Create a new SnakeGame object
        frame.add(game); // Add the game to the frame
        frame.pack(); // Pack the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set the default close operation
        frame.setVisible(true); // Set the frame to be visible
    }
}
