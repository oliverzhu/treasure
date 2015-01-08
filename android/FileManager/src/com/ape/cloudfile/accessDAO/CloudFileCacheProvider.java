package com.ape.cloudfile.accessDAO;


import com.ape.filemanager.R;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class CloudFileCacheProvider extends ContentProvider
{
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final String AUTHORITY = "com.ape.cloudfile.cacheProvider";
    private static final int CLOUD_FILES = 1;
    private static final int CLOUD_FILE = 2;
    private static final int CLOUD_SEARCH_BASIC = 3;
    private static final int CLOUD_SEARCH_SHORTCUT = 4;

    static {
        MATCHER.addURI(AUTHORITY, "access_files", CLOUD_FILES);
        MATCHER.addURI(AUTHORITY, "access_files/#", CLOUD_FILE);
        MATCHER.addURI(AUTHORITY, "cloud/search/" + SearchManager.SUGGEST_URI_PATH_QUERY, CLOUD_SEARCH_BASIC);
        MATCHER.addURI(AUTHORITY, "cloud/search/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", CLOUD_SEARCH_BASIC);
        MATCHER.addURI(AUTHORITY, "cloud/search/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", CLOUD_SEARCH_SHORTCUT);
    }

    private CloudFileDBHelper mDbHelper;

    @Override
    public boolean onCreate()
    {
        mDbHelper = new CloudFileDBHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String limit = uri.getQueryParameter("limit");

        switch (MATCHER.match(uri))
        {
            case CLOUD_FILES:
                return db.query(mDbHelper.getTableName(),
                        projection, selection, selectionArgs, null, null, sortOrder, limit);

            case CLOUD_FILE:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                    where = selection + " and " + where;
                }
                return db.query(mDbHelper.getTableName(),
                        projection, where, selectionArgs, null, null, sortOrder, limit);

            case CLOUD_SEARCH_BASIC:
                return doFileSearch(uri, limit);

            case CLOUD_SEARCH_SHORTCUT:
                return doShortcutSearch(uri, limit);

            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public String getType(Uri uri)
    {
        switch (MATCHER.match(uri))
        {
        case CLOUD_FILES:
            return "vnd.android.cursor.dir/com.ape.cloudfile";

        case CLOUD_FILE:
            return "vnd.android.cursor.item/com.ape.cloudfile";

        default:
            throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        switch (MATCHER.match(uri))
        {
        case CLOUD_FILES:
            long rowid = db.insert(mDbHelper.getTableName(), null, values);
            Uri insertUri = ContentUris.withAppendedId(uri, rowid); // new record uri
            getContext().getContentResolver().notifyChange(uri, null);
            return insertUri;

        default:
            throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri))
        {
        case CLOUD_FILES:
            count = db.delete(mDbHelper.getTableName(), selection, selectionArgs);
            return count;

        case CLOUD_FILE:
            long id = ContentUris.parseId(uri);
            String where = "_id=" + id;
            if (!TextUtils.isEmpty(selection))
            {
                where = selection + " and " + where;
            }
            count = db.delete("person", where, selectionArgs);
            return count;

        default:
            throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri))
        {
        case CLOUD_FILES:
            count = db.update(mDbHelper.getTableName(), values, selection, selectionArgs);
            return count;

        case CLOUD_FILE:
            long id = ContentUris.parseId(uri);
            String where = "_id=" + id;
            if (!TextUtils.isEmpty(selection))
            {
                where = selection + " and " + where;
            }
            count = db.update(mDbHelper.getTableName(), values, where, selectionArgs);
            return count;

        default:
            throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    //For searchable input control, begin.
    public Cursor doFileSearch(Uri uri, String limit)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String searchString = uri.getPath().endsWith("/") ? "" : uri.getLastPathSegment();
        searchString = Uri.decode(searchString).trim();
        if (TextUtils.isEmpty(searchString)) {
            return null;
        }

        searchString = searchString.replace("\\", "\\\\");
        searchString = searchString.replace("%", "\\%");
        searchString = searchString.replace("'", "\\'");
        searchString = "%" + searchString + "%";
        String where = CloudFileDBHelper.FIELD_NAME + " LIKE ? ESCAPE '\\'";
        String[] whereArgs = new String[] { searchString };
        return db.query(mDbHelper.getTableName(), sSearchFileCols, where, whereArgs, null, null, null, limit);
    }

    public Cursor doShortcutSearch(Uri uri, String limit)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String searchString = uri.getLastPathSegment();
        searchString = Uri.decode(searchString).trim();
        if (TextUtils.isEmpty(searchString)) {
            return null;
        }

        String where = CloudFileDBHelper.FIELD_ID + "=?";
        String[] whereArgs = new String[] { searchString };
        return db.query(mDbHelper.getTableName(), sSearchFileCols, where, whereArgs, null, null, null, limit);
    }
    
    private static String[] sSearchFileCols = new String[] {
        CloudFileDBHelper.FIELD_ID,
        "(CASE WHEN file_type=0 THEN " + R.drawable.folder +
        " ELSE CASE WHEN file_type=1 THEN " + R.drawable.file_icon_mp3 +
        " ELSE CASE WHEN file_type=2 THEN " + R.drawable.file_icon_video +
        " ELSE CASE WHEN file_type=3 THEN " + R.drawable.file_icon_picture +
        " ELSE CASE WHEN file_type=5 THEN " + R.drawable.file_icon_txt +
        " ELSE CASE WHEN file_type=6 THEN " + R.drawable.file_icon_zip +
        " ELSE CASE WHEN file_type=7 THEN " + R.drawable.file_icon_apk +
        " ELSE " + R.drawable.file_icon_default + " END END END END END END END" +
        ") AS " + SearchManager.SUGGEST_COLUMN_ICON_1,
        CloudFileDBHelper.FIELD_NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
        CloudFileDBHelper.FIELD_KEY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_2,
        CloudFileDBHelper.FIELD_KEY + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA,
        CloudFileDBHelper.FIELD_ID + " AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID
    };
    //For searchable input control, begin.
}
