package me.wolfii.advancementsscreenoptimizer;

import net.minecraft.util.Identifier;

public interface DrawContextBatchTextureDrawer {
    default void advancementsScreenOptimizer$startBatchRender(Identifier texture) {

    }

    default void advancementsScreenOptimizer$batchRender(int x1, int x2, int y1, int y2, float u1, float u2, float v1, float v2) {

    }

    default void advancementsScreenOptimizer$finishBatchRender() {

    }
}
