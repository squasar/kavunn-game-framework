package com.example;

import java.awt.BasicStroke;
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

public class TetrisGamePanel extends JPanel implements ActionListener, KeyListener {

    private static final int CELL_SIZE = 30;
    private static final int PADDING = 24;
    private static final int SIDEBAR_WIDTH = 210;

    private static final Color BG_TOP = new Color(13, 18, 28);
    private static final Color BG_BOTTOM = new Color(24, 30, 47);
    private static final Color BOARD_BG = new Color(12, 16, 24);
    private static final Color GRID = new Color(36, 44, 62);
    private static final Color CARD = new Color(22, 28, 41, 220);
    private static final Color TEXT = new Color(244, 247, 255);
    private static final Color MUTED = new Color(173, 181, 204);
    private static final Color GHOST = new Color(255, 255, 255, 48);

    private final Context context;
    private final TetrisWorld world;
    private final Timer timer;

    public TetrisGamePanel(Context context, TetrisWorld world) {
        this.context = context;
        this.world = world;
        this.timer = new Timer(world.getTickDelay(), this);

        setFocusable(true);
        setDoubleBuffered(true);
        addKeyListener(this);
        setPreferredSize(new Dimension(
            PADDING * 3 + world.getBoard().getCols() * CELL_SIZE + SIDEBAR_WIDTH,
            PADDING * 2 + world.getBoard().getRows() * CELL_SIZE
        ));

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
        paintBoardSurface(g2);
        paintLockedBlocks(g2);
        paintGhostPiece(g2);
        paintActivePiece(g2);
        paintSidebar(g2);

        if (world.isPaused() || world.isGameOver()) {
            paintOverlay(g2);
        }

        g2.dispose();
    }

    private void paintBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    private void paintBoardSurface(Graphics2D g2) {
        int boardWidth = world.getBoard().getCols() * CELL_SIZE;
        int boardHeight = world.getBoard().getRows() * CELL_SIZE;

        g2.setColor(new Color(6, 10, 18, 140));
        g2.fillRoundRect(PADDING - 8, PADDING - 8, boardWidth + 16, boardHeight + 16, 28, 28);

        g2.setColor(BOARD_BG);
        g2.fillRoundRect(PADDING, PADDING, boardWidth, boardHeight, 24, 24);

        g2.setColor(GRID);
        for (int row = 0; row <= world.getBoard().getRows(); row++) {
            int y = PADDING + row * CELL_SIZE;
            g2.drawLine(PADDING, y, PADDING + boardWidth, y);
        }
        for (int col = 0; col <= world.getBoard().getCols(); col++) {
            int x = PADDING + col * CELL_SIZE;
            g2.drawLine(x, PADDING, x, PADDING + boardHeight);
        }
    }

    private void paintLockedBlocks(Graphics2D g2) {
        for (int row = 0; row < world.getBoard().getRows(); row++) {
            for (int col = 0; col < world.getBoard().getCols(); col++) {
                TetrominoType type = world.getBoard().getCell(row, col);
                if (type != null) {
                    paintCell(g2, col, row, type.color(), 255);
                }
            }
        }
    }

    private void paintGhostPiece(Graphics2D g2) {
        for (Point point : world.getGhostCells()) {
            paintCell(g2, point.x, point.y, GHOST, 90);
        }
    }

    private void paintActivePiece(Graphics2D g2) {
        for (Point point : world.getActivePiece().cells()) {
            paintCell(g2, point.x, point.y, world.getActivePiece().getType().color(), 255);
        }
    }

    private void paintCell(Graphics2D g2, int col, int row, Color color, int alpha) {
        int x = PADDING + col * CELL_SIZE + 2;
        int y = PADDING + row * CELL_SIZE + 2;
        int size = CELL_SIZE - 4;

        Color fill = alpha == 255 ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, size, size, 10, 10);
        g2.setColor(new Color(255, 255, 255, Math.min(alpha, 120)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 10, 10);
    }

