package com.example.twitterprj;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TweetListAdaptor extends ArrayAdapter<UserTweet> {

	private ArrayList<UserTweet> tweets;

	public TweetListAdaptor(Context context, ArrayList<UserTweet> items) {
		super(context, -1, items);
		this.tweets = items;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.item, null);
		}
		UserTweet o = tweets.get(position);
		TextView tt = (TextView) v.findViewById(R.id.toptext);
		TextView bt = (TextView) v.findViewById(R.id.bottomtext);
		tt.setText(o.content);
		bt.setText(o.author);
		return v;
	}
}

class UserTweet {

	String author;
	String content;
}


class Node<T> {
	Node<T> next = null;
	Node<T> top = null;
	T data = null;
	public Node(T data) {
		this.data = data;
	}
	
	public void append(T data){
		Node<T> end = new Node<T>(data);
		if (top != null){
			top.next = end;
		}
		top = end;
	}


}