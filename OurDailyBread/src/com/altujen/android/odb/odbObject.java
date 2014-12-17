package com.altujen.android.odb;

import org.json.simple.JSONObject;

import android.database.Cursor;

public class odbObject {

	private String ID = null;
	private String Order_WT = null;
	private String ODB_Uri = null;
	private String Date = null;
	private String Title = null;
	private String Author = null;
	private String Author_uri = null;
	private String AuthorImg_uri = null;
	private String Mp3_uri = null;
	private String Rd_title = null;
	private String Rd_uri = null;
	private String Rd_text = null;
	private String AnnRd_title = null;
	private String AnnRd_uri = null;
	private String Story = null;
	private String Poem = null;
	private String Thought = null;
	private String Language = null;
	
	private JSONObject odbJson = new JSONObject();
	
	public odbObject(Cursor cursor) {
		super();
		
		if (cursor != null) {
			this.ID = cursor.getString(0);
			this.Order_WT = cursor.getString(1);
			this.ODB_Uri = cursor.getString(2);
			this.Date = cursor.getString(3);
			this.Title = cursor.getString(4);
			this.Author = cursor.getString(5);
			this.Author_uri = cursor.getString(6);
			this.AuthorImg_uri = cursor.getString(7);
			this.Mp3_uri = cursor.getString(8);
			this.Rd_title = cursor.getString(9);
			this.Rd_uri = cursor.getString(10);
			this.Rd_text = cursor.getString(11);
			this.AnnRd_title = cursor.getString(12);
			this.AnnRd_uri = cursor.getString(13);
			this.Story = cursor.getString(14);
			this.Poem = cursor.getString(15);
			this.Thought = cursor.getString(16);
			this.Language = cursor.getString(17);
		}
		
	}
	
	public odbObject(String strOrder_WT, String strODB_Uri, String strDate, String strTitle,
			String strAuthor, String strAuthor_uri, String strAuthorImg_uri, String strMp3_uri,
			String strRd_title, String strRd_uri, String strRd_text,
			String strAnnRd_title, String strAnnRd_uri, String strStory,
			String strPoem, String strThought, String strLanguage) {
		
		super();
		
		this.Order_WT = strOrder_WT;
		this.ODB_Uri = strODB_Uri;
		this.Date = strDate;
		this.Title = strTitle;
		this.Author = strAuthor;
		this.Author_uri = strAuthor_uri;
		this.AuthorImg_uri = strAuthorImg_uri;
		this.Mp3_uri = strMp3_uri;
		this.Rd_title = strRd_title;
		this.Rd_uri = strRd_uri;
		this.Rd_text = strRd_text;
		this.AnnRd_title = strAnnRd_title;
		this.AnnRd_uri = strAnnRd_uri;
		this.Story = strStory;
		this.Poem = strPoem;
		this.Thought = strThought;
		this.Language = strLanguage;
	}
	
	public String getID() {
		return ID;
	}
	public void setID(String strID) {
		this.ID = strID;
	}
	public String getOrder_WT() {
		return Order_WT;
	}
	public void setOrder_WT(String strOrder_WT) {
		this.Order_WT = strOrder_WT;
	}
	public String getODB_Uri() {
		return ODB_Uri;
	}
	public void setODB_Uri(String strODB_Uri) {
		this.ODB_Uri = strODB_Uri;
	}
	public String getDate() {
		return Date;
	}
	public void setDate(String strDate) {
		this.Date = strDate;
	}
	public String getTitle() {
		return Title;
	}
	public void setTitle(String strTitle) {
		this.Title = strTitle;
	}
	public String getAuthor() {
		return Author;
	}
	public void setAuthor(String strAuthor) {
		this.Author = strAuthor;
	}
	public String getAuthor_uri() {
		return Author_uri;
	}
	public void setAuthor_uri(String strAuthor_uri) {
		this.Author_uri = strAuthor_uri;
	}
	public String getAuthorImg_uri() {
		return AuthorImg_uri;
	}
	public void setAuthorImg_uri(String strAuthorImg_uri) {
		this.AuthorImg_uri = strAuthorImg_uri;
	}
	public String getMp3_uri() {
		return Mp3_uri;
	}
	public void setMp3_uri(String strMp3_uri) {
		this.Mp3_uri = strMp3_uri;
	}
	public String getRd_title() {
		return Rd_title;
	}
	public void setRd_title(String strRd_title) {
		this.Rd_title = strRd_title;
	}
	public String getRd_uri() {
		return Rd_uri;
	}
	public void setRd_uri(String strRd_uri) {
		this.Rd_uri = strRd_uri;
	}
	public String getRd_text() {
		return Rd_text;
	}
	public void setRd_text(String strRd_text) {
		this.Rd_text = strRd_text;
	}
	public String getAnnRd_title() {
		return AnnRd_title;
	}
	public void setAnnRd_title(String strAnnRd_title) {
		this.AnnRd_title = strAnnRd_title;
	}
	public String getAnnRd_uri() {
		return AnnRd_uri;
	}
	public void setAnnRd_uri(String strAnnRd_uri) {
		this.AnnRd_uri = strAnnRd_uri;
	}
	public String getStory() {
		return Story;
	}
	public void setStory(String strStory) {
		this.Story = strStory;
	}
	public String getPoem() {
		return Poem;
	}
	public void setPoem(String strPoem) {
		this.Poem = strPoem;
	}
	public String getThought() {
		return Thought;
	}
	public void setThought(String strThought) {
		this.Thought = strThought;
	}
	
	public String getLanguage() {
		return Language;
	}

	public void setLanguage(String language) {
		this.Language = language;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getJSONObject(){
		
		odbJson.put("Order_WT", this.Order_WT);
		odbJson.put("ODB_Uri", this.ODB_Uri);
		odbJson.put("Date", this.Date);
		odbJson.put("Title", this.Title);
		odbJson.put("Author", this.Author);
		odbJson.put("Author_uri", this.Author_uri);
		odbJson.put("AuthorImg_uri", this.AuthorImg_uri);
		odbJson.put("Mp3_uri", this.Mp3_uri);
		odbJson.put("Rd_title", this.Rd_title);
		odbJson.put("Rd_uri", this.Rd_uri);
		odbJson.put("Rd_text", this.Rd_text);
		odbJson.put("AnnRd_title", this.AnnRd_title);
		odbJson.put("AnnRd_uri", this.AnnRd_uri);
		odbJson.put("Story", this.Story);
		odbJson.put("Poem", this.Poem);
		odbJson.put("Thought", this.Thought);
		odbJson.put("Language", this.Language);
		
		return odbJson;
	}
	
}
