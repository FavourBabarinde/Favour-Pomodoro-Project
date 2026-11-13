
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class FavourPomodoro extends JFrame {

    // COLORS
    private static final Color BG = new Color(23, 36, 55);
    private static final Color PANEL = new Color(32, 49, 73);
    private static final Color CORAL = new Color(250, 152, 129);
    private static final Color WHITE = new Color(245, 247, 251);
    private static final Color MUTED = new Color(175, 190, 209);
    private static final Color BUTTON_BG = new Color(49, 67, 90);

    // TIMER DURATIONS
    private static final int FOCUS = 25 * 60;
    private static final int SHORT_BREAK = 5 * 60;
    private static final int LONG_BREAK = 15 * 60;

    // TIMER VARIABLES
    private int remainingSeconds = FOCUS;
    private int totalSeconds = FOCUS;
    private int completedSessions = 0;

    private String currentMode = "Focus";
    private boolean running = false;
    private long endTimeNanos;

    // UI COMPONENTS
    private TimerCircle timerCircle;
    private JLabel sessionLabel;
    private JLabel completedLabel;
    private JLabel durationLabel;

    private RoundedButton startButton;
    private RoundedButton focusButton;
    private RoundedButton shortButton;
    private RoundedButton longButton;

    private javax.swing.Timer timer;

    // CONSTRUCTOR
    public FavourPomodoro() {

        setTitle("Favour Pomodoro - FocusFlow V2");
        setSize(480, 660);
        setMinimumSize(new Dimension(420, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(BG);
        main.setBorder(
            BorderFactory.createEmptyBorder(24, 26, 24, 26)
        );

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("FocusFlow");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel("STAY IN YOUR ZONE");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(MUTED);

        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);

        // MODE BUTTONS
        JPanel modes = new JPanel(new GridLayout(1, 3, 8, 0));
        modes.setOpaque(false);

        focusButton = new RoundedButton("Focus", CORAL, BG);
        shortButton = new RoundedButton(
            "Short Break", BUTTON_BG, WHITE
        );
        longButton = new RoundedButton(
            "Long Break", BUTTON_BG, WHITE
        );

        modes.add(focusButton);
        modes.add(shortButton);
        modes.add(longButton);

        focusButton.addActionListener(e -> setMode("Focus"));
        shortButton.addActionListener(
            e -> setMode("Short Break")
        );
        longButton.addActionListener(
            e -> setMode("Long Break")
        );

        // CIRCULAR TIMER
        timerCircle = new TimerCircle();
        timerCircle.setPreferredSize(new Dimension(300, 300));

        sessionLabel = new JLabel(
            "Session 1 of 4", SwingConstants.CENTER
        );
        sessionLabel.setForeground(MUTED);
        sessionLabel.setFont(
            new Font("Segoe UI", Font.PLAIN, 15)
        );

        JPanel timerSection = new JPanel(
            new BorderLayout()
        );
        timerSection.setOpaque(false);
        timerSection.add(
            timerCircle, BorderLayout.CENTER
        );
        timerSection.add(
            sessionLabel, BorderLayout.SOUTH
        );

        // CONTROL BUTTONS
        JPanel controls = new JPanel(
            new GridLayout(1, 3, 10, 0)
        );
        controls.setOpaque(false);

        startButton = new RoundedButton(
            "Start", CORAL, BG
        );

        RoundedButton resetButton = new RoundedButton(
            "Reset", BUTTON_BG, WHITE
        );

        RoundedButton skipButton = new RoundedButton(
            "Skip", BUTTON_BG, WHITE
        );

        controls.add(startButton);
        controls.add(resetButton);
        controls.add(skipButton);

        startButton.addActionListener(
            e -> startPauseTimer()
        );

        resetButton.addActionListener(
            e -> resetTimer()
        );

        skipButton.addActionListener(
            e -> switchSession(false)
        );

        // STATISTICS
        JPanel stats = new JPanel(new GridLayout(1, 2));
        stats.setOpaque(false);

        durationLabel = new JLabel(
            "Focus: 25 minutes"
        );
        durationLabel.setForeground(MUTED);
        durationLabel.setFont(
            new Font("Segoe UI", Font.PLAIN, 13)
        );

        completedLabel = new JLabel(
            "Completed: 0", SwingConstants.RIGHT
        );
        completedLabel.setForeground(MUTED);
        completedLabel.setFont(
            new Font("Segoe UI", Font.PLAIN, 13)
        );

        stats.add(durationLabel);
        stats.add(completedLabel);

        // CENTER SECTION
        JPanel center = new JPanel(
            new BorderLayout(0, 15)
        );
        center.setOpaque(false);

        center.add(modes, BorderLayout.NORTH);
        center.add(
            timerSection, BorderLayout.CENTER
        );

        // BOTTOM SECTION
        JPanel bottom = new JPanel(
            new BorderLayout(0, 20)
        );
        bottom.setOpaque(false);

        bottom.add(controls, BorderLayout.NORTH);
        bottom.add(stats, BorderLayout.SOUTH);

        // ASSEMBLE WINDOW
        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);

        add(main);

        // SWING TIMER
        timer = new javax.swing.Timer(
            100, e -> tick()
        );

        updateDisplay();
        setVisible(true);
    }

    // START OR PAUSE
    private void startPauseTimer() {

        if (running) {

            remainingSeconds = secondsLeft();
            timer.stop();
            running = false;

            startButton.setText("Resume");

        } else {

            if (remainingSeconds <= 0) {
                remainingSeconds = totalSeconds;
            }

            endTimeNanos = System.nanoTime()
                + TimeUnit.SECONDS.toNanos(
                    remainingSeconds
                );

            running = true;
            timer.start();

            startButton.setText("Pause");
        }

        updateDisplay();
    }

    // CALCULATE REMAINING TIME
    private int secondsLeft() {

        long nanosLeft =
            endTimeNanos - System.nanoTime();

        if (nanosLeft <= 0) {
            return 0;
        }

        return (int) Math.min(
            totalSeconds,
            (nanosLeft + 999_999_999L)
                / 1_000_000_000L
        );
    }

    // TIMER UPDATE
    private void tick() {

        remainingSeconds = secondsLeft();
        updateDisplay();

        if (remainingSeconds == 0) {

            timer.stop();
            running = false;

            switchSession(true);
        }
    }

    // CHANGE TIMER MODE
    private void setMode(String mode) {

        timer.stop();
        running = false;

        currentMode = mode;

        if (mode.equals("Focus")) {

            totalSeconds = FOCUS;

        } else if (mode.equals("Short Break")) {

            totalSeconds = SHORT_BREAK;

        } else {

            totalSeconds = LONG_BREAK;
        }

        remainingSeconds = totalSeconds;
        startButton.setText("Start");

        updateDisplay();
    }

    // RESET TIMER
    private void resetTimer() {

        timer.stop();
        running = false;

        remainingSeconds = totalSeconds;
        startButton.setText("Start");

        updateDisplay();
    }

    // SWITCH BETWEEN FOCUS AND BREAKS
    private void switchSession(boolean completed) {

        String nextMode;

        if (currentMode.equals("Focus")) {

            if (completed) {
                completedSessions++;
            }

            if (completedSessions > 0
                    && completedSessions % 4 == 0) {

                nextMode = "Long Break";

            } else {

                nextMode = "Short Break";
            }

        } else {

            nextMode = "Focus";
        }

        setMode(nextMode);

        if (completed) {

            JOptionPane.showMessageDialog(
                this,
                "Session complete!\nNext: " + nextMode,
                "FocusFlow",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // UPDATE UI
    private void updateDisplay() {

        timerCircle.repaint();

        int sessionNumber =
            (completedSessions % 4) + 1;

        sessionLabel.setText(
            "Session " + sessionNumber + " of 4"
        );

        completedLabel.setText(
            "Completed: " + completedSessions
        );

        durationLabel.setText(
            currentMode + ": "
                + (totalSeconds / 60) + " min"
        );

        focusButton.setColors(
            currentMode.equals("Focus")
                ? CORAL : BUTTON_BG,
            currentMode.equals("Focus")
                ? BG : WHITE
        );

        shortButton.setColors(
            currentMode.equals("Short Break")
                ? CORAL : BUTTON_BG,
            currentMode.equals("Short Break")
                ? BG : WHITE
        );

        longButton.setColors(
            currentMode.equals("Long Break")
                ? CORAL : BUTTON_BG,
            currentMode.equals("Long Break")
                ? BG : WHITE
        );
    }

    // CUSTOM CIRCULAR COUNTDOWN
    private class TimerCircle extends JPanel {

        public TimerCircle() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int w = getWidth();
            int h = getHeight();

            int diameter =
                Math.max(20, Math.min(w, h) - 40);

            int x = (w - diameter) / 2;
            int y = (h - diameter) / 2;

            // INNER CIRCLE
            g2.setColor(PANEL);
            g2.fillOval(
                x, y, diameter, diameter
            );

            // BACKGROUND TRACK
            g2.setStroke(
                new BasicStroke(
                    10,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
                )
            );

            g2.setColor(
                new Color(60, 78, 101)
            );

            g2.drawOval(
                x, y, diameter, diameter
            );

            // PROGRESS ARC
            double progress =
                (double) remainingSeconds
                    / totalSeconds;

            g2.setColor(CORAL);

            g2.draw(
                new Arc2D.Double(
                    x, y,
                    diameter, diameter,
                    90,
                    -360 * progress,
                    Arc2D.OPEN
                )
            );

            // COUNTDOWN TEXT
            String time = String.format(
                "%02d:%02d",
                remainingSeconds / 60,
                remainingSeconds % 60
            );

            g2.setFont(
                new Font(
                    "Segoe UI",
                    Font.BOLD,
                    52
                )
            );

            g2.setColor(WHITE);

            FontMetrics fm =
                g2.getFontMetrics();

            int textX =
                (w - fm.stringWidth(time)) / 2;

            int textY = h / 2;

            g2.drawString(
                time, textX, textY
            );

            // SUBTITLE
            String message =
                currentMode.equals("Focus")
                    ? "Time to focus"
                    : "Time to recharge";

            g2.setFont(
                new Font(
                    "Segoe UI",
                    Font.PLAIN,
                    15
                )
            );

            g2.setColor(MUTED);

            fm = g2.getFontMetrics();

            g2.drawString(
                message,
                (w - fm.stringWidth(message)) / 2,
                textY + 30
            );

            g2.dispose();
        }
    }

    // CUSTOM ROUNDED BUTTON
    private static class RoundedButton
            extends JButton {

        private Color buttonColor;
        private Color textColor;

        public RoundedButton(
                String text,
                Color bg,
                Color fg) {

            super(text);

            buttonColor = bg;
            textColor = fg;

            setFont(
                new Font(
                    "Segoe UI",
                    Font.BOLD,
                    13
                )
            );

            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);

            setCursor(
                Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR
                )
            );
        }

        public void setColors(
                Color bg,
                Color fg) {

            buttonColor = bg;
            textColor = fg;

            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 =
                (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color fill = buttonColor;

            if (getModel().isPressed()) {
                fill = fill.darker();
            } else if (getModel().isRollover()) {
                fill = fill.brighter();
            }

            g2.setColor(fill);

            g2.fillRoundRect(
                0,
                0,
                getWidth(),
                getHeight(),
                22,
                22
            );

            g2.dispose();

            setForeground(textColor);
            super.paintComponent(g);
        }
    }

    // MAIN METHOD
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new FavourPomodoro();
        });
    }
}
