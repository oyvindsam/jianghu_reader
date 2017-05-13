package com.example.samue.jianghureader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.samue.jianghureader.data.LastNovelDbHelper;
import com.example.samue.jianghureader.data.NovelContract.NovelKeys;
import com.example.samue.jianghureader.data.WebParse;

import java.util.ArrayList;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;
import static java.security.AccessController.getContext;


public class ReadingActivity extends AppCompatActivity {

    private static final int NOVEL_LINK_PREV = 1;
    private static final int NOVEL_LINK_NEXT = 2;
    private static final int NOVEL_LINK_CURRENT = 4;
    private static final int CURSOR_NOVEL_LINK = 1;
    private static final double MAX_SCREEN_Y_COORDINATE = 2559.0;
    private static final double MAX_SCREEN_X_COORDINATE = 1418.0;

    String novelName, chapterLink;
    ScrollView scrollView;
    TextView novelTextView, novelHeader, prevTextView, nextTextView, prevBottomTextView,
            nextBottomTextView;
    ProgressBar progress;
    List<String> novelInfoList;

    Display mdisp;
    Point mdispSize;
    int maxY, touchSlop;

    private LastNovelDbHelper mLastNovelDb;

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

        scrollView = (ScrollView) findViewById(R.id.fullscreen_content);
        novelTextView = (TextView) findViewById(R.id.main_text_view);
        novelHeader = (TextView) findViewById(R.id.novel_name_header_novel);
        prevTextView = (TextView) findViewById(R.id.prev_link_text_view);
        nextTextView = (TextView) findViewById(R.id.next_link_text_view);
        prevBottomTextView = (TextView) findViewById(R.id.prev_link_text_view_bottom);
        nextBottomTextView = (TextView) findViewById(R.id.next_link_text_view_bottom);
        progress = (ProgressBar) findViewById(R.id.loading_spinner_reading);
        mLastNovelDb = new LastNovelDbHelper(this);

        scrollView.setVerticalScrollBarEnabled(false);
        prevTextView.setPaintFlags(prevTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // underline under text
        nextTextView.setPaintFlags(nextTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        prevBottomTextView.setPaintFlags(prevBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        nextBottomTextView.setPaintFlags(nextBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        // Data for navigation on screen
        mdisp = getWindowManager().getDefaultDisplay(); // Display
        mdispSize = new Point(); // Point
        mdisp.getSize(mdispSize);
        maxY = mdispSize.y; // Max y coordinate => bottom of display
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        //Log.v("maxy: ", "" + maxY);

        // Registers touch events relative to screen and does one of 3: page up, toggle immersive
        // mode, page down.
        novelTextView.setOnTouchListener(new View.OnTouchListener() {
            float startX, startY, endX, endY, dX, dY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = event.getRawX();
                    startY = event.getRawY();
                    //Log.v("RawX: ", "" + startX);
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    endX = event.getRawX();
                    endY = event.getRawY();
                    dX = Math.abs(endX - startX);
                    dY = Math.abs(endY - startY);

                    double touchMove = (Math.sqrt(Math.pow(dX, 2) + Math.pow(dY, 2)));
                    double touchLocationY = Math.floor((startY / MAX_SCREEN_Y_COORDINATE) * 100);
                    double touchLocationX = Math.floor((startX / MAX_SCREEN_X_COORDINATE) * 100);

                    if (touchMove <= touchSlop) { // touchSlop is computed by the system, and tells
                        if ((touchLocationY > 30 && touchLocationY < 70) && (touchLocationX > 30 &&
                                touchLocationX < 70)) { // middle of screen 30 - 70% both x, y
                            toggle(); // toggle immersive mode
                            scrollView.setVerticalScrollBarEnabled(true);
                        }
                        else if (touchLocationY < 40) { // Upper part of screen 0 - 40%
                            scrollView.scrollBy(0, -(maxY - 20)); // scroll up
                        } else { // lower part of screen 70% +
                            scrollView.scrollBy(0, +(maxY - 20)); // scroll down
                        }
                    }
                }
                return true;
            }
        });

        novelInfoList = new ArrayList<>();

        // Received intent ------------------------------------------------------------------------
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) { // implicit intent
            Uri data = intent.getData();
            chapterLink = data.toString();
            if (chapterLink.endsWith("-index/")) { // dirty hack in case intent is to chapter page
                Intent intentChapter = new Intent(this, ChapterActivity.class);
                intentChapter.putExtra(EXTRA_NOVEL_LINK, chapterLink);
                this.startActivity(intentChapter);
                this.finish(); // close this activity
                return; // return so the activity does not run in background
            } else {
                WEBPARSE.findNovelName(chapterLink, this);
                progress.setVisibility(View.VISIBLE);
                return;
            }
        }

        chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK); // explicit intent
        novelName = intent.getStringExtra(EXTRA_NOVEL_NAME);
        Log.v("Name, link: ", novelName + ", " + chapterLink);


