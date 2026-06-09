package net.shelmarow.betterlockon.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;
import java.util.stream.Collectors;

public class LockOnConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.ConfigValue<String> LOCK_ON_ICON_TYPES;

    public static final ForgeConfigSpec.DoubleValue LOCK_ON_ICON_SIZE;
    public static final ForgeConfigSpec.DoubleValue LOCK_ON_RED;
    public static final ForgeConfigSpec.DoubleValue LOCK_ON_GREEN;
    public static final ForgeConfigSpec.DoubleValue LOCK_ON_BLUE;
    public static final ForgeConfigSpec.DoubleValue LOCK_ON_ALPHA;
    public static final ForgeConfigSpec.BooleanValue LOCK_ON_SIZE_SCALING;

    public static final ForgeConfigSpec.DoubleValue MAX_LOCK_ON_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MAX_TARGET_SELECT_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue MAX_PITCH;
    public static final ForgeConfigSpec.DoubleValue MIN_PITCH;
    public static final ForgeConfigSpec.DoubleValue PITCH_OFFSET;
    public static final ForgeConfigSpec.DoubleValue ROTATION_TRANSITION;

    public static final ForgeConfigSpec.BooleanValue FIX_WOM_ATTACK_LOCK_ON;

    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_CAMERA;
    public static final ForgeConfigSpec.DoubleValue MAX_DYNAMIC_CAMERA_Y;
    public static final ForgeConfigSpec.DoubleValue MAX_DYNAMIC_CAMERA_X;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_FOV;
    public static final ForgeConfigSpec.DoubleValue MAX_FOV_MULTIPLIER;

    public static final ForgeConfigSpec.BooleanValue ENABLE_MODEL_TRANSPARENCY;
    public static final ForgeConfigSpec.DoubleValue START_TRANSPARENCY_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue FULLY_TRANSPARENCY_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue AUTO_SWITCH_FIRST_PERSON;
    public static final ForgeConfigSpec.DoubleValue AUTO_SWITCH_DISTANCE;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_LOCK_ON_JOINT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Lock-On Icon Settings");

        LOCK_ON_ICON_TYPES = builder
                .define("lockOnIconTypes","DefaultType");

        LOCK_ON_ICON_SIZE = builder
                .comment("Lock-on icon base size, Default: 0.4")
                .defineInRange("lockOnIconSize", 0.4, 0.1, 2.0);

        LOCK_ON_RED = builder
                .comment("Lock-on UI red color component Default: 1.0")
                .defineInRange("lockOnRed", 1.0, 0.0, 1.0);

        LOCK_ON_GREEN = builder
                .comment("Lock-on UI green color component Default: 1.0")
                .defineInRange("lockOnGreen", 1.0, 0.0, 1.0);

        LOCK_ON_BLUE = builder
                .comment("Lock-on UI blue color component Default: 1.0")
                .defineInRange("lockOnBlue", 1.0, 0.0, 1.0);

        LOCK_ON_ALPHA = builder
                .comment("Lock-on UI alpha (transparency) component Default: 1.0")
                .defineInRange("lockOnAlpha", 1.0, 0.0, 1.0);

        LOCK_ON_SIZE_SCALING = builder
                .comment("Enable automatic size scaling for large entities",
                        "When enabled, lock-on UI will scale up for larger targets")
                .define("lockOnSizeScaling", true);

        builder.pop();

        builder.push("Lock-On Change Settings");

        MAX_LOCK_ON_DISTANCE = builder
                .comment("The farthest distance that can keep lock on to the target")
                        .defineInRange("maxLockOnDistance", 30.0D, 0.0D, 128D);

        MAX_TARGET_SELECT_DISTANCE = builder
                .comment("The farthest distance you can quickly lock on to the target")
                        .defineInRange("maxTargetSelectDistance", 30D, 0D, 128D);

        MAX_PITCH = builder
                .comment("Max camera pith when you lock on the target")
                        .defineInRange("maxPithWhenLockOn", -30D, -90.0D, 90D);

        MIN_PITCH = builder
                .comment("Min camera pith when you lock on the target")
                .defineInRange("minPithWhenLockOn", 40.0D, -90D, 90D);

        PITCH_OFFSET = builder
                .comment("Pitch offset when you lock on the target")
                        .defineInRange("pitchOffsetWhenLockOn", 15D, -90.0D, 90D);

        ROTATION_TRANSITION = builder
                .defineInRange("rotationTransition", 0.5D, 0.1D, 1D);

        builder.pop();

        builder.push("Lock-On WOM Attack Fix");

        FIX_WOM_ATTACK_LOCK_ON = builder
                .comment("Fix WOM disabled lock on when attacking")
                .define("fixWomAttackLockOn",true);

        builder.pop();

        builder.push("Lock-On Dynamic camera Settings");

        ENABLE_DYNAMIC_CAMERA = builder
                .comment("Enable the dynamic camera")
                .define("enableDynamicCamera", true);

        MAX_DYNAMIC_CAMERA_Y = builder
                .comment("Max height of the camera offset")
                        .defineInRange("maxDynamicCameraY", 2.5D, 0.0D, 15D);

        MAX_DYNAMIC_CAMERA_X = builder
                .comment("Max Horizontal distance of the camera offset")
                        .defineInRange("maxDynamicCameraX", 7.5D, 0.0D, 15D);

        ENABLE_DYNAMIC_FOV = builder
                .comment("Enable the dynamic fov")
                .define("enableDynamicFov", true);

        MAX_FOV_MULTIPLIER = builder
                .comment("Max dynamic fov multiplier")
                .defineInRange("maxFovMultiplier", 1.15D, 1D, 2D);

        builder.pop();

        builder.push("Third Person Camera");

        ENABLE_MODEL_TRANSPARENCY = builder
                .define("enableModelTransparency", true);

        START_TRANSPARENCY_DISTANCE = builder
                .defineInRange("startTransparencyDistance", 9D, 0D, 15D);

        FULLY_TRANSPARENCY_DISTANCE = builder
                .defineInRange("fullyTransparencyDistance", 2D, 0D, 15D);

        AUTO_SWITCH_FIRST_PERSON = builder
                .define("autoSwitchFirstPerson", false);

        AUTO_SWITCH_DISTANCE = builder
                .defineInRange("autoSwitchDistance", 1.0D, 0.0D, 10D);

        builder.pop();

        builder.push("Entity Lock on Joint List");

        ENTITY_LOCK_ON_JOINT = builder
                .comment("example: minecraft:zombie#Head,Chest,Default")
                .defineList("entityLockonJoint",List.of(),o -> o instanceof String s && s.matches("^[^#]+#.+$"));

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    public static List<String> getStringSetForEntity(String entityId) {
        for (String entry : ENTITY_LOCK_ON_JOINT.get()) {
            Map.Entry<String, List<String>> parsed = parseEntry(entry);
            if (parsed != null && parsed.getKey().equals(entityId)) {
                return parsed.getValue();
            }
        }
        return Collections.emptyList();
    }

    public static Map.Entry<String, List<String>> parseEntry(String entry) {
        if (entry == null || !entry.contains("#")) {
            return null;
        }

        String[] parts = entry.split("#", 2);
        String entityId = parts[0];
        String valuesPart = parts[1];

        List<String> values = Arrays.stream(valuesPart.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        return new AbstractMap.SimpleEntry<>(entityId, values);
    }

    public static List<String> parseList(String entry) {
        if (entry == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(entry.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
