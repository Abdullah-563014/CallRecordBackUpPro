package com.uttam.callrecord.backuppro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uttam.callrecord.backuppro.model.CallListModelClass;

public class Database extends SQLiteOpenHelper {

    SQLiteDatabase database;
    private static final String DB_NAME="MyDb.db";
    private static final String TABLE_NAME="MyTable";
    private static int VERSION=1;
    private static final String ID="Id";
    private static final String DATE="Date";
    private static final String MONTH="Month";
    private static final String YEAR="Year";
    private static final String CALL_INDICATOR="CallIndicator";
    private static final String DURATION="Duration";
    private static final String NAME="Name";
    private static final String TIME="Time";
    private static final String FILE_NAME="File";
    private static final String UPLOAD_STATUS="UploadStatus";
    private static final String CREATE_TABLE="CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("+ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+DATE+" TEXT , "+MONTH+" TEXT , "+YEAR+" TEXT , "+CALL_INDICATOR+" TEXT , "+DURATION+" TEXT , "+NAME+" TEXT , "+TIME+" TEXT, "+FILE_NAME+" TEXT,"+UPLOAD_STATUS+" TEXT )";

    public Database(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }




    public SQLiteDatabase initializedDatabase(){
        if (database==null){
            database=this.getWritableDatabase();
        }
        return database;
    }

    public void closeDatabase(){
        if (database!=null){
            database.close();
        }
    }

//===============================================================================================
//===============================================================================================
//===============================================================================================





    public long insertData(String date,String month, String year, CallListModelClass callListModelClass) {
        ContentValues contentValues=new ContentValues();
        contentValues.put(DATE,date);
        contentValues.put(MONTH,month);
        contentValues.put(YEAR,year);
        contentValues.put(CALL_INDICATOR,callListModelClass.getCallIndicator());
        contentValues.put(DURATION,callListModelClass.getDuration());
        contentValues.put(NAME,callListModelClass.getName());
        contentValues.put(TIME,callListModelClass.getTime());
        contentValues.put(FILE_NAME,callListModelClass.getFile());
        contentValues.put(UPLOAD_STATUS,"false");
        long value=database.insert(TABLE_NAME,null,contentValues);
        return value;
    }

    public Cursor getAllData(){
        Cursor cursor=database.rawQuery("SELECT * FROM "+TABLE_NAME+" ORDER BY "+DATE+" DESC ,"+MONTH+" DESC ,"+YEAR+" DESC ",null);
        return cursor;
    }

    public Cursor getDataUsingCallIndicator(String indicator){
        Cursor cursor=database.query(TABLE_NAME,null,CALL_INDICATOR+"=?",new String[]{indicator},null,null,DATE+" DESC ,"+MONTH+" DESC ,"+YEAR+" DESC ",null);
        return cursor;
    }

    public Cursor getDataUsingUploadStatus(String status){
        Cursor cursor=database.query(TABLE_NAME,null,UPLOAD_STATUS+"=?",new String[]{status},null,null,ID+" ASC ",null);
        return cursor;
    }

    public int deleteData(String path){
        int value=database.delete(TABLE_NAME, FILE_NAME+" = ?",new String[]{path});
        return value;
    }

    public int updateDataUsingUploadStatus(String id, String status) {
        ContentValues values = new ContentValues();
        values.put(UPLOAD_STATUS, status);
        return database.update(TABLE_NAME,values,ID+" = "+id,null);
//        return database.update(TABLE_NAME, values, ID +" = ?", new String[]{id});
    }

    public int deleteDataUsingId(String id) {
        int value=database.delete(TABLE_NAME,ID+" = "+id,null);
        return value;
    }

    public int deleteAllData() {
        int value=database.delete(TABLE_NAME, null,null);
        return value;
    }
}
