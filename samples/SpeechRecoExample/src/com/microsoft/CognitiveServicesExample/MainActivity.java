/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license.
 * //
 * Project Oxford: http://ProjectOxford.ai
 * //
 * ProjectOxford SDK GitHub:
 * https://github.com/Microsoft/ProjectOxford-ClientSDK
 * //
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 * //
 * MIT License:
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * //
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * //
 * THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.microsoft.CognitiveServicesExample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.bing.speech.Conversation;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements ISpeechRecognitionServerEvents {
    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    EditText _logText;
    RadioGroup _radioGroup;
    Button _buttonSelectMode;
    Button _startButton;
    Button _selectFile;
    String file_path; // get the filepath of the prerecorded audio
    TextView txt_pace, txt_filler;
    int filler_words;
    long _duration;
    Button resultButton;

    double audio_duration,pace;

    public enum FinalResponseStatus {NotReceived, OK, Timeout}

    /**
     * Gets the primary subscription key
     */
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     *
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     *
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    /**
     * Gets a value indicating whether or not to use the microphone.
     *
     * @return true if [use microphone]; otherwise, false.
     */
    private Boolean getUseMicrophone() {
        int id = this._radioGroup.getCheckedRadioButtonId();
        return id == R.id.micIntentRadioButton ||
                id == R.id.micDictationRadioButton ||
                id == (R.id.micRadioButton - 1);
    }

    /**
     * Gets a value indicating whether LUIS results are desired.
     *
     * @return true if LUIS results are to be returned otherwise, false.
     */
    private Boolean getWantIntent() {
        int id = this._radioGroup.getCheckedRadioButtonId();
        return id == R.id.dataShortIntentRadioButton ||
                id == R.id.micIntentRadioButton;
    }

    /**
     * Gets the current speech recognition mode.
     *
     * @return The speech recognition mode.
     */
    private SpeechRecognitionMode getMode() {
      /*  int id = this._radioGroup.getCheckedRadioButtonId();
        if (id == R.id.micDictationRadioButton ||
                id == R.id.dataLongRadioButton) {
            return SpeechRecognitionMode.LongDictation;
        }

        return SpeechRecognitionMode.ShortPhrase;*/

        return SpeechRecognitionMode.LongDictation;
    }

    /**
     * Gets the default locale.
     *
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "en-us";
    }

    /**
     * Gets the short wave file path.
     *
     * @return The short wave file.
     */
    private String getShortWaveFile() {
        return "whatstheweatherlike.wav";
    }

    /**
     * Gets the long wave file path.
     *
     * @return The long wave file.
     */
    private String getLongWaveFile() {

        Log.e("Filepath", file_path);

        return file_path;
    }

    /**
     * Gets the Cognitive Service Authentication Uri.
     *
     * @return The Cognitive Service Authentication Uri.  Empty if the global default is to be used.
     */
    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this._logText = (EditText) findViewById(R.id.editText1);
        this._radioGroup = (RadioGroup) findViewById(R.id.groupMode);
        this._buttonSelectMode = (Button) findViewById(R.id.buttonSelectMode);
        this._startButton = (Button) findViewById(R.id.button1);
        this._selectFile = (Button) findViewById(R.id.btn_select_File);
        this.txt_pace = (TextView) findViewById(R.id.txt_pace);
        this.txt_filler = (TextView) findViewById(R.id.txt_filler);


        this._startButton.setVisibility(View.INVISIBLE);


        if (getString(R.string.primaryKey).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }

        // setup the buttons
        final MainActivity This = this;
        this._startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                This.StartButton_Click(arg0);
            }
        });

        this._buttonSelectMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                This.ShowMenu(This._radioGroup.getVisibility() == View.INVISIBLE);
            }
        });

        this._radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rGroup, int checkedId) {
                This.RadioButton_Click(rGroup, checkedId);
            }
        });

        this.ShowMenu(true);


    }


    public void action_chooseFile(View view) {

        try {

            Log.e("buttonActivity", "Entered");
            Intent intent_upload = new Intent();
            intent_upload.setType("*/*");
            intent_upload.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent_upload, 1);
            onActivityResult(0, 1, intent_upload);
            this._startButton.setVisibility(View.VISIBLE);


        } catch (Exception e) {

            e.printStackTrace();
        }


    }


    public int no_of_words() {


        String word = _logText.getText().toString();
        System.out.println(word);

        Log.i("fds", word);

        String[] split = word.split(" ");


        return split.length;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        int duration = 0;

        try {


            if (requestCode == 1) {

                if (resultCode == RESULT_OK) {

                    Uri uri = intent.getData();
                    if (uri.getScheme().toString().compareTo("content") == 0) {
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor.moveToFirst()) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);//Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                            Uri filePathUri = Uri.parse(cursor.getString(column_index));
                            String file_name = filePathUri.getLastPathSegment().toString();
                            file_path = filePathUri.getPath();
                            Toast.makeText(this, "File Name & PATH are:" + file_name + "\n" + file_path, Toast.LENGTH_LONG).show();

                            MediaPlayer player = MediaPlayer.create(this, filePathUri);
                            //player.setLooping(true);
                            player.start();


                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(file_path);
                            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                            if (durationStr != null) {
                                duration = Integer.parseInt(durationStr);
                            }


                            long duration_org = duration / 1000;
                            long h = duration_org/ 3600;
                            long m = (duration_org - h * 3600) / 60;
                            long s = duration_org - (h * 3600 + m * 60);

                           /* //mDuration = duration;
                            long h = duration / 3600000;
                            long m = (duration - h * 3600) / 60;
                            long s = duration - (h * 3600 + m * 60);*/

                            this._duration = duration;
                            Log.e("Duration", String.valueOf(_duration));


                            Log.e("Duration",durationStr);
                            audio_duration=s;
                            //this.pace=duration/audio_duration;

                            //words=no_of_words();
                            //txt_pace.setText("Pace"+"         "+"-"+" "+String.valueOf(m)+"words/min");

                        }
                    }

                }
            }


        } catch (Exception e) {

            e.printStackTrace();

        }


    }

    private void ShowMenu(boolean show) {
        if (show) {
            this._radioGroup.setVisibility(View.VISIBLE);
            this._logText.setVisibility(View.INVISIBLE);
        } else {
            this._radioGroup.setVisibility(View.INVISIBLE);
            this._logText.setText("");
            this._logText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles the Click event of the _startButton control.
     */
    private void StartButton_Click(View arg0) {
        this._startButton.setEnabled(false);
        this._radioGroup.setEnabled(false);

        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 20 : 200;

        this.ShowMenu(false);

        this.LogRecognitionStart();

        if (this.getUseMicrophone()) {
            if (this.micClient == null) {
                if (this.getWantIntent()) {
                    this.WriteLine("--- Start microphone dictation with Intent detection ----");

                    this.micClient =
                            SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                } else {
                    this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                            this,
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.micClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.micClient.startMicAndRecognition();
        } else {
            if (null == this.dataClient) {
                if (this.getWantIntent()) {
                    this.dataClient =
                            SpeechRecognitionServiceFactory.createDataClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                } else {
                    this.dataClient = SpeechRecognitionServiceFactory.createDataClient(
                            this,
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.dataClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.SendAudioHelper((this.getMode() == SpeechRecognitionMode.ShortPhrase) ? this.getShortWaveFile() : this.getLongWaveFile());




        }
    }

    /**
     * Logs the recognition start.
     */
    private void LogRecognitionStart() {
        String recoSource;
        if (this.getUseMicrophone()) {
            recoSource = "microphone";
        } else if (this.getMode() == SpeechRecognitionMode.ShortPhrase) {
            recoSource = "short wav file";
        } else {
            recoSource = "long wav file";
        }

        /*this.WriteLine("\n--- Start speech recognition using " + recoSource + " with " + this.getMode() + " mode in " + this.getDefaultLocale() + " language ----\n\n");
*/
    }

    private void SendAudioHelper(String filename) {
        RecognitionTask doDataReco = new RecognitionTask(this.dataClient, this.getMode(), filename);
        try {
            doDataReco.execute().get(m_waitSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            doDataReco.cancel(true);
        }
    }

    public void onFinalResponseReceived(final RecognitionResult response) {

        // Jeyamaal 2-8-2017
        Intent intent_result;
        String result_text=null;


        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this._startButton.setEnabled(true);
            this.isReceivedResponse = FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {

            //Defalut
/*            this.WriteLine("********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                this.WriteLine("[" + i + "]" + " Confidence=" + response.Results[i].Confidence +
                        " Text=\"" + response.Results[i].DisplayText + "\"");
            }*/

            // Jeyamaal 2-8-2017
           /* Intent intent_result;
            String result_text=null;*/

            for (int i = 0; i < response.Results.length; i++) {

                this.WriteLine(response.Results[i].DisplayText + "\"");

                //String results=_logText.getText().toString();
                result_text=response.Results[i].DisplayText + "\"";






            }

            this.WriteLine();
            //HighlightingFillerWords();
          /*  intent_result=new Intent(this,ResultsActivity.class);
            intent_result.putExtra("key_results",result_text);
            startActivity(intent_result);*/
        }

            HighlightingFillerWords();
           // HighlightingFillerWords1();

    }



    /**
     * Called when a final response is received and its intent is parsed
     */
    public void onIntentReceived(final String payload) {
        this.WriteLine("--- Intent received by onIntentReceived() ---");
        this.WriteLine(payload);
        this.WriteLine();
    }

    // Jeyamaal 1-8-2017

    public void onPartialResponseReceived(final String response) {
        // this.WriteLine("--- Partial result received by onPartialResponseReceived() ---");
        // this.WriteLine(response);
        //this.WriteLine();
    }

    public void onError(final int errorCode, final String response) {
        this._startButton.setEnabled(true);
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
        this.WriteLine();
    }

    /**
     * Called when the microphone status has changed.
     *
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {
        this.WriteLine("--- Microphone status change received by onAudioEvent() ---");
        this.WriteLine("********* Microphone status: " + recording + " *********");
        if (recording) {
            this.WriteLine("Please start speaking.");
        }

        WriteLine();
        if (!recording) {
            this.micClient.endMicAndRecognition();
            this._startButton.setEnabled(true);
        }
    }

    /**
     * Writes the line.
     */
    private void WriteLine() {
        this.WriteLine("");
    }

    /**
     * Writes the line.
     *
     * @param text The line to write.
     */
    private void WriteLine(String text) {
        this._logText.append(text + "\n");
    }

    /**
     * Handles the Click event of the RadioButton control.
     *
     * @param rGroup    The radio grouping.
     * @param checkedId The checkedId.
     */
    private void RadioButton_Click(RadioGroup rGroup, int checkedId) {
        // Reset everything
        if (this.micClient != null) {
            this.micClient.endMicAndRecognition();
            try {
                this.micClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.micClient = null;
        }

        if (this.dataClient != null) {
            try {
                this.dataClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.dataClient = null;
        }

        this.ShowMenu(false);
        this._startButton.setEnabled(true);
    }

    /*
     * Speech recognition with data (for example from a file or audio source).  
     * The data is broken up into buffers and each buffer is sent to the Speech Recognition Service.
     * No modification is done to the buffers, so the user can apply their
     * own VAD (Voice Activation Detection) or Silence Detection
     * 
     * @param dataClient
     * @param recoMode
     * @param filename
     */
    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode recoMode;
        String filename;

        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String filename) {
            this.dataClient = dataClient;
            this.recoMode = recoMode;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Note for wave files, we can just send data from the file right to the server.
                // In the case you are not an audio file in wave format, and instead you have just
                // raw data (for example audio coming over bluetooth), then before sending up any 
                // audio data, you must first send up an SpeechAudioFormat descriptor to describe 
                // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
                // String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
                Log.e("File_path", file_path);

                //Previous Code
                //InputStream fileStream = getAssets().open(file_path);

                FileInputStream fileStream = new FileInputStream(file_path);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];

                do {
                    // Get  Audio data to send into byte buffer.
                    bytesRead = fileStream.read(buffer);

                    if (bytesRead > -1) {
                        // Send of audio data to service. 
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                dataClient.endAudio();
            }

            return null;
        }

    }

    private void HighlightingFillerWords() {

        String generated_text = _logText.getText().toString();

        SpannableStringBuilder highlightedtext = new SpannableStringBuilder(generated_text);

        //String filler_word = "\\s(ah|um)\\s";

        String filler_word = "(\\sah\\s)| (um)";
        Pattern pattern = Pattern.compile(filler_word, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(generated_text);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            String hastag = generated_text.substring(start, end);

            Log.e("hastag", hastag);

            ForegroundColorSpan span = new ForegroundColorSpan(Color.YELLOW);

            highlightedtext.setSpan(new BackgroundColorSpan(0xFFFFFF00), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //Log.e("highlightedtext",);


        }
        Log.e("generated Text", generated_text);
        _logText.setText(highlightedtext);
        // pace();


    }




    public void btn_result_clicked(View View){

        String results=_logText.getText().toString();
        Intent intent_result=new Intent(this,ResultsActivity.class);
        intent_result.putExtra("key_results",results);
        intent_result.putExtra("audio_duration",audio_duration);
        startActivity(intent_result);
    }


}
