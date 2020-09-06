package com.example.geoincendios.persistence

import android.R.attr.name
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast



val DATABASE_NAME = "MyDB"
val TABLE_NAME = "ZonasRiesgo"
val COL_ADDRESS = "address"
val COL_LAT = "lat"
val COL_LONG = "lng"
val COL_ID = "id"

class DatabaseHandler(var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null,1){
    override fun onCreate(db: SQLiteDatabase?) {

        val createTable = "CREATE TABLE " + TABLE_NAME +"(" +
                COL_ADDRESS +" VARCHAR(256), " +
                COL_LAT + " VARCHAR(256), " +
                COL_ID + " INTEGER, " +
                COL_LONG +" VARCHAR(256))"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {

    }

    fun insertData(zonaRiesgo: ZonaRiesgoBD){

        val db = this.writableDatabase

        val query = "Select * from " + TABLE_NAME + " where id = "+ zonaRiesgo.id
        val dbRead = this.readableDatabase
        val resultRead = dbRead.rawQuery(query,null)

        if (resultRead.count !=0){
            Toast.makeText(context,"La zona ya se encuentra guardada",Toast.LENGTH_SHORT).show()
            return
        }

        var cv = ContentValues()
        cv.put(COL_ADDRESS, zonaRiesgo.addres)
        cv.put(COL_LAT, zonaRiesgo.lat)
        cv.put(COL_LONG, zonaRiesgo.lng)
        cv.put(COL_ID, zonaRiesgo.id)
        var result = db.insert(TABLE_NAME, null,cv)

        Log.i("BD",cv.get(COL_ADDRESS).toString() + cv.get(COL_LAT).toString() + cv.get(COL_LONG))

        if (result == -1.toLong())
        {
            Toast.makeText(context,"Ha ocurrido un error al intentar guardar la zona",Toast.LENGTH_SHORT).show()
        }
        else{   Toast.makeText(context,"La zona se ha guardado exitosamente",Toast.LENGTH_SHORT).show()}

    }


    fun deleteBD(){
        context.deleteDatabase(DATABASE_NAME)
        Toast.makeText(context,"Se borro la BD",Toast.LENGTH_SHORT).show()
    }

    fun readData() : MutableList<ZonaRiesgoBD>{
        var list : MutableList<ZonaRiesgoBD> = ArrayList()

        val db = this.readableDatabase
        val query = "Select * from " + TABLE_NAME
        val result = db.rawQuery(query,null)
        if(result.moveToFirst()){
            do {
                var zona = ZonaRiesgoBD(0,"","","")
                zona.addres = result.getString(result.getColumnIndex(COL_ADDRESS)).toString()
                zona.lat = result.getString(result.getColumnIndex(COL_LAT)).toString()
                zona.lng = result.getString(result.getColumnIndex(COL_LONG)).toString()
                zona.id = result.getString(result.getColumnIndex(COL_ID)).toInt()
                list.add(zona)
            }while (result.moveToNext())
        }
        result.close()
        db.close()
        return list
    }

}















