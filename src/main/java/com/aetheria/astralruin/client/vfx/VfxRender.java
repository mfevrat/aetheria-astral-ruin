package com.aetheria.astralruin.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Primitives de rendu des effets : quads double face, rubans orientes camera, cylindres. Coordonnees relatives a la camera. */
public final class VfxRender {

    public static void vertex(VertexConsumer vc, Matrix4f m, Matrix3f n, Vector3f p, float u, float v, float r, float g, float b) {
        vc.vertex(m, p.x, p.y, p.z).color(r, g, b, 1F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(n, 0F, 1F, 0F).endVertex();
    }

    /** Quad p0..p3 (UV 0,0 / 0,1 / 1,1 / 1,0), dessine des deux cotes. */
    public static void quad(VertexConsumer vc, PoseStack.Pose pose, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3,
                            float u0, float v0, float u1, float v1, float r, float g, float b) {
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        vertex(vc, m, n, p0, u0, v0, r, g, b);
        vertex(vc, m, n, p1, u0, v1, r, g, b);
        vertex(vc, m, n, p2, u1, v1, r, g, b);
        vertex(vc, m, n, p3, u1, v0, r, g, b);
        vertex(vc, m, n, p3, u1, v0, r, g, b);
        vertex(vc, m, n, p2, u1, v1, r, g, b);
        vertex(vc, m, n, p1, u0, v1, r, g, b);
        vertex(vc, m, n, p0, u0, v0, r, g, b);
    }

    /** Quad centre en c, dans le plan (ax, ay), demi-taille h. */
    public static void plane(VertexConsumer vc, PoseStack.Pose pose, Vector3f c, Vector3f ax, Vector3f ay, float h,
                             float r, float g, float b) {
        Vector3f x = new Vector3f(ax).mul(h), y = new Vector3f(ay).mul(h);
        quad(vc, pose,
                new Vector3f(c).sub(x).sub(y), new Vector3f(c).sub(x).add(y),
                new Vector3f(c).add(x).add(y), new Vector3f(c).add(x).sub(y), 0, 0, 1, 1, r, g, b);
    }

    /** Ruban de a vers b toujours tourne vers la camera (origine). V = 0 en a, vRepeat en b. */
    public static void ribbon(VertexConsumer vc, PoseStack.Pose pose, Vector3f a, Vector3f b, float width,
                              float vOffset, float vRepeat, float r, float g, float bl) {
        Vector3f dir = new Vector3f(b).sub(a);
        Vector3f mid = new Vector3f(a).add(b).mul(0.5F);
        Vector3f side = new Vector3f(dir).cross(mid);
        if (side.lengthSquared() < 1e-8F) return;
        side.normalize().mul(width * 0.5F);
        quad(vc, pose, new Vector3f(a).sub(side), new Vector3f(b).sub(side), new Vector3f(b).add(side), new Vector3f(a).add(side),
                0, vOffset, 1, vOffset + vRepeat, r, g, bl);
    }

    /** Cylindre vertical ouvert (pilier), V qui defile avec scroll. */
    public static void cylinder(VertexConsumer vc, PoseStack.Pose pose, Vector3f base, float radius, float height,
                                int sides, float scroll, float vRepeat, float r, float g, float b) {
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2 * i / sides, a1 = Math.PI * 2 * (i + 1) / sides;
            float x0 = (float) Math.cos(a0) * radius, z0 = (float) Math.sin(a0) * radius;
            float x1 = (float) Math.cos(a1) * radius, z1 = (float) Math.sin(a1) * radius;
            quad(vc, pose,
                    new Vector3f(base.x + x0, base.y, base.z + z0), new Vector3f(base.x + x0, base.y + height, base.z + z0),
                    new Vector3f(base.x + x1, base.y + height, base.z + z1), new Vector3f(base.x + x1, base.y, base.z + z1),
                    (float) i / sides, scroll, (float) (i + 1) / sides, scroll + vRepeat, r, g, b);
        }
    }

    private VfxRender() {}
}
