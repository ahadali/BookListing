package com.example.android.booklisting;


import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class BookQuery {

    private static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Create a private constructor because no one should ever create a {@link BookQuery} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name BookQuery (and an object instance of BookQuery is not needed).
     */
    private BookQuery() {

    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Book> extractFeatureFromJson(String bookJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(bookJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(bookJSON);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of items (or books).
            JSONArray itemsArray = baseJsonResponse.getJSONArray("items");

            // For each book in the itemsArray, create an {@link Book} object
            for (int i = 0; i < itemsArray.length(); i++) {

                // Get a single book at position i within the list of books
                JSONObject currentBook = itemsArray.getJSONObject(i);

                // For a given book, extract the JSONObject associated with the
                // key called "volumeInfo", which represents a list of all properties like author,title,image..etc
                // for that book.
                JSONObject properties = currentBook.getJSONObject("volumeInfo");

                // Extract the value for the key called "title"
                String title = properties.getString("title");

                // Extract the value for the key called "author"
                String author;

                // Check if JSONArray exist
                if (properties.has("authors")) {
                    // Extract the value for the key(JSONArray) called "authors"
                    JSONArray authors = properties.getJSONArray("authors");
                    Log.println(Log.INFO, LOG_TAG, String.valueOf(authors));

                    // Check JSONArray Returns true if it is null
                    if (!properties.isNull("authors")) {
                        // Get 1st element
                        author = (String) authors.get(0);
                    } else {
                        // assign info about missing info about author
                        author = "unknown author";
                    }
                } else {
                    // assign info about missing info about author
                    author = "missing info of authors";
                }

                // Extract the value for the key(JSONObject) called "imageLinks"
                JSONObject image = properties.getJSONObject("imageLinks");

                // Extract String URL of specific cover
                String coverImageUrl = image.getString("smallThumbnail");

                // Extract the value for the key called "smallThumbnail"
                // Using REGEX and StringBuilder
                StringBuilder stringBuilder = new StringBuilder();

                Pattern p = Pattern.compile("id=(.*?)&");
                Matcher m = p.matcher(coverImageUrl);
                if (m.matches()) {
                    String id = m.group(1);
                    coverImageUrl = String.valueOf(stringBuilder.append("https://books.google.com/books/content/images/frontcover/").append(id).append("?fife=w300"));
                } else {
                    Log.i(LOG_TAG, "Issue with cover");
                }

                // Extract the value for the key called "infoLink"
                String infoLink;
                if (properties.has("infoLink")) {
                    infoLink = properties.getString("infoLink");
                } else {
                    infoLink = "No info. available";
                }
                // Create a new {@link Book} object with the title, authors, image, infoLink from the JSON response.
                Book bookItem = new Book(coverImageUrl, title, author, infoLink);

                // Add the new {@link Books} to the list of books.
                books.add(bookItem);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }

        // Return the list of books
        return books;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Query the Google Books Api dataset and return a list of {@link Book} objects.
     */
    static List<Book> fetchBookData(String requestUrl) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> bookLists = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Book}s
        return bookLists;
    }
}
