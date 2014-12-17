package com.altujen.android.odb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.altujen.android.odb.EnumLang;

public class MultiLangUtil {
	
	private static final String strDboxHttp_zhTW = "https://dl.dropboxusercontent.com/u/39423116/odbDataSrc/zhTW/";
	private static final String strDboxHttp_zhCN = "https://dl.dropboxusercontent.com/u/39423116/odbDataSrc/zhCN/";
	private static final String strDboxHttp_EN = "https://dl.dropboxusercontent.com/u/39423116/odbDataSrc/EN/";
	private static final String strDboxHttp_JP = "https://dl.dropboxusercontent.com/u/39423116/odbDataSrc/JP/";
	
	private static final DateFormat dateFormat_zhTW = new SimpleDateFormat("yyyy年 MM月 dd日", Locale.TRADITIONAL_CHINESE);
	private static final DateFormat dateFormat_zhCN = new SimpleDateFormat("yyyy年 MM月 dd日", Locale.SIMPLIFIED_CHINESE);
	// "EEE, MMM d, ''yy"	Wed, Jul 4, '01
	private static final DateFormat dateFormat_EN = new SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH);
	private static final DateFormat dateFormat_JP = new SimpleDateFormat("yyyy年 MM月 dd日", Locale.JAPANESE);
	
	
	public static String getDropboxAddrByLang(int lang) {
		
		switch(lang) {
		case EnumLang.zh_TW:
			return strDboxHttp_zhTW;
			
		case EnumLang.zh_CN:
			return strDboxHttp_zhCN;
			
		case EnumLang.EN:
			return strDboxHttp_EN;
			
		case EnumLang.JP:
			return strDboxHttp_JP;
			
		default:
			return null;
		}
		
	}
	
	public static int getLangCodeByLang(int lang) {
		
		switch(lang) {
		case EnumLang.zh_TW:
			return EnumLang.zh_TW;
			
		case EnumLang.zh_CN:
			return EnumLang.zh_CN;
			
		case EnumLang.EN:
			return EnumLang.EN;
			
		case EnumLang.JP:
			return EnumLang.JP;
			
		default:
			return lang;
		}
		
	}
	
	public static int getLangCodeByCountry(String Country) {
		
		if(Country.equals("TW")) {
			return EnumLang.zh_TW;
		} else if(Country.equals("CN")) {
			return EnumLang.zh_CN;
		} else if(Country.equals("JP")) {
			return EnumLang.JP;
		} else {
			return EnumLang.EN;
		}
		
	}
	
	public static DateFormat getDateFormatByLang(int lang) {
		
		switch(lang) {
		case EnumLang.zh_TW:
			return dateFormat_zhTW;
			
		case EnumLang.zh_CN:
			return dateFormat_zhCN;
			
		case EnumLang.EN:
			return dateFormat_EN;
			
		case EnumLang.JP:
			return dateFormat_JP;
			
		default:
			return dateFormat_EN;
		}
		
	}
	
	public static int getShareEmailLengthByLang(int lang) {
		
		switch(lang) {
		case EnumLang.zh_TW:
			return 129;
			
		case EnumLang.zh_CN:
			return 129;
			
		case EnumLang.EN:
			return 349;
			
		case EnumLang.JP:
			return 219;
			
		default:
			return 129;
		}
		
	}
	
	public static int getShareMsgLengthByLang(int lang) {
		
		switch(lang) {
		case EnumLang.zh_TW:
			return 49;
			
		case EnumLang.zh_CN:
			return 49;
			
		case EnumLang.EN:
			return 129;
			
		case EnumLang.JP:
			return 89;
			
		default:
			return 49;
		}
		
	}
	
}
