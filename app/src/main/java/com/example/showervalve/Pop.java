package com.example.showervalve;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.widget.EditText;

import java.io.IOException;

public class Pop extends Activity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.popupwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        double width = dm.widthPixels * .75;
        double height = dm.heightPixels * .5;

        getWindow().setLayout((int)width, (int)height);

        prefs = getSharedPreferences("prefID", 0);

        final EditText editText = (EditText) findViewById(R.id.textView5);
        editText.setText(Integer.toString(prefs.getInt("timer", 10)));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.getText().length()>0) {
                    SharedPreferences.Editor editor1 = prefs.edit();
                    editor1.putInt("timer", Integer.parseInt(editText.getText().toString()));
                    editor1.apply();
                    editor1.commit();
                    try {
                        MainActivity.out.writeUTF(editText.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
