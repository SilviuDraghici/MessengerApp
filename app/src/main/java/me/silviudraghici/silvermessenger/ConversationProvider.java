package me.silviudraghici.silvermessenger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Silviu on 2015-10-09.
 */
public class ConversationProvider extends ContentProvider{
    static final String KEY_MESSAGE_NUM = "messageNum";
    static final String KEY_CONVERSATION_NUM = "conversationNum";
    static final String KEY_ORIGINATOR = "originator";
    static final String KEY_DATE = "date";
    static final String KEY_MESSAGE = "message";

    static final String MESSAGES_TABLE = "Messages";
    static final String CONVERSATIONS_TABLE = "convo_table";
    static final String USERS_TABLE = "members_table";
    static final int DATABASE_VERSION = 1;

    private static final String CREATE_DATABASE_INSTRUCTION;
    static final String CONVERSATION_QUERY;
    //final Context context;

    static final String PROVIDER_NAME =
            "me.silviudraghici.silvermessenger.provider.Messages";

    static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/");

    static final int CONVERSATION_SWITCH = 1;

    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "#", CONVERSATION_SWITCH);

        CREATE_DATABASE_INSTRUCTION = String.format("create table %s " +
                                                    "(%s integer not null, " +
                                                    "%s integer not null, " +
                                                    "%s long not null, " +
                                                    "%s text not null);",
                                                    MESSAGES_TABLE,
                                                    KEY_CONVERSATION_NUM,
                                                    KEY_ORIGINATOR,
                                                    KEY_DATE,
                                                    KEY_MESSAGE);

        CONVERSATION_QUERY = String.format("SELECT ROWID _id,%s,%s,%s FROM %s WHERE %S = ? " +
                                            "ORDER BY ROWID",
                                            KEY_ORIGINATOR, KEY_DATE, KEY_MESSAGE,
                                            MESSAGES_TABLE, KEY_CONVERSATION_NUM);
    }

    DatabaseHelper DBHelper;
    SQLiteDatabase db;

//    public ConversationProvider(Context context){
//        CREATE_DATABASE_INSTRUCTION = String.format("create table %s " +
//                                                    "(%s integer not null, " +
//                                                    "%s integer not null, " +
//                                                    "%s text not null, " +
//                                                    "%s text not null);",
//                                                    MESSAGES_TABLE,
//                                                    KEY_CONVERSATION_NUM,
//                                                    KEY_ORIGINATOR,
//                                                    KEY_DATE,
//                                                    KEY_MESSAGE);
//        System.out.println(CREATE_DATABASE_INSTRUCTION);
//
//        CONVERSATION_QUERY = String.format("SELECT ROWID _id,%s,%s,%s FROM %s WHERE %S = ? " +
//                                            "ORDER BY ROWID",
//                                            KEY_ORIGINATOR, KEY_DATE, KEY_MESSAGE,
//                                            MESSAGES_TABLE, KEY_CONVERSATION_NUM);
//
//        this.context = context;
//        this.DBHelper = new DatabaseHelper(context);
//    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(MESSAGES_TABLE);

//        if (uriMatcher.match(uri) == BOOK_ID)
//            //---if getting a particular book---
//            sqlBuilder.appendWhere(
//                    _ID + " = " + uri.getPathSegments().get(1));
//        System.out.println(uri.getPathSegments().get(0) + "--------------------------------------");

        if (sortOrder == null || sortOrder.equals(""))
            sortOrder = "ROWID";

        Cursor c = sqlBuilder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);
        c = db.rawQuery(CONVERSATION_QUERY, new String[]{uri.getPathSegments().get(0)});
        //---register to watch a content URI for changes---
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //---add a new message---
        long rowID = db.insert(MESSAGES_TABLE, null, values);

        //---if added successfully---
        if (rowID>0)
        {
            Uri returnUri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        count = db.delete(MESSAGES_TABLE, selection, selectionArgs );
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper(Context context)
        {
            super(context, MESSAGES_TABLE, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(CREATE_DATABASE_INSTRUCTION);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS" + MESSAGES_TABLE);
            onCreate(db);
        }
    }

    public void open(){
        db = DBHelper.getWritableDatabase();
    }

    public void close() {
        DBHelper.close();
    }

    public Cursor getConversation(String conversationId){
        return db.rawQuery(""/*CONVERSATION_QUERY*/, new String[]{conversationId});
    }

    public boolean insertMessage(int conversation, int originatorNum, String date, String message){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CONVERSATION_NUM, conversation);
        initialValues.put(KEY_ORIGINATOR, originatorNum);
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_MESSAGE, message);
        long num = db.insert(MESSAGES_TABLE, null, initialValues);
        System.out.println("PRINT:" + String.valueOf(num>0) + "----------------------------");
        return (num > 0);
    }
}
