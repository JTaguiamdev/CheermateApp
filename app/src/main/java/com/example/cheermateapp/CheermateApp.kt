package com.example.cheermateapp
import com.example.cheermateapp.data.db.AppDb



import android.app.Application
import com.google.gson.Gson

class CheermateApp : Application() {
    lateinit var db: AppDb
    override fun onCreate() {
        super.onCreate()
        db = AppDb.get(this)
    }
}
