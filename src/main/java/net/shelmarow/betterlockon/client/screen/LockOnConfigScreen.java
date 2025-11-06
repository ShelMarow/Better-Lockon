package net.shelmarow.betterlockon.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.shelmarow.betterlockon.client.render.icon.IconTypeManager;
import net.shelmarow.betterlockon.client.render.icon.type.IconType;
import net.shelmarow.betterlockon.config.LockOnConfig;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LockOnConfigScreen extends Screen {
    private final Screen parent;
    private ContainerOptionList optionList;

    private EditBox lockOnIconSize, lockOnChangeDistance, lockOnMinMouseSpeed, maxLockOnDistance,maxTargetSelectDistance,
            minLockOnPitch,pitchOffset, maxSoftAngleX,maxSoftAngleY,changeDistanceMultiply,changeSpeedMultiply;

    private ForgeSlider lockOnIconColorRed, lockOnIconColorGreen, lockOnIconColorBlue, lockOnIconAlpha;

    public LockOnConfigScreen(Minecraft minecraft, Screen screen) {
        super(Component.translatable("screen.betterlockon.config"));
        this.parent = screen;
    }

    @Override
    public void init() {
        this.clearWidgets();

        String on = "screen.betterlockon.config.on";
        String off = "screen.betterlockon.config.off";

        this.optionList = new ContainerOptionList(this.minecraft, this.width, this.height, 40, this.height - 40, 30);

        String iconType = LockOnConfig.LOCK_ON_ICON_TYPES.get();
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.icon_type")).addButton(
                Button.builder(Component.literal(iconType),b->{
                    IconType next = IconTypeManager.getNextIconType();
                    LockOnConfig.LOCK_ON_ICON_TYPES.set(next.getName());
                    LockOnConfig.LOCK_ON_ICON_TYPES.save();
                    b.setMessage(Component.literal(next.getName()));
                }).bounds(0,0,100,20).build()
        ));

        lockOnIconSize = addEditBox(String.valueOf(LockOnConfig.LOCK_ON_ICON_SIZE.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_size")).addEditBox(lockOnIconSize));

        lockOnIconColorRed = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,1,LockOnConfig.LOCK_ON_RED.get(), 0.01,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_color_red")).addSlider(lockOnIconColorRed));

        lockOnIconColorGreen = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,1,LockOnConfig.LOCK_ON_GREEN.get(), 0.01,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_color_green")).addSlider(lockOnIconColorGreen));

        lockOnIconColorBlue = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,1,LockOnConfig.LOCK_ON_BLUE.get(), 0.01,0,true);
        optionList.addEntry((new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_color_blue")).addSlider(lockOnIconColorBlue)));

        lockOnIconAlpha = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,1,LockOnConfig.LOCK_ON_ALPHA.get(), 0.01,0,true);
        optionList.addEntry((new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_alpha")).addSlider(lockOnIconAlpha)));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_size_scale")).addButton(
                Button.builder(Component.translatable(LockOnConfig.LOCK_ON_SIZE_SCALING.get() ? on : off),b->{
                    LockOnConfig.LOCK_ON_SIZE_SCALING.set(!LockOnConfig.LOCK_ON_SIZE_SCALING.get());
                    LockOnConfig.LOCK_ON_SIZE_SCALING.save();
                    b.setMessage(Component.translatable(LockOnConfig.LOCK_ON_SIZE_SCALING.get() ? on : off));
                }).bounds(0,0,100,20).build()
        ));

        lockOnChangeDistance = addEditBox(String.valueOf(LockOnConfig.LOCK_ON_CHANGE_DISTANCE.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_change_distance")).addEditBox(lockOnChangeDistance));

        lockOnMinMouseSpeed = addEditBox(String.valueOf(LockOnConfig.LOCK_ON_MIN_MOUSE_SPEED.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_change_speed")).addEditBox(lockOnMinMouseSpeed));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.target_auto_switch")).addButton(
                Button.builder(Component.translatable(LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.get() ? on : off), (b) -> {
                    LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.set(!LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.get());
                    LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.save();
                    b.setMessage(Component.translatable(LockOnConfig.AUTO_SWITCH_TARGET_WHEN_DIE.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        maxLockOnDistance = addEditBox(String.valueOf(LockOnConfig.MAX_LOCK_ON_DISTANCE.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_lock_on_distance")).addEditBox(maxLockOnDistance));

        maxTargetSelectDistance = addEditBox(String.valueOf(LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_target_select_distance")).addEditBox(maxTargetSelectDistance));

        minLockOnPitch = addEditBox(String.valueOf(LockOnConfig.MIN_PITCH_WHEN_LOCK_ON.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.min_lock_on_pitch")).addEditBox(minLockOnPitch));

        pitchOffset = addEditBox(String.valueOf(LockOnConfig.PITCH_OFFSET_WHEN_LOCK_ON.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.pitch_offset")).addEditBox(pitchOffset));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.enable_soft_lock")).addButton(
                Button.builder(Component.translatable(LockOnConfig.ENABLE_SOFT_LOCK.get() ? on : off),b->{
                    LockOnConfig.ENABLE_SOFT_LOCK.set(!LockOnConfig.ENABLE_SOFT_LOCK.get());
                    LockOnConfig.ENABLE_SOFT_LOCK.save();
                    b.setMessage(Component.translatable(LockOnConfig.ENABLE_SOFT_LOCK.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        maxSoftAngleX = addEditBox(String.valueOf(LockOnConfig.MAX_SOFT_ANGLE_X.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_soft_angle_x")).addEditBox(maxSoftAngleX));

        maxSoftAngleY = addEditBox(String.valueOf(LockOnConfig.MAX_SOFT_ANGLE_Y.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_soft_angle_y")).addEditBox(maxSoftAngleY));

        changeDistanceMultiply = addEditBox(String.valueOf(LockOnConfig.CHANGE_DISTANCE_MULTIPLY.get()));
        optionList.addEntry(new  ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.change_distance_multiply")).addEditBox(changeDistanceMultiply));

        changeSpeedMultiply = addEditBox(String.valueOf(LockOnConfig.CHANGE_SPEED_MULTIPLY.get()));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.change_speed_multiply")).addEditBox(changeSpeedMultiply));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.fix_wom_lockon")).addButton(
                Button.builder(Component.translatable(LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get() ? on : off), (b) -> {
                    LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.set(!LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get());
                    LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.save();
                    b.setMessage(Component.translatable(LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.white_list")));
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.black_list")));

        this.addWidget(this.optionList);

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.betterlockon.config.confirm"),b->{
                    applyChange();
                    onClose();
                }).bounds(width / 2 + 60,height - 30, 100, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.betterlockon.config.cancel"),b->{
                    onClose();
                }).bounds(width / 2 - 160,height - 30, 100, 20).build()
        );
    }

    @Override
    public void render(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.optionList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    public EditBox addEditBox(String value) {
        EditBox editBox = new EditBox(font,0,0,100,20,Component.empty());
        editBox.setFilter(s -> s.matches("\\d*(\\.\\d*)?"));
        editBox.setValue(value);
        return editBox;
    }

    public void applyChange(){
        LockOnConfig.LOCK_ON_ICON_SIZE.set(Double.parseDouble(lockOnIconSize.getValue()));
        LockOnConfig.LOCK_ON_RED.set(lockOnIconColorRed.getValue());
        LockOnConfig.LOCK_ON_GREEN.set(lockOnIconColorGreen.getValue());
        LockOnConfig.LOCK_ON_BLUE.set(lockOnIconColorBlue.getValue());
        LockOnConfig.LOCK_ON_ALPHA.set(lockOnIconAlpha.getValue());

        LockOnConfig.LOCK_ON_CHANGE_DISTANCE.set(Double.parseDouble(lockOnChangeDistance.getValue()));
        LockOnConfig.LOCK_ON_MIN_MOUSE_SPEED.set(Double.parseDouble(lockOnMinMouseSpeed.getValue()));

        LockOnConfig.MAX_LOCK_ON_DISTANCE.set(Double.parseDouble(maxLockOnDistance.getValue()));
        LockOnConfig.MAX_TARGET_SELECT_DISTANCE.set(Double.parseDouble(maxTargetSelectDistance.getValue()));
        LockOnConfig.MIN_PITCH_WHEN_LOCK_ON.set(Double.parseDouble(minLockOnPitch.getValue()));
        LockOnConfig.PITCH_OFFSET_WHEN_LOCK_ON.set(Double.parseDouble(pitchOffset.getValue()));

        LockOnConfig.MAX_SOFT_ANGLE_X.set(Double.parseDouble(maxSoftAngleX.getValue()));
        LockOnConfig.MAX_SOFT_ANGLE_Y.set(Double.parseDouble(maxSoftAngleY.getValue()));
        LockOnConfig.CHANGE_DISTANCE_MULTIPLY.set(Double.parseDouble(changeDistanceMultiply.getValue()));
        LockOnConfig.CHANGE_SPEED_MULTIPLY.set(Double.parseDouble(changeSpeedMultiply.getValue()));

        LockOnConfig.CLIENT_CONFIG.save();
    }

    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
