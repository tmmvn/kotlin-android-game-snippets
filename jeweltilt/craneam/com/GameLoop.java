package jeweltilt.craneam.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class GameLoop
  extends Thread
  {
    private static final String TAG = "Jewel Tilt";  // Game tag in logs
    private static final boolean DEBUG = false;  // sets debug mode on or off
    private static final String ACHIEVEMENTS = "Achievements";
    private static final String PROGRESS = "Progress";
    private static final String SETTINGS = "Settings";
    private static final int MAX_NUM_DIAMONDS = 12;
    private static final int MAX_CHALLENGES = 7;
    private static final int MAX_PROGRESS = 2;
    private GameOver gameOver;
    private float SCALE;  // Screen scale difference to expected
    private int MAX_WIDTH;  // Screen width
    private int MAX_HEIGHT;  // Screen height
    private int MB_MIN_X;  // Movement bounds
    private int MB_MAX_X;
    private int MB_MIN_Y;
    private int MB_MAX_Y;
    private List<PlayerBall> playerList = new ArrayList<PlayerBall>();
    private List<Jewel> jewelList = new ArrayList<Jewel>();
    private List<UIElement> uiElementList = new ArrayList<UIElement>();
    private List<Sparkle> sparkleList = new ArrayList<Sparkle>();
    private boolean running, pregame;  // Flag to see if game is running
    private boolean menu = true;  // flag to see if we are at the menu
    private boolean updateScore = false;  // flag to update score
    private boolean paused = false;  // flag to see if game is paused
    private boolean newHighScore;
    private boolean challengeDone;
    private boolean reversed;
    private boolean noSound;
    private boolean noMusic;
    private GLRenderer renderer;  // Used renderer
    private long timeTracker;  // Tracks time passed and then adds stuff
    private int addSpeed;  // Time between adds
    private int level;  // Sets the amount of adds at an update
    private int numBalls;  // Sets the number of balls the player controls
    private int currentNumDiamonds;
    private int gameMode;
    private int progressLevel;
    private int progressAmount;
    private int currentChallenge;
    private int newPoints;
    private SoundManager soundManager;  // The sound manager for the game
    private JewelTilt main;
    private Context context;
    // variables to be used to get constant FPS
    private long beginTime;
    private long timeDiff;
    private int sleepTime;
    private final static int MAX_FPS = 50;  // Max FPS
    private final static int FRAME_PERIOD = 1000 / MAX_FPS;  // How time each frame takes
    private boolean isCollision = false;  // Boolean to check if collision has happened
    private PlayerBall player1, player2;  // player balls
    private Jewel pad1, pad2;  // Pads to change player color in TAG-mode
    private UIElement menuBg, play;  // menu items
    private UIElement scoreText, livesText, highScoreText;  // hud texts
    private UIElement[] scoreNums = new UIElement[5];  // numbers for score text
    private UIElement[] highScoreNums = new UIElement[5];  // numbers for score text
    private UIElement livesNums;  // number of lives text
    private UIElement gameOverText, gzText;  // Game over text
    private UIElement highText, newHighText;
    private UIElement gameMode0, gameMode1, gameMode2;
    private UIElement tapText;
    private UIElement progressBar, progressBg;
    private UIElement helpText;
    private UIElement sfx, music;
    private int numLives;  // number of lives
    private int numPoints;  // number of points
    private int highScore;  // best score so far
    private int challengeTracker;  // tracks which stage of challenge is happening
    private float[] accelValues = new float[3];  // Accelerometer values
    private float[] touchCoords = new float[2];  // To handle menu touch location
    private Random rand = new Random();  // initialize new random

    // Constructor to initialize stuff
    public GameLoop(JewelTilt mainThread, Context passedContext, GLRenderer glRenderer, int maxWidth, int maxHeight)
      {
        boolean loadingDone = false;
        MAX_WIDTH = maxWidth;  // set max screen width
        MAX_HEIGHT = maxHeight;  // set max screen height
        SCALE = ( maxWidth / 400f + maxHeight / 240f ) / 2f;
        MB_MIN_X = -MAX_WIDTH + 15;  // set movement bounds: min X
        MB_MIN_Y = -MAX_HEIGHT + 40;  // set movement bounds: min Y
        MB_MAX_X = MAX_WIDTH - 15;  // set movement bounds: max X
        MB_MAX_Y = MAX_HEIGHT - 15;  // set movement bounds: max Y
        renderer = glRenderer;  // set renderer
        accelValues[0] = 0;  // initialize acceleration values to 0
        accelValues[1] = 0;  // as above
        accelValues[2] = 0;  // as above
        context = passedContext;  // pass context so we can pass it on as required
        soundManager = new SoundManager(context);  // create a new sound manager
        main = mainThread;
        gameOver = new GameOver(main, this, passedContext, glRenderer, maxWidth, maxHeight);
        while (!loadingDone)
          {
            loadingDone = LoadSettings();
          }
      }

    private void SetGameMode(int gameModeToSet)
      {
        timeTracker = 0;
        currentNumDiamonds = 0;
        gameMode = gameModeToSet;
        newHighScore = false;
        reversed = false;
        // Regular game mode
        if (gameModeToSet == 0)
          {
            numBalls = 2;
            numLives = 5;
            addSpeed = 75;
          }
        // "Tag" game mode
        if (gameModeToSet == 1)
          {
            numBalls = 1;
            numLives = 3;
            addSpeed = 75;
          }
        // "Challenge" game mode
        if (gameModeToSet == 2)
          {
            numBalls = 1;
            numLives = 5;
            addSpeed = 75;
            currentChallenge = 1;
            challengeDone = false;
            challengeTracker = 0;
          }
        // "Inferno" game mode
        if (gameModeToSet == 3)
          {
            numBalls = 4;
            numLives = 5;
            addSpeed = 75;
          }
      }

    // Function to tell the sound manager to stop music
    public void StopMusic(boolean finalStop)
      {
        soundManager.StopMusic(finalStop);
      }

    // Function to tell the sound manager to pause music
    public void PauseMusic()
      {
        soundManager.PauseMusic();
      }

    // Function to tell the sound manager to resume music
    public void ResumeMusic()
      {
        soundManager.ResumeMusic();
      }

    // Function to set the touch coordinates
    public void SetTouchCoords(float valueX, float valueY)
      {
        touchCoords[0] = valueX - MAX_WIDTH;
        touchCoords[1] = ( valueY - MAX_HEIGHT ) * -1;
        if (DEBUG)
          {
            Log.d(TAG, "Set new touch coords: " + touchCoords[0] + ", " + touchCoords[1]);
          }
      }

    // Function to set accelerometer values
    public void SetAccelValues(float valueX, float valueY, float valueZ)
      {
        boolean isNan = false;  // flag to make sure that divisor is a number
        double totalAccel = Math.sqrt(
          ( valueX * valueX ) + ( valueY * valueY ) + ( valueZ * valueZ ));  // total amount of accelration
        isNan = Double.isNaN(totalAccel); // test for not a number
        if (( totalAccel != 0 ) && ( isNan == false ))  // make sure we can divide correctly
          {
            accelValues[0] = (float) Math.asin(valueY / totalAccel);  // from accelration to tilt, X
            accelValues[1] = -(float) Math.asin(valueX / totalAccel) + 0.45f; // from accelration to tilt, Y
            accelValues[2] = (float) Math.asin(valueZ / totalAccel);  // from accelration to tilt, Z
          }
      }

    // Function to set thread state flag
    public void SetRunning(boolean isRunning)
      {
        running = isRunning;
        if (DEBUG)
          {
            Log.d(TAG, "Set game running.");
          }
      }

    // Function to set menu running
    public void SetMenuRunning(boolean menuState)
      {
        menu = menuState;
        if (DEBUG)
          {
            Log.d(TAG, "Set menu running.");
          }
      }

    // Function to pause game and record current state
    public void PauseGame()
      {
        paused = true;
      }

    // Function to resume correct state after pause
    public void ResumeGame()
      {
        paused = false;
      }

    // Collision check between the player ball and a jewel
    private boolean CheckCollision(PlayerBall quad1, Jewel quad2)
      {
        float xDistance = quad1.GetXCoordinate() - quad2.GetXCoordinate();  // calculate x-distance
        float yDistance = quad1.GetYCoordinate() - quad2.GetYCoordinate();  // calculate y-distance
        float length = (float) Math.sqrt(
          ( xDistance * xDistance ) + ( yDistance * yDistance ));  // get the distance with pythagoras statement
        // if length is less then the bounding sphere radiuses, collision has happened
        if (length < ( 0.9f * quad1.GetRadius() + 0.9f * quad2.GetRadius() ))
          {
            return true;
          }
        else
          {
            return false;
          }
      }

    // Function to add new objects
    private void AddObjects(int type)
      {
        boolean isNegative = false;  // if random is negative
        int newX = 0, newY = 0;  // to hold the locations
        Jewel addJewel;
        int index = 0;
        int emptyIndex = -1;
        boolean foundEmpty = false;
        boolean acceptableCoordinate = false;
        while (!acceptableCoordinate)
          {
            isNegative = rand.nextBoolean();  // generate negativity
            newX = rand.nextInt(MAX_WIDTH - 15);  // generate x-coord
            if (isNegative == true)  // make negative if boolean says so
              {
                newX *= -1;
              }
            for (PlayerBall i: playerList)
              {
                int difference = (int) i.GetXCoordinate() - newX;
                if (difference < 0)
                  {
                    difference *= -1;
                  }
                if (difference > i.GetRadius())
                  {
                    acceptableCoordinate = true;
                  }
                else
                  {
                    acceptableCoordinate = false;
                  }
              }
            for (Jewel i: jewelList)
              {
                int difference = (int) i.GetXCoordinate() - newX;
                if (difference < 0)
                  {
                    difference *= -1;
                  }
                if (difference > i.GetRadius())
                  {
                    acceptableCoordinate = true;
                  }
                else
                  {
                    acceptableCoordinate = false;
                  }
              }
          }
        acceptableCoordinate = false;
        while (!acceptableCoordinate)
          {
            isNegative = rand.nextBoolean();  // repeat as x
            if (isNegative == true)
              {
                newY = rand.nextInt(MAX_HEIGHT - 40);
                newY *= -1;
              }
            else
              {
                newY = rand.nextInt(MAX_HEIGHT - 15);
              }
            for (PlayerBall i: playerList)
              {
                int difference = (int) i.GetYCoordinate() - newY;
                if (difference < 0)
                  {
                    difference *= -1;
                  }
                if (difference > i.GetRadius())
                  {
                    acceptableCoordinate = true;
                  }
                else
                  {
                    acceptableCoordinate = false;
                  }
              }
            for (Jewel i: jewelList)
              {
                int difference = (int) i.GetYCoordinate() - newY;
                if (difference < 0)
                  {
                    difference *= -1;
                  }
                if (difference > i.GetRadius())
                  {
                    acceptableCoordinate = true;
                  }
                else
                  {
                    acceptableCoordinate = false;
                  }
              }
          }
        while (!foundEmpty)
          {
            if (jewelList.size() > index)
              {
                if (jewelList.get(index) == null)
                  {
                    foundEmpty = true;
                    emptyIndex = index;
                    if (DEBUG)
                      {
                        Log.d(TAG, "Found an empty index to replace: " + emptyIndex);
                      }
                  }
              }
            else
              {
                foundEmpty = true;
              }
            index++;
          }
        if (type == 0)
          {
            addJewel = new Jewel(12f, 50, type, SCALE);
          }
        else
          {
            addJewel = new Jewel(14f, 150, type, SCALE);
          }
        addJewel.SetCoordinates(newX, newY, 0);
        addJewel.SetTexture(type + 2);
        addJewel.Draw();
        addJewel.GetBuffers()
          .CreateBuffers();
        if (emptyIndex >= 0)
          {
            try
              {
                addJewel.SetListIndex(emptyIndex);
                jewelList.set(emptyIndex, addJewel);
                if (DEBUG)
                  {
                    Log.d(TAG, "GameLoop: Replaced diamond succesfully.");
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (IndexOutOfBoundsException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
          }
        else
          {
            try
              {
                addJewel.SetListIndex(jewelList.size());
                jewelList.add(addJewel);
                if (DEBUG)
                  {
                    Log.d(TAG, "GameLoop: Added diamond succesfully.");
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
          }
        renderer.SynchronizeJewels(jewelList);
      }

    // Function to add new objects
    private void AddObjects(int type, int x, int y)
      {
        Jewel addJewel;
        int index = 0;
        int emptyIndex = -1;
        boolean foundEmpty = false;
        while (!foundEmpty)
          {
            if (jewelList.size() > index)
              {
                if (jewelList.get(index) == null)
                  {
                    foundEmpty = true;
                    emptyIndex = index;
                    if (DEBUG)
                      {
                        Log.d(TAG, "Found an empty index to replace: " + emptyIndex);
                      }
                  }
              }
            else
              {
                foundEmpty = true;
              }
            index++;
          }
        if (type == 0)
          {
            addJewel = new Jewel(12f, 50, type, SCALE);
          }
        else
          {
            addJewel = new Jewel(14f, 150, type, SCALE);
          }
        addJewel.SetCoordinates(x, y, 0);
        addJewel.SetTexture(type + 2);
        addJewel.Draw();
        addJewel.GetBuffers()
          .CreateBuffers();
        if (emptyIndex >= 0)
          {
            try
              {
                addJewel.SetListIndex(emptyIndex);
                jewelList.set(emptyIndex, addJewel);
                if (DEBUG)
                  {
                    Log.d(TAG, "GameLoop: Replaced diamond succesfully.");
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (IndexOutOfBoundsException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed replacing diamond: " + e);
                  }
              }
          }
        else
          {
            try
              {
                addJewel.SetListIndex(jewelList.size());
                jewelList.add(addJewel);
                if (DEBUG)
                  {
                    Log.d(TAG, "GameLoop: Added diamond succesfully.");
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding diamond: " + e);
                  }
              }
          }
        renderer.SynchronizeJewels(jewelList);
      }

    // Function to add new objects
    private void GenerateSparkles(float centerX, float centerY)
      {
        boolean isNegative = false;  // if random is negative
        int newX = 0, newY = 0;  // to hold the locations
        Sparkle addSparkle;
        int index = 0;
        int emptyIndex = -1;
        boolean foundEmpty = false;
        int numSparkles = 1;
        numSparkles += rand.nextInt(8);
        for (int i = 0; i < numSparkles; i++)
          {
            isNegative = rand.nextBoolean();  // generate negativity
            newX = rand.nextInt(21);  // generate x-coord
            if (isNegative == true)  // make negative if boolean says so
              {
                newX *= -1;
              }
            isNegative = rand.nextBoolean();  // repeat as x
            newY = rand.nextInt(21);
            if (isNegative == true)
              {
                newY *= -1;
              }
            while (!foundEmpty)
              {
                if (sparkleList.size() > index)
                  {
                    if (sparkleList.get(index) == null)
                      {
                        foundEmpty = true;
                        emptyIndex = index;
                        if (DEBUG)
                          {
                            Log.d(TAG, "Found an empty index to replace: " + emptyIndex);
                          }
                      }
                  }
                else
                  {
                    foundEmpty = true;
                  }
                index++;
              }
            addSparkle = new Sparkle(8f, ( rand.nextInt(3) + 1 ), SCALE);
            addSparkle.SetCoordinates(( centerX + newX ), ( centerY + newY ), 0);
            addSparkle.SetTexture(19);
            addSparkle.Draw();
            addSparkle.GetBuffers()
              .CreateBuffers();
            if (emptyIndex >= 0)
              {
                try
                  {
                    addSparkle.SetListIndex(emptyIndex);
                    sparkleList.set(emptyIndex, addSparkle);
                    if (DEBUG)
                      {
                        Log.d(TAG, "GameLoop: Replaced sparkle succesfully.");
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed replacing sparkle: " + e);
                      }
                  }
                catch (ClassCastException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed replacing sparkle: " + e);
                      }
                  }
                catch (IndexOutOfBoundsException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed replacing sparkle: " + e);
                      }
                  }
                catch (IllegalArgumentException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed replacing sparkle: " + e);
                      }
                  }
              }
            else
              {
                try
                  {
                    addSparkle.SetListIndex(jewelList.size());
                    sparkleList.add(addSparkle);
                    if (DEBUG)
                      {
                        Log.d(TAG, "GameLoop: Added sparkle succesfully.");
                      }
                  }
                catch (IllegalArgumentException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding sparkle: " + e);
                      }
                  }
                catch (ClassCastException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding sparkle: " + e);
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding sparkle: " + e);
                      }
                  }
              }
          }
        renderer.SynchronizeSparkle(sparkleList);
      }

    // Function to update score
    private void UpdateScore()
      {
        int thousands = numPoints / 1000;  // count how many thousands
        int tens = numPoints / 10;  // count how many tens
        int hundreds = numPoints / 100;  // count how many hundreds
        int tenthousands = numPoints / 10000;  // count how many tens of thousands
        int correctLevel = 1;
        int speedLevel = 75;
        // make sure we stay with single digits with all the values
        if (thousands > 9)
          {
            while (thousands > 9)
              {
                thousands -= 10;
              }
          }
        if (tens > 9)
          {
            while (tens > 9)
              {
                tens -= 10;
              }
          }
        if (hundreds > 9)
          {
            while (hundreds > 9)
              {
                hundreds -= 10;
              }
          }
        if (tenthousands > 9)
          {
            while (tenthousands > 9)
              {
                tenthousands -= 10;
              }
          }
        if (gameMode == 0)
          {
            correctLevel += tenthousands;
          }
        if (gameMode == 1)
          {
            correctLevel += tenthousands * 2 + thousands / 5;
          }
        if (level != correctLevel)
          {
            level = correctLevel;
          }
        if (gameMode == 0)
          {
            speedLevel -= tenthousands * 2 + thousands / 5;
          }
        if (gameMode == 1)
          {
            speedLevel -= tenthousands * 5 + thousands / 2;
          }
        if (speedLevel != addSpeed)
          {
            addSpeed = speedLevel;
          }
        if (addSpeed < 10)
          {
            addSpeed = 10;
          }
        // set textures accordingly
        scoreNums[0].SetTexture(tenthousands + 7);
        scoreNums[1].SetTexture(thousands + 7);
        scoreNums[2].SetTexture(hundreds + 7);
        scoreNums[3].SetTexture(tens + 7);
        updateScore = false;
      }

    // Function to update score
    private void UpdateHighScore()
      {
        int thousands = highScore / 1000;  // count how many thousands
        int tens = highScore / 10;  // count how many tens
        int hundreds = highScore / 100;  // count how many hundreds
        int tenthousands = highScore / 10000;  // count how many tens of thousands
        // make sure we stay with single digits with all the values
        if (thousands > 9)
          {
            while (thousands > 9)
              {
                thousands -= 10;
              }
          }
        if (tens > 9)
          {
            while (tens > 9)
              {
                tens -= 10;
              }
          }
        if (hundreds > 9)
          {
            while (hundreds > 9)
              {
                hundreds -= 10;
              }
          }
        if (tenthousands > 9)
          {
            while (tenthousands > 9)
              {
                tenthousands -= 10;
              }
          }
        // set textures accordingly
        highScoreNums[0].SetTexture(tenthousands + 7);
        highScoreNums[1].SetTexture(thousands + 7);
        highScoreNums[2].SetTexture(hundreds + 7);
        highScoreNums[3].SetTexture(tens + 7);
      }

    private void ResetTouchCoords()
      {
        touchCoords[0] = 1000;  // set default touch coords out of bounds so it doesn't instantly select
        touchCoords[1] = 1000;
      }

    // Function to load and set the UI shown during gameplay
    private boolean InitializeGameUI()
      {
        // Create quads for score text and numbers
        scoreText = new UIElement(25f, SCALE);
        scoreText.SetTexture(5);
        if (DEBUG)
          {
            Log.d(TAG, "Added score text to render.");
          }
        scoreText.Draw();
        scoreText.GetBuffers()
          .CreateBuffers();
        scoreText.SetCoordinates(-MAX_WIDTH + 45 * SCALE, -MAX_HEIGHT, 0.1f);
        try
          {
            uiElementList.add(scoreText);
            scoreText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Score text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Score text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Score text: " + e);
              }
          }
        for (int i = 0; i < 5; i++)
          {
            scoreNums[i] = new UIElement(10f, SCALE);
            scoreNums[i].SetTexture(7);
            if (DEBUG)
              {
                Log.d(TAG, "Added score num" + i + " to render.");
              }
            scoreNums[i].Draw();
            scoreNums[i].GetBuffers()
              .CreateBuffers();
            scoreNums[i].SetCoordinates(-MAX_WIDTH + 45 * SCALE + scoreText.GetRadius() + i * 20 * SCALE,
              -MAX_HEIGHT + 18 * SCALE,
              0.1f);
            try
              {
                uiElementList.add(scoreNums[i]);
                scoreNums[i].SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Score number: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Score number: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Score number: " + e);
                  }
              }
          }
        // Create quad for lives text and lives count display
        livesText = new UIElement(25f, SCALE);
        livesText.SetTexture(6);
        if (DEBUG)
          {
            Log.d(TAG, "Added lives text to renderer.");
          }
        livesText.Draw();
        livesText.GetBuffers()
          .CreateBuffers();
        livesText.SetCoordinates(MAX_WIDTH - 45 * SCALE, -MAX_HEIGHT, 0.1f);
        try
          {
            uiElementList.add(livesText);
            livesText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives text: " + e);
              }
          }
        livesNums = new UIElement(10f, SCALE);
        livesNums.SetTexture(numLives + 7);
        if (DEBUG)
          {
            Log.d(TAG, "Added lives num to renderer.");
          }
        livesNums.Draw();
        livesNums.GetBuffers()
          .CreateBuffers();
        livesNums.SetCoordinates(MAX_WIDTH - 50 * SCALE - livesText.GetRadius(), -MAX_HEIGHT + 16 * SCALE, 0.1f);
        try
          {
            uiElementList.add(livesNums);
            livesNums.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives number: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives number: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Lives number: " + e);
              }
          }
        return true;
      }

    private boolean InitializePlayer(int num)
      {
        if (num == 1)
          {
            // Create the quads for the wisps
            player1 = new PlayerBall(14f, SCALE);
            player1.SetMovemenetBounds(MB_MAX_X, MB_MAX_Y, MB_MIN_X, MB_MIN_Y);
            player1.SetTexture(0);
            player1.Draw();
            player1.GetBuffers()
              .CreateBuffers();
            try
              {
                playerList.add(player1);
                player1.SetListIndex(playerList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player1: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player1: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player1: " + e);
                  }
              }
          }
        if (num == 2)
          {
            player2 = new PlayerBall(14f, SCALE);
            player2.SetMovemenetBounds(MB_MAX_X, MB_MAX_Y, MB_MIN_X, MB_MIN_Y);
            player2.SetTexture(1);
            player2.Draw();
            player2.GetBuffers()
              .CreateBuffers();
            try
              {
                playerList.add(player2);
                player2.SetListIndex(playerList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player2: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player2: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Player2: " + e);
                  }
              }
          }
        return true;
      }

    private boolean InitializePads(int num)
      {
        pad1 = new Jewel(14f, 150, 3, SCALE);
        pad1.SetCoordinates(MAX_WIDTH - pad1.GetRadius() * 1.1f, 0, 0);
        pad1.SetTexture(22);
        pad1.Draw();
        pad1.GetBuffers()
          .CreateBuffers();
        try
          {
            pad1.SetListIndex(jewelList.size());
            jewelList.add(pad1);
            if (DEBUG)
              {
                Log.d(TAG, "GameLoop: Added pad1 succesfully.");
              }
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding pad1: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding pad1: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding pad1: " + e);
              }
          }
        if (num >= 2)
          {
            pad2 = new Jewel(14f, 150, 4, SCALE);
            pad2.SetCoordinates(-MAX_WIDTH + pad2.GetRadius() * 1.1f, 0, 0);
            pad2.SetTexture(23);
            pad2.Draw();
            pad2.GetBuffers()
              .CreateBuffers();
            try
              {
                pad2.SetListIndex(jewelList.size());
                jewelList.add(pad2);
                if (DEBUG)
                  {
                    Log.d(TAG, "GameLoop: Added pad2 succesfully.");
                  }
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding pad2: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding pad2: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding pad2: " + e);
                  }
              }
          }
        renderer.SynchronizeJewels(jewelList);
        return true;
      }

    private boolean LoadGame()
      {
        boolean loadingDone = false;
        numPoints = 0;
        level = 1;
        if (DEBUG)
          {
            Log.d(TAG, "Started loading game data.");
          }
        ResetTouchCoords();
        while (!loadingDone)
          {
            loadingDone = InitializeGameUI();
          }
        loadingDone = false;
        while (!loadingDone)
          {
            loadingDone = InitializePlayer(1);
          }
        loadingDone = false;
        if (numBalls > 1)
          {
            while (!loadingDone)
              {
                loadingDone = InitializePlayer(2);
              }
            loadingDone = false;
          }
        if (gameMode == 1)
          {
            while (!loadingDone)
              {
                loadingDone = InitializePads(2);
              }
            renderer.SynchronizeJewels(jewelList);
          }
        beginTime = 0;
        timeDiff = 0;
        sleepTime = 0;
        isCollision = false;
        renderer.SynchronizePlayer(playerList);
        renderer.SynchronizeUI(uiElementList);
        if (!noMusic)
          {
            soundManager.PlayMusic(2 + rand.nextInt(4));
          }
        if (DEBUG)
          {
            Log.d(TAG, "Game data loaded.");
          }
        return true;
      }

    private boolean LoadHighScore()
      {
        SharedPreferences achievements = main.getSharedPreferences(ACHIEVEMENTS, 0);
        try
          {
            highScore = achievements.getInt("highScore", 0);
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error loading high score: " + e);
              }
            return false;
          }
        return true;
      }

    private boolean LoadProgress()
      {
        SharedPreferences progress = main.getSharedPreferences(PROGRESS, 0);
        try
          {
            progressAmount = progress.getInt("progressAmount", 0);
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error loading progress amount: " + e);
              }
            return false;
          }
        try
          {
            progressLevel = progress.getInt("progressLevel", 0);
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error loading progress level: " + e);
              }
            return false;
          }
        return true;
      }

    private boolean LoadSettings()
      {
        SharedPreferences settings = main.getSharedPreferences(SETTINGS, 0);
        int settingValue = 0;
        try
          {
            settingValue = settings.getInt("noMusic", 0);
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error loading settings for music: " + e);
              }
            return false;
          }
        if (settingValue == 0)
          {
            noMusic = false;
          }
        else
          {
            noMusic = true;
          }
        try
          {
            settingValue = settings.getInt("noSounds", 0);
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Error loading settings for sounds: " + e);
              }
            return false;
          }
        if (settingValue == 0)
          {
            noSound = false;
          }
        else
          {
            noSound = true;
          }
        return true;
      }

    private boolean LoadMenu()
      {
        float barFill = 0;
        if (DEBUG)
          {
            Log.d(TAG, "Started loading menu.");
          }
        LoadHighScore();
        LoadProgress();
        menuBg = new UIElement(150f, SCALE);
        menuBg.SetTexture(17);
        menuBg.SetCoordinates(( MAX_WIDTH - menuBg.GetRadius() * 0.8f ), ( MAX_HEIGHT - menuBg.GetRadius() * 0.8f ), 0);
        menuBg.Draw();
        menuBg.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(menuBg);
            menuBg.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Title: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Title: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Title: " + e);
              }
          }
        play = new UIElement(85f, SCALE);
        play.Distort(1f, 0.3f);
        play.SetCoordinates(-MAX_WIDTH + play.GetRadius() * 2.5f, MAX_HEIGHT - play.GetRadius() * 2f, 0);
        play.SetTexture(18);
        play.Draw();
        play.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(play);
            play.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Play: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Play: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Play: " + e);
              }
          }
        gameMode0 = new UIElement(55f, SCALE);
        gameMode0.Distort(1f, 0.3f);
        gameMode0.SetCoordinates(-MAX_WIDTH * 0.8f, MAX_HEIGHT * 0.6f, 0);
        gameMode0.SetTexture(24);
        gameMode0.Draw();
        gameMode0.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(gameMode0);
            gameMode0.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding GameMode0: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding GameMode0: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding GameMode0: " + e);
              }
          }
        if (progressLevel >= 1)
          {
            gameMode1 = new UIElement(55f, SCALE);
            gameMode1.Distort(1f, 0.3f);
            gameMode1.SetCoordinates(-MAX_WIDTH * 0.1f, MAX_HEIGHT * 0.6f, 0);
            gameMode1.SetTexture(25);
            gameMode1.Draw();
            gameMode1.GetBuffers()
              .CreateBuffers();
            try
              {
                uiElementList.add(gameMode1);
                gameMode1.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode1: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode1: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode1: " + e);
                  }
              }
          }
        if (progressLevel >= 2)
          {
            gameMode2 = new UIElement(55f, SCALE);
            gameMode2.Distort(1f, 0.3f);
            gameMode2.SetCoordinates(-MAX_WIDTH * 0.45f, MAX_HEIGHT * 0.5f, 0);
            gameMode2.SetTexture(31);
            gameMode2.Draw();
            gameMode2.GetBuffers()
              .CreateBuffers();
            try
              {
                uiElementList.add(gameMode2);
                gameMode2.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode2: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode2: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding GameMode2: " + e);
                  }
              }
          }
        // Create quads for highscore text and numbers
        highText = new UIElement(25f, SCALE);
        highText.SetTexture(20);
        highText.Draw();
        highText.GetBuffers()
          .CreateBuffers();
        highText.SetCoordinates(-MAX_WIDTH + 45 * SCALE, -MAX_HEIGHT + 1 * SCALE, 0.1f);
        try
          {
            uiElementList.add(highText);
            highText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        highScoreText = new UIElement(25f, SCALE);
        highScoreText.SetTexture(5);
        if (DEBUG)
          {
            Log.d(TAG, "Added highscore text to render.");
          }
        highScoreText.Draw();
        highScoreText.GetBuffers()
          .CreateBuffers();
        highScoreText.SetCoordinates(( -MAX_WIDTH + highText.GetRadius() * 2.4f ), -MAX_HEIGHT, 0.1f);
        try
          {
            uiElementList.add(highScoreText);
            highScoreText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Highscore text: " + e);
              }
          }
        for (int i = 0; i < 5; i++)
          {
            highScoreNums[i] = new UIElement(10f, SCALE);
            highScoreNums[i].SetTexture(7);
            if (DEBUG)
              {
                Log.d(TAG, "Added highscore num" + i + " to render.");
              }
            highScoreNums[i].Draw();
            highScoreNums[i].GetBuffers()
              .CreateBuffers();
            highScoreNums[i].SetCoordinates(
              -MAX_WIDTH + highText.GetRadius() * 2.5f + highScoreText.GetRadius() + i * 20 * SCALE,
              -MAX_HEIGHT + 18 * SCALE,
              0.1f);
            try
              {
                uiElementList.add(highScoreNums[i]);
                highScoreNums[i].SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Highscore number: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Highscore number: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding Highscore number: " + e);
                  }
              }
          }
        UpdateHighScore();
        if (progressLevel < MAX_PROGRESS)
          {
            progressBg = new UIElement(125f, SCALE);
            progressBg.SetTexture(29);
            if (DEBUG)
              {
                Log.d(TAG, "Added progress background to render.");
              }
            progressBg.Draw();
            progressBg.GetBuffers()
              .CreateBuffers();
            progressBg.SetCoordinates(( MAX_WIDTH - progressBg.GetRadius() ), -MAX_HEIGHT * 1.37f, 0.1f);
            try
              {
                uiElementList.add(progressBg);
                progressBg.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding progress background: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding progress background: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding progress background: " + e);
                  }
              }
            barFill = progressAmount / ( 50000f + progressLevel * 50000f );
            if (barFill > 0f)
              {
                progressBar = new UIElement(125f, SCALE);
                progressBar.SetTexture(30);
                progressBar.SetCoordinates(( MAX_WIDTH - progressBar.GetRadius() ), -MAX_HEIGHT * 1.37f, 0.1f);
                if (barFill < 1)
                  {
                    progressBar.Distort(barFill, 1);
                  }
                progressBar.Draw();
                progressBar.GetBuffers()
                  .CreateBuffers();
                try
                  {
                    uiElementList.add(progressBar);
                    progressBar.SetListIndex(uiElementList.size());
                  }
                catch (IllegalArgumentException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding progress bar: " + e);
                      }
                  }
                catch (ClassCastException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding progress bar: " + e);
                      }
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Failed adding progress bar: " + e);
                      }
                  }
              }
          }
        sfx = new UIElement(25f, SCALE);
        if (!noSound)
          {
            sfx.SetTexture(42);
          }
        else
          {
            sfx.SetTexture(43);
          }
        sfx.SetCoordinates(( MAX_WIDTH - sfx.GetRadius() * 0.8f ), ( 0 - sfx.GetRadius() * 0.8f ), 0);
        sfx.Draw();
        sfx.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(sfx);
            sfx.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding SFX button: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding SFX button: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding SFX button: " + e);
              }
          }
        music = new UIElement(25f, SCALE);
        if (!noMusic)
          {
            music.SetTexture(44);
          }
        else
          {
            music.SetTexture(45);
          }
        music.SetCoordinates(( MAX_WIDTH - music.GetRadius() * 0.8f ),
          ( 0 - sfx.GetRadius() * 1.1f - music.GetRadius() * 1.1f ),
          0);
        music.Draw();
        music.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(music);
            music.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Music button: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Music button: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding Music button: " + e);
              }
          }
        renderer.SynchronizeUI(uiElementList);
        ResetTouchCoords();
        if (!noMusic)
          {
            soundManager.PlayMusic(1);
          }
        if (DEBUG)
          {
            Log.d(TAG, "Loaded game menu.");
          }
        return true;
      }

    private void ManageSparkles()
      {
        for (Sparkle i: sparkleList)
          {
            boolean alive = true;
            if (i != null)
              {
                alive = i.Animate();
              }
            if (!alive)
              {
                try
                  {
                    sparkleList.set(i.GetListIndex(), null);
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error nulling sparkle: " + e);
                      }
                  }
                catch (IndexOutOfBoundsException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error nulling sparkle: " + e);
                      }
                  }
                catch (IllegalArgumentException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error nulling sparkle: " + e);
                      }
                  }
                catch (ClassCastException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error nulling sparkle: " + e);
                      }
                  }
              }
          }
        Sparkle temp = null;
        for (int i = sparkleList.size(); i > 0; i--)
          {
            try
              {
                temp = sparkleList.get(i - 1);
              }
            catch (IndexOutOfBoundsException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error removing sparkle: " + e);
                  }
              }
            if (temp == null)
              {
                try
                  {
                    sparkleList.remove(i - 1);
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error removing sparkle: " + e);
                      }
                  }
                catch (IndexOutOfBoundsException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error removing sparkle: " + e);
                      }
                  }
              }
          }
        for (int i = 0; i < sparkleList.size(); i++)
          {
            try
              {
                sparkleList.get(i)
                  .SetListIndex(i);
              }
            catch (IndexOutOfBoundsException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error changing indexes of sparkles: " + e);
                  }
              }
          }
        renderer.SynchronizeSparkle(sparkleList);
      }

    private boolean UnloadMenu()
      {
        StopMusic(false);
        if (DEBUG)
          {
            Log.d(TAG, "Started unloading menu.");
          }
        menuBg = null;
        play = null;
        try
          {
            uiElementList.clear();
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Gameloop: menu unload failed clearing: " + e);
              }
          }
        renderer.ClearUIList();
        if (DEBUG)
          {
            Log.d(TAG, "Unloaded game menu.");
          }
        return true;
      }

    private void TestForCollisions()
      {
        for (Jewel i: jewelList)
          {
            if (i != null)
              {
                isCollision = CheckCollision(player1, i);  // check for collision: player1
                if (isCollision == true)  // collision has happened
                  {
                    if (gameMode == 0)
                      {
                        GenerateSparkles(i.GetXCoordinate(), i.GetYCoordinate());
                        if (i.GetType() == 2)
                          {
                            renderer.Flash(0.4f, 0, 0);
                            numLives--;
                            livesNums.SetTexture(numLives + 7);
                            if (!noSound)
                              {
                                soundManager.PlaySound(0);
                              }
                          }
                        else
                          {
                            renderer.Flash(0.2f, 0.2f, 0);
                            if (!noSound)
                              {
                                soundManager.PlaySound(2);
                              }
                            numPoints += i.GetScoreValue();  // correct collision, add points
                            updateScore = true;  // set the flag that we update score
                          }
                        try
                          {
                            jewelList.set(i.GetListIndex(), null);
                            currentNumDiamonds--;
                          }
                        catch (UnsupportedOperationException e)
                          {
                            if (DEBUG)
                              {
                                Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                              }
                          }
                        catch (IndexOutOfBoundsException e)
                          {
                            if (DEBUG)
                              {
                                Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                              }
                          }
                        catch (IllegalArgumentException e)
                          {
                            if (DEBUG)
                              {
                                Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                              }
                          }
                        catch (ClassCastException e)
                          {
                            if (DEBUG)
                              {
                                Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                              }
                          }
                      }
                    if (gameMode == 1 || gameMode == 2)
                      {
                        if (( ( ( i.GetType() == 2 && player1.GetType() == 1 ) ||
                                ( i.GetType() == 1 && player1.GetType() == 2 ) ) && !reversed ) || ( reversed &&
                                                                                                     ( ( i.GetType() ==
                                                                                                         2 &&
                                                                                                         player1.GetType() ==
                                                                                                         2 ) ||
                                                                                                       ( i.GetType() ==
                                                                                                         1 &&
                                                                                                         player1.GetType() ==
                                                                                                         1 ) ) ))
                          {
                            GenerateSparkles(i.GetXCoordinate(), i.GetYCoordinate());
                            renderer.Flash(0.4f, 0, 0);
                            numLives--;
                            livesNums.SetTexture(numLives + 7);
                            if (!noSound)
                              {
                                soundManager.PlaySound(0);
                              }
                          }
                        else
                          {
                            if (i.GetType() == 4 || i.GetType() == 3)
                              {
                                player1.ChangeType(i.GetType() - 2);
                                player1.SetTexture(i.GetType() - 3);
                                //renderer.Flash(0.2f, 0.2f, 0.2f);
                              }
                            else
                              {
                                GenerateSparkles(i.GetXCoordinate(), i.GetYCoordinate());
                                if (player1.GetType() == 1)
                                  {
                                    renderer.Flash(0.15f, 0.15f, 0);
                                    if (!noSound)
                                      {
                                        soundManager.PlaySound(2);
                                      }
                                  }
                                if (player1.GetType() == 2)
                                  {
                                    renderer.Flash(0.0f, 0.0f, 0.3f);
                                    if (!noSound)
                                      {
                                        soundManager.PlaySound(1);
                                      }
                                  }
                                numPoints += i.GetScoreValue();  // correct collision, add points
                                if (gameMode == 2)
                                  {
                                    newPoints += i.GetScoreValue();
                                  }
                                updateScore = true;  // set the flag that we update score
                              }
                          }
                        if (i.GetType() != 4 && i.GetType() != 3)
                          {
                            try
                              {
                                jewelList.set(i.GetListIndex(), null);
                                currentNumDiamonds--;
                              }
                            catch (UnsupportedOperationException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (IndexOutOfBoundsException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (IllegalArgumentException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (ClassCastException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                          }
                      }
                  }
                else
                  {
                    if (numBalls > 1)
                      {
                        isCollision = CheckCollision(player2, i);  // check for collision: player2
                        if (isCollision == true)  // otherwise same as above
                          {
                            currentNumDiamonds--;
                            GenerateSparkles(i.GetXCoordinate(), i.GetYCoordinate());
                            if (i.GetType() == 1)
                              {
                                renderer.Flash(0.4f, 0, 0);
                                numLives--;
                                livesNums.SetTexture(numLives + 7);
                                if (!noSound)
                                  {
                                    soundManager.PlaySound(0);
                                  }
                              }
                            else
                              {
                                renderer.Flash(0, 0, 0.3f);
                                if (!noSound)
                                  {
                                    soundManager.PlaySound(1);
                                  }
                                numPoints += i.GetScoreValue();
                                updateScore = true;
                              }
                            try
                              {
                                jewelList.set(i.GetListIndex(), null);
                              }
                            catch (UnsupportedOperationException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (IndexOutOfBoundsException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (IllegalArgumentException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                            catch (ClassCastException e)
                              {
                                if (DEBUG)
                                  {
                                    Log.e(TAG, "GameLoop: Error nulling due to collision: " + e);
                                  }
                              }
                          }
                      }
                  }
              }
            isCollision = false;  // reset flag
          }
        Jewel temp = null;
        int lowestRemoved = jewelList.size();
        for (int i = jewelList.size(); i > 0; i--)
          {
            try
              {
                temp = jewelList.get(i - 1);
              }
            catch (IndexOutOfBoundsException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error removing due to collisions: " + e);
                  }
              }
            if (temp == null)
              {
                try
                  {
                    jewelList.remove(i - 1);
                    lowestRemoved = i - 1;
                  }
                catch (UnsupportedOperationException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error removing due to collisions: " + e);
                      }
                  }
                catch (IndexOutOfBoundsException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error removing due to collisions: " + e);
                      }
                  }
              }
          }
        if (lowestRemoved < jewelList.size())
          {
            for (int i = lowestRemoved; i < jewelList.size(); i++)
              {
                try
                  {
                    jewelList.get(i)
                      .SetListIndex(i);
                  }
                catch (IndexOutOfBoundsException e)
                  {
                    if (DEBUG)
                      {
                        Log.e(TAG, "GameLoop: Error changing indexes due to collisions: " + e);
                      }
                  }
              }
          }
        renderer.SynchronizeJewels(jewelList);
      }

    private boolean SaveHighScore()
      {
        if (highScore < numPoints)
          {
            newHighScore = true;
            progressAmount += 1000f;
            SharedPreferences achievements = main.getSharedPreferences(ACHIEVEMENTS, 0);
            SharedPreferences.Editor editor = achievements.edit();
            editor.putInt("highScore", numPoints);
            return editor.commit();
          }
        else
          {
            return true;
          }
      }

    private boolean SaveSoundSettings()
      {
        SharedPreferences settings = main.getSharedPreferences(SETTINGS, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (noSound)
          {
            editor.putInt("noSound", 1);
          }
        else
          {
            editor.putInt("noSound", 0);
          }
        if (noMusic)
          {
            editor.putInt("noMusic", 1);
          }
        else
          {
            editor.putInt("noMusic", 0);
          }
        return editor.commit();
      }

    private boolean SaveProgress()
      {
        progressAmount += numPoints;
        if (progressLevel < MAX_PROGRESS)
          {
            if (progressAmount >= 50000 + progressLevel * 50000)
              {
                progressLevel++;
                progressAmount = 0;
              }
          }
        SharedPreferences progress = main.getSharedPreferences(PROGRESS, 0);
        SharedPreferences.Editor editor = progress.edit();
        editor.putInt("progressAmount", progressAmount);
        editor.putInt("progressLevel", progressLevel);
        return editor.commit();
      }

    private void Congratulate(int type)
      {
        gzText = new UIElement(75f, SCALE);
        gzText.Distort(1f, 0.5f);
        gzText.SetCoordinates(0, MAX_HEIGHT * 0.1f, 0);
        gzText.SetTexture(26);
        gzText.Draw();
        gzText.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(gzText);
            gzText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding congratulations text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding congratulations text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding congratulations text: " + e);
              }
          }
        // New highscore
        if (type == 0)
          {
            newHighText = new UIElement(75f, SCALE);
            newHighText.Distort(1f, 0.5f);
            newHighText.SetCoordinates(0, 0, 0);
            newHighText.SetTexture(27);
            newHighText.Draw();
            newHighText.GetBuffers()
              .CreateBuffers();
            try
              {
                uiElementList.add(newHighText);
                newHighText.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding new high score text: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding new high score text: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding new high score text: " + e);
                  }
              }
          }
        // Challenge complete
        if (type == 1)
          {
            newHighText = new UIElement(75f, SCALE);
            newHighText.Distort(1f, 0.5f);
            newHighText.SetCoordinates(0, 0, 0);
            newHighText.SetTexture(32);
            newHighText.Draw();
            newHighText.GetBuffers()
              .CreateBuffers();
            try
              {
                uiElementList.add(newHighText);
                newHighText.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding challenge done text: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding challenge done text: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding challenge done text: " + e);
                  }
              }
          }
        renderer.SynchronizeUI(uiElementList);
      }

    private void HandleGameOver()
      {
        boolean touched = false;
        ResetTouchCoords();
        renderer.Flash(-1f, -1f, -1f);
        gameOverText = new UIElement(100f, SCALE);
        gameOverText.SetCoordinates(0, MAX_HEIGHT - gameOverText.GetRadius(), 0);
        gameOverText.SetTexture(21);
        gameOverText.Draw();
        gameOverText.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(gameOverText);
            gameOverText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding game over text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding game over text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding game over text: " + e);
              }
          }
        renderer.SynchronizeUI(uiElementList);
        boolean saving = false;
        while (!saving)
          {
            saving = SaveHighScore();
          }
        saving = false;
        while (!saving)
          {
            saving = SaveProgress();
          }
        if (newHighScore)
          {
            newHighScore = false;
            Congratulate(0);
          }
        try
          {
            Thread.sleep(1000);
          }
        catch (InterruptedException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Error sleeping: " + e);
              }
          }
        tapText = new UIElement(100f, SCALE);
        tapText.Distort(1f, 0.5f);
        tapText.SetCoordinates(0, 0 - tapText.GetRadius(), 0);
        tapText.SetTexture(28);
        tapText.Draw();
        tapText.GetBuffers()
          .CreateBuffers();
        try
          {
            uiElementList.add(tapText);
            tapText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        renderer.SynchronizeUI(uiElementList);
        while (!touched)
          {
            touched = TestTouch(-MAX_WIDTH, MAX_WIDTH, -MAX_HEIGHT, MAX_HEIGHT);
          }
        StopMusic(true);
        renderer.ClearJewelList();
        renderer.ClearPlayerList();
        renderer.ClearUIList();
        renderer.ClearSparkleList();
        gameOver.run();
      }

    private boolean TestTouch(float minX, float maxX, float minY, float maxY)
      {
        // check if touch is inside bounds
        if (( touchCoords[0] >= minX ) && ( touchCoords[0] <= maxX ) && ( touchCoords[1] >= minY ) &&
            ( touchCoords[1] <= maxY ))
          {
            return true;
          }
        else
          {
            return false;
          }
      }

    private boolean LoadHelp(int helpFile)
      {
        ResetTouchCoords();
        if (helpFile < 4)
          {
            helpText = new UIElement(250f, SCALE);
            helpText.SetCoordinates(0, -10f, 0);
          }
        switch (helpFile)
          {
          case 1:
            helpText.SetTexture(33);
            break;
          case 2:
            helpText.SetTexture(34);
            break;
          case 3:
            helpText.SetTexture(35);
            break;
          case 4:
            helpText.SetTexture(36);
            break;
          case 5:
            helpText.SetTexture(37);
            InitializePads(1);
            player1.SetTexture(1);
            player1.ChangeType(2);
            break;
          case 6:
            helpText.SetTexture(38);
            InitializePads(2);
            reversed = true;
            break;
          case 7:
            helpText.SetTexture(39);
            InitializePads(2);
            reversed = false;
            break;
          case 8:
            helpText.SetTexture(40);
            InitializePads(2);
            break;
          case 9:
            helpText.SetTexture(41);
            player1.SetTexture(0);
            player1.ChangeType(1);
            break;
          default:
            break;
          }
        if (helpFile < 4)
          {
            helpText.Draw();
            helpText.GetBuffers()
              .CreateBuffers();
          }
        try
          {
            uiElementList.add(helpText);
            helpText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding help text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding help text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding help text: " + e);
              }
          }
        renderer.SynchronizeUI(uiElementList);
        try
          {
            Thread.sleep(1000);
          }
        catch (InterruptedException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Error sleeping: " + e);
              }
          }
        if (tapText == null)
          {
            tapText = new UIElement(100f, SCALE);
            tapText.Distort(1f, 0.5f);
            tapText.SetCoordinates(0, 0 - tapText.GetRadius(), 0);
            tapText.SetTexture(28);
            tapText.Draw();
            tapText.GetBuffers()
              .CreateBuffers();
          }
        try
          {
            uiElementList.add(tapText);
            tapText.SetListIndex(uiElementList.size());
          }
        catch (IllegalArgumentException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        catch (ClassCastException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
              }
          }
        renderer.SynchronizeUI(uiElementList);
        return true;
      }

    private boolean HandleChallenge(int challengeLevel)
      {
        short[] level1x = { -10, -8, -6, -4, -2, 2, 4, 6, 8, 10 };
        short[] level1y = { 2, 4, 6, 8, 10, -2, -4, -6, -8, -10 };
        short[] level1types = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        short[] level2x = { -10, -8, -6, -4, -2, 2, 4, 6, 8, 10, 20 };
        short[] level2y = { 2, 4, 6, 8, 10, -2, -4, -6, -8, -10, 20 };
        short[] level2types = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0 };
        short[] level3x = { -2, -4, -6, -8, -10, -5, 5, 10, 8, 6, 4, 2 };
        short[] level3y = { 5, 5, 5, -5, -5, -5, 5, 5, 5, -5, -5, -5 };
        short[] level3types = { 2, 2, 2, 0, 1, 2, 2, 1, 0, 1, 1, 1 };
        short[] level7x = { -10, -8, -6, -4, -2, 2, 4, 6, 8, 10, -10, -8, -6, -4, -2, 2, 4, 6, 8, 10, -12, -12, -12, 12,
          12, 12, -2, 2, -2, 2 };
        short[] level7y = { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -12, -2, 2, -12, -2,
          2, -2, -2, 2, 2 };
        short[] level7types = { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          1 };
        if (challengeLevel == 1)
          {
            if (challengeTracker <= 0)
              {
                player1.SetCoordinates(0.9f * -MAX_WIDTH, 0.9f * MAX_HEIGHT, 0);
                for (int i = 0; i < 10; i++)
                  {
                    AddObjects(level1types[i], MAX_WIDTH / level1x[i], MAX_HEIGHT / level1y[i]);
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
              }
            if (newPoints >= 500)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 2)
          {
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 65)
              {
                if (currentNumDiamonds < MAX_NUM_DIAMONDS && challengeTracker < 11)
                  {
                    AddObjects(level2types[challengeTracker],
                      MAX_WIDTH / level2x[challengeTracker],
                      MAX_HEIGHT / level2y[challengeTracker]);
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (newPoints >= 500)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 3)
          {
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 75)
              {
                if (currentNumDiamonds < MAX_NUM_DIAMONDS && challengeTracker < 12)
                  {
                    AddObjects(level3types[challengeTracker],
                      MAX_WIDTH / level3x[challengeTracker],
                      MAX_HEIGHT / level3y[challengeTracker]);
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (challengeTracker >= 12 && jewelList.size() <= 1)
              {
                numLives--;
                return true;
              }
            if (newPoints >= 1600)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 4)
          {
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 65)
              {
                if (currentNumDiamonds < MAX_NUM_DIAMONDS)
                  {
                    AddObjects(rand.nextInt(3));
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (newPoints >= 3000)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 5)
          {
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 55)
              {
                if (currentNumDiamonds < MAX_NUM_DIAMONDS)
                  {
                    AddObjects(rand.nextInt(3));
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (newPoints >= 5000)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 6)
          {
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 55)
              {
                if (currentNumDiamonds < MAX_NUM_DIAMONDS)
                  {
                    if (player1.GetType() == 1)
                      {
                        AddObjects(2);
                      }
                    if (player1.GetType() == 2)
                      {
                        AddObjects(1);
                      }
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (newPoints >= 3000)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        if (challengeLevel == 7)
          {
            if (challengeTracker <= 0)
              {
                player1.SetCoordinates(0.9f * -MAX_WIDTH, 0.9f * MAX_HEIGHT, 0);
                for (int i = 0; i < 20; i++)
                  {
                    AddObjects(level7types[i], MAX_WIDTH / level7x[i], MAX_HEIGHT / level7y[i]);
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
              }
            // increase object adder -tracker
            timeTracker += 1;
            // time to add, add a random jewel
            if (timeTracker >= 55)
              {
                if (challengeTracker < 30)
                  {
                    AddObjects(level7types[challengeTracker],
                      MAX_WIDTH / level7x[challengeTracker],
                      MAX_HEIGHT / level7y[challengeTracker]);
                    challengeTracker++;
                    currentNumDiamonds++;
                  }
                timeTracker = 0;  // reset tracker
              }
            if (challengeTracker >= 30 && jewelList.isEmpty())
              {
                return true;
              }
            if (newPoints >= 1500)
              {
                return true;
              }
            else
              {
                return false;
              }
          }
        return false;
      }

    private boolean FinishChallenge()
      {
        boolean touched = false;
        boolean loading = false;
        ResetTouchCoords();
        renderer.Flash(-1f, -1f, -1f);
        if (currentChallenge < MAX_CHALLENGES)
          {
            numPoints += currentChallenge * 1000;
            currentChallenge++;
            challengeTracker = 0;
            timeTracker = 0;
            newPoints = 0;
            challengeDone = false;
            Congratulate(1);
            tapText = new UIElement(100f, SCALE);
            tapText.Distort(1f, 0.5f);
            tapText.SetCoordinates(0, 0 - tapText.GetRadius(), 0);
            tapText.SetTexture(28);
            tapText.Draw();
            tapText.GetBuffers()
              .CreateBuffers();
            try
              {
                uiElementList.add(tapText);
                tapText.SetListIndex(uiElementList.size());
              }
            catch (IllegalArgumentException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
                  }
              }
            catch (ClassCastException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
                  }
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Failed adding tap text: " + e);
                  }
              }
            try
              {
                jewelList.clear();
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error clearing jewel list: " + e);
                  }
              }
            try
              {
                Thread.sleep(1000);
              }
            catch (InterruptedException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error sleeping: " + e);
                  }
              }
            renderer.SynchronizeJewels(jewelList);
            renderer.SynchronizeUI(uiElementList);
            ResetTouchCoords();
            while (!touched)
              {
                touched = TestTouch(-MAX_WIDTH, MAX_WIDTH, -MAX_HEIGHT, MAX_HEIGHT);
              }
            try
              {
                uiElementList.clear();
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error clearing UI list: " + e);
                  }
              }
            touched = false;
            LoadHelp(currentChallenge + 2);
            ResetTouchCoords();
            while (!touched)
              {
                touched = TestTouch(-MAX_WIDTH, MAX_WIDTH, -MAX_HEIGHT, MAX_HEIGHT);
              }
            try
              {
                uiElementList.clear();
              }
            catch (UnsupportedOperationException e)
              {
                if (DEBUG)
                  {
                    Log.e(TAG, "GameLoop: Error clearing UI list: " + e);
                  }
              }
            while (!loading)
              {
                loading = InitializeGameUI();
              }
            renderer.SynchronizeUI(uiElementList);
            return false;
          }
        else
          {
            HandleGameOver();
            return false;
          }
      }

    public void ReturnToMenu()
      {
        if (running)
          {
            numLives = 0;
          }
      }

    @Override public void run()
      {
        boolean loadingDone = false;  // boolean to see if we are loading
        boolean waiting = false;
        boolean selected1 = false;
        boolean selected2 = false;
        boolean selected3 = false;
        boolean selected4 = false;
        boolean selected5 = false;
        // load menu
        while (!loadingDone)
          {
            loadingDone = LoadMenu();
          }
        // reset the loading flag
        loadingDone = false;
        if (DEBUG)
          {
            Log.d(TAG, "In menu loop.");
          }
        if (DEBUG)
          {
            Log.d(TAG, "Coords to be waited, minX: " + ( gameMode0.GetXCoordinate() - gameMode0.GetRadius() ));
            Log.d(TAG, "Coords to be waited, maxX: " + ( gameMode0.GetXCoordinate() + gameMode0.GetRadius() ));
            Log.d(TAG, "Coords to be waited, minY: " + ( gameMode0.GetYCoordinate() - gameMode0.GetRadius() ));
            Log.d(TAG, "Coords to be waited, maxY: " + ( gameMode0.GetYCoordinate() + gameMode0.GetRadius() ));
          }
        // the menu loop
        while (menu)
          {
            // if we are paused, do nothing
            while (paused)
              {
              }
            selected1 = TestTouch(gameMode0.GetXCoordinate() - gameMode0.GetRadius(),
              gameMode0.GetXCoordinate() + gameMode0.GetRadius(),
              gameMode0.GetYCoordinate() - gameMode0.GetRadius(),
              gameMode0.GetYCoordinate() + gameMode0.GetRadius());
            // check if play CLASSIC has been touched
            if (selected1 && menu)
              {
                menu = false;
                SetGameMode(0);
              }
            if (progressLevel >= 1)
              {
                selected2 = TestTouch(gameMode1.GetXCoordinate() - gameMode1.GetRadius(),
                  gameMode1.GetXCoordinate() + gameMode1.GetRadius(),
                  gameMode1.GetYCoordinate() - gameMode1.GetRadius(),
                  gameMode1.GetYCoordinate() + gameMode1.GetRadius());
                // check if play TAG has been touched
                if (selected2 && menu)
                  {
                    menu = false;
                    SetGameMode(1);
                  }
              }
            if (progressLevel >= 2)
              {
                selected3 = TestTouch(gameMode2.GetXCoordinate() - gameMode2.GetRadius(),
                  gameMode2.GetXCoordinate() + gameMode2.GetRadius(),
                  gameMode2.GetYCoordinate() - gameMode2.GetRadius(),
                  gameMode2.GetYCoordinate() + gameMode2.GetRadius());
                // check if play CHALLENGE has been touched
                if (selected3 && menu)
                  {
                    menu = false;
                    SetGameMode(2);
                  }
              }
            selected4 = TestTouch(sfx.GetXCoordinate() - sfx.GetRadius(),
              sfx.GetXCoordinate() + sfx.GetRadius(),
              sfx.GetYCoordinate() - sfx.GetRadius(),
              sfx.GetYCoordinate() + sfx.GetRadius());
            // check if play CLASSIC has been touched
            if (selected4 && menu)
              {
                if (noSound)
                  {
                    noSound = false;
                    sfx.SetTexture(42);
                  }
                else
                  {
                    noSound = true;
                    sfx.SetTexture(43);
                  }
                SaveSoundSettings();
                ResetTouchCoords();
              }
            selected5 = TestTouch(music.GetXCoordinate() - music.GetRadius(),
              music.GetXCoordinate() + music.GetRadius(),
              music.GetYCoordinate() - music.GetRadius(),
              music.GetYCoordinate() + music.GetRadius());
            // check if play CLASSIC has been touched
            if (selected5 && menu)
              {
                if (noMusic)
                  {
                    noMusic = false;
                    music.SetTexture(44);
                    soundManager.PlayMusic(1);
                  }
                else
                  {
                    noMusic = true;
                    music.SetTexture(45);
                    soundManager.StopMusic(false);
                  }
                SaveSoundSettings();
                ResetTouchCoords();
              }
          }
        selected1 = false;
        selected2 = false;
        selected3 = false;
        if (DEBUG)
          {
            Log.d(TAG, "Unloading menu.");
          }
        // unload the menu
        while (!loadingDone)
          {
            loadingDone = UnloadMenu();
          }
        // reset the loading flag
        loadingDone = false;
        pregame = false;
        if (DEBUG)
          {
            Log.d(TAG, "In pre-game loop");
          }
        while (!loadingDone)
          {
            loadingDone = LoadHelp(gameMode + 1);
          }
        loadingDone = false;
        ResetTouchCoords();
        while (!pregame)
          {
            pregame = TestTouch(-MAX_WIDTH, MAX_WIDTH, -MAX_HEIGHT, MAX_HEIGHT);
          }
        try
          {
            uiElementList.clear();
          }
        catch (UnsupportedOperationException e)
          {
            if (DEBUG)
              {
                Log.e(TAG, "Gameloop: menu unload failed clearing: " + e);
              }
          }
        renderer.ClearUIList();
        // load the game
        while (loadingDone == false)
          {
            loadingDone = LoadGame();
          }
        // reset the loading flag
        loadingDone = false;
        pregame = false;
        if (DEBUG)
          {
            Log.d(TAG, "In game loop.");
          }
        // set the running flag true
        running = true;
        // the game loop
        while (running)
          {
            // Regular game modes (CLASSIC, TAG)
            if (gameMode != 2)
              {
                // if the game is paused do nothing
                while (paused)
                  {
                  }
                // reduce the background color gradually
                renderer.Flash(-0.005f, -0.005f, -0.005f);
                // store current time
                beginTime = System.currentTimeMillis();
                // move player according to accelerometer
                player1.Move(( accelValues[0] * 50 ), ( accelValues[1] * 25 ), 0);
                if (numBalls > 1)
                  {
                    player2.Move(( accelValues[0] * -50 ), ( accelValues[1] * -25 ), 0);
                  }
                // rotate the players to give more animation
                player1.Rotate(20);
                if (numBalls > 1)
                  {
                    player2.Rotate(-20);
                  }
                // test for any collisions
                TestForCollisions();
                // animate sparkles
                ManageSparkles();
                // calculate if sleeping is needed to have constant FPS
                timeDiff = System.currentTimeMillis() - beginTime;
                sleepTime = (int) ( FRAME_PERIOD - timeDiff );
                // increase object adder -tracker
                timeTracker += 1;
                // time to add, add a random jewel
                if (timeTracker >= addSpeed)
                  {
                    //numPoints++;	// award player some points for staying alive
                    // add jewels according to the current difficulty level
                    for (int i = 0; i < level; i++)
                      {
                        if (currentNumDiamonds < MAX_NUM_DIAMONDS)
                          {
                            AddObjects(rand.nextInt(3));
                            currentNumDiamonds++;
                          }
                      }
                    timeTracker = 0;  // reset tracker
                  }
                // check if score update is required
                if (updateScore == true)
                  {
                    UpdateScore();
                  }
                // need to sleep
                if (sleepTime > 0)
                  {
                    try
                      {
                        Thread.sleep(sleepTime);
                      }
                    catch (InterruptedException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GameLoop: Error sleeping: " + e);
                          }
                      }
                  }
                // check if the player is out of lives
                if (numLives <= 0)
                  {
                    HandleGameOver();
                  }
              }
            // Challenge - game mode
            else
              {
                // if the game is paused do nothing
                while (paused)
                  {
                  }
                // reduce the background color gradualy
                renderer.Flash(-0.005f, -0.005f, -0.005f);
                // store current time
                beginTime = System.currentTimeMillis();
                // move player according to accelerometer
                player1.Move(( accelValues[0] * 50 ), ( accelValues[1] * 25 ), 0);
                if (numBalls > 1)
                  {
                    player2.Move(( accelValues[0] * -50 ), ( accelValues[1] * -25 ), 0);
                  }
                // rotate the players to give more animation
                player1.Rotate(20);
                if (numBalls > 1)
                  {
                    player2.Rotate(-20);
                  }
                // test for any collisions
                TestForCollisions();
                // animate sparkles
                ManageSparkles();
                // calculate if sleeping is needed to have constant FPS
                timeDiff = System.currentTimeMillis() - beginTime;
                sleepTime = (int) ( FRAME_PERIOD - timeDiff );
                challengeDone = HandleChallenge(currentChallenge);
                if (challengeDone)
                  {
                    waiting = true;
                    while (waiting)
                      {
                        waiting = FinishChallenge();
                      }
                  }
                // check if score update is required
                if (updateScore == true)
                  {
                    UpdateScore();
                  }
                // need to sleep
                if (sleepTime > 0)
                  {
                    try
                      {
                        Thread.sleep(sleepTime);
                      }
                    catch (InterruptedException e)
                      {
                        if (DEBUG)
                          {
                            Log.e(TAG, "GameLoop: Error sleeping: " + e);
                          }
                      }
                  }
                // check if the player is out of lives
                if (numLives <= 0)
                  {
                    HandleGameOver();
                  }
              }
          }
      }
  }
