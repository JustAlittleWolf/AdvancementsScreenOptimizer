package me.wolfii.advancementsscreenoptimizer;

public interface DrawContextBlocker {
    default void advancementsScreenOptimizer$blockDrawing() {
    }

    default void advancementsScreenOptimizer$unblockDrawingAndDraw() {
    }
}
