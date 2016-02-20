package de.ur.ahci.machine_learning;

/**
 * Face "Landmark" such as nose, left eye etc. basically just a model class for storing coordinates.
 */
public class FaceLandmark {

    private float x;
    private float y;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
