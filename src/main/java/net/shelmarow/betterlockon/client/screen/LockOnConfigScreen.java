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

    private ForgeSlider lockOnIconSize, maxLockOnDistance,maxTargetSelectDistance,
            minLockOnPitch, maxLockOnPitch, pitchOffset, rotationTransition, lockOnIconColorRed,
            lockOnIconColorGreen, lockOnIconColorBlue, lockOnIconAlpha,
            maxDynamicCameraY, maxDynamicCameraX, maxDynamicFov, firstPersonSwitch,
            startTransparency, fullyTransparency;

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


        lockOnIconSize = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(), 0, 5, LockOnConfig.LOCK_ON_ICON_SIZE.get(),0.01,0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.lockon_icon_size")).addSlider(lockOnIconSize));


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


        maxLockOnDistance = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,128, LockOnConfig.MAX_LOCK_ON_DISTANCE.get(), 1,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_lock_on_distance")).addSlider(maxLockOnDistance));


        maxTargetSelectDistance = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),0,128, LockOnConfig.MAX_TARGET_SELECT_DISTANCE.get(), 1,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_target_select_distance")).addSlider(maxTargetSelectDistance));


        minLockOnPitch = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),-90,90, LockOnConfig.MAX_PITCH.get(), 1,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_lock_on_pitch")).addSlider(minLockOnPitch));


        maxLockOnPitch = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),-90,90, LockOnConfig.MIN_PITCH.get(), 1,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.min_lock_on_pitch")).addSlider(maxLockOnPitch));


        pitchOffset = new ForgeSlider(0,0,100,20,
                Component.empty(),Component.empty(),-90,90, LockOnConfig.PITCH_OFFSET.get(), 1,0,true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.pitch_offset")).addSlider(pitchOffset));

        rotationTransition = new ForgeSlider(0,0,100,20,
                Component.empty(), Component.empty(), 0.1, 1, LockOnConfig.ROTATION_TRANSITION.get(), 0.01,0,true);
        optionList.addEntry((new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.rotation_transition")).addSlider(rotationTransition)));


        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.enable_dynamic_camera")).addButton(
                Button.builder(Component.translatable(LockOnConfig.ENABLE_DYNAMIC_CAMERA.get() ? on : off), (b) -> {
                    LockOnConfig.ENABLE_DYNAMIC_CAMERA.set(!LockOnConfig.ENABLE_DYNAMIC_CAMERA.get());
                    LockOnConfig.ENABLE_DYNAMIC_CAMERA.save();
                    b.setMessage(Component.translatable(LockOnConfig.ENABLE_DYNAMIC_CAMERA.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        maxDynamicCameraY = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 0, 15, LockOnConfig.MAX_DYNAMIC_CAMERA_Y.get(), 0.25, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_dynamic_camera_y")).addSlider(maxDynamicCameraY));

        maxDynamicCameraX = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 0, 15, LockOnConfig.MAX_DYNAMIC_CAMERA_X.get(), 0.25, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_dynamic_camera_x")).addSlider(maxDynamicCameraX));


        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.enable_dynamic_fov")).addButton(
                Button.builder(Component.translatable(LockOnConfig.ENABLE_DYNAMIC_FOV.get() ? on : off), (b) -> {
                    LockOnConfig.ENABLE_DYNAMIC_FOV.set(!LockOnConfig.ENABLE_DYNAMIC_FOV.get());
                    LockOnConfig.ENABLE_DYNAMIC_FOV.save();
                    b.setMessage(Component.translatable(LockOnConfig.ENABLE_DYNAMIC_FOV.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        maxDynamicFov = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 1D, 2D, LockOnConfig.MAX_FOV_MULTIPLIER.get(), 0.01D, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.max_dynamic_fov")).addSlider(maxDynamicFov));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.fix_wom_lockon")).addButton(
                Button.builder(Component.translatable(LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get() ? on : off), (b) -> {
                    LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.set(!LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get());
                    LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.save();
                    b.setMessage(Component.translatable(LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.enable_model_transparency")).addButton(
                Button.builder(Component.translatable(LockOnConfig.ENABLE_MODEL_TRANSPARENCY.get() ? on : off), b->{
                    LockOnConfig.ENABLE_MODEL_TRANSPARENCY.set(!LockOnConfig.ENABLE_MODEL_TRANSPARENCY.get());
                    LockOnConfig.ENABLE_MODEL_TRANSPARENCY.save();
                    b.setMessage(Component.translatable(LockOnConfig.ENABLE_MODEL_TRANSPARENCY.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        startTransparency = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 0D, 15D, LockOnConfig.START_TRANSPARENCY_DISTANCE.get(), 0.1D, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.start_transparency_distance")).addSlider(startTransparency));

        fullyTransparency = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 0D, 15D, LockOnConfig.FULLY_TRANSPARENCY_DISTANCE.get(), 0.1D, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.fully_transparency_distance")).addSlider(fullyTransparency));

        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.auto_switch_first_person")).addButton(
                Button.builder(Component.translatable(LockOnConfig.AUTO_SWITCH_FIRST_PERSON.get() ? on : off), b->{
                    LockOnConfig.AUTO_SWITCH_FIRST_PERSON.set(!LockOnConfig.AUTO_SWITCH_FIRST_PERSON.get());
                    LockOnConfig.AUTO_SWITCH_FIRST_PERSON.save();
                    b.setMessage(Component.translatable(LockOnConfig.AUTO_SWITCH_FIRST_PERSON.get() ? on : off));
                }).bounds(0,0, 100, 20).build()
        ));

        firstPersonSwitch = new ForgeSlider(0, 0, 100, 20,
                Component.empty(), Component.empty(), 0.1, 10, LockOnConfig.AUTO_SWITCH_DISTANCE.get(), 0.1, 0, true);
        optionList.addEntry(new ContainerOptionList.Entry(Component.translatable("screen.betterlockon.config.auto_switch_first_person_distance")).addSlider(firstPersonSwitch));

        this.addWidget(this.optionList);

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.betterlockon.config.confirm"),b->{
                    applyChange();
                    //onClose();
                }).bounds(width / 2 + 60,height - 30, 100, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.translatable("screen.betterlockon.config.reset"),b->{
                    resetChange();
                }).bounds(width / 2 - 50,height - 30, 100, 20).build()
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

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    public EditBox addEditBox(String value) {
        EditBox editBox = new EditBox(font,0,0,100,20,Component.empty());
        editBox.setFilter(s -> s.matches("\\d*(\\.\\d*)?"));
        editBox.setValue(value);
        return editBox;
    }

    public void applyChange(){
        LockOnConfig.LOCK_ON_ICON_SIZE.set(lockOnIconSize.getValue());
        LockOnConfig.LOCK_ON_RED.set(lockOnIconColorRed.getValue());
        LockOnConfig.LOCK_ON_GREEN.set(lockOnIconColorGreen.getValue());
        LockOnConfig.LOCK_ON_BLUE.set(lockOnIconColorBlue.getValue());
        LockOnConfig.LOCK_ON_ALPHA.set(lockOnIconAlpha.getValue());


        double maxLockOnDistance = this.maxLockOnDistance.getValue();
        double maxTargetSelectDistance = this.maxTargetSelectDistance.getValue();
        if(maxTargetSelectDistance > maxLockOnDistance){
            maxLockOnDistance = maxTargetSelectDistance;
        }
        LockOnConfig.MAX_LOCK_ON_DISTANCE.set(maxLockOnDistance);
        LockOnConfig.MAX_TARGET_SELECT_DISTANCE.set(maxTargetSelectDistance);

        double minLockOnPitchValue = minLockOnPitch.getValue();
        double maxLockOnPitchValue = maxLockOnPitch.getValue();
        if(minLockOnPitchValue > maxLockOnPitchValue){
            minLockOnPitchValue = maxLockOnPitchValue;
        }
        LockOnConfig.MAX_PITCH.set(minLockOnPitchValue);
        LockOnConfig.MIN_PITCH.set(maxLockOnPitchValue);

        LockOnConfig.PITCH_OFFSET.set(pitchOffset.getValue());
        LockOnConfig.ROTATION_TRANSITION.set(rotationTransition.getValue());

        LockOnConfig.MAX_DYNAMIC_CAMERA_Y.set(maxDynamicCameraY.getValue());
        LockOnConfig.MAX_DYNAMIC_CAMERA_X.set(maxDynamicCameraX.getValue());

        LockOnConfig.MAX_FOV_MULTIPLIER.set(maxDynamicFov.getValue());

        double startTransparency = this.startTransparency.getValue();
        double fullyTransparency = this.fullyTransparency.getValue();
        if(startTransparency < fullyTransparency){
            startTransparency = fullyTransparency;
        }
        LockOnConfig.START_TRANSPARENCY_DISTANCE.set(startTransparency);
        LockOnConfig.FULLY_TRANSPARENCY_DISTANCE.set(fullyTransparency);

        LockOnConfig.AUTO_SWITCH_DISTANCE.set(firstPersonSwitch.getValue());

        LockOnConfig.CLIENT_CONFIG.save();
    }



    private void resetChange() {
        LockOnConfig.LOCK_ON_ICON_TYPES.set(LockOnConfig.LOCK_ON_ICON_TYPES.getDefault());
        LockOnConfig.LOCK_ON_ICON_SIZE.set(LockOnConfig.LOCK_ON_ICON_SIZE.getDefault());
        LockOnConfig.LOCK_ON_RED.set(LockOnConfig.LOCK_ON_RED.getDefault());
        LockOnConfig.LOCK_ON_GREEN.set(LockOnConfig.LOCK_ON_GREEN.getDefault());
        LockOnConfig.LOCK_ON_BLUE.set(LockOnConfig.LOCK_ON_BLUE.getDefault());
        LockOnConfig.LOCK_ON_ALPHA.set(LockOnConfig.LOCK_ON_ALPHA.getDefault());
        LockOnConfig.LOCK_ON_SIZE_SCALING.set(LockOnConfig.LOCK_ON_SIZE_SCALING.getDefault());
        LockOnConfig.MAX_LOCK_ON_DISTANCE.set(LockOnConfig.MAX_LOCK_ON_DISTANCE.getDefault());
        LockOnConfig.MAX_TARGET_SELECT_DISTANCE.set(LockOnConfig.MAX_TARGET_SELECT_DISTANCE.getDefault());
        LockOnConfig.MAX_PITCH.set(LockOnConfig.MAX_PITCH.getDefault());
        LockOnConfig.MIN_PITCH.set(LockOnConfig.MIN_PITCH.getDefault());
        LockOnConfig.PITCH_OFFSET.set(LockOnConfig.PITCH_OFFSET.getDefault());
        LockOnConfig.ROTATION_TRANSITION.set(LockOnConfig.ROTATION_TRANSITION.getDefault());
        LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.set(LockOnConfig.FIX_WOM_ATTACK_LOCK_ON.getDefault());
        LockOnConfig.ENABLE_DYNAMIC_CAMERA.set(LockOnConfig.ENABLE_DYNAMIC_CAMERA.getDefault());
        LockOnConfig.MAX_DYNAMIC_CAMERA_Y.set(LockOnConfig.MAX_DYNAMIC_CAMERA_Y.getDefault());
        LockOnConfig.MAX_DYNAMIC_CAMERA_X.set(LockOnConfig.MAX_DYNAMIC_CAMERA_X.getDefault());
        LockOnConfig.ENABLE_DYNAMIC_FOV.set(LockOnConfig.ENABLE_DYNAMIC_FOV.getDefault());
        LockOnConfig.MAX_FOV_MULTIPLIER.set(LockOnConfig.MAX_FOV_MULTIPLIER.getDefault());
        LockOnConfig.ENABLE_MODEL_TRANSPARENCY.set(LockOnConfig.ENABLE_MODEL_TRANSPARENCY.getDefault());
        LockOnConfig.START_TRANSPARENCY_DISTANCE.set(LockOnConfig.START_TRANSPARENCY_DISTANCE.getDefault());
        LockOnConfig.FULLY_TRANSPARENCY_DISTANCE.set(LockOnConfig.FULLY_TRANSPARENCY_DISTANCE.getDefault());
        LockOnConfig.AUTO_SWITCH_FIRST_PERSON.set(LockOnConfig.AUTO_SWITCH_FIRST_PERSON.getDefault());
        LockOnConfig.AUTO_SWITCH_DISTANCE.set(LockOnConfig.AUTO_SWITCH_DISTANCE.getDefault());
        init();
    }
}
