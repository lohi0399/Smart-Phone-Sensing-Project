package com.example.app_3;

import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParticleFilter {
    private static ParticleFilter particleFilter;
    private List<CellConfiguration> WallLayoutList;
    private List<ParallelogramConfiguration> pWallLayoutList;
    private boolean haveNextNextGaussian = false;


    private ParticleFilter() {

    }

    public static ParticleFilter getInstance() {
        if (particleFilter == null) {
            particleFilter = new ParticleFilter();
        }
        return particleFilter;
    }

    public static double calculateDirection(double azimuthReading) {
        double direction;

        if (azimuthReading >= 0 && azimuthReading < 90) {
            direction = azimuthReading + 30;
        } else if (azimuthReading >= 90 && azimuthReading < 180) {
            direction = azimuthReading - 30;
        } else if (azimuthReading >= 180 && azimuthReading < 270) {
            direction = azimuthReading + 30;
        } else { // 270 <= azimuth_reading < 360
            direction = azimuthReading - 30;
        }

        return Math.toRadians(direction);
    }

    private double randomOffset(double mean, double standardDeviation) {
        return (mean + (SenseUtility.getSenseUtilityInstance().random.nextGaussian() + standardDeviation));


    }

    public List<Particle> particleFilter(List<Particle> particles) {
        double totalWeight = 0.0;
        for (Particle particle : particles) {
            MotionModel.getMotionModelInstance().performMotionModel(particle);
//            double[] newPositionDirection = MotionModel.getMotionModelInstance().performMotionModel(particle);
//            particle.x = (int) newPositionDirection[0];
//            particle.y = (int) newPositionD   irection[1];
//            particle.direction = newPositionDirection[2];
            particle.updateParticlePosition();


//            int collisionResult = checkCollision(particle);
//            if (collisionResult == 0) {
//                particle.weight = 0;
//            } else {
//                particle.weight*= checkCollision(particle);
//            }

            particle.weight = (particle.weight + 1) * checkCollision(particle);

            totalWeight += particle.weight;
        }

        for (Particle particle : particles) {
            particle.weight /= totalWeight;
        }

//        Particle bestParticle = Collections.max(particles, (p1, p2) -> Double.compare(p1.weight, p2.weight));


        particles = performBasicResampling(particles);

        return particles;
    }

    private List<Particle> performBasicResampling(List<Particle> particles) {
        Collections.shuffle(particles);
        Particle bestParticle = findBestParticle(particles);

        for (Particle particle : particles) {
            if (particle.weight == 0) {
                particle.x = (int) (bestParticle.x +
                        SenseUtility.getSenseUtilityInstance().customGaussianNoise(SenseUtility.getSenseUtilityInstance().NOISE_MEAN, SenseUtility.getSenseUtilityInstance().NOISE_STANDARD_DEVIATION));
                particle.y = (int) (bestParticle.y +
                        SenseUtility.getSenseUtilityInstance().customGaussianNoise(SenseUtility.getSenseUtilityInstance().NOISE_MEAN, SenseUtility.getSenseUtilityInstance().NOISE_STANDARD_DEVIATION));
                particle.direction = bestParticle.direction;
//                particle.weight = bestParticle.weight;
                particle.updateParticlePosition();
            }
        }

        return particles;
    }


    private int customNoise(int x) {
        int low = x - 20;
        int high = x + 20;
        return SenseUtility.getSenseUtilityInstance().random.nextInt(high - low) + low;
    }


    private Particle findBestParticle(List<Particle> particles) {
        Particle bestParticle = particles.get(0);
        for (Particle particle : particles) {
            if (particle.weight > bestParticle.weight) {
                bestParticle = particle;
            }

        }
        return bestParticle;
    }


    public int checkCollision(Particle particle) {
        if (hasCollided(particle)) {
            return 0;
        }
        return 2;
    }

    public boolean hasCollided(Particle particle) {

        boolean isPresentInAnyRectangle = false;

        if (WallLayoutList == null) {
            WallLayoutList = SenseUtility.getSenseUtilityInstance().getWallLayout();
        }

        if (pWallLayoutList == null) {
            pWallLayoutList = SenseUtility.getSenseUtilityInstance().getParallelogramLayout();
        }

        Rect ovalBounds = particle.particleShapeDrawable.getBounds();

        for (CellConfiguration cell : WallLayoutList) {
            Rect rectBounds = cell.shapeDrawable.getBounds();
            if ((cell.cellNo.equals("W1") || (cell.cellNo.equals("W2") || (cell.cellNo.equals("W3"))))) {
                if (rectBounds.contains(ovalBounds.centerX(), ovalBounds.centerY())) {
                    isPresentInAnyRectangle = false;
                }
            } else {
                if (rectBounds.contains(ovalBounds.centerX(), ovalBounds.centerY())) {
                    isPresentInAnyRectangle = true;
                }
            }

        }

        for (ParallelogramConfiguration pConfig : pWallLayoutList) {
            if (pConfig.parallelogramDrawable.region.contains(ovalBounds.centerX(), ovalBounds.centerY())) {
                isPresentInAnyRectangle = true;
            }
        }


        return !isPresentInAnyRectangle;
    }


}
