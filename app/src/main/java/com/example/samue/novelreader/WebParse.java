package com.example.samue.novelreader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by samue on 10.04.2017.
 */

public class WebParse {

    public WebParse() {}


    // -----------------MainActivity------------------------------------------------------------

    public void parseNovelLinks(String webAddress, Context context, ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new ParseNovelsPage().execute(webAddress, context);
    }

    private class ParseNovelsPage extends AsyncTask<Object, Void, List<Novel>> {
        List<Novel> tempNovelNames = new ArrayList<>();
        String webAddress;
        MainActivity context;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Novel> doInBackground(Object... params) {
            webAddress = (String) params[0];
            context = (MainActivity) params[1];
            try {
                Document doc = Jsoup.connect(webAddress).get();
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
            } catch (IOException IOE) {
                Log.e("MainActivity -IOE- ", "" + IOE);
            }
            return tempNovelNames;
        }

        @Override
        protected void onPostExecute(List<Novel> novelNames) {
            super.onPreExecute();
            context.setNovelLinks(novelNames);
        }
    }

    //---------------ChapterActivity--------------------------------------------------

    public void parseChapterLinks(String novelLink, ChapterActivity context, ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new ParseNovelChapters().execute(novelLink, context);
    }

    private class ParseNovelChapters extends AsyncTask<Object, Void, List<Chapter>> {
        List<Chapter> tempChapterNames = new ArrayList<>();
        String novelName, novelLink;
        ChapterActivity context;

        @Override
        protected List<Chapter> doInBackground(Object... params) {
            novelLink = (String) params[0];
            context = (ChapterActivity) params[1];

            try {
                Document doc = Jsoup.connect(novelLink).get();
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
            context.setChapterLinks(chapterNames);
        }

    }
 // ------------ReadingActivity-----------------------------------------------------

    public void parseChapterText(String chapterLink, ReadingActivity context, ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new ParseReadingPage().execute(chapterLink, context);
    }

    private class ParseReadingPage extends AsyncTask<Object, Void, List<String>> {
        String chapterLink;
        ReadingActivity context;
        List<String> novelInfo = new ArrayList<>();
        String htmlParse = "";
        String prevLink = "";
        String nextLink = "";
        String novelHeader = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<String> doInBackground(Object... params) {
            chapterLink = (String) params[0];
            context = (ReadingActivity) params[1];
            try {
                Document doc = Jsoup.connect(chapterLink).get();
                novelHeader = doc.select("h1[class=entry-title]").text();
                Elements elements = doc.select("div[itemprop=articleBody]");
                Elements links = doc.select("a[href]");
                Elements paragraphElements = elements.select("p");
                // Add cancel method and mor try catch.
                for (Element link : links) {
                    if (link.text().equals("Previous Chapter") && prevLink.length() < 1) {
                        prevLink += link.attr("href");
                    }
                    else if (link.text().equals("Next Chapter") && nextLink.length() < 1) {
                        nextLink += link.attr("href");
                    }
                }
                for (Element p : paragraphElements) {
                    if (!p.text().contains("Previous Chapter") && !p.text().contains("Next Chapter")) {
                        Log.v("Text: ", p.text());
                        htmlParse += p.text().trim() + "\n\n";
                    }
                }
                novelInfo.add(novelHeader);
                novelInfo.add(prevLink);
                novelInfo.add(nextLink);
                novelInfo.add(htmlParse);

            } catch (IOException IOE) { Log.e("ReadingActivity -IOE-", "" + IOE);}
            return novelInfo;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPreExecute();
            context.setNovelText(result);
        }
    }
}
