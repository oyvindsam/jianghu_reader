package com.example.samue.novelreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.versionName;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static org.jsoup.nodes.Document.OutputSettings.Syntax.html;


public class MainActivity extends AppCompatActivity {

    public static String EXTRA_LINK = "com.example.samue.novelreader.LINK";
    public static String EXTRA_NOVEL_NAME = "com.example.samue.novelreader.NOVEL_NAME";

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
                Elements elements = doc.select("li[id=menu-item-2165]");
                Elements links = elements.select("a[href]");
                links.remove(0);
                for (Element link : links) {
                    if (link.text().contains("(")) {
                        String[] nameSplit = link.text().split("[(]");
                        tempNovelNames.add(new Novel(nameSplit[0].trim(), link.attr("href")));
                    } else {
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
