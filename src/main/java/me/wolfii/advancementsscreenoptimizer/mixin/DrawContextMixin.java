package me.wolfii.advancementsscreenoptimizer.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.wolfii.advancementsscreenoptimizer.DrawContextBatchTextureDrawer;
import me.wolfii.advancementsscreenoptimizer.DrawContextBlocker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin implements DrawContextBlocker, DrawContextBatchTextureDrawer {
    @Shadow
    @Final
    private MatrixStack matrices;

    @Shadow
    @Deprecated
    protected abstract void tryDraw();

    @Shadow
    public abstract void draw();

    @Unique
    private boolean isDrawingBlocked = false;

    @Unique
    private BufferBuilder batchBufferBuilder;

    @Unique
    @Override
    public void advancementsScreenOptimizer$blockDrawing() {
        this.isDrawingBlocked = true;
    }

    @Unique
    @Override
    public void advancementsScreenOptimizer$unblockDrawingAndDraw() {
        this.isDrawingBlocked = false;
        this.draw();
    }

    @Inject(method = "draw()V", at = @At("HEAD"), cancellable = true)
    public void cancelDraw(CallbackInfo ci) {
        if (this.isDrawingBlocked) ci.cancel();
    }

    @Inject(method = "tryDraw", at = @At("HEAD"), cancellable = true)
    public void cancelTryDraw(CallbackInfo ci) {
        if (this.isDrawingBlocked) ci.cancel();
    }

    @Unique
    @Override
    public void advancementsScreenOptimizer$startBatchRender(Identifier texture) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        this.batchBufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
    }

    @Unique
    @Override
    public void advancementsScreenOptimizer$finishBatchRender() {
        BufferRenderer.drawWithGlobalProgram(this.batchBufferBuilder.end());
        this.batchBufferBuilder = null;
    }

    @Unique
    public void advancementsScreenOptimizer$batchRender(int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2) {
        Matrix4f matrix4f = this.matrices.peek().getPositionMatrix();
        this.batchBufferBuilder.vertex(matrix4f, (float) x1, (float) y1, 0.0f).texture(u1, v1);
        this.batchBufferBuilder.vertex(matrix4f, (float) x1, (float) y2, 0.0f).texture(u1, v2);
        this.batchBufferBuilder.vertex(matrix4f, (float) x2, (float) y2, 0.0f).texture(u2, v2);
        this.batchBufferBuilder.vertex(matrix4f, (float) x2, (float) y1, 0.0f).texture(u2, v1);
    }
}
