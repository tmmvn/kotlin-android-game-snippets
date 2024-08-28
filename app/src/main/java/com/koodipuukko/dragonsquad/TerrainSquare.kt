package com.koodipuukko.dragonsquad

class TerrainSquare : Quad(384, 192) {
	init {
		textureWidth = 256
		textureHeight = 256
	}

	fun setTextureLocation(location: Int) {		// texture coordinate presets for tiles
		when (location) {
			0 -> setTextureCoordinates(0, 127, 0, 63) // top left
			1 -> setTextureCoordinates(128, 255, 0, 63) // top right
			2 -> setTextureCoordinates(0, 127, 64, 127) // second left
			3 -> setTextureCoordinates(128, 255, 64, 127) // second right
			4 -> setTextureCoordinates(0, 128, 128, 191) // third left
			5 -> setTextureCoordinates(128, 255, 128, 191) // third right
			6 -> setTextureCoordinates(0, 128, 192, 255) // bottom left
			7 -> setTextureCoordinates(128, 255, 192, 255) // bottom right
		}
	}

	fun offset(x: Int, y: Int) {
		run {
			var i = 0
			while(i < 12) {
				vertices[i] = vertices[i] + x
				i += 3
			}
		}
		var i = 1
		while(i < 12) {
			vertices[i] = vertices[i] + y
			i += 3
		}
	}
}
