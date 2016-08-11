package snaplic.com.wordstore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import layout.NewWord;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static String DEFINE_WORD="com.snaplic.wordstore.DEFINE";
    public static String WORD_EXISTS="com.snaplic.wordstore.Word_EXISTS";
    public static String WORD_COUNT="com.snaplic.wordstore.COUNTER";
    static ActionMode mActionMode;
    ArrayList<String> savedWords;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NewWord().show(getSupportFragmentManager(),"standart");
            }
        });

        mRecyclerView=(RecyclerView)findViewById(R.id.wordsList);
        mLayoutManager= new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        savedWords=getWords();
        if (savedWords.isEmpty())
        {
            savedWords.add("Add your words to see them here");
        }
        mAdapter= new WordsAdapter(this,this,savedWords);

        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        HashSet<String> set=new HashSet<>();
        set.addAll(getWords());
        savedWords.clear();
        savedWords.addAll(set);
        Collections.sort(savedWords, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        mAdapter.notifyDataSetChanged();
    }


    public ArrayList<String> getWords()
    {
        SQLiteDatabase db=new WordsDbHelper(this).getReadableDatabase();
        String[] projection={WordsContract.WordsEntry._ID};
        Cursor queryResult=db.query(
                WordsContract.WordsEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,null,null);

        if(queryResult!=null)
        {
            queryResult.moveToFirst();
            ArrayList<String> list= new ArrayList<>();
            while(queryResult.moveToNext())
            {
                list.add(queryResult.getString(0));
            }

            return list;
        }

        else
        {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }









}


class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder>
{
    ArrayList<String> words;
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    Context context;
    AppCompatActivity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView mTextView;
        public View mView;
        ActionMode actionMode;
        boolean modestate;


        public ViewHolder(View view)
        {
            super(view);
            mView=view;
            mTextView=(TextView)view.findViewById(R.id.wordItem);
        }
    }

    public WordsAdapter(Context context,AppCompatActivity activity,ArrayList<String> words)
    {
        this.words=words;
        this.context=context;
        this.activity=activity;

        context.getTheme().resolveAttribute(R.attr.selectableItemBackground,mTypedValue,true);
        mBackground=mTypedValue.resourceId;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item,parent,false);
        v.setBackgroundResource(mBackground);
        return new ViewHolder(v);
    }


    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.mTextView.setText(words.get(position));
        final Intent intent = new Intent(context, WordDetail.class);
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                intent.putExtra(MainActivity.DEFINE_WORD, words.get(position));
                intent.putExtra(MainActivity.WORD_EXISTS, true);
                intent.putStringArrayListExtra(MainActivity.WORD_COUNT,words);
                context.startActivity(intent);
            }


        });

        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {

                ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

                    // Called when the action mode is created; startActionMode() was called
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        // Inflate a menu resource providing context menu items
                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.delete_menu, menu);
                        return true;
                    }

                    // Called each time the action mode is shown. Always called after onCreateActionMode, but
                    // may be called multiple times if the mode is invalidated.
                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false; // Return false if nothing is done
                    }

                    // Called when the user selects a contextual menu item
                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menuDelete:
                                Log.d("Menu works","the menu is clicked");
                                SQLiteDatabase db=new WordsDbHelper(context).getWritableDatabase();
                                db.delete(
                                        WordsContract.WordsEntry.TABLE_NAME,
                                        WordsContract.WordsEntry._ID+"=?",new String[]{words.get(position)});
                                words.remove(position);


                                notifyDataSetChanged();


                                mode.finish(); // Action picked, so close the CAB
                                return true;
                            default:
                                view.setBackgroundColor(ContextCompat.getColor(context,R.color.white));
                                return false;
                        }
                    }

                    // Called when the user exits the action mode
                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        // mActionMode = null;
                        view.setBackgroundColor(ContextCompat.getColor(context,R.color.background_holo_light));
                    }
                };


                view.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));
                activity.startSupportActionMode(mActionModeCallback);

                return true;
            }
        });
    }





    public int getItemCount()
    {
        return words.size();
    }







}
