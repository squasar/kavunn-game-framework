package render;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class SwingRenderingPanel extends JPanel {

    private final RenderingPipeline pipeline;
    private final Universe universe;
    private final Planet planet;

    public SwingRenderingPanel(RenderingPipeline pipeline, Universe universe, Planet planet) {
        this.pipeline = pipeline;
        this.universe = universe;
        this.planet = planet;
        setDoubleBuffered(true);
    }

    public RenderingPipeline getPipeline() {
        return this.pipeline;
    }

    public Universe getUniverse() {
        return this.universe;
    }

    public Planet getPlanet() {
        return this.planet;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            Java2DRenderBackend backend = new Java2DRenderBackend(g2);
            beforePipelineRender(g2, backend);
            this.pipeline.render(this.universe, this.planet, backend);
            afterPipelineRender(g2, backend);
        } finally {
            g2.dispose();
        }
    }

    protected void beforePipelineRender(Graphics2D graphics, Java2DRenderBackend backend) {
    }

    protected void afterPipelineRender(Graphics2D graphics, Java2DRenderBackend backend) {
    }
}
