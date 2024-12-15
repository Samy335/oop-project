package Master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GameController {
    private static final int code_length = 4;
    private static final int max_attempts = 10;
    private List<Color> colors = Arrays.asList(
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.CYAN, Color.PURPLE
    );
    private List<Color> secretcode;
    private int attempts;
    private Color[] guess = new Color[code_length];
    private VBox mainlayout;
    private GridPane guessgrid;
    private Label feedbacklabel;
    private int seconds = 0;
    private int minutes = 0;
    private Timeline timer;
    private Label timerlabel;
    private Label resultlabel;
    private Button tryAgainButton;
    private boolean isCodeSet = false;

    public GameController() {
        attempts = 0;
        secretcode = new ArrayList<>();
    }
    public VBox createGameLayout() {
        mainlayout = new VBox(20);
        mainlayout.setAlignment(Pos.CENTER);
        timerlabel = new Label("00:00");
        timerlabel.setId("timelabel");
        resultlabel = new Label();
        resultlabel.setFont(new javafx.scene.text.Font("Arial", 16));
        resultlabel.setTextFill(Color.GREEN);
        Label titleLabel = new Label("Welcome to MasterMind 🤔");
        titleLabel.setFont(new javafx.scene.text.Font("Arial", 22));
        titleLabel.setTextFill(Color.RED);
        Label instructionLabel = new Label("Player 1: Set the code. Player 2: Guess the code!");
        instructionLabel.setFont(new javafx.scene.text.Font("Arial", 14));
        guessgrid = new GridPane();
        guessgrid.setHgap(20);
        guessgrid.setVgap(15);
        guessgrid.setAlignment(Pos.CENTER);
        for (int i = 0; i < code_length; i++) {
            Button button = new Button("Choose color");
            button.setPrefSize(120, 50);
            button.setId("colorButton");
            final int index = i;
            button.setOnAction((e) -> pickColor(button, index));
            guessgrid.add(button, i, 0);
        }
        Button actionButton = new Button("Set Code");
        actionButton.setId("actionButton");
        actionButton.setOnAction((e) -> {
            if (!isCodeSet) {
                setSecretCode();
                actionButton.setText("Submit Guess");
            } else {
                submitGuess();
            }
        });
        tryAgainButton = new Button("Try Again");
        tryAgainButton.setId("tryAgainButton");
        tryAgainButton.setVisible(false);
        tryAgainButton.setOnAction((e) -> resetGame());
        feedbacklabel = new Label();
        feedbacklabel.setFont(new javafx.scene.text.Font("Arial", 14));
        feedbacklabel.setTextFill(Color.WHITE);
        HBox timerBox = new HBox(20);
        timerBox.setAlignment(Pos.TOP_LEFT);
        timerBox.getChildren().add(timerlabel);
        mainlayout.getChildren().addAll(titleLabel, instructionLabel, guessgrid, actionButton, feedbacklabel, resultlabel, tryAgainButton);
        return mainlayout;
    }
    private void setSecretCode() {
        boolean allColorsSelected = true;
        for (Color color : guess) {
            if (color == null) {
                allColorsSelected = false;
                break;
            }
        }
        if (!allColorsSelected) {
            feedbacklabel.setText("Player 1: Please select all colors to set the code!");
            feedbacklabel.setTextFill(Color.RED);
        } else {
            secretcode = Arrays.asList(guess.clone());
            feedbacklabel.setText("Code set! Player 2, start guessing.");
            feedbacklabel.setTextFill(Color.GREEN);
            isCodeSet = true;
            resetGuessGrid();
            startTimer();
        }
    }
    public void submitGuess() {
        if (attempts < max_attempts) {
            boolean guessed = true;
            for (Color color : guess) {
                if (color == null) {
                    guessed = false;
                    break;
                }
            }
            if (!guessed) {
                feedbacklabel.setText("Please select all colors before submitting!");
                feedbacklabel.setTextFill(Color.RED);
                return;
            }
            String feedback = giveFeedback(guess);
            feedbacklabel.setText(feedback);
            attempts++;
            if (isCorrectGuess(guess)) {
                feedbacklabel.setText("🎉🎉 Player 2 Wins! 🎉🎉");
                feedbacklabel.setTextFill(Color.GREEN);
                end(true);
            } else if (attempts == max_attempts) {
                feedbacklabel.setText("Player 2 ran out of attempts! The correct code was: " + secretcode);
                feedbacklabel.setTextFill(Color.RED);
                end(false);
            }
        }
    }
    private void end(boolean win) {
        timer.stop();
        if (win) {
            resultlabel.setText("Game Over! Time: " + String.format("%02d:%02d", minutes, seconds));
            resultlabel.setTextFill(Color.GREEN);
        } else {
            resultlabel.setText("Game Over! Time: " + String.format("%02d:%02d", minutes, seconds));
            resultlabel.setTextFill(Color.RED);
        }
        tryAgainButton.setVisible(true);
    }
    private void resetGame() {
        attempts = 0;
        seconds = 0;
        minutes = 0;
        feedbacklabel.setText("");
        resultlabel.setText("");
        tryAgainButton.setVisible(false);
        isCodeSet = false;
        secretcode.clear();
        resetGuessGrid();
    }
    private void resetGuessGrid() {
        for (int i = 0; i < code_length; i++) {
            guess[i] = null;
            Button button = (Button) guessgrid.getChildren().get(i);
            button.setStyle("");
            button.setText("Choose color");
        }
    }
    private boolean isCorrectGuess(Color[] guess) {
        return Arrays.equals(guess, secretcode.toArray(new Color[0]));
    }
    private String giveFeedback(Color[] guess) {
        int correctPos = 0;
        int correctColor = 0;
        boolean[] matchedSecret = new boolean[code_length];
        boolean[] matchedGuess = new boolean[code_length];
        for (int i = 0; i < code_length; i++) {
            if (guess[i] != null && guess[i].equals(secretcode.get(i))) {
                correctPos++;
                matchedSecret[i] = true;
                matchedGuess[i] = true;
            }
        }
        for (int i = 0; i < code_length; i++) {
            if (!matchedGuess[i] && guess[i] != null) {
                for (int j = 0; j < code_length; j++) {
                    if (!matchedSecret[j] && guess[i].equals(secretcode.get(j))) {
                        correctColor++;
                        matchedSecret[j] = true;
                        break;
                    }
                }
            }
        }
        return "Correct position: " + correctPos + ", Correct color but wrong position: " + correctColor;
    }
    public void pickColor(Button button, int index) {
        Color chosen = chooseNextColor(guess[index]);
        button.setStyle("-fx-background-color: " + toHexString(chosen) + ";");
        guess[index] = chosen;
    }
    private Color chooseNextColor(Color current) {
        int index = colors.indexOf(current);
        if (index == -1 || index == colors.size() - 1) {
            return colors.get(0);
        } else {
            return colors.get(index + 1);
        }
    }
    private String toHexString(Color c) {
        if (c == null) return "#000000";
        int r = (int) (c.getRed() * 255);
        int g = (int) (c.getGreen() * 255);
        int b = (int) (c.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    private void startTimer() {
        timer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    seconds++;
                    if (seconds == 60) {
                        seconds = 0;
                        minutes++;
                    }
                    timerlabel.setText(String.format("%02d:%02d", minutes, seconds));
                })
        );
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
}
