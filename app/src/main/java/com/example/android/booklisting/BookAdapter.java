package com.example.android.booklisting;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.squareup.picasso.Picasso;

public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(@NonNull Context context, ArrayList<Book> books) {
        super(context, 0, books);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list, parent, false);
        }

        Book currentBook = getItem(position);

        ImageView bookImage = listItemView.findViewById(R.id.book_image);


        Picasso.with(getContext()).load(currentBook.getBookImageResourceUrl()).into(bookImage);

        TextView bookTitle = listItemView.findViewById(R.id.book_title);

        bookTitle.setText(currentBook.getBookTitleName());

        TextView bookAuthor = listItemView.findViewById(R.id.book_author);

        bookAuthor.setText(currentBook.getBookAuthorName());

        return listItemView;
    }
}
