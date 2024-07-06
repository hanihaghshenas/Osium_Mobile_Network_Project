package com.example.osmiom11

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "info.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "info"
        const val COLUMN_ID = "id"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_ECI = "eci"
        const val COLUMN_SIGNAL_POWER = "signal_power"
        const val COLUMN_TIMING_ADVANCE = "timing_advance"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LATITUDE + " TEXT,"
                + COLUMN_LONGITUDE + " TEXT,"
                + COLUMN_ECI + " TEXT,"
                + COLUMN_SIGNAL_POWER + " TEXT,"
                + COLUMN_TIMING_ADVANCE + " TEXT" + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(latitude: String, longitude: String, eci: String, signalPower: String, timingAdvance: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_LATITUDE, latitude)
        contentValues.put(COLUMN_LONGITUDE, longitude)
        contentValues.put(COLUMN_ECI, eci)
        contentValues.put(COLUMN_SIGNAL_POWER, signalPower)
        contentValues.put(COLUMN_TIMING_ADVANCE, timingAdvance)
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    fun getAllData(): Cursor {
        val db = this.writableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME", null)
    }
    fun clearData() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
    }
    fun getDataByEci(eci: String): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME WHERE $COLUMN_ECI=?", arrayOf(eci))
    }
}
