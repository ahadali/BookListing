package com.example.android.booklisting;

public class Book {

    private String mBookImageResourceUrl;

    private String mBookTitleName;

    private String mBookAuthorsName;

    private String mBookInfoLink;

    public String getBookImageResourceUrl() {
        return mBookImageResourceUrl;
    }

    public String getBookTitleName() {
        return mBookTitleName;
    }

    public String getBookAuthorName() {
        return mBookAuthorsName;
    }

    public String getBookInfoLink(){
        return mBookInfoLink;
}

    public Book(String bookImageResourceUrl, String bookTitleName, String bookAuthorsName, String bookInfoLink){
        mBookImageResourceUrl = bookImageResourceUrl;
        mBookTitleName = bookTitleName;
        mBookAuthorsName = bookAuthorsName;
        mBookInfoLink = bookInfoLink;
    }


}
