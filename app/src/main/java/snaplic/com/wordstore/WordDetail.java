package snaplic.com.wordstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.LightingColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class WordDetail extends AppCompatActivity {
    TextView meaning,usage,wordMeaning,usageContent,description;
    boolean wordExists;
    WordsDbHelper wordsDbHelper;
    String sentWord;


    /*In order to avoid permutations with different Upper and Lower case characters from being added to database remember to
    *** ensure that the word that comes from main activity is transformed to upper or lower
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        wordsDbHelper=new WordsDbHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sentWord=getIntent().getStringExtra(MainActivity.DEFINE_WORD);
        meaning=(TextView)findViewById(R.id.word);
        usage=(TextView)findViewById(R.id.wordusage);
        wordMeaning=(TextView)findViewById(R.id.meaning);
        usageContent=(TextView)findViewById(R.id.useContent);
        description=(TextView)findViewById(R.id.descriptionContent);
        meaning.setText(sentWord);
        usage.setText("Usage");
        wordExists=getIntent().getBooleanExtra(MainActivity.WORD_EXISTS,false);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.detailFab);
        final ArrayList<String> random=getIntent().getStringArrayListExtra(MainActivity.WORD_COUNT);

        ((ProgressBar)findViewById(R.id.meaningProgress)).getIndeterminateDrawable().setColorFilter(new LightingColorFilter(0xFF000000, 0x03A9F2));

        if(random!=null)
        {

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Random rand = new Random();
                    int value = rand.nextInt(random.size());
                    sentWord=random.get(value);
                    Log.d("Randomword",sentWord);
                    meaning.setText(sentWord);
                    NestedScrollView view1=((NestedScrollView)findViewById(R.id.scrollView));
                    view1.smoothScrollTo(0,0);
                    String stringUrl="https://www.vocabulary.com/dictionary/"+sentWord;
                    String stringUrl2="http://sentence.yourdictionary.com/"+sentWord;
                    Log.d("Randomword",stringUrl);
                    Log.d("Randomword",stringUrl2);
                    new DownloadWebpageTask().execute(stringUrl,"meaning");
                    new DownloadWebpageTask().execute(stringUrl2,"usage");



                }
            });

        }
        else
        {
           fab.setVisibility(View.GONE);
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String stringUrl="https://www.vocabulary.com/dictionary/"+sentWord;
        String stringUrl2="http://sentence.yourdictionary.com/"+sentWord;


            new DownloadWebpageTask().execute(stringUrl,"meaning");
            new DownloadWebpageTask().execute(stringUrl2,"usage");





    }




    private class DownloadWebpageTask extends AsyncTask<String, Void, String[]> {
        String destination;
        @Override
        protected String[] doInBackground(String... urls) {
            destination=urls[1];
            Log.d("destination",urls[1]);
            SQLiteDatabase db;

            // params comes from the execute() call: params[0] is the url.
            if(wordExists==false)
            {



                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected())
                {
                    try {
                        StringBuilder res=new StringBuilder();
                        Document doc;
                        db=wordsDbHelper.getWritableDatabase();

                        if (destination.equals("meaning"))
                        {
                            doc=Jsoup.connect(urls[0]).get();
                            Elements shortmeaning=doc.getElementsByClass("short");
                            Elements longMeaning=doc.getElementsByClass("long");
                            if(shortmeaning.isEmpty())
                            {
                                return null;
                            }
                            String result[]={shortmeaning.get(0).text(),longMeaning.get(0).text()};


                            ContentValues values=new ContentValues();
                            values.put(WordsContract.WordsEntry._ID,sentWord);
                            values.put(WordsContract.WordsEntry.COLUMN_NAME_SHORT_DEF,shortmeaning.get(0).text());
                            values.put(WordsContract.WordsEntry.COLUMN_NAME_LONG_DEF,longMeaning.get(0).text());

                            long newRowId=db.insert(
                                    WordsContract.WordsEntry.TABLE_NAME,
                                    WordsContract.WordsEntry.COLUMN_NAME_NULLABLE,
                                    values);
                            Log.d("success", "the row it "+newRowId);
                            return result;


                        }
                        else
                        {
                            doc=Jsoup.connect(urls[0]).get();
                            Log.d("connected",urls[0]);
                            Elements usage=doc.getElementsByClass("li_content");
                            String result[]= new String[usage.size()];
                            int position=0;
                            Log.d("arraysize",usage.size()+"data");
                            StringBuilder builder=new StringBuilder();
                            for(Element use:usage)
                            {
                                result[position]=use.text();
                                builder.append((position+1)+") "+result[position]+"\n\n");
                                position++;
                                Log.d("linkresult",use.text());


                            }
                            ContentValues values=new ContentValues();
                            values.put(WordsContract.WordsEntry.COLUMN_NAME_USAGE,builder.toString());

                            int rowsAffected=db.update(
                                    WordsContract.WordsEntry.TABLE_NAME,
                                    values,
                                    WordsContract.WordsEntry._ID+"=?",
                                    new String[]{sentWord});
                            Log.d("data","rows affected"+rowsAffected);

                            return result;
                        }



                    } catch (IOException e) {
                        return new String[] {"Unable to retrieve web page. URL may be invalid."};
                    }

                }
                else
                {
                    return new String[]{"no WIFI","Please connect to WIFI and retry"};


                }




            }
            else
            {

                db=wordsDbHelper.getReadableDatabase();

                if (destination.equals("meaning"))
                {
                    String[] projection={
                            WordsContract.WordsEntry.COLUMN_NAME_SHORT_DEF,
                            WordsContract.WordsEntry.COLUMN_NAME_LONG_DEF

                    };

                    Cursor queryResult=db.query(
                            WordsContract.WordsEntry.TABLE_NAME,
                            projection,
                            WordsContract.WordsEntry._ID+"=?",
                            new String[]{sentWord},
                            null,null,null);

                    if(queryResult!=null)
                    {
                        queryResult.moveToFirst();

                        return new String[]{
                                queryResult.getString(queryResult.getColumnIndex(WordsContract.WordsEntry.COLUMN_NAME_SHORT_DEF))
                                ,queryResult.getString(queryResult.getColumnIndex(WordsContract.WordsEntry.COLUMN_NAME_LONG_DEF))};
                    }

                    else
                    {
                        return null;
                    }


                }
                else
                {
                    String[] projection={WordsContract.WordsEntry.COLUMN_NAME_USAGE};
                    Cursor queryResult=db.query(
                            WordsContract.WordsEntry.TABLE_NAME,
                            projection,
                            WordsContract.WordsEntry._ID+"=?",
                            new String[]{sentWord},
                            null,null,null);

                    if(queryResult!=null)
                    {
                        queryResult.moveToFirst();
                        return new String[]{queryResult.getString(0)};
                    }

                    else
                    {
                        return null;
                    }


                }





            }

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String[] result)
        {


            if (destination.equals("meaning"))
            {
                ((ProgressBar)findViewById(R.id.meaningProgress)).setVisibility(View.GONE);
                ((ProgressBar)findViewById(R.id.usageProgress)).setVisibility(View.GONE);
                if (result!=null)
                {
                    wordMeaning.setText(result[0]);
                    description.setText(result[1]);

                }
                else
                {
                    wordMeaning.setText("The word provided could not be fetched from the server");
                    description.setText("We are sorry for the inconvenience");

                }


            }
            else
            {
                ((ProgressBar)findViewById(R.id.descriptionProgress)).setVisibility(View.GONE);
                if (result!=null)
                {
                    StringBuilder builder=new StringBuilder();
                    int i=0;
                    for (String res:result)
                    {
                        builder.append((i+1)+") "+result[i]+"\n\n");
                        i++;

                    }
                    usageContent.setText(builder.toString());

                }


            }

        }





    }

}
