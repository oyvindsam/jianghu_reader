package com.example.samue.novelreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ReadingActivity extends AppCompatActivity {

    String novelLink;
    String novelName;
    ScrollView scrollView;
    TextView novelTextView, novelHeader, prevTextView, nextTextView, prevBottomTextView,
            nextBottomTextView;
    ProgressBar progress;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reading);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        Intent intent = getIntent();
        novelLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
        novelName = intent.getStringExtra(MainActivity.EXTRA_NOVEL_NAME);
        progress = (ProgressBar) findViewById(R.id.progress_bar);
        new ParseReadingPage().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mVisible = true;
        toggle();
    }

    // novelItems = (header, prevLink, nextLink, String novelText)
    public void setNovelText(final List<String> novelInfo) {

        scrollView = (ScrollView) findViewById(R.id.fullscreen_content);
        novelTextView = (TextView) findViewById(R.id.main_text_view);
        novelHeader = (TextView) findViewById(R.id.novel_name_header);
        prevTextView = (TextView) findViewById(R.id.prev_link_text_view);
        nextTextView = (TextView) findViewById(R.id.next_link_text_view);
        prevBottomTextView = (TextView) findViewById(R.id.prev_link_text_view_bottom);
        nextBottomTextView = (TextView) findViewById(R.id.next_link_text_view_bottom);

        prevTextView.setPaintFlags(prevTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        nextTextView.setPaintFlags(nextTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        prevBottomTextView.setPaintFlags(prevBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        nextBottomTextView.setPaintFlags(nextBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        novelTextView.setText(novelInfo.get(3));
        novelHeader.setText(novelInfo.get(0));
        prevTextView.setText(getString(R.string.previous));
        nextTextView.setText(getString(R.string.next));
        prevBottomTextView.setText(getString(R.string.previous));
        nextBottomTextView.setText(getString(R.string.next));

        novelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("scrollView", "toggle");
                toggle();
            }
        });

        prevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novelLink = novelInfo.get(1);
                new ParseReadingPage().execute();
                progress.setVisibility(View.VISIBLE);
            }
        });

        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novelLink = novelInfo.get(2);
                new ParseReadingPage().execute();
                progress.setVisibility(View.VISIBLE);
            }
        });

        prevBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novelLink = novelInfo.get(1);
                new ParseReadingPage().execute();
                progress.setVisibility(View.VISIBLE);
            }
        });

        nextBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                novelLink = novelInfo.get(2);
                new ParseReadingPage().execute();
                progress.setVisibility(View.VISIBLE);
            }
        });
        scrollView.fullScroll(ScrollView.FOCUS_UP);
        progress.setVisibility(View.INVISIBLE);
    }

    public class ParseReadingPage extends AsyncTask<Void, Void, Void> {

        List<String> novelInfo = new ArrayList<String>();
        String htmlParse = "";
        String prevLink = "";
        String nextLink = "";
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect(novelLink).get();
                String novelHeader = doc.select("h1[class=entry-title]").text();
                Elements elements = doc.select("div[itemprop=articleBody]");
                Elements links = doc.select("a[href]");
                Elements paragraphElements = elements.select("p");
                for (Element link : links) {
                    if (link.text().equals("Previous Chapter") && prevLink.length() < 1) {
                        prevLink += link.attr("href");
                    }
                    else if (link.text().equals("Next Chapter") && nextLink.length() < 1) {
                        nextLink += link.attr("href");
                    }
                }
                for (Element p : paragraphElements) {
                    if (!p.text().contains("Previous Chapter") && !p.text().contains("Previous Chapter")) {
                        Log.v("Text: ", p.text());
                        htmlParse += p.text().trim() + "\n\n";
                    }
                }
                novelInfo.add(novelHeader);
                novelInfo.add(prevLink);
                novelInfo.add(nextLink);
                novelInfo.add(htmlParse);


            } catch (Exception e) { Log.e("main", ""+e);}


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPreExecute();
            setNovelText(novelInfo);
        }

    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        Log.v("HIDE", "trying to hide");
        if (actionBar != null) {
        Log.v("HIDE", "hiding");
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
