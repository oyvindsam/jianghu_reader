package com.example.samue.jianghureader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samue.jianghureader.data.NovelContract.NovelEntry;
import com.example.samue.jianghureader.data.WebParsingInterface;
import com.example.samue.jianghureader.layout.NovelsFragment;
import com.example.samue.jianghureader.model.ReadingPage;

import java.util.ArrayList;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;


public class ReadingActivity extends AppCompatActivity implements WebParsingInterface<ReadingPage> {

    private static final String LOG_ID = ReadingActivity.class.getSimpleName();

    String chapterLink, nextLink, prevLink;
    private Uri mUri;
    ScrollView scrollView;
    TextView novelTextView, novelHeader, prevTextView, nextTextView, prevBottomTextView,
            nextBottomTextView;
    ProgressBar progress;
    List<String> novelInfoList;

    Display mdisp;
    Point mdispSize;
    int maxY, touchSlop;
    int xCor, yCor, maxWidth, maxHeight;


    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //private View scrollView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            scrollView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };


    private final Runnable mToggleScrollBar = new Runnable() {
        @Override
        public void run() {
            scrollView.setVerticalScrollBarEnabled(false);
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
        getSupportActionBar().hide();
        setContentView(R.layout.activity_reading);

        mVisible = true;
        scrollView = (ScrollView) findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        novelTextView = (TextView) findViewById(R.id.main_text_view);
        novelHeader = (TextView) findViewById(R.id.novel_name_header_novel);
        prevTextView = (TextView) findViewById(R.id.prev_link_text_view);
        nextTextView = (TextView) findViewById(R.id.next_link_text_view);
        prevBottomTextView = (TextView) findViewById(R.id.prev_link_text_view_bottom);
        nextBottomTextView = (TextView) findViewById(R.id.next_link_text_view_bottom);
        progress = (ProgressBar) findViewById(R.id.loading_spinner_reading);

        scrollView.setVerticalScrollBarEnabled(false);
        prevTextView.setPaintFlags(prevTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // underline under text
        nextTextView.setPaintFlags(nextTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        prevBottomTextView.setPaintFlags(prevBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        nextBottomTextView.setPaintFlags(nextBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        // touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        // Registers touch events relative to screen and does one of 3: page up, toggle immersive
        // mode, page down.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        maxWidth = size.x;
        maxHeight = size.y;

        novelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // compute x,y coordinates relative to screen. (based on pixel touch location)
                int touchLocationY =  (int) Math.floor(((double) yCor / maxHeight) * 100);
                int touchLocationX = (int) Math.floor(((double) xCor / maxWidth) * 100);

                if ((touchLocationY > 30 && touchLocationY < 70) && (touchLocationX > 30 &&
                        touchLocationX < 70)) { // middle of screen 30 - 70% both x, y
                    toggle(); // toggle immersive mode
                    scrollView.setVerticalScrollBarEnabled(true);
                }
                else if (touchLocationY < 40) { // Upper part of screen 0 - 40%
                    scrollView.scrollBy(0, -(maxHeight - 20)); // scroll up
                } else { // lower part of screen 70% +
                    scrollView.scrollBy(0, +(maxHeight - 20)); // scroll down
                }
            }
        });

        novelTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    xCor = (int) event.getRawX();
                    yCor = (int) event.getRawY();
                }
                return false;
            }
        });



        novelInfoList = new ArrayList<>();

        // Received intent ------------------------------------------------------------------------
        Intent intent = getIntent();

        chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK); // explicit intent
        mUri = intent.getData();
        Log.v(LOG_ID, mUri.toString());

        WEBPARSE.parseReadingPage(chapterLink, this);
    }

    private void setTouchCordinates(double x, double y) {
        xCor = (int) x;
        yCor = (int) y;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVisible = true;
        toggle(); // immersive mode
    }


    private void updateLastChapter(String chapterLink) {
        ContentValues values = new ContentValues();
        values.put(NovelEntry.COLUMN_NOVEL_LAST_CHAPTER_LINK, chapterLink);

        // add int test here
        getContentResolver().update(
                mUri,
                values,
                null,
                null
        );
    }

    // called in WebParse to update/set data in reading activity
    public void setNovelText(final ReadingPage readingPage) {

        chapterLink = readingPage.getChapterLink(); // so "last" novel visited
        nextLink = readingPage.getNovelNextLink();
        prevLink = readingPage.getNovelPrevLink();

        updateLastChapter(chapterLink);

        novelTextView.setText(readingPage.getNovelText()); // main text
        novelHeader.setText(readingPage.getChapterHeader()); // novel name
        prevTextView.setText(getString(R.string.previous)); // Setting text here so it looks cleaner when opening for first time
        nextTextView.setText(getString(R.string.next));
        prevBottomTextView.setText(getString(R.string.previous));
        nextBottomTextView.setText(getString(R.string.next));


        prevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(prevLink);
            }
        });

        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(nextLink);
            }
        });

        prevBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(prevLink);
            }
        });

        nextBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(nextLink);
            }
        });

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private void startWebParse(String link) {
        WEBPARSE.parseReadingPage(link, this);
    }

    @Override
    public void startLoading() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishedLoading(List<ReadingPage> readingText) {
        if (readingText == null) {
            errorLoading();
            return;
        }
        setNovelText(readingText.get(0)); // only one element
        progress.setVisibility(View.GONE);
    }

    private void errorLoading() {
        Toast.makeText(this, "Error loading chapter", Toast.LENGTH_SHORT).show();
        finish();
    }

    // --------------------- Fullscreen controls --------------------------------------

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
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        mHideHandler.postDelayed(mToggleScrollBar, AUTO_HIDE_DELAY_MILLIS);

    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        scrollView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ----------------------------------------------------------------------------------

}
