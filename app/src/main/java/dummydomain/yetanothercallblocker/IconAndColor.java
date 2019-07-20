package dummydomain.yetanothercallblocker;

class IconAndColor {
    private int iconResId;
    private int colorResId;

    public IconAndColor(int icon, int color) {
        this.iconResId = icon;
        this.colorResId = color;
    }

    static IconAndColor of(int icon, int color) {
        return new IconAndColor(icon, color);
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getColorResId() {
        return colorResId;
    }
}