        if (mLastNovelDb.getLastChapter(mLastNovelDb.getReadableDatabase(), novelName).size() == 0) {
            Log.v("Lastchapter = 0", "-added first entry to table-");
            ContentValues values = new ContentValues();
            values.put(NovelKeys.COLUMN_NOVEL_NAME, novelName);
            values.put(NovelKeys.COLUMN_NOVEL_LINK, chapterLink);

            SQLiteDatabase database = mLastNovelDb.getWritableDatabase();
            database.insert(NovelKeys.TABLE_NAME, null, values);
            database.close();
        }

        //novelInfoList = new ArrayList<>();
        WEBPARSE.parseChapterText(chapterLink, this, progress);
    }



    @Override
    protected void onResume() {
        super.onResume();
        mVisible = true;
        toggle(); // immersive mode
    }

    public void updateNovelName(String newNovelName) {
        novelName = newNovelName;
        WEBPARSE.parseChapterText(chapterLink, this, progress);
    }

    // novelItems = (header, prevLink, nextLink, String novelText)
    public void setNovelText(List<String> newNovelInfo) {
        chapterLink = newNovelInfo.get(NOVEL_LINK_CURRENT); // so "last" novel visited
        novelInfoList.clear();
        novelInfoList.addAll(newNovelInfo);

        ContentValues values = new ContentValues();
        values.put(NovelKeys.COLUMN_NOVEL_LINK, chapterLink);
        values.put(NovelKeys.COLUMN_NOVEL_NAME, novelName);
        mLastNovelDb.updateLastRead(novelName, mLastNovelDb.getWritableDatabase(), values);


        novelTextView.setText(novelInfoList.get(3)); // main text
        novelHeader.setText(novelInfoList.get(0)); // novel name
        prevTextView.setText(getString(R.string.previous)); // Setting text here so it looks cleaner when opening for first time
        nextTextView.setText(getString(R.string.next));
        prevBottomTextView.setText(getString(R.string.previous));
        nextBottomTextView.setText(getString(R.string.next));

        // novelInfo: { novelName, nextLink, prevLink, mainText }
        prevTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WEBPARSE.parseChapterText(novelInfoList.get(NOVEL_LINK_PREV), (ReadingActivity) v.getContext(), progress);
            }
        });

        nextTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WEBPARSE.parseChapterText(novelInfoList.get(NOVEL_LINK_NEXT), (ReadingActivity) v.getContext(), progress);
            }
        });

        prevBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WEBPARSE.parseChapterText(novelInfoList.get(NOVEL_LINK_PREV), (ReadingActivity) v.getContext(), progress);
            }
        });

        nextBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WEBPARSE.parseChapterText(novelInfoList.get(NOVEL_LINK_NEXT), (ReadingActivity) v.getContext(), progress);
            }
        });

        progress.setVisibility(View.INVISIBLE);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
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
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
}
