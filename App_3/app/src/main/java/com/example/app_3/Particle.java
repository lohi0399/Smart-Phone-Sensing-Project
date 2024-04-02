package com.example.app_3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.widget.ImageView;

import java.util.List;
import java.util.Collections;
import java.util.Random;

public class Particle {
    public final int radius = 6;
    public int x;
    public int y;
    public double direction;

    public double directionNoise;
    public double weight;

    public double stepNoise;
    public ShapeDrawable particleShapeDrawable;

    private Particle(int x, int y, int direction, double weight , double stepNoise, double directionNoise) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.weight = weight;
        this.stepNoise = stepNoise;
        this.directionNoise = directionNoise;
        this.particleShapeDrawable = new ShapeDrawable(new OvalShape());
        this.particleShapeDrawable.getPaint().setColor(Color.BLUE);
        this.particleShapeDrawable.setBounds((this.x - radius), (this.y - radius), (this.x + radius), (this.y + radius));
    }

    public static Particle createNewParticle(int x, int y, int direction, double weight, double stepNoise , double directionNoise) {
        return new Particle(x, y, direction, weight , stepNoise , directionNoise);
    }

    public void updateParticlePosition() {
        this.particleShapeDrawable.setBounds((this.x - radius), (this.y - radius), (this.x + radius), (this.y + radius));

    }

}

