package com.bgh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import java.util.concurrent.*;

public class Translate extends Activity {
    private Spinner fromSpinner;
    private Spinner toSpinner;

    private EditText origText;

    private TextView transText;
    private TextView retransText;

    private TextWatcher textWatcher;
    private AdapterView.OnItemSelectedListener itemListener;

    private Handler guiThread;
    private ExecutorService transThread;
    private Runnable updateTask;
    private Future transPending;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initThreading();
        findViews();
        setAdapters();
        setListeners();
    }

    private void setListeners() {
        textWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* do noting */
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                queueUpdate(1000);
            }

            public void afterTextChanged(Editable editable) {
                /* do noting */
            }
        };

        itemListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                queueUpdate(200);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                /* do nothing */
            }
        };

        origText.addTextChangedListener(textWatcher);
        fromSpinner.setOnItemSelectedListener(itemListener);
        toSpinner.setOnItemSelectedListener(itemListener);
    }

    private void queueUpdate(long delayMillis) {
        guiThread.removeCallbacks(updateTask);
        guiThread.postDelayed(updateTask, delayMillis);
    }

    public void setTranslated(String text) {
        guiSetText(transText, text);
    }

    public void setRetranslated(String text) {
        guiSetText(retransText, text);
    }

    private void guiSetText(final TextView transText, final String text) {
        guiThread.post(new Runnable() {
            public void run() {
                transText.setText(text);
            }
        });
    }

    private void setAdapters() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(adapter);
        toSpinner.setAdapter(adapter);

        fromSpinner.setSelection(8);
        toSpinner.setSelection(1);
    }

    private void findViews() {
        fromSpinner = (Spinner) findViewById(R.id.from_language);
        toSpinner = (Spinner) findViewById(R.id.to_language);

        origText = (EditText) findViewById(R.id.original_text);

        transText = (TextView) findViewById(R.id.translated_text);
        retransText = (TextView) findViewById(R.id.retranslated_text);
    }

    private void initThreading() {
        guiThread = new Handler();
        transThread = Executors.newSingleThreadExecutor();

        updateTask = new Runnable() {
            public void run() {
                //get original text
                String original = origText.getText().toString().trim();

                //cancel previous translation if there was one
                if (transPending != null) transPending.cancel(true);

                //take care of the easy case
                if (original.length() == 0) {
                    transText.setText(R.string.empty);
                    retransText.setText(R.string.empty);
                } else {
                    transText.setText(R.string.empty);
                    retransText.setText(R.string.empty);

                    try {
                        TranslateTask translateTask = new TranslateTask(Translate.this, original, getLang(fromSpinner), getLang(toSpinner));
                        transPending = transThread.submit(translateTask);
                    } catch (RejectedExecutionException e) {
                        //Unable to start new task
                        transText.setText(R.string.translation_error);
                        retransText.setText(R.string.translation_error);
                    }
                }
            }
        };
    }

    private String getLang(Spinner spinner) {
        String result = spinner.getSelectedItem().toString();
        int lparen = result.indexOf("(");
        int rparen = result.indexOf(")");
        return result.substring(lparen + 1, rparen);
    }


}
