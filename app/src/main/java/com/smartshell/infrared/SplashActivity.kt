package com.smartshell.infrared

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.splash_activity.*
import tyrantgit.explosionfield.ExplosionField


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

    }

    override fun onResume() {
        super.onResume()

        val explosionField = ExplosionField.attach2Window(this)
        explosionField.clear()
        val handler = Handler()
        handler.postDelayed(Runnable {
            explosionField.explode(iv)
            handler.postDelayed(Runnable { go2MainActivity() }, 1000)
        }, 1000)
    }

    private fun go2MainActivity() {
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        this@SplashActivity.finish()
    }

}
