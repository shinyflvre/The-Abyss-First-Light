package net.yezon.theabyss.client.model;

import com.google.common.collect.MapMaker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class ElementRotations {
    private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);
    private static final Map<BlockElementRotation, Matrix4fc> TRANSFORMS = new MapMaker().weakKeys().makeMap();

    private ElementRotations() {
    }

    public static @Nullable BlockElementRotation parse(JsonObject element) {
        if (!element.has("rotation")) {
            return null;
        }
        JsonObject rotation = GsonHelper.getAsJsonObject(element, "rotation");
        Direction.Axis axis;
        float angle;
        Matrix4f transform;
        if (!rotation.has("axis") && !rotation.has("angle")) {
            if (!rotation.has("x") && !rotation.has("y") && !rotation.has("z")) {
                return null;
            }
            float x = GsonHelper.getAsFloat(rotation, "x", 0.0F);
            float y = GsonHelper.getAsFloat(rotation, "y", 0.0F);
            float z = GsonHelper.getAsFloat(rotation, "z", 0.0F);
            transform = new Matrix4f().rotationZYX(z * DEG_TO_RAD, y * DEG_TO_RAD, x * DEG_TO_RAD);
            axis = Direction.Axis.Y;
            angle = 0.0F;
        } else {
            if (!rotation.has("axis") || !rotation.has("angle")) {
                return null;
            }
            angle = GsonHelper.getAsFloat(rotation, "angle");
            if (angle == 0.0F || Mth.abs(angle) == 22.5F || Mth.abs(angle) == 45.0F || !Float.isFinite(angle)) {
                return null;
            }
            axis = Direction.Axis.byName(GsonHelper.getAsString(rotation, "axis").toLowerCase(Locale.ROOT));
            if (axis == null) {
                return null;
            }
            transform = new Matrix4f().rotation(angle * DEG_TO_RAD, unit(axis));
        }
        Vector3f origin = vector(rotation, "origin").mul(0.0625F);
        boolean rescale = GsonHelper.getAsBoolean(rotation, "rescale", false);
        if (rescale) {
            transform.scale(rescale(transform));
        }
        BlockElementRotation result = new BlockElementRotation(origin, axis, angle, rescale);
        TRANSFORMS.put(result, transform);
        return result;
    }

    public static boolean apply(Vector3f vertex, @Nullable BlockElementRotation rotation) {
        Matrix4fc transform = rotation == null ? null : TRANSFORMS.get(rotation);
        if (transform == null) {
            return false;
        }
        vertex.sub(rotation.origin());
        transform.transformPosition(vertex);
        vertex.add(rotation.origin());
        return true;
    }

    private static Vector3f unit(Direction.Axis axis) {
        return new Vector3f(axis == Direction.Axis.X ? 1.0F : 0.0F, axis == Direction.Axis.Y ? 1.0F : 0.0F, axis == Direction.Axis.Z ? 1.0F : 0.0F);
    }

    private static Vector3f rescale(Matrix4fc rotation) {
        return new Vector3f(axisScale(rotation, Direction.Axis.X), axisScale(rotation, Direction.Axis.Y), axisScale(rotation, Direction.Axis.Z));
    }

    private static float axisScale(Matrix4fc rotation, Direction.Axis axis) {
        Vector3f turned = rotation.transformDirection(unit(axis));
        return 1.0F / Math.max(Math.max(Math.abs(turned.x), Math.abs(turned.y)), Math.abs(turned.z));
    }

    private static Vector3f vector(JsonObject json, String name) {
        JsonArray array = GsonHelper.getAsJsonArray(json, name);
        if (array.size() != 3) {
            throw new JsonParseException("Expected 3 " + name + " values, found: " + array.size());
        }
        float[] values = new float[3];
        for (int i = 0; i < 3; i++) {
            values[i] = GsonHelper.convertToFloat(array.get(i), name + "[" + i + "]");
        }
        return new Vector3f(values[0], values[1], values[2]);
    }
}
