package com.example.onlinetrivia;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Links {

    private Context context;

    // Regular expression to detect URLs in a string
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[\\w.-]+(?:\\.[\\w\\.-]+)+)(?:[\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?"
    );


    public Links(Context context) {
        this.context = context;
    }

    public String detectUrls(String text) {
        Matcher matcher = URL_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String url = matcher.group();
            // Here, you can replace the URL with a clickable span or any other representation.
            // For simplicity, let's just keep the URL as is.
            matcher.appendReplacement(sb, url);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    


    public void generatePreview(String url, LinkPreviewCallback callback) {
        new FetchLinkPreviewTask(callback).execute(url);
    }

    public void handleUrlClick(String url) {
        if (url != null && !url.isEmpty()) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        }
    }

    private class FetchLinkPreviewTask extends AsyncTask<String, Void, LinkPreview> {

        private LinkPreviewCallback callback;

        FetchLinkPreviewTask(LinkPreviewCallback callback) {
            this.callback = callback;
        }

        @Override
        protected LinkPreview doInBackground(String... urls) {
            String url = urls[0];
            try {
                Document doc = Jsoup.connect(url).get();
                String title = doc.title();
                String description = doc.select("meta[name=description]").attr("content");
                String imageUrl = doc.select("meta[property=og:image]").attr("content");
                return new LinkPreview(url, title, description, imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkPreview linkPreview) {
            if (callback != null) {
                callback.onLinkPreviewGenerated(linkPreview);
            }
        }
    }

    public interface LinkPreviewCallback {
        void onLinkPreviewGenerated(LinkPreview linkPreview);
    }

    public static class LinkPreview {
        private String url;
        private String title;
        private String description;
        private String imageUrl;

        LinkPreview(String url, String title, String description, String imageUrl) {
            this.url = url;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
        }


    }
}
