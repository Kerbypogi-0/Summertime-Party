package com.kerbcorp.catchthatpig;
import android.graphics.Canvas;
import android.graphics.Paint;
import java.util.List;

public class Player {
    private double positionX;
    private double positionY;
    private double radius;
    private Paint paint;

    private static final double MAX_SPEED = 12.0;
    private boolean isCarryingPig = false;

    private boolean isAI;
    private Cage homeCage;

    public Player(double positionX, double positionY, double radius, int color, boolean isAI, Cage homeCage) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.radius = radius;
        this.isAI = isAI;
        this.homeCage = homeCage;

        paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    public void draw(Canvas canvas) {
        canvas.drawCircle((float) positionX, (float) positionY, (float) radius, paint);
        if (isCarryingPig) {
            Paint pigPaint = new Paint();
            pigPaint.setColor(android.graphics.Color.rgb(255, 182, 193));
            canvas.drawCircle((float) positionX, (float) positionY, (float) radius / 2, pigPaint);
        }
    }

    public void update(com.crunch.catchthatpig.Joystick joystick, int screenWidth, int screenHeight, List<com.crunch.catchthatpig.Pig> pigs) {
        if (isAI) {
            // --- AI PATHFINDING LOGIC ---
            if (isCarryingPig) {
                // STATE 2: Carrying a pig -> Head directly to home cage!
                if (homeCage != null) {
                    moveToTarget(homeCage.getBounds().centerX(), homeCage.getBounds().centerY());
                }
            } else {
                // STATE 1: Not carrying a pig -> Search for the closest pig in the circle
                if (pigs != null) {
                    com.crunch.catchthatpig.Pig closestPig = null;
                    double shortestDistance = Double.MAX_VALUE;

                    for (com.crunch.catchthatpig.Pig pig : pigs) {
                        if (pig != null && !pig.isCaught) {
                            double dist = Math.hypot(positionX - pig.x, positionY - pig.y);
                            if (dist < shortestDistance) {
                                shortestDistance = dist;
                                closestPig = pig;
                            }
                        }
                    }

                    if (closestPig != null) {
                        moveToTarget(closestPig.x, closestPig.y);
                    }
                }
            }
        } else {
            // --- HUMAN JOYSTICK LOGIC ---
            if (joystick != null) {
                positionX += joystick.getActuatorX() * MAX_SPEED;
                positionY += joystick.getActuatorY() * MAX_SPEED;
            }
        }

        // --- SCREEN BOUNDARIES ---
        if (positionX < radius) positionX = radius;
        if (positionX > screenWidth - radius) positionX = screenWidth - radius;
        if (positionY < radius) positionY = radius;
        if (positionY > screenHeight - radius) positionY = screenHeight - radius;
    }

    private void moveToTarget(double targetX, double targetY) {
        double dx = targetX - positionX;
        double dy = targetY - positionY;
        double distance = Math.hypot(dx, dy);

        if (distance > 0) {
            double aiSpeed = MAX_SPEED * 0.35;
            positionX += (dx / distance) * aiSpeed;
            positionY += (dy / distance) * aiSpeed;
        }
    }

    public double getX() { return positionX; }
    public double getY() { return positionY; }
    public double getRadius() { return radius; }
    public boolean isCarryingPig() { return isCarryingPig; }
    public void setCarryingPig(boolean carrying) { this.isCarryingPig = carrying; }
}