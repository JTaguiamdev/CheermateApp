// Replace: app/src/main/java/com/example/cheermateapp/data/db/DbProvider.kt
package com.cheermateapp.data.db

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson

object DbProvider {
    @Volatile
    private var INSTANCE: AppDb? = null

    fun get(context: Context, gson: Gson = Gson()): AppDb =
        INSTANCE ?: synchronized(this) {
            val typeConverter = AppTypeConverters(gson)  // Create instance

            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "cheermate.db"
            )
                .addTypeConverter(typeConverter)  // Pass the instance
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = db
            db
        }
}
