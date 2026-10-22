package net.backtooltweak;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class TweakRender {
    public enum Slot { NONE, BACK, BELT }

    /** Qual renderer do BackSlot esta desenhando agora: costas (item 1) ou cintura (item 2). */
    public static Slot SLOT = Slot.NONE;
    /** quantos push() nossos ainda faltam dar pop(). */
    public static int PUSHED = 0;

    private TweakRender() {}

    public static void apply(MatrixStack ms, Pose t) {
        float x = t.x, ry = t.rotY, rz = t.rotZ;
        if (t.mirror) { x = -x; ry = -ry; rz = -rz; }
        ms.translate(x / 16f, t.y / 16f, t.z / 16f);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ry));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(t.rotX));
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rz));
        float s = Math.max(0.01f, 1f + t.scale);
        ms.scale(s, s, s);
    }
}
