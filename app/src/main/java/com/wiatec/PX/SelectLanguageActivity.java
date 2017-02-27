package com.wiatec.PX;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.wiatec.update.MainActivity;
import com.wiatec.update.R;

/**
 * Created by PX on 2016-11-17.
 */

public class SelectLanguageActivity extends Activity {
    private Spinner spinner;
    private Button bt_Select;
    private LinearLayout ll_Language;
    private String [] languages;
    private String currentLanguage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_language);

        ll_Language = (LinearLayout) findViewById(R.id.ll_language);
        spinner = (Spinner) findViewById(R.id.spinner);
        bt_Select = (Button) findViewById(R.id.bt_select);

        sharedPreferences = getSharedPreferences("language" ,MODE_PRIVATE);
        currentLanguage = sharedPreferences.getString("language" ,"");
        if("".equals(currentLanguage)){
            ll_Language.setVisibility(View.VISIBLE);
            languages = new String[] { getString(R.string.english) ,getString(R.string.spanish) ,getString(R.string.italian)
                    ,getString(R.string.korea) ,getString(R.string.chinese) ,getString(R.string.chinese_tw) ,};
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SelectLanguageActivity.this ,android.R.layout.simple_spinner_dropdown_item ,languages);
            spinner.setAdapter(arrayAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentLanguage = languages[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    currentLanguage = languages[0];
                }
            });

            bt_Select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = sharedPreferences.edit();
                    Log.d("----px----" , currentLanguage);
                    editor.putString("language",currentLanguage);
                    editor.commit();
                    startActivity(new Intent(SelectLanguageActivity.this , MainActivity.class));
                    finish();
                }
            });
        }else {
            startActivity(new Intent(SelectLanguageActivity.this , MainActivity.class));
            Log.d("----px----" , currentLanguage);
        }



    }
}
