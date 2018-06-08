package com.example.demo36.ball;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.ArrayList;


//Radius of intersect?. Set level choice for eg. speed of ghosts, no. of ghosts, no. (size?) of holes. Implement random ghost patrol locations

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    //ball position, accel, velocity variables
    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;

    //start and finish location position variables
    private float xStartPos, xFinishPos, yStartPos, yFinishPos;

    //screen boundary variables
    public float xMax, yMax;
    public int canvasW, canvasH;

    //initialise graphics used
    private Bitmap ball, start, finish, hole, ghost_follow_img, patrolling_ghost_img, projectile_img;

    //initialise hole variables
    private int numHoles;
    private double difficulty;
    private int holeDiameter;
    private HoleLocations holeLocations;
    boolean isBallInHole;


    //initialise ghost variables
    private Ghost ghost_follow;
    private PatrollingGhost patrollingGhost;
    private Projectile projectile;
    private ArrayList projectilePositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );


        Maze maze = new Maze(this);
        setContentView(maze);

//Initialise SensorManager and assign to mAccelerometer
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        numHoles = 10;
        holeDiameter = 100;
        isBallInHole = false;
        difficulty = 1.5;

    }


    private class Maze extends View {
        public Maze(Context context) {
            super(context);

            //locate ball image source and initialise Bitmap version of ball
            Bitmap ballSource = BitmapFactory.decodeResource(getResources(), R.drawable.oldsailingboat);
            ball = Bitmap.createScaledBitmap(ballSource, 100, 100, true);

            //locate start and finish images, and initialise onto canvas
            Bitmap startSource = BitmapFactory.decodeResource(getResources(), R.drawable.cemeterygates);
            Bitmap finishSource = BitmapFactory.decodeResource(getResources(), R.drawable.sandcastle);
            Bitmap holeSource = BitmapFactory.decodeResource(getResources(), R.drawable.storm);
            Bitmap ghostSource = BitmapFactory.decodeResource(getResources(), R.drawable.shark);
            Bitmap projectileSource = BitmapFactory.decodeResource(getResources(), R.drawable.snake);

            start = Bitmap.createScaledBitmap(startSource, 100, 100, true);
            finish = Bitmap.createScaledBitmap(finishSource, 100, 100, true);
            hole = Bitmap.createScaledBitmap(holeSource, 100, 100, true);

            ghost_follow_img = Bitmap.createScaledBitmap(ghostSource, 80, 80, true);
            projectile_img = Bitmap.createScaledBitmap(projectileSource, 40, 40, true);
            patrolling_ghost_img = Bitmap.createScaledBitmap(ghostSource, 80, 80, true);

        }


        @Override
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            canvasW = w;
            canvasH = h;

            // Adjust the margins taking into account the dimensions of our image
            xMax = (float) (canvasW - 100);
            yMax = (float) (canvasH - 100);

            xStartPos = 30;
            yStartPos = 30;

            xFinishPos = xMax - 30;
            yFinishPos = yMax - 30;

            holeLocations = new HoleLocations(numHoles, (int) (holeDiameter * difficulty), canvasW, canvasH);

            ghost_follow = new Ghost((int) xMax, (int) yMax / 2, 1);
            patrollingGhost = new PatrollingGhost((int) (Math.random()*canvasW), (int) (Math.random()*canvasH), 2, 1, 200, 500);

            //Log.i("locations", "x:" + holeLocations.getHoleX(1) + "y:" + holeLocations.getHoleY(1) );
            //Log.i("xMax", xMax + "");
            //Log.i("width" , canvasW + "");

        }


        @Override
        protected void onDraw(Canvas canvas) {

            canvas.drawColor( Color.GRAY);
            canvas.drawBitmap(start, xStartPos, yStartPos, null);   //should I separate these drawings so that it doesn't refresh everything?
            canvas.drawBitmap(finish, xFinishPos, yFinishPos, null);

            //draw holes
            for (int i = 0; i < numHoles; i++) {
                canvas.drawBitmap(hole, holeLocations.getHoleX(i), holeLocations.getHoleY(i), null);
                //Log.i("no. holes generated", "" + holeLocations.getActualNumberOfHoles());
            }

            //draw & update ghosts
            canvas.drawBitmap(ghost_follow_img, ghost_follow.getX(), ghost_follow.getY(), null);

            if (projectilePositions != null) {
                for (int j = 0; j < projectilePositions.size(); j++) {
                    projectile = (Projectile) projectilePositions.get(j);
                    canvas.drawBitmap(projectile_img, (int) projectile.getX(), (int) projectile.getY(), null);
                }
            }

            canvas.drawBitmap(patrolling_ghost_img, patrollingGhost.getX(), patrollingGhost.getY(), null);

            //Log.i("width" , canvasW + "");
            //Log.i("locations", "x:" + holeLocations.getHoleX(1) + "y:" + holeLocations.getHoleY(1) );

            canvas.drawBitmap(ball, xPos, yPos, null);      //Bitmap, float, float, paint
            invalidate();                                         //calls onDraw again to refresh position
        }
    }


    protected void onResume() {
        super.onResume();

        // Every time we resume the app we re-register the listener to the accelerometer sensor events
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();

        // Every time we Pause the app we de-register the listener to the accelerometer sensor events
        mSensorManager.unregisterListener(this);
    }


    private void updateBall() {

        float frameTime = 2f;
        float sensitivity = 0.25f;


        //Log.i("velocity, accel in", "v " + xVel + "a " + xAccel);

        if (xPos >= xMax && xVel != 0 && xAccel != 0) {
            xVel = 0;
            xAccel = 0;
        } else if (xPos <= 0 && xVel != 0 && xAccel != 0) {
            xVel = 0;
            xAccel = 0;
        }

        if (yPos >= yMax && yVel != 0 && yAccel != 0) {
            yVel = 0;
            yAccel = 0;
        } else if (yPos <= 0 && yVel != 0 && yAccel != 0) {
            yVel = 0;
            yAccel = 0;
        }


        xVel = xVel + yAccel * frameTime;
        yVel = yVel + xAccel * frameTime;

        xPos = xPos + (int) (xVel * frameTime * sensitivity);
        yPos = yPos + (int) (yVel * frameTime * sensitivity);

        if (xPos >= xMax) {
            xPos = xMax;
        } else if (xPos < 0) {
            xPos = 0;
        }

        if (yPos >= yMax) {
            yPos = yMax;
        } else if (yPos < 0) {
            yPos = 0;
        }

        //move ghosts
        ghost_follow.moveGhost((int) xPos, (int) yPos);
        ghost_follow.shoot((int) xPos, (int) yPos);
        projectilePositions = ghost_follow.getProjectiles();
        patrollingGhost.ghostPatrolConstY();

        //check whether ball is in a hole
        for (int j = 0; j < numHoles; j++) {
            //Log.i("check if in hole", isBallInHole + "");
            if ((xPos >= holeLocations.getHoleX(j) - 100 && xPos <= holeLocations.getHoleX(j) + 100) && (yPos >= holeLocations.getHoleY(j) - 100 && yPos <= holeLocations.getHoleY(j) + 100)) {
                isBallInHole = true;
                break;
            } else isBallInHole = false;
        }

        if (isBallInHole) {
            //Toast hole_message = Toast.makeText(this, "whoops!", Toast.LENGTH_SHORT);
            //hole_message.show();
            startActivity(new Intent(MainActivity.this, EndloseActivity.class));
        }


        //check whether ghost has captured player
        if (ghost_follow.hasCapturedPlayer((int) xPos, (int) yPos, 50, 100)) {
            startActivity(new Intent(MainActivity.this, EndloseActivity.class));
        }

        if (patrollingGhost.hasCapturedPlayer((int) xPos, (int) yPos, 50, 100)) {
            startActivity(new Intent(MainActivity.this, EndloseActivity.class));
            //Toast hole_message = Toast.makeText(this, "whoops!", Toast.LENGTH_SHORT);
            //hole_message.show();
        }


        //check whether ball is over the finish hole; if so, move to endpage intent
        if ((xPos >= xFinishPos - 100 && xPos <= xFinishPos + 100) && (yPos >= yFinishPos - 100 && yPos <= yFinishPos + 100)) {
            //Toast finish_message = Toast.makeText(this, "finish", Toast.LENGTH_SHORT);
            //finish_message.show();

            startActivity(new Intent(MainActivity.this, EndActivity.class));
        }

        //Log.i("position", "x " + xPos + "y " + yPos);
        //Log.i("velocity, accel out", "v " + xVel + "a " + xAccel);


    }

    float sign(float x) {

        return x < 0 ? -1 : +1;

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Here we capture the events of the accelerometer sensor
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // And here we store the data coming from the sensor.
            // We need to flip the y axis so that it points in a consistent direction
            //Log.i("Acceleration", "x: " + sensorEvent.values[0] + ", y: " + sensorEvent.values[1]);

            //TODO 3 - Assign the sign of the acceleration [sign(....)] to the variables
            //         xAccel and yAccel. You will need to revert ( multiply by -1) the yAccel
            xAccel = (1) * sign(sensorEvent.values[1]);
            yAccel = (-1) * sign(sensorEvent.values[0]);

            //Once we get new data from the sensor, we update the Ball position
            //Log.i("updating ball", "go");
            updateBall();
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }





    public class HoleLocations {

        private int numberOfHoles;
        private float holeDiameter;
        private double xHoleLocation[];
        private double yHoleLocation[];
        private boolean isHoleBad;

        // Constructor will generate all the holes
        public HoleLocations(int numHoles, int diameter, int xSize, int ySize) {
            // Initialise class variables used in other methods
            numberOfHoles = numHoles;
            holeDiameter = diameter;
            // including arrays to store locations
            xHoleLocation = new double[numberOfHoles];
            yHoleLocation = new double[numberOfHoles];

            // Calculate screen boundaries
            float xMax = xSize - holeDiameter;
            float yMax = ySize - holeDiameter;
            float xMin = holeDiameter;
            float yMin = holeDiameter;

            // Generate the holes - allow three failures before giving up
            int failedAttempts = 0;
            int counter; // Declare counter here so it remains in scope after the loop
            for (counter = 0; counter < numberOfHoles && failedAttempts < 300; counter++) {
                //generate random numbers within bounds and assign to arrays
                //example code used: double randomx = Math.random() * xMax;
                xHoleLocation[counter] = xMin + Math.random() * (xMax - xMin);
                yHoleLocation[counter] = yMin + Math.random() * (yMax - yMin);
                isHoleBad = false;


                //check that the hole is not in the start or end position
                if (xHoleLocation[counter] <= 30 + holeDiameter && yHoleLocation[counter] <= 30 + holeDiameter) {
                    counter--;
                    failedAttempts++;
                    isHoleBad = true;
                }
                if (xHoleLocation[counter] >= xSize - 30 + holeDiameter && yHoleLocation[counter] >= ySize - 30 + holeDiameter) {
                    counter--;
                    failedAttempts++;
                    isHoleBad = true;
                }

                //check that the hole will not overlap with other holes
                //cycle through all other holes
                for (int counter2 = counter - 1; counter2 >= 0; counter2--) {
                    if (isHoleBad) {
                        break;
                    }


                    //check that the hole will not overlap with other holes
                    //cycle through all other holes
                    for (int counter3 = counter - 1; counter2 >= 0; counter2--) {

                        //calculate x and y distances apart
                        double xDifference = Math.abs(xHoleLocation[counter] - xHoleLocation[counter3]);
                        double yDifference = Math.abs(yHoleLocation[counter] - yHoleLocation[counter3]);

                        //check distance apart is not less than hole diameter
                        //if bad, decrement counter so this value is overwritten with a new attempt
                        //if (xDifference < holeDiameter || yDifference < holeDiameter) {
                        if (xDifference < holeDiameter && yDifference < holeDiameter) {
                            counter--;
                            failedAttempts++;
                            break;
                        }
                    }
                }
            }


            // If we failed to generate all the holes, set the number of actual holes generated
            if (failedAttempts >= 300) {
                numberOfHoles = counter + 1;
            }
        }

            // Return the actual number of holes generated
            public int getActualNumberOfHoles() {
                return numberOfHoles;
            }

            // Provide methods to get a hole's x and y coordinates
            // Note: Decided against defining a coordinate class as it just makes things overly complicated for
            // this simple task
            // Return -1 for an invalid hole number
            public int getHoleX ( int index){
                if (index < numberOfHoles)
                    return (int) xHoleLocation[index];
                else
                    return -1;
            }

            public int getHoleY ( int index){
                if (index < numberOfHoles)
                    return (int) yHoleLocation[index];
                else
                    return -1;
            }

    }






    //This ghost chases the player, speeding up over time
    public class Ghost {        //calling it a ghost at the moment because it can fly over holes!
        private int xPos;       //x position of ghost
        private int yPos;       //y position of ghost
        private int speed;      //speed of ghost 2-20 are good rough values on my phone, no guarantees for other phones!
        private int count;      //counter to hopefully make the ghost speed up over time

        private int count2;             //simple counter
        private boolean firstShot = true;      //is it the first shot?
        private ArrayList projectiles;  //Array containing all projectiles

        //Constructor
        public Ghost(int startx, int starty, int start_speed) {
            xPos = startx;
            yPos = starty;
            speed = start_speed;
            count = 0;
        }

        //moves ghost towards player
        public void moveGhost(int xPosPlayer, int yPosPlayer) {      //x and y PosPlayer are coordinates of player
            int xDifference;                                        //displacement between player and ghost
            int yDifference;

            // Don't move every time, to keep speed down
            if (count < 9) {
                xDifference = xPos - xPosPlayer;                    //calculate displacements
                yDifference = yPos - yPosPlayer;

                //actually do the moving here
                if (xDifference == 0) {
                } else {
                    xPos = xPos - (speed * xDifference / Math.abs(xDifference));
                }
                if (yDifference == 0) {
                } else {
                    yPos = yPos - (speed * yDifference / Math.abs(yDifference));
                }
            }

            // Gradually get faster
            count++;
            if (count > 10) {
                count = 0;
                if (speed < 10)
                    speed = speed + 1;
            }
        }

        //method to check that the ghost hasn't somehow wandered off the screen, and correct if it has
        public void checkGhostOnScreen(int xScreenSize, int yScreenSize) {
            if (xPos > (xScreenSize))
                xPos = xScreenSize;
            if (yPos > (yScreenSize))
                yPos = yScreenSize;
            if (xPos < 0)
                xPos = 0;
            if (yPos < 0)
                yPos = 0;
        }

        //method to make the ghost change to look scarier after it has reached top speed
        public boolean isGhostScary() {
            if (speed >= 20)                     //could call this in an if statement at regular intervals,
                return true;                    //then we could change the bitmap for the ghost by redrawing in the activity
            else                                //we don't have to do this though, it's a luxury
                return false;
        }

        //method to check for collision with player, feel free to ignore/delete this and use your own collision detection
        public boolean hasCapturedPlayer(int xPosPlayer, int yPosPlayer, int ghostSize, int playerSize) {

            int xGhostMiddle = xPos + (ghostSize / 2);
            int yGhostMiddle = yPos + (ghostSize / 2);
            int xPlayerMiddle = xPosPlayer + (playerSize / 2);
            int yPlayerMiddle = yPosPlayer + (playerSize / 2);
            double distx2 = Math.pow(xGhostMiddle - xPlayerMiddle, 2);   //Pythagoras begins
            double disty2 = Math.pow(yGhostMiddle - yPlayerMiddle, 2);   //Math.pow(something,2) is squaring something
            double dist = Math.sqrt(distx2 + disty2);
            double minDist = (ghostSize + playerSize) / 2;                //Pythagoras ends
            if (dist < minDist)
                return true;
            else
                return false;

        }

        //methods to get the position of the ghost if we need to for whatever reason, just in case
        public int getX() {
            return xPos;
        }

        public int getY() {
            return yPos;
        }

        //creates new projectiles at regular intervals and moves them towards where player was when they were created
        public void shoot(int xPlayerPos, int yPlayerPos) {
            count2++;                 //don't shoot every tenth of a second, that would be silly!
            if (count2 > 25) {
                count2 = 0;
                //creates bullets
                if (firstShot){
                    projectiles = new ArrayList();
                    Projectile firstProjectile = new Projectile(xPos,yPos,xPlayerPos,yPlayerPos);
                    projectiles.add(firstProjectile);
                    firstShot = false;
                }
                else{
                    Projectile newProjectile = new Projectile(xPos,yPos,xPlayerPos,yPlayerPos);
                    projectiles.add(newProjectile);
                }
            }
            //move projectiles once there are any
            if(projectiles != null) {
                for (int i = 0; i < projectiles.size(); i++)
                    ((Projectile) (projectiles.get(i))).moveProjectile();
            }

        }

        //to get projectiles for this ghost so they can be redrawn in their new position
        public ArrayList getProjectiles() {
            return projectiles;
        }

    }







    public class Projectile {


        //Define private variables
        private double xProjectile;    //Projectile x position
        private double yProjectile;    //Projectile y position
        private double xDifference;    //x difference between projectile and player
        private double yDifference;    //same but for y
        private int projectileSpeed = 12;    //speed of projectile movement
        private double magDifference;       //magnitude of difference
        private double xVel;        //x and y velocities of projectile
        private double yVel;

        //Constructor, x and yStart will be the position of the centre of the ghost when the projectile is created
        //This also calculates x and yVel, setting the velocity of the projectile, which is constant
        public Projectile(int xStart, int yStart, int xPlayerPos, int yPlayerPos){
            xProjectile = xStart + 55;      //55 the ghost size, this makes it shoot from the centre not top-left
            yProjectile = yStart + 55;
            xDifference = xPlayerPos - xProjectile;
            yDifference = yPlayerPos - yProjectile;
            magDifference = Math.sqrt(Math.pow(xDifference,2) + (Math.pow(yDifference,2)));     //just pythagoras
            xVel = projectileSpeed * xDifference/magDifference;
            yVel = projectileSpeed * yDifference/magDifference;
        }

        //Methods

        //move projectile using velocity calculated by constructor
        public void moveProjectile() {
            xProjectile = xProjectile + xVel;
            yProjectile = yProjectile + yVel;
        }

        //method to check for collision with player, returns true if collision detected
        public boolean hasProjectileHitPlayer(int xPosPlayer, int yPosPlayer, int projectileSize, int playerSize) {

            double xPlayerMiddle = xPosPlayer + (playerSize / 2);
            double yPlayerMiddle = yPosPlayer + (playerSize / 2);
            double distx2 = Math.pow(xProjectile - xPlayerMiddle, 2);   //Pythagoras begins
            double disty2 = Math.pow(yProjectile - yPlayerMiddle, 2);   //Math.pow(something,2) is squaring something
            double dist = Math.sqrt(distx2 + disty2);
            double minDist = (projectileSize + playerSize) / 2;                //Pythagoras ends
            if (dist < minDist)
                return true;
            else
                return false;

        }

        //methods to get the position of the ghost if we need to for whatever reason, just in case
        public double getX(){
            return xProjectile;
        }

        public double getY() {
            return yProjectile;
        }

    }









    //This ghost patrols horizontally
    public class PatrollingGhost {                      //Again, calling it a ghost as it can fly over holes
        private int xPos;       //x position of ghost
        private int yPos;       //y position of ghost
        private int speed;      //speed of ghost 5-10 are good rough values on my phone, no guarantees for other phones!
        private int movementDirection;    //1 indicates x is increasing, 0 indicates x is decreasing
        private int xEndOneOfPath;     //The x coordinates of the ends of the path
        private int xEndTwoOfPath;     //END TWO SHOULD BE A LARGER NUMBER THAN END ONE please :)

        //Constructor
        public PatrollingGhost(int startx, int starty, int start_speed, int start_direction, int x_end_one, int x_end_two) {
            xPos = startx;
            yPos = starty;          //remember that yPos will never change, this is the height of the patrol path
            speed = start_speed;
            movementDirection = start_direction;
            xEndOneOfPath = x_end_one;
            xEndTwoOfPath = x_end_two;
        }

        //moves ghost along patrol path
        public void ghostPatrolConstY() {
            //Change movement direction if the ghost has reached the end of the path
            if (xPos >= xEndTwoOfPath) {
                movementDirection = 0;
            } else if (xPos <= xEndOneOfPath) {
                movementDirection = 1;
            }

            //actually move the ghost
            if (movementDirection == 1) {
                xPos = xPos + speed;
            }

            if (movementDirection == 0) {
                xPos = xPos - speed;
            }
        }

        //method to check for collision with player, feel free to ignore/delete this and use your own collision detection
        public boolean hasCapturedPlayer(int xPosPlayer, int yPosPlayer, int ghostSize, int playerSize) {

            int xGhostMiddle = xPos + (ghostSize / 2);
            int yGhostMiddle = yPos + (ghostSize / 2);
            int xPlayerMiddle = xPosPlayer + (playerSize / 2);
            int yPlayerMiddle = yPosPlayer + (playerSize / 2);
            double distx2 = Math.pow(xGhostMiddle - xPlayerMiddle, 2);   //Pythagoras begins
            double disty2 = Math.pow(yGhostMiddle - yPlayerMiddle, 2);   //Math.pow(something,2) is squaring something
            double dist = Math.sqrt(distx2 + disty2);
            double minDist = (ghostSize + playerSize) / 2;                //Pythagoras ends
            if (dist < minDist)
                return true;
            else
                return false;

        }

        //methods to get the position of the ghost if we need to for whatever reason, just in case
        public int getX() {
            return xPos;
        }

        public int getY() {
            return yPos;
        }

    }


}