    private void paintSidebar(Graphics2D g2) {
        int sidebarX = PADDING * 2 + world.getBoard().getCols() * CELL_SIZE;
        int cardWidth = SIDEBAR_WIDTH - PADDING;

        g2.setColor(CARD);
        g2.fillRoundRect(sidebarX, PADDING, cardWidth, 236, 26, 26);
        g2.fillRoundRect(sidebarX, PADDING + 252, cardWidth, 170, 26, 26);

        g2.setColor(TEXT);
        g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 28));
        g2.drawString("Blockfall", sidebarX + 18, PADDING + 38);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        g2.setColor(MUTED);
        g2.drawString("A faster, harder sibling to Snake.", sidebarX + 18, PADDING + 62);

        g2.setColor(TEXT);
        g2.drawString("Score", sidebarX + 18, PADDING + 108);
        g2.drawString("Lines", sidebarX + 18, PADDING + 148);
        g2.drawString("Level", sidebarX + 18, PADDING + 188);

        g2.setFont(new Font("JetBrains Mono", Font.BOLD, 20));
        g2.drawString(String.valueOf(world.getScore()), sidebarX + 108, PADDING + 108);
        g2.drawString(String.valueOf(world.getLinesCleared()), sidebarX + 108, PADDING + 148);
        g2.drawString(String.valueOf(world.getLevel()), sidebarX + 108, PADDING + 188);

        g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        g2.drawString("Next Piece", sidebarX + 18, PADDING + 286);
        paintPreviewPiece(g2, sidebarX + 18, PADDING + 306, world.getNextType());

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        g2.setColor(MUTED);
        g2.drawString("Left / Right : Shift", sidebarX + 18, PADDING + 448);
        g2.drawString("Up / X / W : Rotate CW", sidebarX + 18, PADDING + 470);
        g2.drawString("Z : Rotate CCW", sidebarX + 18, PADDING + 492);
        g2.drawString("Down / S : Soft drop", sidebarX + 18, PADDING + 514);
        g2.drawString("Space : Hard drop", sidebarX + 18, PADDING + 536);
        g2.drawString("P : Pause    R : Restart", sidebarX + 18, PADDING + 558);
    }

    private void paintPreviewPiece(Graphics2D g2, int startX, int startY, TetrominoType type) {
        for (int[] cell : type.cells(0)) {
            int x = startX + cell[1] * 24;
            int y = startY + cell[0] * 24;
            g2.setColor(type.color());
            g2.fillRoundRect(x, y, 20, 20, 8, 8);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRoundRect(x, y, 20, 20, 8, 8);
        }
    }

    private void paintOverlay(Graphics2D g2) {
        int width = world.getBoard().getCols() * CELL_SIZE - 48;
        int height = 140;
        int x = PADDING + 24;
        int y = (getHeight() / 2) - (height / 2);

        g2.setColor(new Color(5, 7, 13, 200));
        g2.fillRoundRect(x, y, width, height, 28, 28);

        g2.setColor(TEXT);
        g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 30));
        g2.drawString(world.isGameOver() ? "Run Over" : "Paused", x + 24, y + 48);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2.setColor(MUTED);
        g2.drawString(
            world.isGameOver() ? "The stack reached the top. Press R to jump back in." : "Press P to continue the run.",
            x + 24,
            y + 84
        );
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        context.queueCommand(world);
        context.executeQueuedCommands();
        timer.setDelay(world.getTickDelay());
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        TetrisAction action = switch (event.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> TetrisAction.MOVE_LEFT;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> TetrisAction.MOVE_RIGHT;
            case KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_X -> TetrisAction.ROTATE_CW;
            case KeyEvent.VK_Z -> TetrisAction.ROTATE_CCW;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> TetrisAction.SOFT_DROP;
            case KeyEvent.VK_SPACE -> TetrisAction.HARD_DROP;
            case KeyEvent.VK_P -> TetrisAction.TOGGLE_PAUSE;
            case KeyEvent.VK_R -> TetrisAction.RESTART;
            default -> null;
        };

        if (action != null) {
            context.queueCommand(new TetrisActionCommand(world, action));
            context.executeQueuedCommands();
            timer.setDelay(world.getTickDelay());
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        // Step-based input only.
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // Step-based input only.
    }
}
