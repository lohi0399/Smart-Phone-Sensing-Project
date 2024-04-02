package com.example.app_3;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sense extends AppCompatActivity implements SensorEventListener {

    // Alpha for low-pass filter
    private final float ALPHA = 0.15f;
    private final float ALPHA2 = 0.80f;
    private final float NS_TO_S = 1.0f / 1000000000.0f;
    long timestamp = 0;
    private int currentStep = 0;
    private ActivityDetection activityDetection;
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private Sensor countSensor;
    // Last azimuth value
    private float lastAzimuth = 0;
    private float dt = 0;

    private Button reInitiateButton;
    private Switch dataSwitch;
    private ImageView canvasView;
    private TextView stepScreen;
    private TextView cellScreen;
    private TextView azimuthScreen;


    private SenseUtility senseUtility;

    private List<CellConfiguration> cellConfigurationList;
    private List<ParallelogramConfiguration> parallelogramConfigurationList;
    private List<Particle> particleList;
    private Bitmap blankBitmap;

    private Canvas canvas;
    private Display display;
    private Point size;
    private int screenWidth;
    private int screenHeight;

    private boolean firstRegister = false;
    private boolean hasConvergenceOccured = false;

    private ArrayList<String> activityList = new ArrayList<>();
    private int currentActivityCount = 0;
    private int maxActivityCount = 3;
    private boolean activityRunning = false;

    @Override
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);
        setBottomNavigationViewListener(findViewById(R.id.bottom_navigator));
        initiateInteractables();
        stepScreen.setText(String.valueOf(currentStep));
        initializeListeners();

        createCells();
        createParticles();

    }

    private void createParticles() {


        for (int i = 0; i < senseUtility.TOTAL_NUM_PARTICLES; i++) {

            Particle P1 = Particle.createNewParticle(senseUtility.random.nextInt(screenWidth - 0) + 0, senseUtility.random.nextInt(screenHeight - 0) + 0,
                    90, 1, senseUtility.customGaussianNoise(senseUtility.PARTICLE_NOISE_MEAN, senseUtility.PARTICLE_NOISE_STANDARD_DEVIATION), senseUtility.getRandomOrientationNoiseForParticles());
            while (!checkCreatedParticleIsInLayout(P1)) {
                P1 = Particle.createNewParticle(senseUtility.random.nextInt(screenWidth - 0) + 0, senseUtility.random.nextInt(screenHeight - 0) + 0,
                        90, 1, senseUtility.customGaussianNoise(senseUtility.PARTICLE_NOISE_MEAN, senseUtility.PARTICLE_NOISE_STANDARD_DEVIATION), senseUtility.getRandomOrientationNoiseForParticles());
            }
            particleList.add(P1);
        }

        drawParticlesOnScreen();
    }

    private boolean checkCreatedParticleIsInLayout(Particle particle) {
        Rect ovalBounds = particle.particleShapeDrawable.getBounds();
        for (CellConfiguration cell : cellConfigurationList) {
            Rect rectBounds = cell.shapeDrawable.getBounds();
            if (rectBounds.contains(ovalBounds)) {
                return true;
            }
        }

        for (ParallelogramConfiguration pCell : parallelogramConfigurationList) {
            if (pCell.parallelogramDrawable.region.contains(ovalBounds.centerX(), ovalBounds.centerY())) {
                return true;
            }
        }
        return false;
    }

    private void drawParticlesOnScreen() {
        for (Particle particle : particleList) {
            particle.particleShapeDrawable.draw(canvas);
        }
    }

    private void updateParticlePositionsOnScreen() {
        blankBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);

        canvas.drawColor(Color.WHITE);

        drawWallsOnScreen();

        for (Particle particle : particleList) {
            if (particle.particleShapeDrawable.getPaint().getColor() == Color.BLUE) {
                particle.particleShapeDrawable.getPaint().setColor(Color.RED);
            } else {
                particle.particleShapeDrawable.getPaint().setColor(Color.BLUE);
            }
            particle.updateParticlePosition();
            particle.particleShapeDrawable.draw(canvas);
        }


    }

    private void initializeListeners() {
        dataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    reInitiateButton.setEnabled(true);
                    firstRegister = true;
                    if (countSensor != null) {
                        sensorManager.registerListener(Sense.this, countSensor, SensorManager.SENSOR_DELAY_UI);
                    } else {
                        Toast.makeText(Sense.this, "Count sensor not available!", Toast.LENGTH_LONG).show();
                    }
                    sensorManager.registerListener(Sense.this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    sensorManager.registerListener(Sense.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
                } else {
                    reInitiateButton.setEnabled(false);
                    firstRegister = false;
                    stepScreen.setText(" ");
                    azimuthScreen.setText(" ");
                    sensorManager.unregisterListener(Sense.this);
                    gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                    currentStep = 0;
                }
            }
        });
    }


    public void CompassReinitiateHandler(View v){

        sensorManager.unregisterListener(Sense.this);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        firstRegister = true;
        if (countSensor != null) {
            sensorManager.registerListener(Sense.this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(Sense.this, "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        sensorManager.registerListener(Sense.this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(Sense.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);

    }

    private void initiateInteractables() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        dataSwitch = findViewById(R.id.start_locating);
        stepScreen = findViewById(R.id.activity);
        cellScreen = findViewById(R.id.cell_screen);
        azimuthScreen = findViewById(R.id.azimuth_screen);
        reInitiateButton = findViewById(R.id.button);
        reInitiateButton.setEnabled(false);
        activityDetection = new ActivityDetection();
        senseUtility = SenseUtility.getSenseUtilityInstance();
        cellConfigurationList = new ArrayList<>();
        parallelogramConfigurationList = new ArrayList<>();
        particleList = new ArrayList<>();
//      sensorManager.registerListener(Sense.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
//      sensorManager.registerListener(Sense.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        canvasView = (ImageView) findViewById(R.id.canvas);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        blankBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);
    }

    private void createCells() {
        // Convert meters to pixels
        int pixelHeight = (int) (senseUtility.REAL_WORLD_HEIGHT_C1 * senseUtility.PIXEL_SCALE);
        int pixelWidth = (int) (senseUtility.REAL_WORLD_WIDTH_C1 * senseUtility.PIXEL_SCALE);
        int thickness = 22;
        int A = (int) (1.74 * senseUtility.PIXEL_SCALE);
        int B = (int) (3.26 * senseUtility.PIXEL_SCALE);
        int C = (int) (2.75 * senseUtility.PIXEL_SCALE);
        int D = (int) (3.81 * senseUtility.PIXEL_SCALE);
        int E = (int) (1.24 * senseUtility.PIXEL_SCALE);
        int F = (int) (1.56 * senseUtility.PIXEL_SCALE);
        int G = (int) (10.23 * senseUtility.PIXEL_SCALE);
        int H = (int) (1.905 * senseUtility.PIXEL_SCALE);
        int I = (int) (4.32 * senseUtility.PIXEL_SCALE);
        int J = (int) (2.73 * senseUtility.PIXEL_SCALE);
        int K = (int) (1.01 * senseUtility.PIXEL_SCALE);
        int L = (int) (1.79 * senseUtility.PIXEL_SCALE);
        int M = (int) (3.57 * senseUtility.PIXEL_SCALE);
        int N = (int) (4.22 * senseUtility.PIXEL_SCALE);
        drawRequiredWalls(pixelWidth, pixelHeight, A, B, C, D, E, F, G, H, I, J, K, L, M, N, thickness);
    }

    private void drawRequiredWalls(int pixelWidth, int pixelHeight, int A, int B, int C, int D, int E, int F, int G, int H, int I, int J, int K, int L, int M, int N, int thickness) {
        defineAndAppendNewWall(20, 150, 20 + pixelWidth, 150 + pixelHeight, "C1", Color.GREEN); // cell 1
        defineAndAppendNewWall(20 + pixelWidth, 150, 20 + (2 * pixelWidth), 150 + pixelHeight, "C2", Color.GRAY); // cell 2
        defineAndAppendNewWall(20 + (2 * pixelWidth), 150, 20 + (3 * pixelWidth), 150 + pixelHeight, "C3", Color.YELLOW); // cell 3
        defineAndAppendNewWall(20 + (2 * pixelWidth), 150 + pixelHeight, 20 + (3 * pixelWidth), 150 + pixelHeight + L, "C7", Color.MAGENTA); // cell 7
        defineAndAppendNewWall(20 + (2 * pixelWidth), 150 + pixelHeight, 20 + (2 * pixelWidth) + H, 150 + pixelHeight + thickness, "W1", Color.BLACK); // cell 7 wall 1
        defineAndAppendNewWall(20 + (3 * pixelWidth) - H, 150 + pixelHeight, 20 + (3 * pixelWidth), 150 + pixelHeight + thickness, "W2", Color.BLACK); // cell 7 wall 2
        defineAndAppendNewWall(20 + (3 * pixelWidth) - thickness, 150 + pixelHeight + thickness, 20 + (3 * pixelWidth), 150 + pixelHeight + L, "W3", Color.BLACK); // cell 7 wall 3
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 - F, 20 + (3 * pixelWidth) + M, 150 - F + B, "C8", Color.GREEN); // cell 8
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 - F + B, 20 + (3 * pixelWidth) + M, 150 - F + B + C, "C9", Color.GRAY); // cell 9
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 - F + B + C, 20 + (3 * pixelWidth) + E + 20, 150 - F + B + C + D, "C10", Color.YELLOW); // cell 10
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 + pixelHeight + L + I + G, 20 + (3 * pixelWidth) + E + 20, 150 + pixelHeight + L + I + G + D, "C11", Color.MAGENTA); // cell 11
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 + pixelHeight + L + I + G + D, 20 + (3 * pixelWidth) + M, 150 + pixelHeight + L + I + G + D + C , "C12", Color.GREEN); // cell 12
        defineAndAppendNewWall(20 + (3 * pixelWidth), 150 + pixelHeight + L + I + G + D + C , 20 + (3 * pixelWidth) + M, 150 + pixelHeight + L + I + G + D + C + B - 40 , "C13", Color.GRAY); // cell 13
        defineAndAppendNewWall(20 + (2 * pixelWidth), 150 + pixelHeight + L + I + G + D + A, 20 + (3 * pixelWidth), 150 + pixelHeight + L + I + G + D + A + pixelHeight +20, "C14", Color.YELLOW); // cell 14
        defineAndAppendNewWall(20 + pixelWidth, 150 + pixelHeight + L + I + G + D + A, 20 + (2 * pixelWidth), 150 + pixelHeight + L + I + G + D + A + pixelHeight + 20, "C15", Color.MAGENTA); // cell 15
        defineAndAppendNewWall(20, 150 + pixelHeight + L + I + G + D + A, 20 + pixelWidth, 150 + pixelHeight + L + I + G + D + A + pixelHeight +20, "C16", Color.GREEN); // cell 16
        defineAndAppendParallelogramWall(20 + (3 * pixelWidth) - 25 , 150 - F + B + C + D, 20 + (3 * pixelWidth) + E + 10, 150 - F + B + C + D, 20 + pixelWidth + (pixelWidth - K) / 2 + K + 40 + 70, 150 - F + B + C + D + N, 20 + pixelWidth + (pixelWidth - K) / 2 +40, 150 - F + B + C + D + N, "C17", Color.MAGENTA); // cell 17
        defineAndAppendParallelogramWall(20 + pixelWidth + (pixelWidth - K) / 2 +40, 150 - F + B + C + D + N + J, 40 + pixelWidth + (pixelWidth - K) / 2 + K + 20 + 70, 150 - F + B + C + D + N + J, 40 + (3 * pixelWidth) + E + 30, 150 + pixelHeight + L + I + G, 20 + (3 * pixelWidth) - 20, 150 + pixelHeight + L + I + G, "C18", Color.YELLOW); // cell 18
        defineAndAppendNewWall(20 + pixelWidth + (pixelWidth - K) / 2 + 40, 150 - F + B + C + D + N, 20 + pixelWidth + (pixelWidth - K) / 2 + K + 40 + 20 + 40, 150 - F + B + C + D + N + J, "C19", Color.GRAY); // cell 19

        senseUtility.setWallLayout(cellConfigurationList);

        senseUtility.setParallelogramLayout(parallelogramConfigurationList);

        drawWallsOnScreen();
    }

    private void drawWallsOnScreen() {
        for (CellConfiguration cell : cellConfigurationList)
            cell.shapeDrawable.draw(canvas);

        for (ParallelogramConfiguration cell : parallelogramConfigurationList) {
            canvas.drawPath(cell.parallelogramDrawable.path, cell.parallelogramDrawable.paint);
        }
    }

    private void defineAndAppendNewWall(int left, int top, int right, int bottom, String cellNo, int color) {

        ShapeDrawable d1 = new ShapeDrawable(new RectShape());

        d1.getPaint().setColor(color);
        d1.getPaint().setAlpha(128);
        d1.setBounds(left, top, right, bottom);
        CellConfiguration cellconfig = new CellConfiguration(cellNo, d1);

        cellConfigurationList.add(cellconfig);
    }

    private void defineAndAppendParallelogramWall(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, String cellNo, int color) {

        ParallelogramDrawable p1 = new ParallelogramDrawable(x1, y1, x2, y2, x3, y3, x4, y4, color);

        ParallelogramConfiguration parallelogramConfiguration = new ParallelogramConfiguration(cellNo, p1);

        parallelogramConfigurationList.add(parallelogramConfiguration);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            performGyroSensorChangedActions(event);
        } else if ((event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) || (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)) {

            if (!firstRegister) {
                performParticleFilterActions();
                if (!hasConvergenceOccured) {
                    hasConvergenceOccured = checkConvergence();
                }
                if (hasConvergenceOccured) {
                    detectAndShowCell();
                }
            }else {
                currentStep--;
            }
            firstRegister = false;
            currentStep++;
//            stepScreen.setText(String.valueOf(event.values[0]));
            stepScreen.setText(String.valueOf(currentStep));

        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            calculateAndDisplayAzimuthAngle(event);
        }

    }

    private void detectAndShowCell() {
        Particle bestParticle = findBestParticle(particleList);
        String detectedCell = locateCell(bestParticle.particleShapeDrawable.getBounds().centerX(), bestParticle.particleShapeDrawable.getBounds().centerY());
        if (!(detectedCell == null)) {
            if (detectedCell.equals("W1") || detectedCell.equals("W2") || detectedCell.equals("W3")) {
                detectedCell = "C7";
            }
            cellScreen.setText(detectedCell);
        } else {
            if (!detectCellBasedOnPopulation()) {
                cellScreen.setText(" ");
            }
//            cellScreen.setText(" ");
        }
    }

    private boolean detectCellBasedOnPopulation() {
        HashMap<String, Integer> cellCount = new HashMap<>();
        for (Particle particle : particleList) {
            String particleCell = locateCell(particle.x, particle.y);
            if (cellCount.containsKey(particleCell)) {
                cellCount.put(particleCell, Integer.valueOf(cellCount.get(particleCell) + 1));
            } else {
                cellCount.put(particleCell, Integer.valueOf(1));
            }
        }
        Map.Entry<String, Integer> entry = getHighestFrequencyEntry(cellCount);
        if ((entry.getValue() / Integer.valueOf(senseUtility.TOTAL_NUM_PARTICLES)) >= 0.95) {
            cellScreen.setText(entry.getKey());
            return true;
        }
        return false;

    }

    private Map.Entry<String, Integer> getHighestFrequencyEntry(HashMap<String, Integer> cellCount) {
        return Collections.max(cellCount.entrySet(), Map.Entry.comparingByValue());
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

    private String locateCell(int x, int y) {

        for (CellConfiguration cell : cellConfigurationList) {
            Rect bounds = cell.shapeDrawable.getBounds();
            if (bounds.contains(x, y)) {
                return cell.cellNo;
            }
        }

        for (ParallelogramConfiguration pCell : parallelogramConfigurationList) {
            if (pCell.parallelogramDrawable.region.contains(x, y)) {
                return pCell.cellNo;
            }
        }
        return null;
    }

    private boolean checkConvergence() {

        Particle bestParticle = Collections.max(particleList, (p1, p2) -> Double.compare(p1.weight, p2.weight));
        int collectiveCentroidX = 0;
        int collectiveCentroidY = 0;
        int additiveX = 0;
        int additiveY = 0;

        for (Particle particle : particleList) {
            additiveX += particle.x;
            additiveY += particle.y;
        }
        collectiveCentroidX = additiveX / senseUtility.TOTAL_NUM_PARTICLES;
        collectiveCentroidY = additiveY / senseUtility.TOTAL_NUM_PARTICLES;

//        Toast.makeText(getApplicationContext(), "BestParticle X: " + bestParticle.x + ",Y:" + bestParticle.y + "\nCollective X:"
//                + collectiveCentroidX + ",Y:" + collectiveCentroidY, Toast.LENGTH_SHORT).show();


        if ((Math.abs(bestParticle.x - collectiveCentroidX) < senseUtility.CONVERGENCE_LIMIT) && (Math.abs(bestParticle.y - collectiveCentroidY) < senseUtility.CONVERGENCE_LIMIT)) {

            Toast.makeText(Sense.this, " Potential Convergence Occurred", Toast.LENGTH_LONG).show();

            return true;
        }

        return false;

    }

    private void performParticleFilterActions() {
        particleList = ParticleFilter.getInstance().particleFilter(particleList);
        updateParticlePositionsOnScreen();
    }

    private void calculateAndDisplayAzimuthAngle(SensorEvent event) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

        // Remap coordinate system
        float[] remappedRotationMatrix = new float[16];
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix);

        // Convert to orientations
        float[] orientations = new float[3];
        SensorManager.getOrientation(remappedRotationMatrix, orientations);

