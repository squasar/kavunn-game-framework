package assetmanager.opengl;

import assetmanager.catalog.AssetKey;
import assetmanager.ui.UiElementType;

public final class OpenGLUiElementDraft {

    private final String elementId;
    private final UiElementType type;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final String paletteRole;
    private final AssetKey materialKey;
    private final String textValue;

    public OpenGLUiElementDraft(
        String elementId,
        UiElementType type,
        double x,
        double y,
        double width,
        double height,
        String paletteRole,
        AssetKey materialKey,
        String textValue
    ) {
        this.elementId = elementId == null || elementId.isBlank() ? "element" : elementId.trim();
        this.type = type == null ? UiElementType.PANEL : type;
        this.x = x;
        this.y = y;
        this.width = Math.max(0.0, width);
        this.height = Math.max(0.0, height);
        this.paletteRole = paletteRole == null || paletteRole.isBlank() ? "primary" : paletteRole.trim();
        this.materialKey = materialKey;
        this.textValue = textValue == null ? "" : textValue;
    }

    public String getElementId() {
        return this.elementId;
    }

    public UiElementType getType() {
        return this.type;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public String getPaletteRole() {
        return this.paletteRole;
    }

    public AssetKey getMaterialKey() {
        return this.materialKey;
    }

    public String getTextValue() {
        return this.textValue;
    }
}
