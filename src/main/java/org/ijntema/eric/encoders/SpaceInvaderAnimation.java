package org.ijntema.eric.encoders;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class SpaceInvaderAnimation extends JPanel implements Runnable {
    private static final int WIDTH = 352;
    private static final int HEIGHT = 288;
    private static final int FRAME_RATE = 30;
    private static final int INVADER_WIDTH = 40;
    private static final int INVADER_HEIGHT = 30;
    private static final int INVADER_STEP = 5;
    private static final int STAR_COUNT = 25;
    private static final int TWINKLE_RATE = 50;
    private static final int LAYER_COUNT = 3;
    private int invaderX = 0;
    private int invaderY = HEIGHT / 2 - INVADER_HEIGHT / 2;
    private int directionX = 1; // 1 for right, -1 for left
    private int directionY = 0; // 1 for down, -1 for up, 0 for no vertical movement
    private int animationFrame = 0;
    private static final int NUM_FRAMES = 2;
    private Random random = new Random();
    private ArrayList<Star> stars = new ArrayList<>();
    private int twinkleCounter = 0;
    private int[] layerOffsets = new int[LAYER_COUNT];
    private int[] layerSpeeds = {1, 2, 3}; // Different speeds for each layer
    private int planetStartX = 100; // Starting X position of the planet (fixed)
    private int planetY = 100; // Y position of the planet (fixed)
    private double orbitRadius = 100.0; // Radius of the circular orbit
    private double orbitSpeed = 0.01; // Speed of orbit in radians per frame
    private double currentAngle = 0.0; // Current angle in radians

    public SpaceInvaderAnimation() {
        initializeStars();
        new Thread(this).start();
    }

    private void initializeStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(3) + 1));
        }
    }

    private BufferedImage createFrame(int invaderX, int invaderY, int animationFrame) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        drawBackground(g);
        drawSpaceInvader(g, invaderX, invaderY, animationFrame);
        g.dispose();
        return image;
    }

    private void drawBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw layers with parallax effect
        for (int layer = 0; layer < LAYER_COUNT; layer++) {
            drawLayer(g, layer);
        }

        // Draw the planet in the first layer (farthest back)
        drawPlanet(g);
    }

    private void drawLayer(Graphics g, int layer) {
        int offset = layerOffsets[layer];

        // Draw stars for each layer
        for (Star star : stars) {
            g.setColor(Color.WHITE);
            int starX = star.x + offset / (layer + 1); // Move slower for farther layers
            if (starX >= WIDTH) starX -= WIDTH;
            g.fillRect(starX, star.y, star.size, star.size);
        }
    }

    private void drawPlanet(Graphics g) {
        int planetX = calculatePlanetXPosition(); // Calculate current X position of the planet
        int planetY = calculatePlanetYPosition(); // Calculate current Y position of the planet

        g.setColor(new Color(0, 0, 128)); // Dark blue planet
        g.fillOval(planetX, planetY, 100, 100); // Draw planet
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(planetX + 30, planetY + 30, 10, 10); // Crater 1
        g.fillOval(planetX + 40, planetY + 60, 20, 20); // Crater 2
        g.fillOval(planetX + 70, planetY + 40, 15, 15); // Crater 3
    }

    private int calculatePlanetXPosition() {
        // Calculate the current X position of the planet based on circular motion
        return (int) (planetStartX + orbitRadius * Math.cos(currentAngle));
    }

    private int calculatePlanetYPosition() {
        // Calculate the current Y position of the planet based on circular motion
        return (int) (planetY + orbitRadius * Math.sin(currentAngle));
    }

    private void drawSpaceInvader(Graphics g, int x, int y, int frame) {
        g.setColor(Color.GREEN);
        // Draw a more detailed and animated space invader
        if (frame == 0) {
            g.fillRect(x + 10, y, 20, 5);     // Top rectangle
            g.fillRect(x + 5, y + 5, 30, 5);  // Second rectangle
            g.fillRect(x, y + 10, 40, 10);    // Body
            g.fillRect(x + 5, y + 20, 10, 10);// Left leg
            g.fillRect(x + 25, y + 20, 10, 10);// Right leg
        } else if (frame == 1) {
            g.fillRect(x + 10, y, 20, 5);     // Top rectangle
            g.fillRect(x + 5, y + 5, 30, 5);  // Second rectangle
            g.fillRect(x, y + 10, 40, 10);    // Body
            g.fillRect(x, y + 20, 10, 10);    // Left leg
            g.fillRect(x + 30, y + 20, 10, 10);// Right leg
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage frame = createFrame(invaderX, invaderY, animationFrame);
        g.drawImage(frame, 0, 0, this);
    }

    @Override
    public void run() {
        while (true) {
            updateInvaderPosition();
            updateLayerOffsets();
            twinkleStars();
            updatePlanetPosition();
            animationFrame = (animationFrame + 1) % NUM_FRAMES;
            repaint();
            try {
                Thread.sleep(1000 / FRAME_RATE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to generate a frame and return it as BufferedImage
    public BufferedImage generateFrame() {

        BufferedImage frame = createFrame(invaderX, invaderY, animationFrame);
        updateInvaderPosition();
        updateLayerOffsets();
        twinkleStars();
        updatePlanetPosition();
        animationFrame = (animationFrame + 1) % NUM_FRAMES;

        return frame;
    }

    private void updateInvaderPosition() {
        invaderX += INVADER_STEP * directionX;
        invaderY += INVADER_STEP * directionY;

        if (invaderX < -INVADER_WIDTH) {
            invaderX = WIDTH;
        } else if (invaderX > WIDTH) {
            invaderX = -INVADER_WIDTH;
        }

        if (invaderY < 0) {
            invaderY = 0;
            directionY = random.nextInt(2); // 0 or 1
        } else if (invaderY > HEIGHT - INVADER_HEIGHT) {
            invaderY = HEIGHT - INVADER_HEIGHT;
            directionY = random.nextInt(2) - 1; // -1 or 0
        }

        // Randomly change direction
        if (random.nextInt(10) < 2) { // 20% chance to change direction
            directionX = random.nextInt(3) - 1; // -1, 0, or 1
            directionY = random.nextInt(3) - 1; // -1, 0, or 1
        }
    }

    private void updateLayerOffsets() {
        for (int layer = 0; layer < LAYER_COUNT; layer++) {
            layerOffsets[layer] += layerSpeeds[layer];
            if (layerOffsets[layer] >= WIDTH) {
                layerOffsets[layer] -= WIDTH;
            }
        }
    }

    private void updatePlanetPosition() {
        // Update the angle for circular motion
        currentAngle += orbitSpeed;

        // Ensure the angle stays within 0 to 2π radians
        if (currentAngle > Math.PI * 2) {
            currentAngle -= Math.PI * 2;
        }
    }

    private void twinkleStars() {
        twinkleCounter++;
        if (twinkleCounter % TWINKLE_RATE == 0) {
            int index = random.nextInt(stars.size());
            stars.get(index).twinkle();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invader Animation");
        SpaceInvaderAnimation animation = new SpaceInvaderAnimation();
        frame.add(animation);
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Star {
    int x, y;
    int size;

    public Star(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void twinkle() {
        this.size = (this.size % 3) + 1; // Change star size to create a twinkling effect
    }
}