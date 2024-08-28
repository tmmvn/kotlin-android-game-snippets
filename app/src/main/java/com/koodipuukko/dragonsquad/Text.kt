package com.koodipuukko.dragonsquad

class Text
	(xPos: Int, yPos: Int, fontToUse: Int) {
	private var text: String? = null
	private var buffer: Buffers = Buffers()
	var x: Int
	var y: Int
	private var fontId: Int

	init {
		x = xPos
		y = yPos
		fontId = fontToUse
	}

	fun setXPosition(value: Int) {
		x = value
	}

	fun setYPosition(value: Int) {
		y = value
	}

	fun getBuffer(): Buffers {
		return buffer
	}

	fun setText(contents: String?) {
		text = contents
	}

	fun render() {
		val font: Font = if(fontId == 1) {
			Font(24)
		} else {
			Font(15)
		}
		for(i in 0 until text!!.length) {
			when (text!![i]) {
				'a' -> font.SetTextureLocation(0)
				'A' -> font.SetTextureLocation(0)
				'b' -> font.SetTextureLocation(1)
				'B' -> font.SetTextureLocation(1)
				'c' -> font.SetTextureLocation(2)
				'C' -> font.SetTextureLocation(2)
				'd' -> font.SetTextureLocation(3)
				'D' -> font.SetTextureLocation(3)
				'e' -> font.SetTextureLocation(4)
				'E' -> font.SetTextureLocation(4)
				'f' -> font.SetTextureLocation(5)
				'F' -> font.SetTextureLocation(5)
				'g' -> font.SetTextureLocation(6)
				'G' -> font.SetTextureLocation(6)
				'h' -> font.SetTextureLocation(7)
				'H' -> font.SetTextureLocation(7)
				'i' -> font.SetTextureLocation(8)
				'I' -> font.SetTextureLocation(8)
				'j' -> font.SetTextureLocation(9)
				'J' -> font.SetTextureLocation(9)
				'k' -> font.SetTextureLocation(10)
				'K' -> font.SetTextureLocation(10)
				'l' -> font.SetTextureLocation(11)
				'L' -> font.SetTextureLocation(11)
				'm' -> font.SetTextureLocation(12)
				'M' -> font.SetTextureLocation(12)
				'n' -> font.SetTextureLocation(13)
				'N' -> font.SetTextureLocation(13)
				'o' -> font.SetTextureLocation(14)
				'O' -> font.SetTextureLocation(14)
				'p' -> font.SetTextureLocation(15)
				'P' -> font.SetTextureLocation(15)
				'q' -> font.SetTextureLocation(16)
				'Q' -> font.SetTextureLocation(16)
				'r' -> font.SetTextureLocation(17)
				'R' -> font.SetTextureLocation(17)
				's' -> font.SetTextureLocation(18)
				'S' -> font.SetTextureLocation(18)
				't' -> font.SetTextureLocation(19)
				'T' -> font.SetTextureLocation(19)
				'u' -> font.SetTextureLocation(20)
				'U' -> font.SetTextureLocation(20)
				'v' -> font.SetTextureLocation(21)
				'V' -> font.SetTextureLocation(21)
				'w' -> font.SetTextureLocation(22)
				'W' -> font.SetTextureLocation(22)
				'x' -> font.SetTextureLocation(23)
				'X' -> font.SetTextureLocation(23)
				'y' -> font.SetTextureLocation(24)
				'Y' -> font.SetTextureLocation(24)
				'z' -> font.SetTextureLocation(25)
				'Z' -> font.SetTextureLocation(25)
				'1' -> font.SetTextureLocation(26)
				'2' -> font.SetTextureLocation(27)
				'3' -> font.SetTextureLocation(28)
				'4' -> font.SetTextureLocation(29)
				'5' -> font.SetTextureLocation(30)
				'6' -> font.SetTextureLocation(31)
				'7' -> font.SetTextureLocation(32)
				'8' -> font.SetTextureLocation(33)
				'9' -> font.SetTextureLocation(34)
				'0' -> font.SetTextureLocation(35)
			}
			if(i == 0) {
				font.offset(x, y)
			} else {
				font.offset(x - 34, 0)
			}
			font.addIndices(buffer)
			font.addVertexes(buffer)
			font.addTextureCoordinates(buffer)
		}
		buffer.setTextureId(3)
		buffer.createBuffers(true, true, true)
	}
}
