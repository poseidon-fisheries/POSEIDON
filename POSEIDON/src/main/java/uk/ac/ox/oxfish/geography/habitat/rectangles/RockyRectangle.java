package uk.ac.ox.oxfish.geography.habitat.rectangles;

class RockyRectangle {

    private final int topLeftX;
    private final int topLeftY;
    private final int width;
    private final int height;


    public RockyRectangle(final int topLeftX, final int topLeftY, final int width, final int height) {
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.width = width;
        this.height = height;
    }

    public int getTopLeftX() {
        return topLeftX;
    }

    public int getTopLeftY() {
        return topLeftY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
