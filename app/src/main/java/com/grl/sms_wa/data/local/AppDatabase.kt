package com.grl.sms_wa.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grl.sms_wa.data.local.dao.UserDao
import com.grl.sms_wa.data.local.entities.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "app_database"
    }
}