//            float filteredAzimuth = lowPass(orientations[0], lastAzimuth);
        float filteredAzimuth = complementryFilter(orientations[0], lastAzimuth);
//        filteredAzimuth = lowPass(orientations[0], filteredAzimuth);

        senseUtility.setCurrentAzimuthInRadians(filteredAzimuth);
        float azimuthInDegrees = (float) (Math.toDegrees(filteredAzimuth) + 360) % 360;
        senseUtility.setCurrentAzimuthInDegrees(azimuthInDegrees);
        azimuthScreen.setText(String.valueOf(azimuthInDegrees));
        lastAzimuth = filteredAzimuth;
    }

    private void performGyroSensorChangedActions(SensorEvent event) {
        senseUtility.setGyroscopeX(event.values[0]);  // Rate of rotation around the x-axis
        if (timestamp != 0) {
            dt = (event.timestamp - timestamp) * NS_TO_S;
        }
        timestamp = event.timestamp;
    }


    private float complementryFilter(float azimuthNew, float azimuthOld) {
        return ALPHA2 * (azimuthOld + senseUtility.getGyroscopeX() * dt) + (1 - ALPHA2) * azimuthNew;
    }


    private float lowPass(float current, float last) {
        return last * (1.0f - ALPHA) + current * ALPHA;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }


    private void setBottomNavigationViewListener(BottomNavigationView bottomNaviView) {

        bottomNaviView.setSelectedItemId(R.id.sense);


        bottomNaviView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (item.getItemId() == R.id.debug) {
                    startActivity(new Intent(getApplicationContext(), Debug.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (item.getItemId() == R.id.sense) {
                    return true;
                }

                return false;

            }
        });

    }
}
