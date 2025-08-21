package lsplus.main.util;

public class Rect {
    public int x;
    public int y;
    public int width;
    public int height;
    public int vaultNumber;

    public Rect(int x, int y, int width, int height, int vaultNumber) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vaultNumber = vaultNumber;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}