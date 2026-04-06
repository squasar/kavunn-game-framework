package assetmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;

import assetmanager.catalog.AssetKey;
import assetmanager.catalog.EntityAssetBundle;
import assetmanager.font.FontStyle;
import assetmanager.opengl.OpenGLBlendMode;
import assetmanager.opengl.OpenGLDraftLibrary;
import assetmanager.opengl.OpenGLFontDraft;
import assetmanager.opengl.OpenGLMaterialDraft;
import assetmanager.opengl.OpenGLPaletteDraft;
import assetmanager.opengl.OpenGLShaderDraft;
import assetmanager.opengl.OpenGLTextureDraft;
import assetmanager.opengl.OpenGLUiElementDraft;
import assetmanager.opengl.OpenGLUiModuleDraft;
import assetmanager.source.BufferedImageAssetSource;
import assetmanager.source.EntityImageSequence;
import assetmanager.ui.UiElementType;
import physics.palette.PaletteSet;
import physics.palette.PhysicsColor;

public final class AssetManagerSmokeTest {

    private AssetManagerSmokeTest() {
    }

    public static void main(String[] args) {
        System.out.println(run());
    }

    public static String run() {
        AssetManager manager = new AssetManager();

        AssetKey entityKey = AssetKey.of("demo", "ember-survivor");
        EntityImageSequence sequence = new EntityImageSequence(entityKey);
        sequence.addFrame("idle-0", "idle", 0, 120L, new BufferedImageAssetSource("idle-0", buildFrame(0)));
        sequence.addFrame("idle-1", "idle", 1, 120L, new BufferedImageAssetSource("idle-1", buildFrame(1)));
        sequence.addFrame("dash-0", "dash", 0, 80L, new BufferedImageAssetSource("dash-0", buildFrame(2)));

        EntityAssetBundle bundle = new EntityAssetBundle(entityKey, sequence);
        manager.registerEntityBundle(bundle);

        OpenGLDraftLibrary library = buildDraftLibrary(entityKey);
        manager.registerDraftLibrary(library);

        manager.prepareAllEntityAssets();
        int totalTriangles = manager.requirePreparedMeshSequence(entityKey).getFrames().stream()
            .mapToInt(frame -> frame.getMesh().getTriangles().size())
            .sum();
        PaletteSet palette = manager.requirePreparedMeshSequence(entityKey).getFrames().get(0).getPalette();

        return String.format(
            Locale.US,
            "AssetManager smoke test passed: entityFrames=%d preparedFrames=%d totalTriangles=%d paletteSwatches=%d fonts=%d uiModules=%d materials=%d",
            sequence.size(),
            manager.requirePreparedMeshSequence(entityKey).size(),
            totalTriangles,
            palette.getSwatches().size(),
            library.getFonts().size(),
            library.getUiModules().size(),
            library.getMaterials().size()
        );
    }

    private static OpenGLDraftLibrary buildDraftLibrary(AssetKey entityKey) {
        OpenGLDraftLibrary library = new OpenGLDraftLibrary(AssetKey.of("demo", "default-drafts"));
        AssetKey paletteKey = AssetKey.of("demo", "ember-palette");
        AssetKey fontKey = AssetKey.of("demo", "ritual-ui-font");
        AssetKey shaderKey = AssetKey.of("demo", "flat-ui-shader");
        AssetKey textureKey = AssetKey.of("demo", "ember-survivor-texture");
        AssetKey materialKey = AssetKey.of("demo", "ember-survivor-material");
        AssetKey moduleKey = AssetKey.of("demo", "combat-hud");

        library.registerPalette(new OpenGLPaletteDraft(
            paletteKey,
            new PaletteSet(List.of(
                new physics.palette.PaletteSwatch("primary", new PhysicsColor(238, 175, 112, 255), 0.55),
                new physics.palette.PaletteSwatch("secondary", new PhysicsColor(61, 31, 24, 255), 0.28),
                new physics.palette.PaletteSwatch("accent", new PhysicsColor(140, 224, 205, 255), 0.17)
            ))
        ));
        library.registerFont(new OpenGLFontDraft(fontKey, "Serif", FontStyle.BOLD, 18, 1.25, new PhysicsColor(246, 229, 197, 255), true, null));
        library.registerShader(new OpenGLShaderDraft(
            shaderKey,
            "attribute vec2 aPosition; void main(){ gl_Position = vec4(aPosition, 0.0, 1.0); }",
            "void main(){ gl_FragColor = vec4(1.0); }",
            List.of("uProjection", "uColor"),
            List.of("aPosition", "aColor")
        ));
        library.registerTexture(new OpenGLTextureDraft(textureKey, entityKey, true, "linear-mipmap-linear", "linear", "clamp-to-edge"));
        library.registerMaterial(new OpenGLMaterialDraft(materialKey, shaderKey, textureKey, paletteKey, OpenGLBlendMode.ALPHA_BLEND, 1.0));
        library.registerUiModule(new OpenGLUiModuleDraft(
            moduleKey,
            "hud",
            960.0,
            640.0,
            paletteKey,
            fontKey,
            List.of(
                new OpenGLUiElementDraft("panel", UiElementType.PANEL, 18.0, 16.0, 924.0, 84.0, "secondary", materialKey, ""),
                new OpenGLUiElementDraft("health-bar", UiElementType.PROGRESS_BAR, 670.0, 30.0, 220.0, 12.0, "primary", materialKey, ""),
                new OpenGLUiElementDraft("banner", UiElementType.TEXT, 250.0, 108.0, 460.0, 34.0, "accent", materialKey, "Ashwake HUD")
            )
        ));
        return library;
    }

    private static BufferedImage buildFrame(int frameIndex) {
        BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(0, 0, 0, 0));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

            graphics.setColor(new Color(216, 110 + frameIndex * 20, 82, 255));
            graphics.fillOval(16 + frameIndex * 3, 10, 34, 34);

            graphics.setColor(new Color(78, 36, 28, 255));
            graphics.fillRoundRect(22, 34, 30, 30, 14, 14);

            graphics.setColor(new Color(247, 220, 180, 220));
            graphics.setStroke(new BasicStroke(4f));
            graphics.drawLine(38, 24, 58 + frameIndex * 4, 10 + frameIndex * 8);
        } finally {
            graphics.dispose();
        }
        return image;
    }
}
