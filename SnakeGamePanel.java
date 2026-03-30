package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.example.core.Context;

public class SnakeGamePanel extends JPanel implements ActionListener, KeyListener {

    private static final int CELL_SIZE = 28;
    private static final int HUD_HEIGHT = 88;
    private static final int TICK_MS = 120;

    private static final Color SURFACE_TOP = new Color(14, 27, 31);
    private static final Color SURFACE_BOTTOM = new Color(22, 46, 42);
    private static final Color BOARD_DARK = new Color(24, 54, 48);
    private static final Color BOARD_LIGHT = new Color(30, 66, 58);
    private static final Color SNAKE_HEAD = new Color(242, 197, 70);
    private static final Color SNAKE_BODY = new Color(106, 201, 145);
    private static final Color FOOD = new Color(235, 94, 72);
    private static final Color HUD_TEXT = new Color(240, 245, 237);
    private static final Color HUD_MUTED = new Color(186, 199, 190);

    private final Context context;
    private final SnakeWorld world;
    private final Timer timer;

    public SnakeGamePanel(Context context, SnakeWorld world) {
        this.context = context;
        this.world = world;
        this.timer = new Timer(TICK_MS, this);

        setFocusable(true);
        setDoubleBuffered(true);
        addKeyListener(this);
        setPreferredSize(
            new Dimension(world.getBoardWidth() * CELL_SIZE, world.getBoardHeight() * CELL_SIZE + HUD_HEIGHT)
        );

        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        paintBackground(g2);
        paintHud(g2);
        paintBoard(g2);
        paintFood(g2);
        paintSnake(g2);

        if (world.isGameOver()) {
            paintOverlay(g2);
        }

        g2.dispose();
    }

    private void paintBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, SURFACE_TOP, 0, getHeight(), SURFACE_BOTTOM));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintHud(Graphics2D g2) {
        g2.setColor(new Color(8, 18, 18, 170));
        g2.fillRoundRect(14, 14, getWidth() - 28, HUD_HEIGHT - 24, 24, 24);

        g2.setColor(HUD_TEXT);
        g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        g2.drawString("Snake", 30, 50);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g2.setColor(HUD_MUTED);
        g2.drawString("Arrows or WASD to steer", 31, 73);

        g2.setColor(HUD_TEXT);
        g2.drawString("Score: " + world.getScore(), getWidth() - 160, 50);
        g2.drawString("Length: " + world.getSnake().getLength(), getWidth() - 160, 73);
    }

    private void paintBoard(Graphics2D g2) {
        for (int y = 0; y < world.getBoardHeight(); y++) {
            for (int x = 0; x < world.getBoardWidth(); x++) {
                g2.setColor((x + y) % 2 == 0 ? BOARD_DARK : BOARD_LIGHT);
                g2.fillRoundRect(x * CELL_SIZE, y * CELL_SIZE + HUD_HEIGHT, CELL_SIZE, CELL_SIZE, 10, 10);
            }
        }
    }

    private void paintFood(Graphics2D g2) {
        Point food = world.getFood().getPosition();
        int x = food.x * CELL_SIZE + 5;
        int y = food.y * CELL_SIZE + HUD_HEIGHT + 5;
        int size = CELL_SIZE - 10;

        g2.setColor(FOOD);
        g2.fillOval(x, y, size, size);
        g2.setColor(new Color(255, 224, 181, 180));
        g2.fillOval(x + 4, y + 4, size / 3, size / 3);
    }

    private void paintSnake(Graphics2D g2) {
        List<Point> segments = world.getSnake().getSegments();

        for (int i = segments.size() - 1; i >= 0; i--) {
            Point segment = segments.get(i);
            int x = segment.x * CELL_SIZE + 3;
            int y = segment.y * CELL_SIZE + HUD_HEIGHT + 3;
            int size = CELL_SIZE - 6;

            g2.setColor(i == 0 ? SNAKE_HEAD : SNAKE_BODY);
            g2.fillRoundRect(x, y, size, size, 14, 14);
        }
    }

    private void paintOverlay(Graphics2D g2) {
        g2.setColor(new Color(6, 12, 13, 165));
        g2.fillRoundRect(32, HUD_HEIGHT + 56, getWidth() - 64, 164, 28, 28);

        g2.setColor(HUD_TEXT);
        g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
        g2.drawString(world.isVictory() ? "Board Cleared" : "Game Over", 62, HUD_HEIGHT + 112);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2.setColor(HUD_MUTED);
        g2.drawString(
            world.isVictory() ? "You filled every tile on the board." : "The snake collided and the run ended.",
            62,
            HUD_HEIGHT + 146
        );
        g2.drawString("Press R to restart.", 62, HUD_HEIGHT + 176);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!world.isGameOver()) {
            context.queueCommand(world);
        }
        context.executeQueuedCommands();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> queueDirection(Direction.UP);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> queueDirection(Direction.DOWN);
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> queueDirection(Direction.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> queueDirection(Direction.RIGHT);
            case KeyEvent.VK_R -> {
                context.queueCommand(new RestartSnakeCommand(world));
                context.executeQueuedCommands();
                repaint();
            }
            default -> {
                return;
            }
        }
    }

    private void queueDirection(Direction direction) {
        context.queueCommand(new DirectionCommand(world.getSnake(), direction));
        context.executeQueuedCommands();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // Not needed for the step-based loop.
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // Not needed for the step-based loop.
    }
}
