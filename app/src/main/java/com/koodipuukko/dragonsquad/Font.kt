package com.koodipuukko.dragonsquad

class Font
	(size: Int) : Quad(size, size) {
	init {
		textureWidth = 256
		textureHeight = 256
	}

	fun SetTextureLocation(location: Int) {		// texture coordinate presets for tiles
		when (location) {
			0 -> setTextureCoordinates(31, 0, 0, 31) // a
			1 -> setTextureCoordinates(63, 32, 0, 31) // b
			2 -> setTextureCoordinates(95, 64, 0, 31) // c
			3 -> setTextureCoordinates(127, 96, 0, 31) // d
			4 -> setTextureCoordinates(159, 128, 0, 31) // e
			5 -> setTextureCoordinates(191, 160, 0, 31) // f
			6 -> setTextureCoordinates(223, 192, 0, 31) // g
			7 -> setTextureCoordinates(255, 224, 0, 31) // h
			8 -> setTextureCoordinates(31, 0, 32, 63) // i
			9 -> setTextureCoordinates(63, 32, 32, 63) // j
			10 -> setTextureCoordinates(95, 64, 32, 63) // k
			11 -> setTextureCoordinates(127, 96, 32, 63) // l
			12 -> setTextureCoordinates(159, 128, 32, 63) // m
			13 -> setTextureCoordinates(191, 160, 32, 63) // n
			14 -> setTextureCoordinates(223, 192, 32, 63) // o
			15 -> setTextureCoordinates(255, 224, 32, 63) // p
			16 -> setTextureCoordinates(31, 0, 64, 95) // q
			17 -> setTextureCoordinates(63, 32, 64, 95) // r
			18 -> setTextureCoordinates(95, 64, 64, 95) // s
			19 -> setTextureCoordinates(127, 96, 64, 95) // t
			20 -> setTextureCoordinates(159, 128, 64, 95) // u
			21 -> setTextureCoordinates(191, 160, 64, 95) // v
			22 -> setTextureCoordinates(223, 192, 64, 95) // w
			23 -> setTextureCoordinates(255, 224, 64, 95) // x
			24 -> setTextureCoordinates(31, 0, 96, 127) // y
			25 -> setTextureCoordinates(63, 32, 96, 127) // z
			26 -> setTextureCoordinates(95, 64, 96, 127) // 1
			27 -> setTextureCoordinates(127, 96, 96, 127) // 2
			28 -> setTextureCoordinates(159, 128, 96, 127) // 3
			29 -> setTextureCoordinates(191, 160, 96, 127) // 4
			30 -> setTextureCoordinates(223, 192, 96, 127) // 5
			31 -> setTextureCoordinates(255, 224, 96, 127) // 6
			32 -> setTextureCoordinates(31, 0, 128, 159) // 7
			33 -> setTextureCoordinates(63, 32, 128, 159) // 8
			34 -> setTextureCoordinates(95, 64, 128, 159) // 9
			35 -> setTextureCoordinates(127, 96, 128, 159) // 0
			else -> setTextureCoordinates(
				159,
				128,
				128,
				159
			) // Bottom right corner as default
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
