package ashwake;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Timer;

import core.Context;
import render.DefaultRenderingPipeline;
import render.Java2DRenderBackend;
import render.SwingRenderingPanel;
import render.Universe;

final class AshwakeGamePanel extends SwingRenderingPanel implements ActionListener, KeyListener {

    private final Context context;
    private final AshwakeRunWorld world;
    private final Timer timer;

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    private boolean fireUpPressed;
    private boolean fireDownPressed;
    private boolean fireLeftPressed;
    private boolean fireRightPressed;

    AshwakeGamePanel(Context context, AshwakeRunWorld world, Universe universe, DefaultRenderingPipeline pipeline) {
        super(pipeline, universe, world);
        this.context = context;
        this.world = world;
        this.timer = new Timer(16, this);
        setFocusable(true);
        setPreferredSize(new Dimension(AshwakeRoomWorld.ROOM_WIDTH, AshwakeRoomWorld.ROOM_HEIGHT));
        addKeyListener(this);
        this.timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        queueContinuousInput();
        this.context.queueCommand(this.world);
        this.context.executeQueuedCommands();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W -> this.upPressed = true;
            case KeyEvent.VK_S -> this.downPressed = true;
            case KeyEvent.VK_A -> this.leftPressed = true;
            case KeyEvent.VK_D -> this.rightPressed = true;

            case KeyEvent.VK_UP -> this.fireUpPressed = true;
            case KeyEvent.VK_DOWN -> this.fireDownPressed = true;
            case KeyEvent.VK_LEFT -> this.fireLeftPressed = true;
            case KeyEvent.VK_RIGHT -> this.fireRightPressed = true;

            case KeyEvent.VK_SHIFT, KeyEvent.VK_SPACE -> executeCommand(new AshwakeDashCommand(this.world));
            case KeyEvent.VK_Q -> executeCommand(new AshwakeSecondaryCommand(this.world));
            case KeyEvent.VK_P -> executeCommand(new AshwakeTogglePauseCommand(this.world));
            case KeyEvent.VK_R -> executeCommand(new AshwakeRestartCommand(this.world));
            case KeyEvent.VK_E, KeyEvent.VK_ENTER -> executeCommand(new AshwakeAdvanceRoomCommand(this.world));
            case KeyEvent.VK_1 -> executeCommand(new AshwakeSelectModifierCommand(this.world, 0));
            case KeyEvent.VK_2 -> executeCommand(new AshwakeSelectModifierCommand(this.world, 1));
            case KeyEvent.VK_3 -> executeCommand(new AshwakeSelectModifierCommand(this.world, 2));
            default -> {
                return;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W -> this.upPressed = false;
            case KeyEvent.VK_S -> this.downPressed = false;
            case KeyEvent.VK_A -> this.leftPressed = false;
            case KeyEvent.VK_D -> this.rightPressed = false;

            case KeyEvent.VK_UP -> this.fireUpPressed = false;
            case KeyEvent.VK_DOWN -> this.fireDownPressed = false;
            case KeyEvent.VK_LEFT -> this.fireLeftPressed = false;
            case KeyEvent.VK_RIGHT -> this.fireRightPressed = false;
            default -> {
                return;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // Step loop uses pressed/released state only.
    }

    @Override
    protected void beforePipelineRender(Graphics2D graphics, Java2DRenderBackend backend) {
        AshwakeRenderFactory.configureGraphics(graphics);
    }

    private void queueContinuousInput() {
        double moveX = (this.rightPressed ? 1.0 : 0.0) - (this.leftPressed ? 1.0 : 0.0);
        double moveY = (this.downPressed ? 1.0 : 0.0) - (this.upPressed ? 1.0 : 0.0);
        this.context.queueCommand(new AshwakeMoveCommand(this.world, moveX, moveY));

        double fireX = (this.fireRightPressed ? 1.0 : 0.0) - (this.fireLeftPressed ? 1.0 : 0.0);
        double fireY = (this.fireDownPressed ? 1.0 : 0.0) - (this.fireUpPressed ? 1.0 : 0.0);
        if (fireX != 0.0 || fireY != 0.0) {
            this.context.queueCommand(new AshwakeFireCommand(this.world, fireX, fireY));
        }
    }

    private void executeCommand(core.Relation command) {
        this.context.queueCommand(command);
        this.context.executeQueuedCommands();
        repaint();
    }
}
