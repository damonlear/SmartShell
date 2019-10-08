package com.csg.smartshell.demo

import android.app.Application

class AndroidApplication : Application() {
    companion object {

        private var app: AndroidApplication? = null
            get() {
                if (field == null) {
                    field = AndroidApplication()
                }
                return field
            }

        fun get(): AndroidApplication {
            return app!!
        }
    }
}