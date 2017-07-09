package com.example.samue.jianghureader.data;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.example.samue.jianghureader.model.Chapter;
import com.example.samue.jianghureader.model.Novel;
import com.example.samue.jianghureader.model.ReadingPage;
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

/*
This class hold 3 inner classes which is responsible for fetching (scraping) data from given link
with Jsoup. Alle 3 classes take in a WebParsingInterface<E> which has 2 methods, startLoading() and
finishedLoading(List<E>).
 */
/*
public class WebParse {

    private static final String LOG_ID = WebParse.class.getSimpleName();


    public WebParse() {
    }

    public static class NovelLinksLoader extends AsyncTaskLoader<List<Novel>> {

        String mLink;
        WebParsingInterface<Novel> webParsingInterface;
        List<Novel> mNovelInfo = null;

        public NovelLinksLoader(Context context) { //}, WebParsingInterface<Novel> webParsingInterface, String webAddress) {
            super(context);
            this.webParsingInterface = webParsingInterface;
            //mLink = webAddress;
        }

        @Override
        protected void onStartLoading() {
            if (mNovelInfo != null) {
                deliverResult(mNovelInfo);
            } else {
                //startLoading();
                webParsingInterface.startLoading();
                forceLoad();
            }
        }

        @Override
        public List<Novel> loadInBackground() {
            List<Novel> tempNovelNames = new ArrayList<>();

            try {
                Document doc = Jsoup.connect(mLink).get();
                Elements elements = doc.select("li[id=menu-item-2165]"); // select menu item
                Elements linkElements = elements.select("a[href]"); // get all links i an array
                linkElements.remove(0); // first link redundant

                for (Element linkElement : linkElements) {
                    if (linkElement.text().contains("(")) { // chinese name inside brackets ()
                        String[] nameSplit = linkElement.text().split("[(]"); // nameSlipt = { "english", "chinese"}
                        tempNovelNames.add(new Novel(nameSplit[0].trim(), linkElement.attr("href"))); // english name, link
                    } else { // if it does not have a chinese name in header
                        tempNovelNames.add(new Novel(linkElement.text(), linkElement.attr("href")));
                    }
                }
            } catch (IOException IOE) {
                Log.e("MainActivity -IOE- ", "" + IOE);
                return null; // pass null to onPostExecute, so calling activity can handle error loading
            }
            return tempNovelNames;
        }

        @Override
        public void deliverResult(List<Novel> data) {
            mNovelInfo = data;
            super.deliverResult(mNovelInfo);
        }
    }

    // -----------------MainActivity------------------------------------------------------------

    public void parseNovelLinks(String webAddress, WebParsingInterface<Novel> webParsingInterface) {
        new ParseNovelLinks(webParsingInterface).execute(webAddress);
    }

    private class ParseNovelLinks extends AsyncTask<String, Void, List<Novel>> {
        WebParsingInterface<Novel> webParsingInterface;

        private ParseNovelLinks(WebParsingInterface<Novel> webParsingInterface) {
            this.webParsingInterface = webParsingInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            webParsingInterface.startLoading();
        }

        @Override
        protected List<Novel> doInBackground(String... link) {
            String webAddress = link[0];
            List<Novel> tempNovelNames = new ArrayList<>();

            try {
                Document doc = Jsoup.connect(webAddress).get();
                Elements elements = doc.select("li[id=menu-item-2165]"); // select menu item
                Elements linkElements = elements.select("a[href]"); // get all links i an array
                linkElements.remove(0); // first link redundant

                for (Element linkElement : linkElements) {
                    if (linkElement.text().contains("(")) { // chinese name inside brackets ()
                        String[] nameSplit = linkElement.text().split("[(]"); // nameSlipt = { "english", "chinese"}
                        tempNovelNames.add(new Novel(nameSplit[0].trim(), linkElement.attr("href"))); // english name, link
                    } else { // if it does not have a chinese name in header
                        tempNovelNames.add(new Novel(linkElement.text(), linkElement.attr("href")));
                    }
                }
            } catch (IOException IOE) {
                Log.e("MainActivity -IOE- ", "" + IOE);
                return null; // pass null to onPostExecute, so calling activity can handle error loading
            }
            return tempNovelNames;
        }

        @Override
        protected void onPostExecute(List<Novel> novels) {
            super.onPreExecute();
            webParsingInterface.finishedLoading(novels);
        }
    }

    //---------------ChapterActivity--------------------------------------------------

    public void parseChapterLinks(String novelLink, WebParsingInterface<Chapter> webParsingInterface) {
        new ParseChapterLinks(webParsingInterface).execute(novelLink);
    }


    private class ParseChapterLinks extends AsyncTask<String, Void, List<Chapter>> {
        WebParsingInterface<Chapter> webParsingInterface;

        private ParseChapterLinks(WebParsingInterface<Chapter> webParsingInterface) {
            this.webParsingInterface = webParsingInterface;
        }

        @Override
        protected List<Chapter> doInBackground(String... link) {
            final String webAddress = link[0];
            List<Chapter> chapterList = new ArrayList<>();

            try {
                Document doc = Jsoup.connect(webAddress).get();

                Elements elements = doc.select("div[itemprop=articleBody]"); // area where links are
                Elements linkElements = elements.select("a[href]"); // all links
                for (Element linkElement : linkElements) {
                    if (linkElement.text().contains("Chapter")) { // if link text contains chapter --> add
                        chapterList.add(new Chapter(linkElement.text(), linkElement.attr("href")));
                    }
                }
            } catch (IOException IOE) {
                Log.e("ChapterActivity -IOE-", "" + IOE);
            }
            return chapterList;
        }

        @Override
        protected void onPostExecute(List<Chapter> chapterList) {
            super.onPreExecute();
            webParsingInterface.finishedLoading(chapterList);
        }

    }
    // ------------ReadingActivity-----------------------------------------------------

    public void parseReadingPage(String chapterLink, WebParsingInterface<ReadingPage> webParsingInterface) {
        new ParseReadingPage(webParsingInterface).execute(chapterLink);
    }

    private class ParseReadingPage extends AsyncTask<String, Void, List<ReadingPage>> {
        WebParsingInterface<ReadingPage> webParsingInterface;

        private ParseReadingPage(WebParsingInterface<ReadingPage> webParsingInterface) {
            this.webParsingInterface = webParsingInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            webParsingInterface.startLoading();
        }

        @Override
        protected List<ReadingPage> doInBackground(String... params) {
            List<ReadingPage> novelInfo = new ArrayList<>();
            String prevLink = "";
            String nextLink = "";
            String chapterHeader = "";
            String chapterLink;
            String mainText = "";
            ReadingPage readingPage = new ReadingPage();
            chapterLink = params[0];
            Log.v(LOG_ID, chapterLink);
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
                Log.e("ReadingActivity -IOE-", "" + IOE);
                return null; // pass null to onPostExecute, so calling activity can handle error loading
            }
            return novelInfo;
        }

        @Override
        protected void onPostExecute(List<ReadingPage> result) {
            super.onPreExecute();
            webParsingInterface.finishedLoading(result);
        }
    }
}
*/



