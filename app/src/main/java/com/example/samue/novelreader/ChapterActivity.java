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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static com.example.samue.novelreader.MainActivity.EXTRA_NOVEL_LINK;

public class ChapterActivity extends AppCompatActivity {

    private String chapterLink;
    private GridView novelChaptersTextView;
    private TextView novelHeader;
    private ProgressBar progress;
    private ChapterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        progress = (ProgressBar) findViewById(R.id.progress_bar_chapter);
        novelHeader = (TextView) findViewById(R.id.novel_name_header_chapter);
        novelChaptersTextView = (GridView) findViewById(R.id.chapter_list);
        novelHeader.setText("Loading...");
        progress.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        chapterLink = intent.getStringExtra(EXTRA_NOVEL_LINK);

        new ParseNovelChapters().execute(chapterLink);
    }

    public void setChapterLinks(List<Chapter> chapterLinks) {
        adapter = new ChapterAdapter(this, chapterLinks);
        novelChaptersTextView.setAdapter(adapter);
        progress.setVisibility(View.INVISIBLE);
    }

    class ParseNovelChapters extends AsyncTask<String, Void, List<Chapter>> {
        List<Chapter> tempChapterNames = new ArrayList<>();
        String novelName = "";

        @Override
        protected List<Chapter> doInBackground(String... chapterLinks) {
            try {
                Document doc = Jsoup.connect(chapterLinks[0]).get();
                novelName = doc.select("h1[class=entry-title]").text();
                if (novelName.contains("(")) { // chinese name inside brackets ()
                    String[] nameSplit = novelName.split("[(]"); // nameSplit = { "english", "chinese"}
                    novelName = nameSplit[0].trim();
                }
                if (novelName.contains("–")) {
                    String[] nameSplit = novelName.split("[–]");
                    novelName = nameSplit[0];
                }
                Elements elements = doc.select("div[itemprop=articleBody]"); // area where links are
                Elements links = elements.select("a[href]"); // all links
                for (Element link : links) {
                    if (link.text().contains("Chapter")) { // if link text contains chapter --> add
                        tempChapterNames.add(new Chapter(link.id(), link.text(), link.attr("href")));
                    }
                }
            } catch (IOException IOE) { Log.e("ChapterActivity -IOE-", "" + IOE);}
            return tempChapterNames;
        }

        @Override
        protected void onPostExecute(List<Chapter> chapterNames) {
            super.onPreExecute();
            novelHeader.setText(novelName);
            setChapterLinks(chapterNames);
        }

    }

}
