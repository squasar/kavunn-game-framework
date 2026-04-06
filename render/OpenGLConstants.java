package render;

public final class OpenGLConstants {

    public static final int COLOR_BUFFER_BIT = 0x00004000;
    public static final int DEPTH_BUFFER_BIT = 0x00000100;
    public static final int STENCIL_BUFFER_BIT = 0x00000400;

    public static final int TRIANGLES = 0x0004;
    public static final int TRIANGLE_STRIP = 0x0005;
    public static final int TRIANGLE_FAN = 0x0006;
    public static final int LINES = 0x0001;
    public static final int LINE_STRIP = 0x0003;
    public static final int POINTS = 0x0000;

    public static final int FLOAT = 0x1406;
    public static final int UNSIGNED_INT = 0x1405;
    public static final int UNSIGNED_SHORT = 0x1403;
    public static final int UNSIGNED_BYTE = 0x1401;

    public static final int TEXTURE_2D = 0x0DE1;
    public static final int TEXTURE_MIN_FILTER = 0x2801;
    public static final int TEXTURE_MAG_FILTER = 0x2800;
    public static final int TEXTURE_WRAP_S = 0x2802;
    public static final int TEXTURE_WRAP_T = 0x2803;
    public static final int RGBA = 0x1908;
    public static final int LINEAR = 0x2601;
    public static final int NEAREST = 0x2600;
    public static final int CLAMP_TO_EDGE = 0x812F;

    private OpenGLConstants() {
    }
}
