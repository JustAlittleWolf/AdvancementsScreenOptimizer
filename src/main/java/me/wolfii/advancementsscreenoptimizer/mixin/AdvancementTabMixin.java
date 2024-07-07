package me.wolfii.advancementsscreenoptimizer.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

@Mixin(AdvancementTab.class)
public abstract class AdvancementTabMixin {
    @Unique
    private final List<AdvancementWidget> advancementWidgets = new ArrayList<>();
    @Unique
    private boolean needsUpdate = true;

    @Unique
    public int pageWidth = AdvancementsScreen.PAGE_WIDTH;
    @Unique
    public int pageHeight = AdvancementsScreen.PAGE_HEIGHT;

    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/math/MathHelper;floor(D)I", ordinal = 1))
    private void initBatchRendering(DrawContext context, int x, int y, CallbackInfo ci, @Local Identifier identifier) {
        context.advancementsScreenOptimizer$startBatchRender(identifier);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V"))
    private void batchRender(DrawContext instance, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        instance.advancementsScreenOptimizer$batchRender(x, x + width, y, y + height, (u + 0.0F) / (float) textureWidth, (u + (float) width) / (float) textureWidth, (v + 0.0F) / (float) textureHeight, (v + (float) height) / (float) textureHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderLines(Lnet/minecraft/client/gui/DrawContext;IIZ)V", ordinal = 0))
    private void finishBatchRendering(DrawContext context, int x, int y, CallbackInfo ci) {
        context.advancementsScreenOptimizer$finishBatchRender();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;enableScissor(IIII)V"))
    private void captureMaxPage(DrawContext instance, int x1, int y1, int x2, int y2) {
        this.pageWidth = x2 - x1;
        this.pageHeight = y2 - y1;
        instance.enableScissor(x1, y1, x2, y2);
    }


    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderWidgets(Lnet/minecraft/client/gui/DrawContext;II)V"))
    private void fasterRender(AdvancementWidget instance, DrawContext context, int x, int y) {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            this.advancementWidgets.clear();
            Stack<Iterator<AdvancementWidget>> stack = new Stack<>();
            stack.push(((AdvancementWidgetAccessor) instance).getChildren().iterator());
            this.advancementWidgets.add(instance);
            while (!stack.isEmpty()) {
                if (!stack.peek().hasNext()) {
                    stack.pop();
                    continue;
                }
                AdvancementWidget widget = stack.peek().next();
                this.advancementWidgets.add(widget);
                stack.push(((AdvancementWidgetAccessor) widget).getChildren().iterator());
            }
        }

        context.advancementsScreenOptimizer$blockDrawing();
        for (AdvancementWidget widget : this.advancementWidgets) {
            if (!widget.advancementsScreenOptimizer$shouldDrawLine(x, y, this.pageWidth, this.pageHeight)) continue;
            widget.renderLines(context, x, y, true);
        }
        context.advancementsScreenOptimizer$unblockDrawingAndDraw();

        context.advancementsScreenOptimizer$blockDrawing();
        for (AdvancementWidget widget : this.advancementWidgets) {
            if (!widget.advancementsScreenOptimizer$shouldDrawLine(x, y, this.pageWidth, this.pageHeight)) continue;
            widget.renderLines(context, x, y, false);
        }
        context.advancementsScreenOptimizer$unblockDrawingAndDraw();

        // Applying batching here makes item models darker
        for (AdvancementWidget widget : this.advancementWidgets) {
            if (!widget.advancementsScreenOptimizer$shouldDrawWidget(x, y, this.pageWidth, this.pageHeight)) continue;
            widget.renderWidgets(context, x, y);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;renderLines(Lnet/minecraft/client/gui/DrawContext;IIZ)V"))
    private void cancelRenderLines(AdvancementWidget instance, DrawContext context, int x, int y, boolean border) {

    }

    @Inject(method = "addWidget", at = @At("HEAD"))
    private void refreshAdvancementWidgets(AdvancementWidget widget, AdvancementEntry advancement, CallbackInfo ci) {
        this.needsUpdate = true;
    }
}
