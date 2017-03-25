package com.example.samue.novelreader;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.example.samue.novelreader.MainActivity.APPLICATION_ID;
import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_NAME;
import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_LINK;
import static com.example.samue.novelreader.MainActivity.FROM_MAIN;
import static com.example.samue.novelreader.MainActivity.FROM_READING;

public class ChapterActivity extends AppCompatActivity {


    private String chapterLink;
    GridView novelChaptersTextView;
    TextView novelHeader;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        progress = (ProgressBar) findViewById(R.id.progress_bar_chapter);
        novelHeader = (TextView) findViewById(R.id.novel_name_header_chapter);
        progress.setVisibility(View.VISIBLE);
        novelHeader.setText("Loading...");

        Intent intent = getIntent();
        chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK);
        /*if (intent != null) {
            String strData = intent.getExtras().getString(APPLICATION_ID);
            if (strData.equals(FROM_MAIN)) {
                chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK);
            } else if (strData.equals(FROM_READING)) {
                chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK);
            }
        }*/
        Log.v("chapterLink: ", chapterLink);

        new ParseNovelChapters().execute();
    }

    public void setChapterLinks(ArrayList<Chapter> chapterLinks) {
        novelChaptersTextView = (GridView) findViewById(R.id.chapter_list);
        ChapterAdapter adapter = new ChapterAdapter(this, chapterLinks);

        novelChaptersTextView.setAdapter(adapter);
        progress.setVisibility(View.INVISIBLE);

    }

    class ParseNovelChapters extends AsyncTask<Void, Void, Void> {
        ArrayList<Chapter> tempChapterNames = new ArrayList<>();
        String novelName = "";
        @Override
        protected Void doInBackground(Void... params) {

            try {
                Document doc = Jsoup.connect(chapterLink).get();
                novelName = doc.select("h1[class=entry-title]").text();
                if (novelName.contains("(")) { // chinese name inside brackets ()
                    String[] nameSplit = novelName.split("[(]"); // nameSlipt = { "english", "chinese"}
                    novelName = nameSplit[0].trim();
                }
                if (novelName.contains("–")) {
                    String[] nameSplit = novelName.split("[–]");
                    novelName = nameSplit[0];
                }
                Elements elements = doc.select("div[itemprop=articleBody]"); // area where links are
                Elements links = elements.select("a[href]"); // all links
                for (Element link : links) {
                    if (link.text().contains("Chapter")) {
                        tempChapterNames.add(new Chapter(link.id(), link.text(), link.attr("href")));
                    }
                }
            } catch (Exception e) { Log.e("main", ""+e);}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPreExecute();
            novelHeader.setText(novelName);
            setChapterLinks(tempChapterNames);
        }

    }
}
