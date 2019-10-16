//Nicholas Latham


package app.level.compass;


import processing.core.PApplet;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class Sketch extends PApplet {

    Context context;
    SensorManager manager;    //manager for sensors
    Sensor sensorAcc;    //sensor object for accelerometer
    Sensor sensorMag;    //sensor object for magnetometer
    AccelerometerListener listener;    //listener for accelerometer
    MagnetometerListener listenerMag;  //listener for magnetometer
    float[] magnetData = new float[3]; //data from magnetometer
    float[] accelData = new float[3]; //data from accelerometer
    float oldAngle = 0;    //stores the previous angle 


    //function that runs at the very beginning that sets the settings
    public void settings() {
        fullScreen();
        smooth(8);  
        orientation(PORTRAIT);

    }

    //function that runs at the beginning after settings
    // initializes the sensors and listeners
    public void setup() {

        context = getActivity();
        manager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        
        //initializes the sensors
        sensorAcc = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMag = manager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD));
        listener = new AccelerometerListener();
        listenerMag = new MagnetometerListener();

        //initializes the listeners
        manager.registerListener(listener, sensorAcc, SensorManager.SENSOR_DELAY_FASTEST);
        manager.registerListener(listenerMag, sensorMag, SensorManager.SENSOR_DELAY_FASTEST);


    }

    //main function
    public void draw() {

        //create a matrix with the accelerometer data and the magnetometer data
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelData, magnetData);
        
        //using the matrix, get the bearings
        float[] orientationAngles = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationAngles);


        showDisplay(orientationAngles);

    }

    //shows the display on the screen
    private void showDisplay(float[] orientationAngles){

        background(77,51,25);
        fill(250);

        doCompass(orientationAngles[0]);

        doLevel(accelData[0],accelData[1]);



    }

    //function that handles all the compass stuff
    private void doCompass(float angle){
        int compassMiddleX = width/2;
        int compassMiddleY = (height*27)/100;
        int compassDiameter = (width*3)/4;
        int compassRadius = compassDiameter/2;

        angle = newAngle(angle);

        background(20,30,48);
        stroke(250);
        fill(250, 0, 0);
        strokeWeight(3 );

        //shape of the north arrow
        fill(108,19,19);
        noStroke();
        beginShape();
        vertex(compassMiddleX, compassMiddleY);
        vertex(compassMiddleX+(compassRadius/25)*cos(PI*3/4+angle), compassMiddleY-(compassRadius/25)*sin(PI*3/4+angle));
        vertex(compassMiddleX+compassRadius*cos(PI/2+angle), compassMiddleY-compassRadius*sin(PI/2+angle));
        vertex(compassMiddleX+(compassRadius/25)*cos(PI/4+angle), compassMiddleY-(compassRadius/25)*sin(PI/4+angle));
        vertex(compassMiddleX, compassMiddleY);
        endShape();

        //shape of the south arrow
        fill(250);
        beginShape();
        vertex(compassMiddleX, compassMiddleY);
        vertex(compassMiddleX+(compassRadius/25)*cos(-PI*3/4+angle), compassMiddleY-(compassRadius/25)*sin(-PI*3/4+angle));
        vertex(compassMiddleX+compassRadius*cos(-PI/2+angle), compassMiddleY-compassRadius*sin(-PI/2+angle));
        vertex(compassMiddleX+(compassRadius/25)*cos(-PI/4+angle), compassMiddleY-(compassRadius/25)*sin(-PI/4+angle));
        vertex(compassMiddleX, compassMiddleY);
        endShape();

        stroke(250);
        textFont(createFont("SansSerif", 15 * displayDensity));
        //show small ticks every 5 degrees
        for(int i = 0; i < 360; i+=5){
            float a = radians(i);
            line(compassMiddleX+((compassDiameter*19)/20)/2*cos(PI/2+a), compassMiddleY-((compassDiameter*19)/20)/2*sin(PI/2+a),
                    compassMiddleX+(compassDiameter/2)*cos(PI/2+a), compassMiddleY-(compassDiameter/2)*sin(PI/2+a));
        }
        //show medium ticks every 15 degrees
        for(int i = 0; i < 360; i+= 15){
            float a = radians(i);
            line(compassMiddleX+((compassDiameter*18)/20)/2*cos(PI/2+a), compassMiddleY-((compassDiameter*18)/20)/2*sin(PI/2+a),
                    compassMiddleX+(compassDiameter/2)*cos(PI/2+a), compassMiddleY-(compassDiameter/2)*sin(PI/2+a));

            textAlign(CENTER,CENTER);
            text(i, compassMiddleX+(compassDiameter*23/40)*cos(PI/2-a), compassMiddleY-(compassDiameter*23/40)*sin(PI/2-a));
        }
        //show big ticks evey 90 degrees
        for(int i = 0; i < 360; i+= 90){
            float a = radians(i);
            line(compassMiddleX+((compassDiameter*17)/20)/2*cos(PI/2+a), compassMiddleY-((compassDiameter*17)/20)/2*sin(PI/2+a),
                    compassMiddleX+(compassDiameter/2)*cos(PI/2+a), compassMiddleY-(compassDiameter/2)*sin(PI/2+a));
        }

        showAngle(angle);
    }

    //takes in the angle and returns  a new angle based on the old position
    //this makes it so that the compass doesn't jump around from one position to the next 
    //  and instead smoothly transitions
    private float newAngle(float a){
        float oldAngleSouth = 0;
        float angle = degrees(a);

        if(oldAngle <= 0) oldAngleSouth = 180 + oldAngle;
        else oldAngleSouth = oldAngle - 180;

        if(abs(oldAngle - angle) > 2){
            if(oldAngle > 0){
                if(angle > oldAngle || angle < oldAngleSouth) {
                    if (oldAngle > 179) oldAngle = -oldAngle;
                    else oldAngle++;
                }else oldAngle--;
            }else{
                if(angle < oldAngle || angle > oldAngleSouth){
                    if(oldAngle < -179) oldAngle = -oldAngle;
                    else oldAngle --;
                }else oldAngle++;
            }
            return radians(oldAngle);
        }else return radians(angle);
    }



    //shows at what angle the compass is from North
    private void showAngle(float a){
        int angle = parseInt(degrees(-a));
        if(angle < 0){
            angle = 360 + angle;
        }

        textFont(createFont("SansSerif", 30 * displayDensity));
        textAlign(CENTER,CENTER);
        text(angle+"°", width/2, (height*57)/100);
    }

    //shows the level
    private void doLevel(float x, float y){

        int levelX = width/2;
        int levelY = (height*3)/4;
        int levelRadius = (width*3)/8;
        int bubbleDiameter = levelRadius/5;
        float deltaX = ((levelRadius-bubbleDiameter)/20)*x;
        float deltaY = ((levelRadius-bubbleDiameter)/20)*y;
        int percentX = parseInt(x*10);
        int percentY = parseInt(y*10);


        fill(250);
        textFont(createFont("SansSerif", 30 * displayDensity));
        textAlign(CENTER, CENTER);
        text(percentX+"%", (width*8)/10, levelY);
        text(percentY+"%", width/2, (height*18)/20);


        stroke(250);
        //fill(194, 188, 188);
        fill(20,30,48);
        ellipse(levelX, levelY,levelRadius,levelRadius);
        ellipse(levelX, levelY, (bubbleDiameter*5)/4, (bubbleDiameter*5)/4);


        textFont(createFont("SansSerif", 30 * displayDensity));
        text(x, width/2, height*4/5);

        for(int i = 0; i < 360; i+= 90){
            float a = radians(i);
            line(levelX, levelY,levelX+(levelRadius/2)*cos(PI/2+a), levelY+(levelRadius/2)*sin(PI/2+a));
        }


        noStroke();
        fill(108,19,19);
        ellipse(levelX+deltaX, levelY-deltaY, bubbleDiameter, bubbleDiameter);
    }


    //listener for the Accelerometer
    class AccelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            accelData = event.values;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }

    //listener for the Magnetometer
    class MagnetometerListener implements SensorEventListener{
        public void onSensorChanged(SensorEvent event){
            magnetData = event.values;
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
}