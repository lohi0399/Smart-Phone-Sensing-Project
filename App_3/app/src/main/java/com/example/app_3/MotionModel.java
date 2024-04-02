package com.example.app_3;

public class MotionModel {
    public static MotionModel motionModel;

    private MotionModel() {

    }

    public static MotionModel getMotionModelInstance() {
        if (motionModel == null) {
            motionModel = new MotionModel();
        }
        return motionModel;
    }

    public double[] performMotionModel(Particle particle) {
        double stepLength = SenseUtility.getSenseUtilityInstance().STEP_LENGTH;
//        double azimuth = SenseUtility.getSenseUtilityInstance().getCurrentAzimuthInDegrees();
//        particle.direction = ParticleFilter.calculateDirection(azimuth);
        particle.direction = SenseUtility.getSenseUtilityInstance().getCurrentAzimuthInRadiansWithOffset() + particle.directionNoise;
        particle.x += stepLength * Math.cos(particle.direction) + particle.stepNoise;
        particle.y += stepLength * Math.sin(particle.direction) + particle.stepNoise;
        return new double[]{particle.x, particle.y, particle.direction};
    }

}
