package net.shelmarow.betterlockon.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.shelmarow.betterlockon.client.render.type.LockOnIconTypes;

public class LockOnConfig {
    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.EnumValue<LockOnIconTypes> LOCK_ON_ICON_TYPES;

    public static final ModConfigSpec.DoubleValue LOCK_ON_CHANGE_DISTANCE;
    public static final ModConfigSpec.DoubleValue LOCK_ON_MIN_MOUSE_SPEED;

    public static final ModConfigSpec.BooleanValue AUTO_SWITCH_TARGET_WHEN_DIE;

    public static final ModConfigSpec.DoubleValue MAX_LOCK_ON_DISTANCE;
    public static final ModConfigSpec.DoubleValue MAX_TARGET_SELECT_DISTANCE;
    public static final ModConfigSpec.DoubleValue MIN_PITCH_WHEN_LOCK_ON;
    public static final ModConfigSpec.DoubleValue PITCH_OFFSET_WHEN_LOCK_ON;

    public static final ModConfigSpec.BooleanValue ENABLE_SOFT_LOCK;
    public static final ModConfigSpec.DoubleValue MAX_SOFT_ANGLE_X;
    public static final ModConfigSpec.DoubleValue MAX_SOFT_ANGLE_Y;
    public static final ModConfigSpec.DoubleValue CHANGE_DISTANCE_MULTIPLY;
    public static final ModConfigSpec.DoubleValue CHANGE_SPEED_MULTIPLY;

    public static final ModConfigSpec.DoubleValue LOCK_ON_ICON_SIZE;
    public static final ModConfigSpec.DoubleValue LOCK_ON_RED;
    public static final ModConfigSpec.DoubleValue LOCK_ON_GREEN;
    public static final ModConfigSpec.DoubleValue LOCK_ON_BLUE;
    public static final ModConfigSpec.DoubleValue LOCK_ON_ALPHA;
    public static final ModConfigSpec.BooleanValue LOCK_ON_SIZE_SCALING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Lock-On Icon Type");

        LOCK_ON_ICON_TYPES = builder
                .defineEnum("lockOnIconTypes",LockOnIconTypes.DEFAULT);

        builder.pop();

        builder.push("Lock-On Change Settings");

        LOCK_ON_CHANGE_DISTANCE = builder
                .comment("Lock-on will only change target when mouse move more than this distance")
                        .defineInRange("lockOnChangeDistance", 600.0D, 10.0D, Double.MAX_VALUE);

        LOCK_ON_MIN_MOUSE_SPEED = builder
                .comment("Lock-on will only change target when mouse move fast than this speed")
                .defineInRange("lockOnMinMouseSpeed", 20.0D, 0.0D, Double.MAX_VALUE);

        AUTO_SWITCH_TARGET_WHEN_DIE = builder
                .comment("Auto switch your target when the main target dies")
                .define("autoSwitchTargetWhenDie",true);

        MAX_LOCK_ON_DISTANCE = builder
                .comment("The farthest distance that can keep lock on to the target")
                        .defineInRange("maxLockOnDistance", 30.0D, 0.0D, 128D);

        MAX_TARGET_SELECT_DISTANCE = builder
                .comment("The farthest distance you can quickly lock on to the target")
                        .defineInRange("maxTargetSelectDistance", 20.0D, 0.0D, 128D);

        MIN_PITCH_WHEN_LOCK_ON = builder
                .comment("Min camera pith when you lock on the target")
                        .defineInRange("minPitthWhenLockOn", 30.0D, -45.0D, 75D);

        PITCH_OFFSET_WHEN_LOCK_ON = builder
                .comment("Pitch offset when you lock on the target")
                        .defineInRange("pitchOffsetWhenLockOn", 0.0D, -90.0D, 90D);

        builder.pop();

        builder.push("Soft Lock Settings");

        ENABLE_SOFT_LOCK = builder
                .comment("Enable soft lock")
                        .define("enableSoftLock",false);

        MAX_SOFT_ANGLE_X = builder
                .comment("Max soft angle x")
                        .defineInRange("maxSoftAngleX",60D,0D,120D);

        MAX_SOFT_ANGLE_Y = builder
                .comment("Max soft angle y" +
                        "Notice: pitch will be limited by MIN_PITCH_WHEN_LOCK_ON config")
                .defineInRange("maxSoftAngleY",30D,0D,120D);

        CHANGE_DISTANCE_MULTIPLY = builder
                .comment("change distance multiply")
                        .defineInRange("changeDistanceMultiply",2D,0.0D,20D);

        CHANGE_SPEED_MULTIPLY = builder
                .comment("Change speed multiply")
                        .defineInRange("changeSpeedMultiply",4D,0.0D,20D);

        builder.pop();

        builder.push("Lock-On UI Settings");

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
        CLIENT_CONFIG = builder.build();
    }
}
