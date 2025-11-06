package net.shelmarow.betterlockon.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ContainerOptionList extends ContainerObjectSelectionList<ContainerOptionList.Entry> {

    public ContainerOptionList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight) {
        super(pMinecraft, pWidth, pHeight, pY0, pY1, pItemHeight);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 40;
    }

    @Override
    public int addEntry(@NotNull Entry pEntry) {
        return super.addEntry(pEntry);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final Component label;
        private Button button = null;
        private EditBox editBox = null;
        private ForgeSlider slider = null;

        public Entry(Component label) {
            this.label = label;
        }

        public Entry addButton(Button button) {
            this.button = button;
            return this;
        }

        public Entry addSlider(ForgeSlider slider) {
            this.slider = slider;
            return this;
        }

        public Entry addEditBox(EditBox editBox) {
            this.editBox = editBox;
            return this;
        }


        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {

            int posX = (2*left + width);
            Font font = Minecraft.getInstance().font;
            graphics.drawString(font, label, left, top + 12, 0xFFFFFF);
            if(this.button != null) {
                button.setX(posX/2 + 20);
                button.setY(top + 8);
                button.render(graphics, mouseX, mouseY, partialTick);
            }
            else if(this.slider != null) {
                slider.setX(posX/2 + 20);
                slider.setY(top + 8);
                slider.render(graphics, mouseX, mouseY, partialTick);
            }
            else if(this.editBox != null) {
                editBox.setX(posX/2 + 20);
                editBox.setY(top + 8);
                editBox.render(graphics, mouseX, mouseY, partialTick);
            }

        }



        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            if(button != null) {
                return List.of(button);
            }
            else if(slider != null) {
                return List.of(slider);
            }
            else if(editBox != null) {
                return List.of(editBox);
            }
            return Collections.emptyList();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            if(button != null) {
                return List.of(button);
            }
            else if(slider != null) {
                return List.of(slider);
            }
            else if(editBox != null) {
                return List.of(editBox);
            }
            return Collections.emptyList();
        }
    }
}
