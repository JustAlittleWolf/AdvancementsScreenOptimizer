package me.wolfii.advancementsscreenoptimizer;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface AdvancementWidgetDrawCheck {
    default boolean advancementsScreenOptimizer$shouldDrawLine(int x, int y, int pageWidth, int pageHeight) {
        return true;
    }

    default boolean advancementsScreenOptimizer$shouldDrawWidget(int x, int y, int pageWidth, int pageHeight) {
        return true;
    }
}
