package layout;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import snaplic.com.wordstore.MainActivity;
import snaplic.com.wordstore.R;
import snaplic.com.wordstore.WordDetail;


public class NewWord extends DialogFragment
{


    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        final LayoutInflater inflater=getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View view=inflater.inflate(R.layout.dialog_content,null);
        builder.setView(view)
                .setTitle("Add new word")
                .setPositiveButton("Add", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // FIRE ZE MISSILES!
                        Intent intent= new Intent(getActivity(), WordDetail.class);
                        EditText editText=(EditText)view.findViewById(R.id.dialogText);
                        intent.putExtra(MainActivity.DEFINE_WORD,editText.getText().toString());
                        intent.putExtra(MainActivity.WORD_EXISTS,false);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}



