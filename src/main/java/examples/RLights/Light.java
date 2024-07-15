package examples.RLights;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;

import static com.raylib.Raylib.*;

public class Light {
    public final static int LIGHT_DIRECTIONAL = 0,
            LIGHT_POINT = 1;
    public final static int MAX_LIGHTS = 4;

    private static int lightsCount = 0;

    private final Vector3 position;
    private final Color color;

    private final IntPointer enabledP;
    private final IntPointer typeP;
    private final FloatPointer positionP;
    private final FloatPointer targetP;
    private final FloatPointer colorP;

    private final int enabledLoc;
    private final int typeLoc;
    private final int positionLoc;
    private final int targetLoc;
    private final int colorLoc;

    public static int unsignedByteToInt(byte b) {
        if (b < 0) return 256 - b;
        else return b;
    }

    public Light(int type,
                 Vector3 position,
                 Vector3 target,
                 Color color,
                 Shader shader) {

        this.enabledP = new IntPointer(1);
        setEnabled(true);
        this.typeP = new IntPointer(1);
        this.typeP.put(0, type);
        this.position = position;
        this.positionP = new FloatPointer(position.x(), position.y(), position.z());
        this.targetP = new FloatPointer(target.x(), target.y(), target.z());
        this.color = color;
        this.colorP = new FloatPointer(
                ((float) unsignedByteToInt(color.r())) / 255f,
                ((float) unsignedByteToInt(color.g())) / 255f,
                ((float) unsignedByteToInt(color.b())) / 255f,
                ((float) unsignedByteToInt(color.a())) / 255f);
        this.enabledLoc = GetShaderLocation(shader, "lights[" + lightsCount + "].enabled");
        this.typeLoc = GetShaderLocation(shader, "lights[" + lightsCount + "].type");
        this.positionLoc = GetShaderLocation(shader, "lights[" + lightsCount + "].position");
        this.targetLoc = GetShaderLocation(shader, "lights[" + lightsCount + "].target");
        this.colorLoc = GetShaderLocation(shader, "lights[" + lightsCount + "].color");

        UpdateLightValues(shader);

        lightsCount++;
    }

    public void setEnabled(boolean enabledP) {
        this.enabledP.put(0, enabledP ? 1 : 0);
    }

    public boolean getEnabled() {
        return this.enabledP.get() == 1;
    }


    public void UpdateLightValues(Shader shader) {
        // Send to shader light enabled state and type
        SetShaderValue(shader, enabledLoc, enabledP, SHADER_UNIFORM_INT);
        SetShaderValue(shader, typeLoc, typeP, SHADER_UNIFORM_INT);

        // Send to shader light position values

        SetShaderValue(shader, positionLoc, positionP, SHADER_UNIFORM_VEC3);

        // Send to shader light target position values
        SetShaderValue(shader, targetLoc, targetP, SHADER_UNIFORM_VEC3);

        // Send to shader light color values
        SetShaderValue(shader, colorLoc, colorP, SHADER_UNIFORM_VEC4);

    }

    public Vector3 getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }
}
