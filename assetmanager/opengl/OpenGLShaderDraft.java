package assetmanager.opengl;

import java.util.List;

import assetmanager.catalog.AssetKey;

public final class OpenGLShaderDraft {

    private final AssetKey key;
    private final String vertexSource;
    private final String fragmentSource;
    private final List<String> uniforms;
    private final List<String> attributes;

    public OpenGLShaderDraft(
        AssetKey key,
        String vertexSource,
        String fragmentSource,
        List<String> uniforms,
        List<String> attributes
    ) {
        if (key == null) {
            throw new IllegalArgumentException("An OpenGL shader draft requires a key.");
        }
        this.key = key;
        this.vertexSource = vertexSource == null ? "" : vertexSource;
        this.fragmentSource = fragmentSource == null ? "" : fragmentSource;
        this.uniforms = List.copyOf(uniforms == null ? List.of() : uniforms);
        this.attributes = List.copyOf(attributes == null ? List.of() : attributes);
    }

    public AssetKey getKey() {
        return this.key;
    }

    public String getVertexSource() {
        return this.vertexSource;
    }

    public String getFragmentSource() {
        return this.fragmentSource;
    }

    public List<String> getUniforms() {
        return this.uniforms;
    }

    public List<String> getAttributes() {
        return this.attributes;
    }
}
