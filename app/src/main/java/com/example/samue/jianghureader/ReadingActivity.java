package com.example.samue.jianghureader;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.samue.jianghureader.data.NovelContract.NovelEntry;
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


public class ReadingActivity extends AppCompatActivity implements WebParsingInterface<ReadingPage>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_ID = ReadingActivity.class.getSimpleName();
    public static final String CHAPTER_LINK = "chapter_link";
    public static final String CHAPTER_HEADER = "chapter_header";
    public static final String CHAPTER_TEXT = "chapter_text";
    public static final String NEXT_LINK = "next_link";
    public static final String PREV_LINK = "prev_link";
    private static final int LOADER_ID = 3;


    private String mChapterlink, mChapterHeader, mChapterText, mNextLink, mPrevLink;
    private Uri mUri;
    private ScrollView mScrollView;
    private TextView mNovelTextView, mChapterHeaderTextView, mPrevTopTextView, mNextTopTextView, mPrevBottomTextView,
            mNextBottomTextView;
    private ProgressBar mProgressBar;
    private float mTextSize;
    private int mXCor, mYCor;

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
    //private View mScrollView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mScrollView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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
            mScrollView.setVerticalScrollBarEnabled(false);
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

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.hide();
        }

        mVisible = true;
        mScrollView = (ScrollView) findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.

        mNovelTextView = (TextView) findViewById(R.id.main_text_view);
        mChapterHeaderTextView = (TextView) findViewById(R.id.novel_name_header_novel);
        mPrevTopTextView = (TextView) findViewById(R.id.prev_link_text_view);
        mNextTopTextView = (TextView) findViewById(R.id.next_link_text_view);
        mPrevBottomTextView = (TextView) findViewById(R.id.prev_link_text_view_bottom);
        mNextBottomTextView = (TextView) findViewById(R.id.next_link_text_view_bottom);
        mProgressBar = (ProgressBar) findViewById(R.id.loading_spinner_reading);

        mScrollView.setVerticalScrollBarEnabled(false);
        mPrevTopTextView.setPaintFlags(mPrevTopTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // underline under text
        mNextTopTextView.setPaintFlags(mNextTopTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mPrevBottomTextView.setPaintFlags(mPrevBottomTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        mNextBottomTextView.setPaintFlags(mNextBottomTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mPrevTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(mPrevLink);
            }
        });

        // Registers touch events relative to screen and does one of 3: page up, toggle immersive
        // mode, page down.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        final int maxWidth = size.x;
        final int maxHeight = size.y;

        mNovelTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mXCor = (int) event.getRawX();
                    mYCor = (int) event.getRawY();

                }
                return false;
            }
        });

        mNovelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // compute x,y coordinates relative to screen. (based on pixel touch location)
                int touchLocationY = (int) Math.floor(((double) mYCor / maxHeight) * 100);
                int touchLocationX = (int) Math.floor(((double) mXCor / maxWidth) * 100);
                int ySlop = (int) (maxHeight * 0.025);

                if ((touchLocationY > 30 && touchLocationY < 70) && (touchLocationX > 30 &&
                        touchLocationX < 70)) { // middle of screen 30 - 70% both x, y
                    toggle(); // toggle immersive mode
                    mScrollView.setVerticalScrollBarEnabled(true);
                } else if (touchLocationY < 40) { // Upper part of screen 0 - 40%
                    mScrollView.scrollBy(0, -(maxHeight - ySlop)); // scroll up
                } else { // lower part of screen 70% +
                    mScrollView.scrollBy(0, +(maxHeight - ySlop)); // scroll down
                }
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String textSizeString = sharedPreferences.getString(getString(R.string.pref_text_size_key),
                getString(R.string.pref_text_size_default));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        mTextSize = Float.valueOf(textSizeString);
        mNovelTextView.setTextSize(mTextSize);

        mPrevTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(mPrevLink);
            }
        });
        mPrevBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(mPrevLink);
            }
        });

        mNextBottomTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(mNextLink);
            }
        });
        mNextTopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startWebParse(mNextLink);
            }
        });


        if (savedInstanceState != null) {
            ReadingPage rp = new ReadingPage();
            rp.setChapterLink(savedInstanceState.getString(CHAPTER_LINK));
            rp.setChapterHeader(savedInstanceState.getString(CHAPTER_HEADER));
            rp.setChapterText(savedInstanceState.getString(CHAPTER_TEXT));
            rp.setChapterNextLink(savedInstanceState.getString(NEXT_LINK));
            rp.setChapterPrevLink(savedInstanceState.getString(PREV_LINK));
            mUri = Uri.parse(savedInstanceState.getString("uri"));
            setNovelText(rp);
            mVisible = savedInstanceState.getBoolean("navigation_visible");
        } else {
 /*
            Received intent ------------------------------------------------------------------------
            Behold!! Shit happens here..
            Basically:
            1. If intent matches ACTION_VIEW, get uri which is a link to (hopefully) a WW novel.
            2. Compare incoming novel with novels in database, if OK we basically have correct URI and mChapterlink
            3. Then, check if the link is to the ToC page instead of a chapter, if it is start ChapterActivity
            4. If the first if test did not pass, the intent is a "normal" intent from ChaptersFragment
            5. We have all req data and can start loading data and updating database.

            */
            Intent intent = getIntent();
            if (Intent.ACTION_VIEW.equals(intent.getAction())) { // implicit intent
                Uri data = intent.getData();
                mChapterlink = data.toString();
                mUri = findNovelUri(mChapterlink);
                if (mUri == null) { // Could not find novel in database, exit
                    errorLoading();
                    return;
                }
                if (mChapterlink.endsWith("-index/")) { // dirty hack in case intent is to chapter page
                    Intent intentChapter = new Intent(this, ChapterActivity.class);
                    intentChapter.putExtra(EXTRA_NOVEL_LINK, mChapterlink);
                    intentChapter.setData(mUri); // important
                    this.startActivity(intentChapter);
                    this.finish(); // close this activity
                    return; // return so the activity does not run in background
                }
            } else { // "normal" intent

                mChapterlink = intent.getStringExtra(EXTRA_NOVEL_LINK); // explicit intent
                mUri = intent.getData();
            }

            Bundle bundle = new Bundle();
            bundle.putString("link", mChapterlink);
            getSupportLoaderManager().initLoader(LOADER_ID, bundle, webParseReadingLoader);
        }

    }

    /*
    Finds and returns a uri to matching novel link
    1. Get a cursor with all novels
    2. Go send each novelTocLin to static method novelIsSame in ChaptersFragment
    3. If match --> return URI from matching element in database
     */
    private Uri findNovelUri(String testLink) {
        // Used thread here? with handler..
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
            while (cursor.moveToNext()) {
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

        outState.putString(CHAPTER_LINK, mChapterlink);
        outState.putString(CHAPTER_HEADER, mChapterHeader);
        outState.putString(CHAPTER_TEXT, mChapterText);
        outState.putString(NEXT_LINK, mNextLink);
        outState.putString(PREV_LINK, mPrevLink);
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

        mChapterlink = readingPage.getChapterLink(); // so "last" novel visited
        mChapterHeader = readingPage.getChapterHeader();
        mChapterText = readingPage.getChapterText();
        mNextLink = readingPage.getChapterNextLink();
        mPrevLink = readingPage.getChapterPrevLink();

        updateLastChapter(mChapterlink);

        mNovelTextView.setText(mChapterText); // main text
        mChapterHeaderTextView.setText(mChapterHeader); // novel name
        mPrevTopTextView.setText(getString(R.string.previous)); // Setting text here so it looks cleaner when opening for first time
        mNextTopTextView.setText(getString(R.string.next));
        mPrevBottomTextView.setText(getString(R.string.previous));
        mNextBottomTextView.setText(getString(R.string.next));

        mScrollView.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private void startWebParse(String link) {
        Bundle bundle = new Bundle();
        bundle.putString("link", link);
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, webParseReadingLoader);
    }

    @Override
    public void startLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void finishedLoading(List<ReadingPage> readingText) {
        if (readingText == null) {
            errorLoading();
            return;
        }
        setNovelText(readingText.get(0)); // only one element
        mProgressBar.setVisibility(View.GONE);
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
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
    // --------------------- Fullscreen controls --------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (savedInstanceState != null && mVisible) { // rotating
            hide();
        }
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
        mScrollView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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
                .setText(mChapterlink)
                .startChooser();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_text_size_key))) {
            mTextSize = Float.valueOf(sharedPreferences.getString(key,
                    getString(R.string.pref_text_size_default)));
            mNovelTextView.setTextSize(mTextSize);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private LoaderCallbacks<List<ReadingPage>> webParseReadingLoader = new LoaderCallbacks<List<ReadingPage>>() {

        @Override
        public Loader<List<ReadingPage>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<ReadingPage>>(getBaseContext()) {
                String chapterLink = args.getString("link");
                List<ReadingPage> mNovelInfo = null;

                @Override
                protected void onStartLoading() {
                    if (mNovelInfo != null) {
                        deliverResult(mNovelInfo);
                    } else {
                        mProgressBar.setVisibility(View.VISIBLE);
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

                    if (!chapterLink.startsWith("http://")) { // fix for pasting link without http prefix
                        chapterLink = "http://" + chapterLink;
                    }
                    try {
                        Document doc = Jsoup.connect(chapterLink).get();
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

                        readingPage.setChapterLink(chapterLink); // link to chapter
                        readingPage.setChapterHeader(chapterHeader); // chapter header
                        readingPage.setChapterPrevLink(prevLink); // prev link
                        readingPage.setChapterNextLink(nextLink); // next link
                        readingPage.setChapterText(mainText); // novel text

                        if (readingPage.illegalState()) { // check if some values are not set
                            return null; // pass null to onPostExecute, so calling activity can handle error loading
                        }

                        novelInfo.add(readingPage);

                    } catch (IOException IOE) {
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
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<List<ReadingPage>> loader) {

        }

    };
}
