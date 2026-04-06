package render;

import java.awt.Graphics2D;
import java.util.Objects;

public class Java2DRenderBackend implements RenderBackend {

    private Graphics2D graphics;

    public Java2DRenderBackend(Graphics2D graphics) {
        this.setGraphics(graphics);
    }

    @Override
    public String getBackendName() {
        return "java2d";
    }

    public Graphics2D getGraphics() {
        if (this.graphics == null) {
            throw new IllegalStateException("Java2D backend does not currently hold a Graphics2D context.");
        }
        return this.graphics;
    }

    public void setGraphics(Graphics2D graphics) {
        this.graphics = Objects.requireNonNull(graphics, "Java2D rendering requires a Graphics2D context.");
    }
}
