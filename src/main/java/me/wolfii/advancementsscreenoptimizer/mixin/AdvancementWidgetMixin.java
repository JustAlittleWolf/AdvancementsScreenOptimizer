package me.wolfii.advancementsscreenoptimizer.mixin;

import me.wolfii.advancementsscreenoptimizer.AdvancementWidgetDrawCheck;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin implements AdvancementWidgetDrawCheck {
    @Shadow
    @Nullable
    private AdvancementWidget parent;

    @Shadow
    public abstract int getX();

    @Shadow
    public abstract int getY();

    @Unique
    private static final int WIDGET_MARGIN = 30;


    @Redirect(method = "renderLines", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<AdvancementWidget> stopRecursiveRenderLines(List<AdvancementWidget> instance) {
        return Collections.emptyIterator();
    }

    @Redirect(method = "renderWidgets", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<AdvancementWidget> stopRecursiveRenderWidgets(List<AdvancementWidget> instance) {
        return Collections.emptyIterator();
    }

    @Unique
    @Override
    public boolean advancementsScreenOptimizer$shouldDrawLine(int x, int y, int pageWidth, int pageHeight) {
        byte currentWidgetCohenSutherland = this.getCohenSutherlandCode(this.getX() + x, this.getY() + y, pageWidth, pageHeight);
        if (this.parent == null) return false;
        byte parentWidgetCohenSutherland = this.getCohenSutherlandCode(this.parent.getX() + x, this.parent.getY() + y, pageWidth, pageHeight);
        if ((currentWidgetCohenSutherland | parentWidgetCohenSutherland) == 0b0000) return true;
        return (currentWidgetCohenSutherland & parentWidgetCohenSutherland) == 0b0000;
    }

    @Unique
    @Override
    public boolean advancementsScreenOptimizer$shouldDrawWidget(int x, int y, int pageWidth, int pageHeight) {
        int pageX = this.getX() + x;
        int pageY = this.getY() + y;
        if (pageX > pageWidth) return false;
        if (pageY > pageHeight) return false;
        return pageX >= -WIDGET_MARGIN && pageY >= -WIDGET_MARGIN;
    }

    @Unique
    private byte getCohenSutherlandCode(int pageX, int pageY, int pageWidth, int pageHeight) {
        byte cohenSutherlandCode = 0b0000;
        if (pageX < -WIDGET_MARGIN) cohenSutherlandCode |= 0b0001;
        if (pageX > pageWidth) cohenSutherlandCode |= 0b0010;
        if (pageY > pageHeight) cohenSutherlandCode |= 0b0100;
        if (pageY < -WIDGET_MARGIN) cohenSutherlandCode |= 0b1000;
        return cohenSutherlandCode;
    }
}
