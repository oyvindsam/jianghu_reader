package com.example.samue.novelreader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static String APPLICATION_ID = "com.example.samue.novelreader";
    public static String FROM_MAIN = "com.example.samue.novelreader.MainActivity";
    public static String FROM_READING = "com.example.samue.novelreader.ReadingActivity";
    public static String EXTRA_NOVEL_NAME = "com.example.samue.novelreader.NOVEL_NAME";
    public static String EXTRA_NOVEL_LINK = "com.example.samue.novelreader.NOVEL_LINK";

    GridView novelLinksTextView;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress = (ProgressBar) findViewById(R.id.progress_bar_main);
        progress.setVisibility(View.VISIBLE);

        new ParseNovelsPage().execute();

    }

    public void setNovelLinks(ArrayList<Novel> novelLinks) {
        novelLinksTextView = (GridView) findViewById(R.id.novel_list);
        LinkAdapter adapter = new LinkAdapter(this, novelLinks);

        novelLinksTextView.setAdapter(adapter);
        progress.setVisibility(View.INVISIBLE);

    }


    public class ParseNovelsPage extends AsyncTask<Void, Void, Void>{
        ArrayList<Novel> tempNovelNames = new ArrayList<>();
        @Override
        protected Void doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://www.wuxiaworld.com/").get();
                Elements elements = doc.select("li[id=menu-item-2165]"); // select menu item
                Elements links = elements.select("a[href]"); // get all links i an array
                links.remove(0); // first link redundant
                for (Element link : links) {
                    if (link.text().contains("(")) { // chinese name inside brackets ()
                        String[] nameSplit = link.text().split("[(]"); // nameSlipt = { "english", "chinese"}
                        tempNovelNames.add(new Novel(nameSplit[0].trim(), link.attr("href"))); // english name, link
                    } else { // if it does not have a chinese name in header
                        tempNovelNames.add(new Novel(link.text(), link.attr("href")));
                    }
                }
            } catch (Exception e) { Log.e("main", ""+e);}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPreExecute();
            setNovelLinks(tempNovelNames);
        }}



}
