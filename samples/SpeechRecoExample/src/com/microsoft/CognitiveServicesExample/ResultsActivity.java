package com.microsoft.CognitiveServicesExample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.value;

/**
 * Created by Marukan on 9/21/2017.
 */

public class ResultsActivity extends Activity {

    EditText result;
    TextView txt_total_count_words,txt_filler_count,txt_total_time,txt_pace,txt_time_mgmt;
    double audio_duration,pace;
    Button btn_startOver;
    String time_result;
    Button btn_time_mgmt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_page);

        time_result="";
        txt_total_count_words=(TextView)findViewById(R.id.text_total_word);
        txt_filler_count=(TextView)findViewById(R.id.txt_filler_count);
        txt_pace=(TextView)findViewById(R.id.txt_pace);
        txt_total_time=(TextView)findViewById(R.id.txt_time);
        result=(EditText)findViewById(R.id.txt_speech_results);
        btn_startOver=(Button)findViewById(R.id.btn_startOver1);
        txt_time_mgmt=(TextView)findViewById(R.id.txt_time_mgmt);
        btn_time_mgmt=(Button)findViewById(R.id.btn_time_mgmt);

        Intent intent= getIntent();
        Bundle bundle=intent.getExtras();
        String message=bundle.getString("key_results");
        audio_duration=bundle.getDouble("audio_duration");


        result=(EditText)findViewById(R.id.txt_speech_results);
        btn_startOver=(Button)findViewById(R.id.btn_startOver1);
        result.setText(message+"\n");

        //HighlightingFillerWords();

        filler_word_count();

    }




    public void filler_word_count() {

        int ah_count= 0;
        int um_count=0;
        int total_filler=0;
        String[] wc = result.getText().toString().split("\\s+");
        String time_result;

        for (int i = 0; i < wc.length; i++) {
            System.out.println(wc[i] + "\n");

 /*           String filler_word_ah = "ah";


            Pattern pattern_ah= Pattern.compile(filler_word_ah,Pattern.CASE_INSENSITIVE);
            Matcher matcher_ah = pattern_ah.matcher(result.getText());


            while(matcher_ah.find()){

                ah_count=ah_count+1;
                Log.e("Total Ah count",String.valueOf(ah_count));
            }*/

            // txt_filler.setText(String.valueOf(wc.length));


        }

/*        for (int i = 0; i < wc.length; i++) {
            System.out.println(wc[i] + "\n");*/



        String filler_word_ah = "(\\sah\\s)";


        Pattern pattern_ah= Pattern.compile(filler_word_ah,Pattern.CASE_INSENSITIVE);
        Matcher matcher_ah = pattern_ah.matcher(result.getText());


        while(matcher_ah.find()){

            ah_count=ah_count+1;
            Log.e("Total Ah count",String.valueOf(ah_count));
        }


        /*String umm=" "+"um.\"";
        String ummm=" "+umm;*/
        String filler_um="(\\sum+.*\\W+.*)";

        Pattern pattern_um= Pattern.compile(filler_um,Pattern.CASE_INSENSITIVE);
        Matcher matcher_um = pattern_um.matcher(result.getText());


            while(matcher_um.find()){

                um_count=um_count+1;
                Log.e("Total um count",String.valueOf(um_count));
            }



            // txt_filler.setText(String.valueOf(wc.length));


      /*  }*/
        total_filler=um_count+ah_count;
       /* DecimalFormat df=new DecimalFormat("0.00");
        String formate = df.format(wc.length/audio_duration);
        double parse= (Double)df.parse(formate) ;*/
        txt_total_time.setText("       "+"Total Time"+"                  "+audio_duration+"  "+"seconds");
        txt_total_count_words.setText("       "+"Number of Words"+"            "+String.valueOf(wc.length)+" "+"words");
        txt_filler_count.setText("       "+"Number of Filler Words"+"       "+String.valueOf(total_filler));
        txt_pace.setText("       "+"Pace"+"                         "+String.valueOf(Math.round(wc.length/(audio_duration/60)))+" words per minute");

        if(audio_duration<=120){

            time_result="greeen";
            btn_time_mgmt.setBackgroundColor(Color.GREEN);
        }

        else if(audio_duration>=120 && audio_duration<=150  ){

            time_result="yellow";
            btn_time_mgmt.setBackgroundColor(Color.YELLOW);
        }

        else if(audio_duration>=150  ){

            time_result="red";
            btn_time_mgmt.setBackgroundColor(Color.RED);
        }

        txt_time_mgmt.setText("       "+"Time Card");

//wc.length/audio_duration
    }

    public void onClick_startOver(View view){

        Log.e("Clicled","Cleikedddddddd");
        Intent intent= new Intent(this,MainActivity.class);
        startActivity(intent);
    }






    private void pace() {

       // txt_pace.setText("Pace" + "         " + "-" + " " + String.valueOf(no_of_words()) + "words/min");

    }
}
