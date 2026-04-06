package render;

public final class OpenGLESConstants {

    public static final int COLOR_BUFFER_BIT = OpenGLConstants.COLOR_BUFFER_BIT;
    public static final int DEPTH_BUFFER_BIT = OpenGLConstants.DEPTH_BUFFER_BIT;
    public static final int STENCIL_BUFFER_BIT = OpenGLConstants.STENCIL_BUFFER_BIT;

    public static final int TRIANGLES = OpenGLConstants.TRIANGLES;
    public static final int TRIANGLE_STRIP = OpenGLConstants.TRIANGLE_STRIP;
    public static final int TRIANGLE_FAN = OpenGLConstants.TRIANGLE_FAN;
    public static final int LINES = OpenGLConstants.LINES;
    public static final int LINE_STRIP = OpenGLConstants.LINE_STRIP;
    public static final int POINTS = OpenGLConstants.POINTS;

    public static final int FLOAT = OpenGLConstants.FLOAT;
    public static final int UNSIGNED_INT = OpenGLConstants.UNSIGNED_INT;
    public static final int UNSIGNED_SHORT = OpenGLConstants.UNSIGNED_SHORT;
    public static final int UNSIGNED_BYTE = OpenGLConstants.UNSIGNED_BYTE;

    public static final int TEXTURE_2D = OpenGLConstants.TEXTURE_2D;
    public static final int TEXTURE_MIN_FILTER = OpenGLConstants.TEXTURE_MIN_FILTER;
    public static final int TEXTURE_MAG_FILTER = OpenGLConstants.TEXTURE_MAG_FILTER;
    public static final int TEXTURE_WRAP_S = OpenGLConstants.TEXTURE_WRAP_S;
    public static final int TEXTURE_WRAP_T = OpenGLConstants.TEXTURE_WRAP_T;
    public static final int TEXTURE0 = 0x84C0;
    public static final int TEXTURE1 = 0x84C1;
    public static final int TEXTURE2 = 0x84C2;

    public static final int BLEND = 0x0BE2;
    public static final int SRC_ALPHA = 0x0302;
    public static final int ONE_MINUS_SRC_ALPHA = 0x0303;

    public static final int ARRAY_BUFFER = 0x8892;
    public static final int ELEMENT_ARRAY_BUFFER = 0x8893;
    public static final int STATIC_DRAW = 0x88E4;
    public static final int DYNAMIC_DRAW = 0x88E8;
    public static final int STREAM_DRAW = 0x88E0;

    public static final int VERTEX_SHADER = 0x8B31;
    public static final int FRAGMENT_SHADER = 0x8B30;

    public static final int RGBA = OpenGLConstants.RGBA;
    public static final int LINEAR = OpenGLConstants.LINEAR;
    public static final int NEAREST = OpenGLConstants.NEAREST;
    public static final int CLAMP_TO_EDGE = OpenGLConstants.CLAMP_TO_EDGE;

    private OpenGLESConstants() {
    }
}
