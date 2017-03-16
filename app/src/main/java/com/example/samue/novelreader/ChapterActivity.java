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

public class ChapterActivity extends AppCompatActivity {

    private String chapterLink, novelName;
    GridView novelChaptersTextView;
    TextView novelHeader;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);
        Intent intent = getIntent();
        chapterLink = intent.getStringExtra(MainActivity.EXTRA_LINK);
        novelName = intent.getStringExtra(MainActivity.EXTRA_NOVEL_NAME);
        progress = (ProgressBar) findViewById(R.id.progress_bar_chapter);
        novelHeader = (TextView) findViewById(R.id.novel_name_header_chapter);
        progress.setVisibility(View.VISIBLE);
        novelHeader.setText(novelName);

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
        @Override
        protected Void doInBackground(Void... params) {

            try {
                Document doc = Jsoup.connect(chapterLink).get();
                Elements elements = doc.select("div[itemprop=articleBody]");
                Elements links = elements.select("a[href]");
                for (Element link : links) {
                    if (link.text().contains("Chapter")) {
                        tempChapterNames.add(new Chapter(novelName, link.id(), link.text(), link.attr("href")));
                    }
                }
            } catch (Exception e) { Log.e("main", ""+e);}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPreExecute();
            setChapterLinks(tempChapterNames);
        }

    }
}
