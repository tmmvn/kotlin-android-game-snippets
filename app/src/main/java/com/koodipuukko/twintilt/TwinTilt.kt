package com.koodipuukko.twintilt

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.koodipuukko.R

class TwinTilt : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_twin_tilt)
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_twin_tilt, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		val id = item.itemId
		if(id == R.id.action_settings) {
			return true
		}
		return super.onOptionsItemSelected(item)
	}

	fun sendMessage(view: View?) {
		val intent: Intent = Intent(this, TwinTiltCode::class.java)
		startActivity(intent)
	}
}
