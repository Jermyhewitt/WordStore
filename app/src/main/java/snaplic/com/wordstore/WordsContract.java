package snaplic.com.wordstore;

import android.provider.BaseColumns;

/**
 * Created by Jermaine on 10/8/2016.
 */
public class WordsContract {

    public WordsContract()
    {

    }

    public static abstract class WordsEntry implements BaseColumns
    {
        public static final String TABLE_NAME="WordsTable";
        public static final String COLUMN_NAME_SHORT_DEF="shortDefinition";
        public static final String COLUMN_NAME_LONG_DEF="longDefinition";
        public static final String COLUMN_NAME_USAGE="usage";
        public static final String COLUMN_NAME_NULLABLE="usage";

    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WordsEntry.TABLE_NAME + " (" +
                    WordsEntry._ID +TEXT_TYPE+ " PRIMARY KEY," +
                    WordsEntry.COLUMN_NAME_SHORT_DEF+TEXT_TYPE+COMMA_SEP+
                    WordsEntry.COLUMN_NAME_LONG_DEF+TEXT_TYPE + COMMA_SEP +
                    WordsEntry.COLUMN_NAME_USAGE+ TEXT_TYPE+
                    " )";


    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + WordsEntry.TABLE_NAME;
}




