package net.backtooltweak;

/** Pose de um item. x/y/z em pixels (1/16 de bloco), rotacoes em graus, scale = diferenca (0 = normal, 0.5 = 50% maior). */
public class Pose {
    public boolean visible = true;
    public boolean mirror = false;
    public float x, y, z;
    public float rotX, rotY, rotZ;
    public float scale;

    public static final String[] FIELDS = {"x", "y", "z", "rotX", "rotY", "rotZ", "scale", "visible", "mirror"};

    public boolean set(String field, float v) {
        switch (field) {
            case "x" -> x = v;
            case "y" -> y = v;
            case "z" -> z = v;
            case "rotX" -> rotX = v;
            case "rotY" -> rotY = v;
            case "rotZ" -> rotZ = v;
            case "scale" -> scale = v;
            case "visible" -> visible = v >= 0.5f;
            case "mirror" -> mirror = v >= 0.5f;
            default -> { return false; }
        }
        return true;
    }

    public float get(String field) {
        return switch (field) {
            case "x" -> x;
            case "y" -> y;
            case "z" -> z;
            case "rotX" -> rotX;
            case "rotY" -> rotY;
            case "rotZ" -> rotZ;
            case "scale" -> scale;
            case "visible" -> visible ? 1f : 0f;
            case "mirror" -> mirror ? 1f : 0f;
            default -> 0f;
        };
    }

    public void reset() {
        visible = true; mirror = false;
        x = y = z = rotX = rotY = rotZ = scale = 0f;
    }

    public Pose copy() {
        Pose p = new Pose();
        p.visible = visible; p.mirror = mirror;
        p.x = x; p.y = y; p.z = z;
        p.rotX = rotX; p.rotY = rotY; p.rotZ = rotZ;
        p.scale = scale;
        return p;
    }

    public String summary() {
        return String.format("x=%.2f y=%.2f z=%.2f rotX=%.1f rotY=%.1f rotZ=%.1f scale=%.2f visible=%s mirror=%s",
                x, y, z, rotX, rotY, rotZ, scale, visible, mirror);
    }
}
