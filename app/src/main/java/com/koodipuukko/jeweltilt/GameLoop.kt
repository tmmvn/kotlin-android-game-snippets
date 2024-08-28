package com.koodipuukko.jeweltilt

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.ArrayList
import kotlin.math.asin
import kotlin.math.sqrt
import kotlin.random.Random

class GameLoop
	(
	mainThread: JewelTilt,
	passedContext: Context,
	glRenderer: GLRenderer,
	maxWidth: Int,
	maxHeight: Int
) : Thread() {
	private val gameOver: GameOver
	private val scale: Float // Screen scale difference to expected
	private val maxWidth: Int // Screen width
	private val maxHeight: Int // Screen height
	private val movementBoundsMinX: Int // Movement bounds
	private val movementBoundsMaxX: Int
	private val movementBoundsMinY: Int
	private val movementBoundsMaxY: Int
	private val playerList: MutableList<PlayerBall?> = ArrayList<PlayerBall?>()
	private val jewelList: MutableList<Jewel?> = ArrayList<Jewel?>()
	private val uiElementList: MutableList<UIElement?> = ArrayList<UIElement?>()
	private val sparkleList: MutableList<Sparkle?> = ArrayList<Sparkle?>()
	private var running = false
	private var pregame = false // Flag to see if game is running
	private var menu = true // flag to see if we are at the menu
	private var updateScore = false // flag to update score
	private var paused = false // flag to see if game is paused
	private var newHighScore = false
	private var challengeDone = false
	private var reversed = false
	private var noSound = false
	private var noMusic = false
	private val renderer: GLRenderer // Used renderer
	private var timeTracker: Long = 0 // Tracks time passed and then adds stuff
	private var addSpeed = 0 // Time between adds
	private var level = 0 // Sets the amount of adds at an update
	private var numBalls = 0 // Sets the number of balls the player controls
	private var currentNumDiamonds = 0
	private var gameMode = 0
	private var progressLevel = 0
	private var progressAmount = 0
	private var currentChallenge = 0
	private var newPoints = 0
	private val soundManager: SoundManager // The sound manager for the game
	private val main: JewelTilt
	private val context: Context
	// variables to be used to get constant FPS
	private var beginTime: Long = 0
	private var timeDiff: Long = 0
	private var sleepTime = 0
	private var isCollision =
		false // Boolean to check if collision has happened
	private var player1: PlayerBall? = null
	private var player2: PlayerBall? = null // player balls
	private var pad1: Jewel? = null
	private var pad2: Jewel? = null // Pads to change player color in TAG-mode
	private var menuBg: UIElement? = null
	private var play: UIElement? = null // menu items
	private var scoreText: UIElement? = null
	private var livesText: UIElement? = null
	private var highScoreText: UIElement? = null // hud texts
	private val scoreNums: Array<UIElement?> =
		arrayOfNulls<UIElement>(5) // numbers for score text
	private val highScoreNums: Array<UIElement?> =
		arrayOfNulls<UIElement>(5) // numbers for score text
	private var livesNums: UIElement? = null // number of lives text
	private var gameOverText: UIElement? = null
	private var gzText: UIElement? = null // Game over text
	private var highText: UIElement? = null
	private var newHighText: UIElement? = null
	private var gameMode0: UIElement? = null
	private var gameMode1: UIElement? = null
	private var gameMode2: UIElement? = null
	private var tapText: UIElement? = null
	private var progressBar: UIElement? = null
	private var progressBg: UIElement? = null
	private var helpText: UIElement? = null
	private var sfx: UIElement? = null
	private var music: UIElement? = null
	private var numLives = 0 // number of lives
	private var numPoints = 0 // number of points
	private var highScore = 0 // best score so far
	private var challengeTracker =
		0 // tracks which stage of challenge is happening
	private val accelValues = FloatArray(3) // Accelerometer values
	private val touchCoords = FloatArray(2) // To handle menu touch location
	private val rand: Random = Random.Default // initialize new random
	// Constructor to initialize stuff
	init {
		var loadingDone = false
		this.maxWidth = maxWidth // set max screen width
		this.maxHeight = maxHeight // set max screen height
		scale = (maxWidth / 400f + maxHeight / 240f) / 2f
		movementBoundsMinX = -this.maxWidth + 15 // set movement bounds: min X
		movementBoundsMinY = -this.maxHeight + 40 // set movement bounds: min Y
		movementBoundsMaxX = this.maxWidth - 15 // set movement bounds: max X
		movementBoundsMaxY = this.maxHeight - 15 // set movement bounds: max Y
		renderer = glRenderer // set renderer
		accelValues[0] = 0f // initialize acceleration values to 0
		accelValues[1] = 0f // as above
		accelValues[2] = 0f // as above
		context = passedContext // pass context so we can pass it on as required
		soundManager = SoundManager(context) // create a new sound manager
		main = mainThread
		gameOver =
			GameOver(main, this, passedContext, glRenderer, maxWidth, maxHeight)
		while(!loadingDone) {
			loadingDone = loadSettings()
		}
	}

	private fun setGameMode(gameModeToSet: Int) {
		timeTracker = 0
		currentNumDiamonds = 0
		gameMode = gameModeToSet
		newHighScore = false
		reversed = false				// Regular game mode
		if(gameModeToSet == 0) {
			numBalls = 2
			numLives = 5
			addSpeed = 75
		}				// "Tag" game mode
		if(gameModeToSet == 1) {
			numBalls = 1
			numLives = 3
			addSpeed = 75
		}				// "Challenge" game mode
		if(gameModeToSet == 2) {
			numBalls = 1
			numLives = 5
			addSpeed = 75
			currentChallenge = 1
			challengeDone = false
			challengeTracker = 0
		}				// "Inferno" game mode
		if(gameModeToSet == 3) {
			numBalls = 4
			numLives = 5
			addSpeed = 75
		}
	}
	// Function to tell the sound manager to stop music
	fun stopMusic(finalStop: Boolean) {
		soundManager.stopMusic(finalStop)
	}
	// Function to tell the sound manager to pause music
	fun pauseMusic() {
		soundManager.pauseMusic()
	}
	// Function to tell the sound manager to resume music
	fun resumeMusic() {
		soundManager.resumeMusic()
	}
	// Function to set the touch coordinates
	fun setTouchCoords(valueX: Float, valueY: Float) {
		touchCoords[0] = valueX - maxWidth
		touchCoords[1] = (valueY - maxHeight) * -1
		if(DEBUG) {
			Log.d(
				TAG,
				"Set new touch coords: " + touchCoords[0] + ", " + touchCoords[1]
			)
		}
	}
	// Function to set accelerometer values
	fun setAccelValues(valueX: Float, valueY: Float, valueZ: Float) {
		var isNan = false // flag to make sure that divisor is a number
		val totalAccel: Double = sqrt(
			((valueX * valueX) + (valueY * valueY) + (valueZ * valueZ)).toDouble()
		) // total amount of accelration
		isNan = totalAccel.isNaN() // test for not a number
		if((totalAccel != 0.0) && !isNan) // make sure we can divide correctly
		{
			accelValues[0] =
				asin(valueY / totalAccel).toFloat() // from accelration to tilt, X
			accelValues[1] =
				(-asin(valueX / totalAccel)).toFloat() + 0.45f // from accelration to tilt, Y
			accelValues[2] =
				asin(valueZ / totalAccel).toFloat() // from accelration to tilt, Z
		}
	}
	// Function to set thread state flag
	fun setRunning(isRunning: Boolean) {
		running = isRunning
		if(DEBUG) {
			Log.d(TAG, "Set game running.")
		}
	}
	// Function to set menu running
	fun setMenuRunning(menuState: Boolean) {
		menu = menuState
		if(DEBUG) {
			Log.d(TAG, "Set menu running.")
		}
	}
	// Function to pause game and record current state
	fun pauseGame() {
		paused = true
	}
	// Function to resume correct state after pause
	fun resumeGame() {
		paused = false
	}
	// Collision check between the player ball and a jewel
	private fun checkCollision(quad1: PlayerBall, quad2: Jewel): Boolean {
		val xDistance: Float =
			quad1.getXCoordinate() - quad2.getXCoordinate() // calculate x-distance
		val yDistance: Float =
			quad1.getYCoordinate() - quad2.getYCoordinate() // calculate y-distance
		val length = sqrt(
			(xDistance * xDistance) + (yDistance * yDistance)
		) as Float // get the distance with pythagoras statement				// if length is less then the bounding sphere radiuses, collision has happened
		return if(length < (0.9f * quad1.getRadius() + 0.9f * quad2.getRadius
				())) {
			true
		} else {
			false
		}
	}
	// Function to add new objects
	private fun addObjects(type: Int) {
		var isNegative = false // if random is negative
		var newX = 0
		var newY = 0 // to hold the locations
		val addJewel: Jewel
		var index = 0
		var emptyIndex = -1
		var foundEmpty = false
		var acceptableCoordinate = false
		while(!acceptableCoordinate) {
			isNegative = rand.nextBoolean() // generate negativity
			newX = rand.nextInt(maxWidth - 15) // generate x-coord
			if(isNegative) // make negative if boolean says so
			{
				newX *= -1
			}
			for(i in playerList) {
				var difference = i?.getXCoordinate()!!.toInt() - newX
				if(difference < 0) {
					difference *= -1
				}
				acceptableCoordinate = if(difference > i.getRadius()) {
					true
				} else {
					false
				}
			}
			for(i in jewelList) {
				var difference = i?.getXCoordinate()!!.toInt() - newX
				if(difference < 0) {
					difference *= -1
				}
				acceptableCoordinate = if(difference > i.getRadius()) {
					true
				} else {
					false
				}
			}
		}
		acceptableCoordinate = false
		while(!acceptableCoordinate) {
			isNegative = rand.nextBoolean() // repeat as x
			if(isNegative) {
				newY = rand.nextInt(maxHeight - 40)
				newY *= -1
			} else {
				newY = rand.nextInt(maxHeight - 15)
			}
			for(i in playerList) {
				var difference = i?.getYCoordinate()!!.toInt() - newY
				if(difference < 0) {
					difference *= -1
				}
				acceptableCoordinate = if(difference > i.getRadius()) {
					true
				} else {
					false
				}
			}
			for(i in jewelList) {
				var difference = i?.getYCoordinate()!!.toInt() - newY
				if(difference < 0) {
					difference *= -1
				}
				acceptableCoordinate = if(difference > i.getRadius()) {
					true
				} else {
					false
				}
			}
		}
		while(!foundEmpty) {
			if(jewelList.size > index) {
				if(jewelList[index] == null) {
					foundEmpty = true
					emptyIndex = index
					if(DEBUG) {
						Log.d(
							TAG,
							"Found an empty index to replace: $emptyIndex"
						)
					}
				}
			} else {
				foundEmpty = true
			}
			index++
		}
		if(type == 0) {
			addJewel = Jewel(12f, 50, type, scale)
		} else {
			addJewel = Jewel(14f, 150, type, scale)
		}
		addJewel.setCoordinates(newX.toFloat(), newY.toFloat(), 0f)
		addJewel.setTexture(type + 2)
		addJewel.draw()
		addJewel.getBuffers().createBuffers()
		if(emptyIndex >= 0) {
			try {
				addJewel.setListIndex(emptyIndex)
				jewelList.set(emptyIndex, addJewel)
				if(DEBUG) {
					Log.d(TAG, "GameLoop: Replaced diamond succesfully.")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: IndexOutOfBoundsException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			}
		} else {
			try {
				addJewel.setListIndex(jewelList.size)
				jewelList.add(addJewel)
				if(DEBUG) {
					Log.d(TAG, "GameLoop: Added diamond succesfully.")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			}
		}
		renderer.synchronizeJewels(jewelList)
	}
	// Function to add new objects
	private fun addObjects(type: Int, x: Int, y: Int) {
		val addJewel: Jewel
		var index = 0
		var emptyIndex = -1
		var foundEmpty = false
		while(!foundEmpty) {
			if(jewelList.size > index) {
				if(jewelList[index] == null) {
					foundEmpty = true
					emptyIndex = index
					if(DEBUG) {
						Log.d(
							TAG,
							"Found an empty index to replace: $emptyIndex"
						)
					}
				}
			} else {
				foundEmpty = true
			}
			index++
		}
		if(type == 0) {
			addJewel = Jewel(12f, 50, type, scale)
		} else {
			addJewel = Jewel(14f, 150, type, scale)
		}
		addJewel.setCoordinates(x.toFloat(), y.toFloat(), 0f)
		addJewel.setTexture(type + 2)
		addJewel.draw()
		addJewel.getBuffers().createBuffers()
		if(emptyIndex >= 0) {
			try {
				addJewel.setListIndex(emptyIndex)
				jewelList[emptyIndex] = addJewel
				if(DEBUG) {
					Log.d(TAG, "GameLoop: Replaced diamond succesfully.")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: IndexOutOfBoundsException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed replacing diamond: $e")
				}
			}
		} else {
			try {
				addJewel.setListIndex(jewelList.size)
				jewelList.add(addJewel)
				if(DEBUG) {
					Log.d(TAG, "GameLoop: Added diamond succesfully.")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding diamond: $e")
				}
			}
		}
		renderer.synchronizeJewels(jewelList)
	}
	// Function to add new objects
	private fun generateSparkles(centerX: Float, centerY: Float) {
		var isNegative = false // if random is negative
		var newX = 0
		var newY = 0 // to hold the locations
		var addSparkle: Sparkle
		var index = 0
		var emptyIndex = -1
		var foundEmpty = false
		var numSparkles = 1
		numSparkles += rand.nextInt(8)
		for(i in 0 until numSparkles) {
			isNegative = rand.nextBoolean() // generate negativity
			newX = rand.nextInt(21) // generate x-coord
			if(isNegative) // make negative if boolean says so
			{
				newX *= -1
			}
			isNegative = rand.nextBoolean() // repeat as x
			newY = rand.nextInt(21)
			if(isNegative) {
				newY *= -1
			}
			while(!foundEmpty) {
				if(sparkleList.size > index) {
					if(sparkleList[index] == null) {
						foundEmpty = true
						emptyIndex = index
						if(DEBUG) {
							Log.d(
								TAG,
								"Found an empty index to replace: $emptyIndex"
							)
						}
					}
				} else {
					foundEmpty = true
				}
				index++
			}
			addSparkle = Sparkle(8f, (rand.nextInt(3) + 1), scale)
			addSparkle.setCoordinates((centerX + newX), (centerY + newY), 0f)
			addSparkle.setTexture(19)
			addSparkle.draw()
			addSparkle.getBuffers().createBuffers()
			if(emptyIndex >= 0) {
				try {
					addSparkle.setListIndex(emptyIndex)
					sparkleList[emptyIndex] = addSparkle
					if(DEBUG) {
						Log.d(TAG, "GameLoop: Replaced sparkle succesfully.")
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed replacing sparkle: $e")
					}
				} catch(e: ClassCastException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed replacing sparkle: $e")
					}
				} catch(e: IndexOutOfBoundsException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed replacing sparkle: $e")
					}
				} catch(e: IllegalArgumentException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed replacing sparkle: $e")
					}
				}
			} else {
				try {
					addSparkle.setListIndex(jewelList.size)
					sparkleList.add(addSparkle)
					if(DEBUG) {
						Log.d(TAG, "GameLoop: Added sparkle succesfully.")
					}
				} catch(e: IllegalArgumentException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding sparkle: $e")
					}
				} catch(e: ClassCastException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding sparkle: $e")
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding sparkle: $e")
					}
				}
			}
		}
		renderer.synchronizeSparkle(sparkleList)
	}
	// Function to update score
	private fun updateScore() {
		var thousands = numPoints / 1000 // count how many thousands
		var tens = numPoints / 10 // count how many tens
		var hundreds = numPoints / 100 // count how many hundreds
		var tenthousands = numPoints / 10000 // count how many tens of thousands
		var correctLevel = 1
		var speedLevel =
			75				// make sure we stay with single digits with all the values
		if(thousands > 9) {
			while(thousands > 9) {
				thousands -= 10
			}
		}
		if(tens > 9) {
			while(tens > 9) {
				tens -= 10
			}
		}
		if(hundreds > 9) {
			while(hundreds > 9) {
				hundreds -= 10
			}
		}
		if(tenthousands > 9) {
			while(tenthousands > 9) {
				tenthousands -= 10
			}
		}
		if(gameMode == 0) {
			correctLevel += tenthousands
		}
		if(gameMode == 1) {
			correctLevel += tenthousands * 2 + thousands / 5
		}
		if(level != correctLevel) {
			level = correctLevel
		}
		if(gameMode == 0) {
			speedLevel -= tenthousands * 2 + thousands / 5
		}
		if(gameMode == 1) {
			speedLevel -= tenthousands * 5 + thousands / 2
		}
		if(speedLevel != addSpeed) {
			addSpeed = speedLevel
		}
		if(addSpeed < 10) {
			addSpeed = 10
		}				// set textures accordingly
		scoreNums[0]?.setTexture(tenthousands + 7)
		scoreNums[1]?.setTexture(thousands + 7)
		scoreNums[2]?.setTexture(hundreds + 7)
		scoreNums[3]?.setTexture(tens + 7)
		updateScore = false
	}
	// Function to update score
	private fun updateHighScore() {
		var thousands = highScore / 1000 // count how many thousands
		var tens = highScore / 10 // count how many tens
		var hundreds = highScore / 100 // count how many hundreds
		var tenthousands =
			highScore / 10000 // count how many tens of thousands				// make sure we stay with single digits with all the values
		if(thousands > 9) {
			while(thousands > 9) {
				thousands -= 10
			}
		}
		if(tens > 9) {
			while(tens > 9) {
				tens -= 10
			}
		}
		if(hundreds > 9) {
			while(hundreds > 9) {
				hundreds -= 10
			}
		}
		if(tenthousands > 9) {
			while(tenthousands > 9) {
				tenthousands -= 10
			}
		}				// set textures accordingly
		highScoreNums[0]?.setTexture(tenthousands + 7)
		highScoreNums[1]?.setTexture(thousands + 7)
		highScoreNums[2]?.setTexture(hundreds + 7)
		highScoreNums[3]?.setTexture(tens + 7)
	}

	private fun resetTouchCoords() {
		touchCoords[0] =
			1000f // set default touch coords out of bounds so it doesn't instantly select
		touchCoords[1] = 1000f
	}
	// Function to load and set the UI shown during gameplay
	private fun InitializeGameUI(): Boolean {		// Create quads for score text and numbers
		scoreText = UIElement(25f, scale)
		scoreText?.setTexture(5)
		if(DEBUG) {
			Log.d(TAG, "Added score text to render.")
		}
		scoreText?.draw()
		scoreText?.getBuffers()?.createBuffers()
		scoreText?.setCoordinates(-maxWidth + 45f * scale, -maxHeight.toFloat
			(), 0.1f)
		try {
			uiElementList.add(scoreText)
			scoreText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Score text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Score text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Score text: $e")
			}
		}
		for(i in 0..4) {
			scoreNums[i] = UIElement(10f, scale)
			scoreNums[i]?.setTexture(7)
			if(DEBUG) {
				Log.d(TAG, "Added score num$i to render.")
			}
			scoreNums[i]?.draw()
			scoreNums[i]?.getBuffers()?.createBuffers()
			scoreNums[i]?.setCoordinates(
				-maxWidth + 45f * scale + scoreText!!.getRadius() + i * 20f *
				scale,
				-maxHeight + 18f * scale,
				0.1f
			)
			try {
				uiElementList.add(scoreNums[i])
				scoreNums[i]?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Score number: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Score number: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Score number: $e")
				}
			}
		}				// Create quad for lives text and lives count display
		livesText = UIElement(25f, scale)
		livesText?.setTexture(6)
		if(DEBUG) {
			Log.d(TAG, "Added lives text to renderer.")
		}
		livesText?.draw()
		livesText?.getBuffers()?.createBuffers()
		livesText?.setCoordinates(maxWidth - 45f * scale, -maxHeight.toFloat
			(), 0.1f)
		try {
			uiElementList.add(livesText)
			livesText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives text: $e")
			}
		}
		livesNums = UIElement(10f, scale)
		livesNums?.setTexture(numLives + 7)
		if(DEBUG) {
			Log.d(TAG, "Added lives num to renderer.")
		}
		livesNums?.draw()
		livesNums?.getBuffers()?.createBuffers()
		livesNums?.setCoordinates(
			maxWidth - 50f * scale - livesText!!.getRadius(),
			-maxHeight + 16f * scale,
			0.1f
		)
		try {
			uiElementList.add(livesNums)
			livesNums?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives number: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives number: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Lives number: $e")
			}
		}
		return true
	}

	private fun InitializePlayer(num: Int): Boolean {
		if(num == 1) {			// Create the quads for the wisps
			player1 = PlayerBall(14f, scale)
			player1?.setMovemenetBounds(movementBoundsMaxX,
				movementBoundsMaxY, movementBoundsMinX, movementBoundsMinY)
			player1?.setTexture(0)
			player1?.draw()
			player1?.getBuffers()?.createBuffers()
			try {
				playerList.add(player1)
				player1?.setListIndex(playerList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player1: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player1: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player1: $e")
				}
			}
		}
		if(num == 2) {
			player2 = PlayerBall(14f, scale)
			player2?.setMovemenetBounds(movementBoundsMaxX,
				movementBoundsMaxY, movementBoundsMinX, movementBoundsMinY)
			player2?.setTexture(1)
			player2?.draw()
			player2?.getBuffers()?.createBuffers()
			try {
				playerList.add(player2)
				player2?.setListIndex(playerList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player2: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player2: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Player2: $e")
				}
			}
		}
		return true
	}

	private fun InitializePads(num: Int): Boolean {
		pad1 = Jewel(14f, 150, 3, scale)
		pad1?.setCoordinates(maxWidth - pad1!!.getRadius() * 1.1f, 0f, 0f)
		pad1?.setTexture(22)
		pad1?.draw()
		pad1?.getBuffers()?.createBuffers()
		try {
			pad1?.setListIndex(jewelList.size)
			jewelList.add(pad1)
			if(DEBUG) {
				Log.d(TAG, "GameLoop: Added pad1 succesfully.")
			}
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding pad1: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding pad1: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding pad1: $e")
			}
		}
		if(num >= 2) {
			pad2 = Jewel(14f, 150, 4, scale)
			pad2?.setCoordinates(-maxWidth + pad2!!.getRadius() * 1.1f, 0f, 0f)
			pad2?.setTexture(23)
			pad2?.draw()
			pad2?.getBuffers()?.createBuffers()
			try {
				pad2?.setListIndex(jewelList.size)
				jewelList.add(pad2)
				if(DEBUG) {
					Log.d(TAG, "GameLoop: Added pad2 succesfully.")
				}
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding pad2: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding pad2: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding pad2: $e")
				}
			}
		}
		renderer.synchronizeJewels(jewelList)
		return true
	}

	private fun loadGame(): Boolean {
		var loadingDone = false
		numPoints = 0
		level = 1
		if(DEBUG) {
			Log.d(TAG, "Started loading game data.")
		}
		resetTouchCoords()
		while(!loadingDone) {
			loadingDone = InitializeGameUI()
		}
		loadingDone = false
		while(!loadingDone) {
			loadingDone = InitializePlayer(1)
		}
		loadingDone = false
		if(numBalls > 1) {
			while(!loadingDone) {
				loadingDone = InitializePlayer(2)
			}
			loadingDone = false
		}
		if(gameMode == 1) {
			while(!loadingDone) {
				loadingDone = InitializePads(2)
			}
			renderer.synchronizeJewels(jewelList)
		}
		beginTime = 0
		timeDiff = 0
		sleepTime = 0
		isCollision = false
		renderer.synchronizePlayer(playerList)
		renderer.synchronizeUI(uiElementList)
		if(!noMusic) {
			soundManager.playMusic(2 + rand.nextInt(4))
		}
		if(DEBUG) {
			Log.d(TAG, "Game data loaded.")
		}
		return true
	}

	private fun loadHighScore(): Boolean {
		val achievements: SharedPreferences = main.getSharedPreferences(
			ACHIEVEMENTS, 0
		)
		try {
			highScore = achievements.getInt("highScore", 0)
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "Error loading high score: $e")
			}
			return false
		}
		return true
	}

	private fun loadProgress(): Boolean {
		val progress: SharedPreferences = main.getSharedPreferences(PROGRESS, 0)
		try {
			progressAmount = progress.getInt("progressAmount", 0)
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "Error loading progress amount: $e")
			}
			return false
		}
		try {
			progressLevel = progress.getInt("progressLevel", 0)
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "Error loading progress level: $e")
			}
			return false
		}
		return true
	}

	private fun loadSettings(): Boolean {
		val settings: SharedPreferences = main.getSharedPreferences(SETTINGS, 0)
		var settingValue = 0
		try {
			settingValue = settings.getInt("noMusic", 0)
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "Error loading settings for music: $e")
			}
			return false
		}
		noMusic = if(settingValue == 0) {
			false
		} else {
			true
		}
		try {
			settingValue = settings.getInt("noSounds", 0)
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "Error loading settings for sounds: $e")
			}
			return false
		}
		noSound = settingValue != 0
		return true
	}

	private fun loadMenu(): Boolean {
		var barFill = 0f
		if(DEBUG) {
			Log.d(TAG, "Started loading menu.")
		}
		loadHighScore()
		loadProgress()
		menuBg = UIElement(150f, scale)
		menuBg?.setTexture(17)
		menuBg?.setCoordinates(
			(maxWidth - menuBg!!.getRadius() * 0.8f),
			(maxHeight - menuBg!!.getRadius() * 0.8f),
			0f
		)
		menuBg?.draw()
		menuBg?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(menuBg)
			menuBg?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Title: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Title: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Title: $e")
			}
		}
		play = UIElement(85f, scale)
		play?.distort(1f, 0.3f)
		play?.setCoordinates(
			-maxWidth + play!!.getRadius() * 2.5f,
			maxHeight - play!!.getRadius() * 2f,
			0f
		)
		play?.setTexture(18)
		play?.draw()
		play?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(play)
			play?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Play: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Play: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Play: $e")
			}
		}
		gameMode0 = UIElement(55f, scale)
		gameMode0?.distort(1f, 0.3f)
		gameMode0?.setCoordinates(-maxWidth * 0.8f, maxHeight * 0.6f, 0f)
		gameMode0?.setTexture(24)
		gameMode0?.draw()
		gameMode0?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(gameMode0)
			gameMode0?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding GameMode0: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding GameMode0: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding GameMode0: $e")
			}
		}
		if(progressLevel >= 1) {
			gameMode1 = UIElement(55f, scale)
			gameMode1?.distort(1f, 0.3f)
			gameMode1?.setCoordinates(-maxWidth * 0.1f, maxHeight * 0.6f, 0f)
			gameMode1?.setTexture(25)
			gameMode1?.draw()
			gameMode1?.getBuffers()?.createBuffers()
			try {
				uiElementList.add(gameMode1)
				gameMode1?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode1: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode1: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode1: $e")
				}
			}
		}
		if(progressLevel >= 2) {
			gameMode2 = UIElement(55f, scale)
			gameMode2?.distort(1f, 0.3f)
			gameMode2?.setCoordinates(-maxWidth * 0.45f, maxHeight * 0.5f, 0f)
			gameMode2?.setTexture(31)
			gameMode2?.draw()
			gameMode2?.getBuffers()?.createBuffers()
			try {
				uiElementList.add(gameMode2)
				gameMode2?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode2: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode2: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding GameMode2: $e")
				}
			}
		}				// Create quads for highscore text and numbers
		highText = UIElement(25f, scale)
		highText?.setTexture(20)
		highText?.draw()
		highText?.getBuffers()?.createBuffers()
		highText?.setCoordinates(
			-maxWidth + 45 * scale,
			-maxHeight + 1 * scale,
			0.1f
		)
		try {
			uiElementList.add(highText)
			highText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		}
		highScoreText = UIElement(25f, scale)
		highScoreText?.setTexture(5)
		if(DEBUG) {
			Log.d(TAG, "Added highscore text to render.")
		}
		highScoreText?.draw()
		highScoreText?.getBuffers()?.createBuffers()
		highScoreText?.setCoordinates(
			(-maxWidth + highText!!.getRadius() * 2.4f),
			-maxHeight.toFloat(),
			0.1f
		)
		try {
			uiElementList.add(highScoreText)
			highScoreText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Highscore text: $e")
			}
		}
		for(i in 0..4) {
			highScoreNums[i] = UIElement(10f, scale)
			highScoreNums[i]?.setTexture(7)
			if(DEBUG) {
				Log.d(TAG, "Added highscore num$i to render.")
			}
			highScoreNums[i]?.draw()
			highScoreNums[i]?.getBuffers()?.createBuffers()
			highScoreNums[i]?.setCoordinates(
				-maxWidth + highText!!.getRadius() * 2.5f + highScoreText!!
					.getRadius() + i * 20f * scale,
				-maxHeight + 18f * scale,
				0.1f
			)
			try {
				uiElementList.add(highScoreNums[i])
				highScoreNums[i]?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Highscore number: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Highscore number: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding Highscore number: $e")
				}
			}
		}
		updateHighScore()
		if(progressLevel < MAX_PROGRESS) {
			progressBg = UIElement(125f, scale)
			progressBg?.setTexture(29)
			if(DEBUG) {
				Log.d(TAG, "Added progress background to render.")
			}
			progressBg?.draw()
			progressBg?.getBuffers()?.createBuffers()
			progressBg?.setCoordinates(
				(maxWidth - progressBg!!.getRadius()),
				-maxHeight * 1.37f,
				0.1f
			)
			try {
				uiElementList.add(progressBg)
				progressBg?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding progress background: $e"
					)
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding progress background: $e"
					)
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding progress background: $e"
					)
				}
			}
			barFill = progressAmount / (50000f + progressLevel * 50000f)
			if(barFill > 0f) {
				progressBar = UIElement(125f, scale)
				progressBar?.setTexture(30)
				progressBar?.setCoordinates(
					(maxWidth - progressBar!!.getRadius()),
					-maxHeight * 1.37f,
					0.1f
				)
				if(barFill < 1) {
					progressBar?.distort(barFill, 1f)
				}
				progressBar?.draw()
				progressBar?.getBuffers()?.createBuffers()
				try {
					uiElementList.add(progressBar)
					progressBar?.setListIndex(uiElementList.size)
				} catch(e: IllegalArgumentException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding progress bar: $e")
					}
				} catch(e: ClassCastException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding progress bar: $e")
					}
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Failed adding progress bar: $e")
					}
				}
			}
		}
		sfx = UIElement(25f, scale)
		if(!noSound) {
			sfx?.setTexture(42)
		} else {
			sfx?.setTexture(43)
		}
		sfx?.setCoordinates(
			(maxWidth - sfx!!.getRadius() * 0.8f),
			(0 - sfx!!.getRadius() * 0.8f),
			0f
		)
		sfx?.draw()
		sfx?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(sfx)
			sfx?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding SFX button: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding SFX button: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding SFX button: $e")
			}
		}
		music = UIElement(25f, scale)
		if(!noMusic) {
			music?.setTexture(44)
		} else {
			music?.setTexture(45)
		}
		music?.setCoordinates(
			(maxWidth - music!!.getRadius() * 0.8f),
			(0 - sfx!!.getRadius() * 1.1f - music!!.getRadius() * 1.1f),
			0f
		)
		music?.draw()
		music?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(music)
			music?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Music button: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Music button: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding Music button: $e")
			}
		}
		renderer.synchronizeUI(uiElementList)
		resetTouchCoords()
		if(!noMusic) {
			soundManager.playMusic(1)
		}
		if(DEBUG) {
			Log.d(TAG, "Loaded game menu.")
		}
		return true
	}

	private fun manageSparkles() {
		for(i in sparkleList) {
			var alive = true
			if(i != null) {
				alive = i.animate()
			}
			if(!alive) {
				try {
					sparkleList.set(i!!.getListIndex(), null)
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error nulling sparkle: $e")
					}
				} catch(e: IndexOutOfBoundsException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error nulling sparkle: $e")
					}
				} catch(e: IllegalArgumentException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error nulling sparkle: $e")
					}
				} catch(e: ClassCastException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error nulling sparkle: $e")
					}
				}
			}
		}
		var temp: Sparkle? = null
		for(i in sparkleList.size downTo 1) {
			try {
				temp = sparkleList[i - 1]
			} catch(e: IndexOutOfBoundsException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error removing sparkle: $e")
				}
			}
			if(temp == null) {
				try {
					sparkleList.removeAt(i - 1)
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error removing sparkle: $e")
					}
				} catch(e: IndexOutOfBoundsException) {
					if(DEBUG) {
						Log.e(TAG, "GameLoop: Error removing sparkle: $e")
					}
				}
			}
		}
		for(i in 0 until sparkleList.size) {
			try {
				sparkleList[i]?.setListIndex(i)
			} catch(e: IndexOutOfBoundsException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Error changing indexes of sparkles: $e"
					)
				}
			}
		}
		renderer.synchronizeSparkle(sparkleList)
	}

	private fun unloadMenu(): Boolean {
		stopMusic(false)
		if(DEBUG) {
			Log.d(TAG, "Started unloading menu.")
		}
		menuBg = null
		play = null
		try {
			uiElementList.clear()
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "Gameloop: menu unload failed clearing: $e")
			}
		}
		renderer.clearUIList()
		if(DEBUG) {
			Log.d(TAG, "Unloaded game menu.")
		}
		return true
	}

	private fun testForCollisions() {
		for(i in jewelList) {
			if(i != null) {
				isCollision =
					checkCollision(player1!!, i) // check for collision: player1
				if(isCollision) // collision has happened
				{
					if(gameMode == 0) {
						generateSparkles(i.getXCoordinate(), i.getYCoordinate())
						if(i.getType() == 2) {
							renderer.flash(0.4f, 0f, 0f)
							numLives--
							livesNums?.setTexture(numLives + 7)
							if(!noSound) {
								soundManager.playSound(0)
							}
						} else {
							renderer.flash(0.2f, 0.2f, 0f)
							if(!noSound) {
								soundManager.playSound(2)
							}
							numPoints += i.getScoreValue() // correct collision, add points
							updateScore =
								true // set the flag that we update score
						}
						try {
							jewelList[i.getListIndex()] = null
							currentNumDiamonds--
						} catch(e: UnsupportedOperationException) {
							if(DEBUG) {
								Log.e(
									TAG,
									"GameLoop: Error nulling due to collision: $e"
								)
							}
						} catch(e: IndexOutOfBoundsException) {
							if(DEBUG) {
								Log.e(
									TAG,
									"GameLoop: Error nulling due to collision: $e"
								)
							}
						} catch(e: IllegalArgumentException) {
							if(DEBUG) {
								Log.e(
									TAG,
									"GameLoop: Error nulling due to collision: $e"
								)
							}
						} catch(e: ClassCastException) {
							if(DEBUG) {
								Log.e(
									TAG,
									"GameLoop: Error nulling due to collision: $e"
								)
							}
						}
					}
					if(gameMode == 1 || gameMode == 2) {
						if((((i.getType() == 2 && player1!!.getType() == 1) ||
						(i.getType() == 1 && player1!!.getType() == 2)) &&
						!reversed) || (reversed && ((i.getType() == 2 &&
							player1!!.getType() == 2) || (i.getType() == 1 &&
							player1!!.getType() == 1)))) {
							generateSparkles(
								i.getXCoordinate(),
								i.getYCoordinate()
							)
							renderer.flash(0.4f, 0f, 0f)
							numLives--
							livesNums?.setTexture(numLives + 7)
							if(!noSound) {
								soundManager.playSound(0)
							}
						} else {
							if(i.getType() == 4 || i.getType() == 3) {
								player1?.changeType(i.getType() - 2)
								player1?.setTexture(i.getType() - 3)
							//renderer.Flash(0.2f, 0.2f, 0.2f);
							} else {
								generateSparkles(
									i.getXCoordinate(),
									i.getYCoordinate()
								)
								if(player1?.getType() == 1) {
									renderer.flash(0.15f, 0.15f, 0f)
									if(!noSound) {
										soundManager.playSound(2)
									}
								}
								if(player1?.getType() == 2) {
									renderer.flash(0.0f, 0.0f, 0.3f)
									if(!noSound) {
										soundManager.playSound(1)
									}
								}
								numPoints += i.getScoreValue() // correct collision, add points
								if(gameMode == 2) {
									newPoints += i.getScoreValue()
								}
								updateScore =
									true // set the flag that we update score
							}
						}
						if(i.getType() != 4 && i.getType() != 3) {
							try {
								jewelList[i.getListIndex()] = null
								currentNumDiamonds--
							} catch(e: UnsupportedOperationException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: IndexOutOfBoundsException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: IllegalArgumentException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: ClassCastException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							}
						}
					}
				} else {
					if(numBalls > 1) {
						isCollision = checkCollision(
							player2!!,
							i
						) // check for collision: player2
						if(isCollision) // otherwise same as above
						{
							currentNumDiamonds--
							generateSparkles(
								i.getXCoordinate(),
								i.getYCoordinate()
							)
							if(i.getType() == 1) {
								renderer.flash(0.4f, 0f, 0f)
								numLives--
								livesNums?.setTexture(numLives + 7)
								if(!noSound) {
									soundManager.playSound(0)
								}
							} else {
								renderer.flash(0f, 0f, 0.3f)
								if(!noSound) {
									soundManager.playSound(1)
								}
								numPoints += i.getScoreValue()
								updateScore = true
							}
							try {
								jewelList[i.getListIndex()] = null
							} catch(e: UnsupportedOperationException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: IndexOutOfBoundsException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: IllegalArgumentException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							} catch(e: ClassCastException) {
								if(DEBUG) {
									Log.e(
										TAG,
										"GameLoop: Error nulling due to collision: $e"
									)
								}
							}
						}
					}
				}
			}
			isCollision = false // reset flag
		}
		var temp: Jewel? = null
		var lowestRemoved: Int = jewelList.size
		for(i in jewelList.size downTo 1) {
			try {
				temp = jewelList[i - 1]
			} catch(e: IndexOutOfBoundsException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error removing due to collisions: $e")
				}
			}
			if(temp == null) {
				try {
					jewelList.removeAt(i - 1)
					lowestRemoved = i - 1
				} catch(e: UnsupportedOperationException) {
					if(DEBUG) {
						Log.e(
							TAG,
							"GameLoop: Error removing due to collisions: $e"
						)
					}
				} catch(e: IndexOutOfBoundsException) {
					if(DEBUG) {
						Log.e(
							TAG,
							"GameLoop: Error removing due to collisions: $e"
						)
					}
				}
			}
		}
		if(lowestRemoved < jewelList.size) {
			for(i in lowestRemoved until jewelList.size) {
				try {
					jewelList[i]?.setListIndex(i)
				} catch(e: IndexOutOfBoundsException) {
					if(DEBUG) {
						Log.e(
							TAG,
							"GameLoop: Error changing indexes due to collisions: $e"
						)
					}
				}
			}
		}
		renderer.synchronizeJewels(jewelList)
	}

	private fun SaveHighScore(): Boolean {
		if(highScore < numPoints) {
			newHighScore = true
			progressAmount = (progressAmount + 1000f).toInt()
			val achievements: SharedPreferences = main.getSharedPreferences(
				ACHIEVEMENTS, 0
			)
			val editor: SharedPreferences.Editor = achievements.edit()
			editor.putInt("highScore", numPoints)
			return editor.commit()
		} else {
			return true
		}
	}

	private fun SaveSoundSettings(): Boolean {
		val settings: SharedPreferences = main.getSharedPreferences(SETTINGS, 0)
		val editor: SharedPreferences.Editor = settings.edit()
		if(noSound) {
			editor.putInt("noSound", 1)
		} else {
			editor.putInt("noSound", 0)
		}
		if(noMusic) {
			editor.putInt("noMusic", 1)
		} else {
			editor.putInt("noMusic", 0)
		}
		return editor.commit()
	}

	private fun SaveProgress(): Boolean {
		progressAmount += numPoints
		if(progressLevel < MAX_PROGRESS) {
			if(progressAmount >= 50000 + progressLevel * 50000) {
				progressLevel++
				progressAmount = 0
			}
		}
		val progress: SharedPreferences = main.getSharedPreferences(PROGRESS, 0)
		val editor: SharedPreferences.Editor = progress.edit()
		editor.putInt("progressAmount", progressAmount)
		editor.putInt("progressLevel", progressLevel)
		return editor.commit()
	}

	private fun Congratulate(type: Int) {
		gzText = UIElement(75f, scale)
		gzText?.distort(1f, 0.5f)
		gzText?.setCoordinates(0f, maxHeight * 0.1f, 0f)
		gzText?.setTexture(26)
		gzText?.draw()
		gzText?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(gzText)
			gzText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding congratulations text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding congratulations text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding congratulations text: $e")
			}
		}				// New highscore
		if(type == 0) {
			newHighText = UIElement(75f, scale)
			newHighText?.distort(1f, 0.5f)
			newHighText?.setCoordinates(0f, 0f, 0f)
			newHighText?.setTexture(27)
			newHighText?.draw()
			newHighText?.getBuffers()?.createBuffers()
			try {
				uiElementList.add(newHighText)
				newHighText?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding new high score text: $e"
					)
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding new high score text: $e"
					)
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding new high score text: $e"
					)
				}
			}
		}				// Challenge complete
		if(type == 1) {
			newHighText = UIElement(75f, scale)
			newHighText?.distort(1f, 0.5f)
			newHighText?.setCoordinates(0f, 0f, 0f)
			newHighText?.setTexture(32)
			newHighText?.draw()
			newHighText?.getBuffers()?.createBuffers()
			try {
				uiElementList.add(newHighText)
				newHighText?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding challenge done text: $e"
					)
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding challenge done text: $e"
					)
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(
						TAG,
						"GameLoop: Failed adding challenge done text: $e"
					)
				}
			}
		}
		renderer.synchronizeUI(uiElementList)
	}

	private fun handleGameOver() {
		var touched = false
		resetTouchCoords()
		renderer.flash(-1f, -1f, -1f)
		gameOverText = UIElement(100f, scale)
		gameOverText?.setCoordinates(0f, maxHeight - gameOverText!!.getRadius(),
			0f)
		gameOverText?.setTexture(21)
		gameOverText?.draw()
		gameOverText?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(gameOverText)
			gameOverText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding game over text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding game over text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding game over text: $e")
			}
		}
		renderer.synchronizeUI(uiElementList)
		var saving = false
		while(!saving) {
			saving = SaveHighScore()
		}
		saving = false
		while(!saving) {
			saving = SaveProgress()
		}
		if(newHighScore) {
			newHighScore = false
			Congratulate(0)
		}
		try {
			Thread.sleep(1000)
		} catch(e: InterruptedException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Error sleeping: $e")
			}
		}
		tapText = UIElement(100f, scale)
		tapText?.distort(1f, 0.5f)
		tapText?.setCoordinates(0f, 0f - tapText!!.getRadius(), 0f)
		tapText?.setTexture(28)
		tapText?.draw()
		tapText?.getBuffers()?.createBuffers()
		try {
			uiElementList.add(tapText)
			tapText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		}
		renderer.synchronizeUI(uiElementList)
		while(!touched) {
			touched = testTouch(
				-maxWidth.toFloat(),
				maxWidth.toFloat(),
				-maxHeight.toFloat(),
				maxHeight.toFloat()
			)
		}
		stopMusic(true)
		renderer.clearJewelList()
		renderer.clearPlayerList()
		renderer.clearUIList()
		renderer.clearSparkleList()
		gameOver.run()
	}

	private fun testTouch(
		minX: Float,
		maxX: Float,
		minY: Float,
		maxY: Float
	): Boolean {		// check if touch is inside bounds
		return (touchCoords[0] >= minX) && (touchCoords[0] <= maxX) && (touchCoords[1] >= minY) && (touchCoords[1] <= maxY)
	}

	private fun LoadHelp(helpFile: Int): Boolean {
		resetTouchCoords()
		if(helpFile < 4) {
			helpText = UIElement(250f, scale)
			helpText?.setCoordinates(0f, -10f, 0f)
		}
		when (helpFile) {
			1 -> helpText?.setTexture(33)
			2 -> helpText?.setTexture(34)
			3 -> helpText?.setTexture(35)
			4 -> helpText?.setTexture(36)
			5 -> {
				helpText?.setTexture(37)
				InitializePads(1)
				player1?.setTexture(1)
				player1?.changeType(2)
			}

			6 -> {
				helpText?.setTexture(38)
				InitializePads(2)
				reversed = true
			}

			7 -> {
				helpText?.setTexture(39)
				InitializePads(2)
				reversed = false
			}

			8 -> {
				helpText?.setTexture(40)
				InitializePads(2)
			}

			9 -> {
				helpText?.setTexture(41)
				player1?.setTexture(0)
				player1?.changeType(1)
			}

			else -> {}
		}
		if(helpFile < 4) {
			helpText?.draw()
			helpText?.getBuffers()?.createBuffers()
		}
		try {
			uiElementList.add(helpText)
			helpText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding help text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding help text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding help text: $e")
			}
		}
		renderer.synchronizeUI(uiElementList)
		try {
			Thread.sleep(1000)
		} catch(e: InterruptedException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Error sleeping: $e")
			}
		}
		if(tapText == null) {
			tapText = UIElement(100f, scale)
			tapText?.distort(1f, 0.5f)
			tapText?.setCoordinates(0f, 0f - tapText!!.getRadius(), 0f)
			tapText?.setTexture(28)
			tapText?.draw()
			tapText?.getBuffers()?.createBuffers()
		}
		try {
			uiElementList.add(tapText)
			tapText?.setListIndex(uiElementList.size)
		} catch(e: IllegalArgumentException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		} catch(e: ClassCastException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "GameLoop: Failed adding tap text: $e")
			}
		}
		renderer.synchronizeUI(uiElementList)
		return true
	}

	private fun handleChallenge(challengeLevel: Int): Boolean {
		val level1x = shortArrayOf(-10, -8, -6, -4, -2, 2, 4, 6, 8, 10)
		val level1y = shortArrayOf(2, 4, 6, 8, 10, -2, -4, -6, -8, -10)
		val level1types = shortArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
		val level2x = shortArrayOf(-10, -8, -6, -4, -2, 2, 4, 6, 8, 10, 20)
		val level2y = shortArrayOf(2, 4, 6, 8, 10, -2, -4, -6, -8, -10, 20)
		val level2types = shortArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0)
		val level3x = shortArrayOf(-2, -4, -6, -8, -10, -5, 5, 10, 8, 6, 4, 2)
		val level3y = shortArrayOf(5, 5, 5, -5, -5, -5, 5, 5, 5, -5, -5, -5)
		val level3types = shortArrayOf(2, 2, 2, 0, 1, 2, 2, 1, 0, 1, 1, 1)
		val level7x = shortArrayOf(
			-10,
			-8,
			-6,
			-4,
			-2,
			2,
			4,
			6,
			8,
			10,
			-10,
			-8,
			-6,
			-4,
			-2,
			2,
			4,
			6,
			8,
			10,
			-12,
			-12,
			-12,
			12,
			12,
			12,
			-2,
			2,
			-2,
			2
		)
		val level7y = shortArrayOf(
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-5,
			-12,
			-2,
			2,
			-12,
			-2,
			2,
			-2,
			-2,
			2,
			2
		)
		val level7types = shortArrayOf(
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1,
			1
		)
		if(challengeLevel == 1) {
			if(challengeTracker <= 0) {
				player1?.setCoordinates(0.9f * -maxWidth, 0.9f * maxHeight, 0f)
				for(i in 0..9) {
					addObjects(
						level1types[i].toInt(),
						maxWidth / level1x[i],
						maxHeight / level1y[i]
					)
					challengeTracker++
					currentNumDiamonds++
				}
			}
			return if(newPoints >= 500) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 2) {			// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 65) {
				if(currentNumDiamonds < MAX_NUM_DIAMONDS && challengeTracker < 11) {
					addObjects(
						level2types[challengeTracker].toInt(),
						maxWidth / level2x[challengeTracker],
						maxHeight / level2y[challengeTracker]
					)
					challengeTracker++
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			return if(newPoints >= 500) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 3) {			// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 75) {
				if(currentNumDiamonds < MAX_NUM_DIAMONDS && challengeTracker < 12) {
					addObjects(
						level3types[challengeTracker].toInt(),
						maxWidth / level3x[challengeTracker],
						maxHeight / level3y[challengeTracker]
					)
					challengeTracker++
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			if(challengeTracker >= 12 && jewelList.size <= 1) {
				numLives--
				return true
			}
			return if(newPoints >= 1600) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 4) {			// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 65) {
				if(currentNumDiamonds < MAX_NUM_DIAMONDS) {
					addObjects(rand.nextInt(3))
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			return if(newPoints >= 3000) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 5) {			// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 55) {
				if(currentNumDiamonds < MAX_NUM_DIAMONDS) {
					addObjects(rand.nextInt(3))
					challengeTracker++
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			return if(newPoints >= 5000) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 6) {			// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 55) {
				if(currentNumDiamonds < MAX_NUM_DIAMONDS) {
					if(player1?.getType() == 1) {
						addObjects(2)
					}
					if(player1?.getType() == 2) {
						addObjects(1)
					}
					challengeTracker++
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			return if(newPoints >= 3000) {
				true
			} else {
				false
			}
		}
		if(challengeLevel == 7) {
			if(challengeTracker <= 0) {
				player1?.setCoordinates(0.9f * -maxWidth, 0.9f * maxHeight, 0f)
				for(i in 0..19) {
					addObjects(
						level7types[i].toInt(),
						maxWidth / level7x[i],
						maxHeight / level7y[i]
					)
					challengeTracker++
					currentNumDiamonds++
				}
			}						// increase object adder -tracker
			timeTracker += 1						// time to add, add a random jewel
			if(timeTracker >= 55) {
				if(challengeTracker < 30) {
					addObjects(
						level7types[challengeTracker].toInt(),
						maxWidth / level7x[challengeTracker],
						maxHeight / level7y[challengeTracker]
					)
					challengeTracker++
					currentNumDiamonds++
				}
				timeTracker = 0 // reset tracker
			}
			if(challengeTracker >= 30 && jewelList.isEmpty()) {
				return true
			}
			return if(newPoints >= 1500) {
				true
			} else {
				false
			}
		}
		return false
	}

	private fun finishChallenge(): Boolean {
		var touched = false
		var loading = false
		resetTouchCoords()
		renderer.flash(-1f, -1f, -1f)
		if(currentChallenge < MAX_CHALLENGES) {
			numPoints += currentChallenge * 1000
			currentChallenge++
			challengeTracker = 0
			timeTracker = 0
			newPoints = 0
			challengeDone = false
			Congratulate(1)
			tapText = UIElement(100f, scale)
			tapText?.distort(1f, 0.5f)
			tapText?.setCoordinates(0f, 0f - tapText!!.getRadius(), 0f)
			tapText?.setTexture(28)
			tapText?.draw()
			tapText?.getBuffers()?.createBuffers()
			try {
				uiElementList.add(tapText)
				tapText?.setListIndex(uiElementList.size)
			} catch(e: IllegalArgumentException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding tap text: $e")
				}
			} catch(e: ClassCastException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding tap text: $e")
				}
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Failed adding tap text: $e")
				}
			}
			try {
				jewelList.clear()
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error clearing jewel list: $e")
				}
			}
			try {
				Thread.sleep(1000)
			} catch(e: InterruptedException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error sleeping: $e")
				}
			}
			renderer.synchronizeJewels(jewelList)
			renderer.synchronizeUI(uiElementList)
			resetTouchCoords()
			while(!touched) {
				touched = testTouch(
					-maxWidth.toFloat(),
					maxWidth.toFloat(),
					-maxHeight.toFloat(),
					maxHeight.toFloat()
				)
			}
			try {
				uiElementList.clear()
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error clearing UI list: $e")
				}
			}
			touched = false
			LoadHelp(currentChallenge + 2)
			resetTouchCoords()
			while(!touched) {
				touched = testTouch(
					-maxWidth.toFloat(),
					maxWidth.toFloat(),
					-maxHeight.toFloat(),
					maxHeight.toFloat()
				)
			}
			try {
				uiElementList.clear()
			} catch(e: UnsupportedOperationException) {
				if(DEBUG) {
					Log.e(TAG, "GameLoop: Error clearing UI list: $e")
				}
			}
			while(!loading) {
				loading = InitializeGameUI()
			}
			renderer.synchronizeUI(uiElementList)
			return false
		} else {
			handleGameOver()
			return false
		}
	}

	fun returnToMenu() {
		if(running) {
			numLives = 0
		}
	}

	@Override override fun run() {
		var loadingDone = false // boolean to see if we are loading
		var waiting = false
		var selected1 = false
		var selected2 = false
		var selected3 = false
		var selected4 = false
		var selected5 = false				// load menu
		while(!loadingDone) {
			loadingDone = loadMenu()
		}				// reset the loading flag
		loadingDone = false
		if(DEBUG) {
			Log.d(TAG, "In menu loop.")
		}
		if(DEBUG) {
			Log.d(
				TAG,
				"Coords to be waited, minX: " + (gameMode0!!.getXCoordinate() -
				gameMode0!!.getRadius())
			)
			Log.d(
				TAG,
				"Coords to be waited, maxX: " + (gameMode0!!.getXCoordinate()
				+ gameMode0!!.getRadius())
			)
			Log.d(
				TAG,
				"Coords to be waited, minY: " + (gameMode0!!.getYCoordinate()
				- gameMode0!!.getRadius())
			)
			Log.d(
				TAG,
				"Coords to be waited, maxY: " + (gameMode0!!.getYCoordinate()
				+ gameMode0!!.getRadius())
			)
		}				// the menu loop
		while(menu) {			// if we are paused, do nothing
			while(paused) {
			}
			selected1 = testTouch(
				gameMode0!!.getXCoordinate() - gameMode0!!.getRadius(),
				gameMode0!!.getXCoordinate() + gameMode0!!.getRadius(),
				gameMode0!!.getYCoordinate() - gameMode0!!.getRadius(),
				gameMode0!!.getYCoordinate() + gameMode0!!.getRadius()
			)						// check if play CLASSIC has been touched
			if(selected1 && menu) {
				menu = false
				setGameMode(0)
			}
			if(progressLevel >= 1) {
				selected2 = testTouch(
					gameMode1!!.getXCoordinate() - gameMode1!!.getRadius(),
					gameMode1!!.getXCoordinate() + gameMode1!!.getRadius(),
					gameMode1!!.getYCoordinate() - gameMode1!!.getRadius(),
					gameMode1!!.getYCoordinate() + gameMode1!!.getRadius()
				)								// check if play TAG has been touched
				if(selected2 && menu) {
					menu = false
					setGameMode(1)
				}
			}
			if(progressLevel >= 2) {
				selected3 = testTouch(
					gameMode2!!.getXCoordinate() - gameMode2!!.getRadius(),
					gameMode2!!.getXCoordinate() + gameMode2!!.getRadius(),
					gameMode2!!.getYCoordinate() - gameMode2!!.getRadius(),
					gameMode2!!.getYCoordinate() + gameMode2!!.getRadius()
				)								// check if play CHALLENGE has been touched
				if(selected3 && menu) {
					menu = false
					setGameMode(2)
				}
			}
			selected4 = testTouch(
				sfx!!.getXCoordinate() - sfx!!.getRadius(),
				sfx!!.getXCoordinate() + sfx!!.getRadius(),
				sfx!!.getYCoordinate() - sfx!!.getRadius(),
				sfx!!.getYCoordinate() + sfx!!.getRadius()
			)						// check if play CLASSIC has been touched
			if(selected4 && menu) {
				if(noSound) {
					noSound = false
					sfx!!.setTexture(42)
				} else {
					noSound = true
					sfx!!.setTexture(43)
				}
				SaveSoundSettings()
				resetTouchCoords()
			}
			selected5 = testTouch(
				music!!.getXCoordinate() - music!!.getRadius(),
				music!!.getXCoordinate() + music!!.getRadius(),
				music!!.getYCoordinate() - music!!.getRadius(),
				music!!.getYCoordinate() + music!!.getRadius()
			)						// check if play CLASSIC has been touched
			if(selected5 && menu) {
				if(noMusic) {
					noMusic = false
					music!!.setTexture(44)
					soundManager.playMusic(1)
				} else {
					noMusic = true
					music!!.setTexture(45)
					soundManager.stopMusic(false)
				}
				SaveSoundSettings()
				resetTouchCoords()
			}
		}
		selected1 = false
		selected2 = false
		selected3 = false
		if(DEBUG) {
			Log.d(TAG, "Unloading menu.")
		}				// unload the menu
		while(!loadingDone) {
			loadingDone = unloadMenu()
		}				// reset the loading flag
		loadingDone = false
		pregame = false
		if(DEBUG) {
			Log.d(TAG, "In pre-game loop")
		}
		while(!loadingDone) {
			loadingDone = LoadHelp(gameMode + 1)
		}
		loadingDone = false
		resetTouchCoords()
		while(!pregame) {
			pregame = testTouch(
				-maxWidth.toFloat(),
				maxWidth.toFloat(),
				-maxHeight.toFloat(),
				maxHeight.toFloat()
			)
		}
		try {
			uiElementList.clear()
		} catch(e: UnsupportedOperationException) {
			if(DEBUG) {
				Log.e(TAG, "Gameloop: menu unload failed clearing: $e")
			}
		}
		renderer.clearUIList()				// load the game
		while(!loadingDone) {
			loadingDone = loadGame()
		}				// reset the loading flag
		loadingDone = false
		pregame = false
		if(DEBUG) {
			Log.d(TAG, "In game loop.")
		}				// set the running flag true
		running = true				// the game loop
		while(running) {			// Regular game modes (CLASSIC, TAG)
			if(gameMode != 2) {				// if the game is paused do nothing
				while(paused) {
				}								// reduce the background color gradually
				renderer.flash(
					-0.005f,
					-0.005f,
					-0.005f
				)								// store current time
				beginTime =
					System.currentTimeMillis()								// move player according to accelerometer
				player1?.move((accelValues[0] * 50), (accelValues[1] * 25), 0f)
				if(numBalls > 1) {
					player2?.move(
						(accelValues[0] * -50),
						(accelValues[1] * -25),
						0f
					)
				}								// rotate the players to give more animation
				player1?.rotate(20f)
				if(numBalls > 1) {
					player2?.rotate(-20f)
				}								// test for any collisions
				testForCollisions()								// animate sparkles
				manageSparkles()								// calculate if sleeping is needed to have constant FPS
				timeDiff = System.currentTimeMillis() - beginTime
				sleepTime =
					(FRAME_PERIOD - timeDiff).toInt()								// increase object adder -tracker
				timeTracker += 1								// time to add, add a random jewel
				if(timeTracker >= addSpeed) {					//numPoints++;	// award player some points for staying alive
					// add jewels according to the current difficulty level
					for(i in 0 until level) {
						if(currentNumDiamonds < MAX_NUM_DIAMONDS) {
							addObjects(rand.nextInt(3))
							currentNumDiamonds++
						}
					}
					timeTracker = 0 // reset tracker
				}								// check if score update is required
				if(updateScore) {
					updateScore()
				}								// need to sleep
				if(sleepTime > 0) {
					try {
						Thread.sleep(sleepTime.toLong())
					} catch(e: InterruptedException) {
						if(DEBUG) {
							Log.e(TAG, "GameLoop: Error sleeping: $e")
						}
					}
				}								// check if the player is out of lives
				if(numLives <= 0) {
					handleGameOver()
				}
			} else {				// if the game is paused do nothing
				while(paused) {
				}								// reduce the background color gradualy
				renderer.flash(
					-0.005f,
					-0.005f,
					-0.005f
				)								// store current time
				beginTime =
					System.currentTimeMillis()								// move player according to accelerometer
				player1?.move((accelValues[0] * 50), (accelValues[1] * 25), 0f)
				if(numBalls > 1) {
					player2?.move(
						(accelValues[0] * -50),
						(accelValues[1] * -25),
						0f
					)
				}								// rotate the players to give more animation
				player1?.rotate(20f)
				if(numBalls > 1) {
					player2?.rotate(-20f)
				}								// test for any collisions
				testForCollisions()								// animate sparkles
				manageSparkles()								// calculate if sleeping is needed to have constant FPS
				timeDiff = System.currentTimeMillis() - beginTime
				sleepTime = (FRAME_PERIOD - timeDiff).toInt()
				challengeDone = handleChallenge(currentChallenge)
				if(challengeDone) {
					waiting = true
					while(waiting) {
						waiting = finishChallenge()
					}
				}								// check if score update is required
				if(updateScore) {
					updateScore()
				}								// need to sleep
				if(sleepTime > 0) {
					try {
						Thread.sleep(sleepTime.toLong())
					} catch(e: InterruptedException) {
						if(DEBUG) {
							Log.e(TAG, "GameLoop: Error sleeping: $e")
						}
					}
				}								// check if the player is out of lives
				if(numLives <= 0) {
					handleGameOver()
				}
			}
		}
	}

	companion object {
		private const val TAG = "Jewel Tilt" // Game tag in logs
		private const val DEBUG = false // sets debug mode on or off
		private const val ACHIEVEMENTS = "Achievements"
		private const val PROGRESS = "Progress"
		private const val SETTINGS = "Settings"
		private const val MAX_NUM_DIAMONDS = 12
		private const val MAX_CHALLENGES = 7
		private const val MAX_PROGRESS = 2
		private const val MAX_FPS = 50 // Max FPS
		private const val FRAME_PERIOD =
			1000 / MAX_FPS // How time each frame takes
	}
}
