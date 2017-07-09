package com.example.samue.jianghureader;

import android.annotation.SuppressLint;
//import android.content.AsyncTaskLoader;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

//import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samue.jianghureader.data.NovelContract;
import com.example.samue.jianghureader.data.NovelContract.NovelEntry;
import com.example.samue.jianghureader.data.WebParse;
import com.example.samue.jianghureader.data.WebParsingInterface;
import com.example.samue.jianghureader.layout.ChaptersFragment;
import com.example.samue.jianghureader.model.ReadingPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.samue.jianghureader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.jianghureader.MainActivity.WEBPARSE;


public class ReadingActivity extends AppCompatActivity implements WebParsingInterface<ReadingPage>,
        LoaderCallbacks<List<ReadingPage>>,
        SharedPreferences.OnSharedPreferenceChangeListener  {

    private static final String LOG_ID = ReadingActivity.class.getSimpleName();
    public static final String CHAPTER_LINK = "chapter_link";
    public static final String CHAPTER_HEADER = "chapter_header";
    public static final String CHAPTER_TEXT = "chapter_text";
    public static final String NEXT_LINK = "next_link";
    public static final String PREV_LINK = "prev_link";
    private static final int LOADER_ID = 3;


    String chapterLink, chapterHeader, chapterText, nextLink, prevLink;
    private Uri mUri;
    ScrollView scrollView;
    TextView novelTextView, chapterHeaderTextView, prevTopTextView, nextTopTextView, prevBottomTextView,
            nextBottomTextView;
    ProgressBar progress;
    private float mTextSize;

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
    private static final int AUTO_HIDE_DELAY_MILLIS = 100;

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
        setContentView(R.layout.activity_reading);
        Log.v(LOG_ID, "onCreate called");

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.hide();
        }

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
        chapterHeaderTextView = (TextView) findViewById(R.id.novel_name_header_novel);
        prevTopTextView = (TextView) findViewById(R.id.prev_link_text_view);
        nextTopTextView = (TextView) findViewById(R.id.next_link_text_view);
        prevBottomTextView = (TextView) findViewById(R.id.prev_link_text_view_bottom);
        nextBottomTextView = (TextView) findViewById(R.id.next_link_text_view_bottom);
        progress = (ProgressBar) findViewById(R.id.loading_spinner_reading);

        scrollView.setVerticalScrollBarEnabled(false);
        prevTopTextView.setPaintFlags(prevTopTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG); // underline under text
        nextTopTextView.setPaintFlags(nextTopTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        prevBottomTextView.setPaintFlags(prevBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        nextBottomTextView.setPaintFlags(nextBottomTextView.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        prevTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(prevLink);
            }
        });

        // touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();

        // Registers touch events relative to screen and does one of 3: page up, toggle immersive
        // mode, page down.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        maxWidth = size.x;
        maxHeight = size.y;

        novelTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    xCor = (int) event.getRawX();
                    yCor = (int) event.getRawY();
                    Log.v(LOG_ID, "Screen COR x,y: " + xCor + ", " + yCor);

                }
                return false;
            }
        });

        novelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // compute x,y coordinates relative to screen. (based on pixel touch location)
                int touchLocationY =  (int) Math.floor(((double) yCor / maxHeight) * 100);
                int touchLocationX = (int) Math.floor(((double) xCor / maxWidth) * 100);
                int ySlop = (int) (maxHeight*0.025);

                if ((touchLocationY > 30 && touchLocationY < 70) && (touchLocationX > 30 &&
                        touchLocationX < 70)) { // middle of screen 30 - 70% both x, y
                    toggle(); // toggle immersive mode
                    scrollView.setVerticalScrollBarEnabled(true);
                }
                else if (touchLocationY < 40) { // Upper part of screen 0 - 40%
                    scrollView.scrollBy(0, -(maxHeight - ySlop)); // scroll up
                } else { // lower part of screen 70% +
                    scrollView.scrollBy(0, +(maxHeight - ySlop)); // scroll down
                }
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String textSizeString = sharedPreferences.getString(getString(R.string.pref_text_size_key),
                getString(R.string.pref_text_size_default));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mTextSize = Float.valueOf(textSizeString);
        Log.v(LOG_ID, "Text Size: " + mTextSize);
        novelTextView.setTextSize(mTextSize);


        prevTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(prevLink);
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
        nextTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(nextLink);
            }
        });


        if(savedInstanceState != null) {
            ReadingPage rp = new ReadingPage();
            rp.setChapterLink(savedInstanceState.getString(CHAPTER_LINK));
            rp.setChapterHeader(savedInstanceState.getString(CHAPTER_HEADER));
            rp.setChapterText(savedInstanceState.getString(CHAPTER_TEXT));
            rp.setChapterNextLink(savedInstanceState.getString(NEXT_LINK));
            rp.setChapterPrevLink(savedInstanceState.getString(PREV_LINK));
            mUri = Uri.parse(savedInstanceState.getString("uri"));
            setNovelText(rp);
            mVisible = savedInstanceState.getBoolean("navigation_visible");
            //scrollView.setScrollY(savedInstanceState.getInt("scroll"));
        } else {
 /*
            Received intent ------------------------------------------------------------------------
            Behold!! Shit happens here..
            Basically:
            1. If intent matches ACTION_VIEW, get uri which is a link to (hopefully) a WW novel.
            2. Compare incoming novel with novels in database, if OK we basically have correct URI and chapterLink
            3. Then, check if the link is to the ToC page instead of a chapter, if it is start ChapterActivity
            4. If the first if test did not pass, the intent is a "normal" intent from ChaptersFragment
            5. We have all req data and can start loading data and updating database.

            */
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) { // implicit intent
                Uri data = intent.getData();
                chapterLink = data.toString();
                Log.v(LOG_ID, "Uri is: " + data);
                mUri = findNovelUri(chapterLink);
                if (mUri == null) { // Could not find novel in database, exit
                    errorLoading();
                    return;
                }
                if (chapterLink.endsWith("-index/")) { // dirty hack in case intent is to chapter page
                    Intent intentChapter = new Intent(this, ChapterActivity.class);
                    intentChapter.putExtra(EXTRA_NOVEL_LINK, chapterLink);
                    intentChapter.setData(mUri); // important
                    this.startActivity(intentChapter);
                    this.finish(); // close this activity
                    return; // return so the activity does not run in background
                }
            } else { // "normal" intent

                chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK); // explicit intent
                mUri = intent.getData();
                Log.v(LOG_ID, mUri.toString());
            }

            //WEBPARSE.parseReadingPage(chapterLink, this); // old implementation
            LoaderCallbacks<List<ReadingPage>> callback = ReadingActivity.this;
            Bundle bundle = new Bundle();
            bundle.putString("link", chapterLink);
            getSupportLoaderManager().initLoader(LOADER_ID, bundle, callback);
        }

    }

    /*
    Finds and returns a uri to matching novel link
    1. Get a cursor with all novels
    2. Go send each novelTocLin to static method novelIsSame in ChaptersFragment
    3. If match --> return URI from matching element in database
     */
    private Uri findNovelUri(String testLink) {
        String[] projection = {
                NovelEntry._ID,
                NovelEntry.COLUMN_NOVEL_TOC_LINK,
        };

        Cursor cursor = getContentResolver().query(
                NovelEntry.CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int linkColumnIndex = cursor.getColumnIndex(NovelEntry.COLUMN_NOVEL_TOC_LINK);
            int idColumnIndex = cursor.getColumnIndex(NovelEntry._ID);
            while(cursor.moveToNext()) {
                String cursorNovelLink = cursor.getString(linkColumnIndex);
                if (ChaptersFragment.novelIsSame(testLink, cursorNovelLink)) {
                    int id = cursor.getInt(idColumnIndex);
                    Uri uri = ContentUris.withAppendedId(NovelEntry.CONTENT_URI, id);
                    cursor.close();
                    return uri;
                }
            }
            cursor.close();
        }
        return null; // link did not match any novel in databse
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_ID, "onSaveInstanceState called");

        outState.putString(CHAPTER_LINK, chapterLink);
        outState.putString(CHAPTER_HEADER, chapterHeader);
        outState.putString(CHAPTER_TEXT, chapterText);
        outState.putString(NEXT_LINK, nextLink);
        outState.putString(PREV_LINK, prevLink);
        outState.putString("uri", mUri.toString());
        outState.putBoolean("navigation_visible", mVisible);
        super.onSaveInstanceState(outState);
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
        chapterHeader = readingPage.getChapterHeader();
        chapterText = readingPage.getChapterText();
        nextLink = readingPage.getChapterNextLink();
        prevLink = readingPage.getChapterPrevLink();

        updateLastChapter(chapterLink);

        novelTextView.setText(chapterText); // main text
        chapterHeaderTextView.setText(chapterHeader); // novel name
        prevTopTextView.setText(getString(R.string.previous)); // Setting text here so it looks cleaner when opening for first time
        nextTopTextView.setText(getString(R.string.next));
        prevBottomTextView.setText(getString(R.string.previous));
        nextBottomTextView.setText(getString(R.string.next));

        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private void startWebParse(String link) {
        //WEBPARSE.parseReadingPage(link, this);
        Bundle bundle = new Bundle();
        bundle.putString("link", link);
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
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
        //delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    private void errorLoading() {
        Toast.makeText(this, "Error loading chapter", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_ID, "onResume called");
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
    // --------------------- Fullscreen controls --------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.v(LOG_ID, "onPostCreate called");
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (savedInstanceState != null && mVisible) { // rotating
            Log.v(LOG_ID, "onPostC -- hiding... ");
            hide();
        }
    }

    private void toggle() {
        if (mVisible) {
            Log.v(LOG_ID, "toggle called, mVisible TRUE, hiding...");
            hide();
        } else {
            Log.v(LOG_ID, "toggle called, mVisible FALSE, showing...");
            show();
        }
    }

    private void hide() {
        Log.v(LOG_ID, "Hide called, mVisible: " + mVisible);
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.postDelayed(mHidePart2Runnable, AUTO_HIDE_DELAY_MILLIS);
        mHideHandler.postDelayed(mToggleScrollBar, AUTO_HIDE_DELAY_MILLIS);

    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
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
        Log.v(LOG_ID, "delayedHide called");
        mVisible = false;
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ----------------------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reading, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_share_chapter:
                shareChapterLink();
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareChapterLink() {
        String mimeType = "text/plain";
        String title = "Share chapter link";
        ShareCompat.IntentBuilder.from(this)
                .setChooserTitle(title)
                .setType(mimeType)
                .setText(chapterLink)
                .startChooser();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_text_size_key))) {
            mTextSize = Float.valueOf(sharedPreferences.getString(key,
                    getString(R.string.pref_text_size_default)));
            novelTextView.setTextSize(mTextSize);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public Loader<List<ReadingPage>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<ReadingPage>>(this) {
            String mLink = args.getString("link");
            List<ReadingPage> mNovelInfo = null;

            @Override
            protected void onStartLoading() {
                if (mNovelInfo != null) {
                    deliverResult(mNovelInfo);
                } else {
                    //startLoading();
                    progress.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<ReadingPage> loadInBackground() {
                List<ReadingPage> novelInfo = new ArrayList<>();
                String prevLink = "";
                String nextLink = "";
                String chapterHeader = "";

                String mainText = "";
                ReadingPage readingPage = new ReadingPage();
                Log.v(LOG_ID, mLink);
                if (!mLink.startsWith("http://")) { // fix for pasting link without http prefix
                    mLink = "http://" + mLink;
                }
                try {
                    Document doc = Jsoup.connect(mLink).get();
                    chapterHeader = doc.select("h1[class=entry-title]").text();
                    Elements elements = doc.select("div[itemprop=articleBody]");
                    Elements links = doc.select("a[href]");
                    Elements paragraphElements = elements.select("p");
                    // Add cancel method and mor try catch.
                    for (Element link : links) {
                        if (link.text().equals("Previous Chapter") && prevLink.length() < 1) {
                            prevLink += link.attr("href");
                        } else if (link.text().equals("Next Chapter") && nextLink.length() < 1) {
                            nextLink += link.attr("href");
                        }
                    }
                    for (Element p : paragraphElements) {
                        if (!p.text().contains("Previous Chapter") && !p.text().contains("Next Chapter")) {
                            mainText += p.text().trim() + "\n\n";
                        }
                    }

                    readingPage.setChapterLink(mLink); // link to chapter
                    readingPage.setChapterHeader(chapterHeader); // chapter header
                    readingPage.setChapterPrevLink(prevLink); // prev link
                    readingPage.setChapterNextLink(nextLink); // next link
                    readingPage.setChapterText(mainText); // novel text

                    if (readingPage.illegalState()) { // check if some values are not set
                        return null; // pass null to onPostExecute, so calling activity can handle error loading
                    }

                    novelInfo.add(readingPage);

                } catch (IOException IOE) {
                    Log.e("ReadingActivity -IOE-", "" + IOE);
                    return null; // pass null to onPostExecute, so calling activity can handle error loading
                }
                return novelInfo;
            }

            @Override
            public void deliverResult(List<ReadingPage> data) {
                mNovelInfo = data;
                super.deliverResult(mNovelInfo);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<ReadingPage>> loader, List<ReadingPage> data) {
        if (data == null) {
            errorLoading();
            return;
        }
        setNovelText(data.get(0)); // only one element
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<List<ReadingPage>> loader) {

    }


}
