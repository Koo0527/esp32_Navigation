package com.example.motonav;

/** 没有路线离线包时，按可见范围和准备耗时提前更新普通底图。 */
public final class MapCoveragePolicy {
    private MapCoveragePolicy() {}

    public static double viewRadius(int kmh) {
        double scale = Math.max(0.38, Math.min(0.6, 0.85 - Math.max(0, kmh) * 0.006));
        return 241.0 / scale;
    }

    public static double aheadDistance(int kmh, boolean satellite) {
        if (satellite) {
            return kmh > 5 ? 180.0 : 0.0;
        }
        if (kmh <= 5) return 0.0;
        if (kmh >= 100) return 920.0;
        if (kmh >= 80) return 780.0;
        if (kmh >= 50) return 620.0;
        return 450.0;
    }

    public static boolean shouldRefresh(double dx, double dy, float bearing, int kmh,
                                        boolean satellite, double preparationSeconds) {
        return shouldRefresh(dx, dy, bearing, kmh, satellite, preparationSeconds, satellite ? 700 : 1200);
    }

    public static boolean shouldRefresh(double dx, double dy, float bearing, int kmh,
                                        boolean satellite, double preparationSeconds, double half) {
        // 普通图按最远缩放预留，减速时固件仍在此缓慢收回视野，不能立即按小视野计算。
        double radius = satellite ? viewRadius(kmh) : viewRadius(150);
        double limit = half - radius - (kmh >= 60 ? 120.0 : 60.0);
        double effPrep = Math.max(kmh >= 60 ? 30.0 : 1.0, Math.min(60.0, preparationSeconds));
        double distance = Math.max(0, kmh) / 3.6 * effPrep;
        double angle = Math.toRadians(bearing);
        return Math.max(Math.abs(dx), Math.abs(dy)) >= limit ||
            Math.max(Math.abs(dx + Math.sin(angle) * distance), Math.abs(dy + Math.cos(angle) * distance)) >= limit;
    }
}
