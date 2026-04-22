package net.shelmarow.betterlockon.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LockOnConfig {
    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.ConfigValue<String> LOCK_ON_ICON_TYPES;

    public static final ModConfigSpec.DoubleValue LOCK_ON_ICON_SIZE;
    public static final ModConfigSpec.DoubleValue LOCK_ON_RED;
    public static final ModConfigSpec.DoubleValue LOCK_ON_GREEN;
    public static final ModConfigSpec.DoubleValue LOCK_ON_BLUE;
    public static final ModConfigSpec.DoubleValue LOCK_ON_ALPHA;
    public static final ModConfigSpec.BooleanValue LOCK_ON_SIZE_SCALING;

    public static final ModConfigSpec.DoubleValue MAX_LOCK_ON_DISTANCE;
    public static final ModConfigSpec.DoubleValue MAX_TARGET_SELECT_DISTANCE;
    public static final ModConfigSpec.DoubleValue MAX_PITCH;
    public static final ModConfigSpec.DoubleValue MIN_PITCH;
    public static final ModConfigSpec.DoubleValue PITCH_OFFSET;

    public static final ModConfigSpec.BooleanValue ENABLE_DYNAMIC_CAMERA;
    public static final ModConfigSpec.DoubleValue MAX_DYNAMIC_CAMERA_Y;
    public static final ModConfigSpec.DoubleValue MAX_DYNAMIC_CAMERA_X;
    public static final ModConfigSpec.BooleanValue ENABLE_DYNAMIC_FOV;
    public static final ModConfigSpec.DoubleValue MAX_FOV_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Lock-On Icon Settings");

        LOCK_ON_ICON_TYPES = builder.define("lockOnIconTypes", "DefaultType");

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
                .comment("Max camera pitch when you lock on the target")
                .defineInRange("maxPithWhenLockOn", -30D, -90.0D, 90D);

        MIN_PITCH = builder
                .comment("Min camera pitch when you lock on the target")
                .defineInRange("minPithWhenLockOn", 40.0D, -90D, 90D);

        PITCH_OFFSET = builder
                .comment("Pitch offset when you lock on the target")
                .defineInRange("pitchOffsetWhenLockOn", 15D, -90.0D, 90D);

        builder.pop();

        builder.push("Lock-On Dynamic camera Settings");

        ENABLE_DYNAMIC_CAMERA = builder
                .comment("Enable the dynamic camera")
                .define("enableDynamicCamera", true);

        MAX_DYNAMIC_CAMERA_Y = builder
                .comment("Max height of the camera offset")
                .defineInRange("maxDynamicCameraY", 2.5D, 0.0D, 15D);

        MAX_DYNAMIC_CAMERA_X = builder
                .comment("Max horizontal distance of the camera offset")
                .defineInRange("maxDynamicCameraX", 7.5D, 0.0D, 15D);

        ENABLE_DYNAMIC_FOV = builder
                .comment("Enable the dynamic fov")
                .define("enableDynamicFov", false);

        MAX_FOV_MULTIPLIER = builder
                .comment("Max dynamic fov multiplier")
                .defineInRange("maxFovMultiplier", 1.15D, 1D, 2D);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
