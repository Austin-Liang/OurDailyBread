package com.altujen.android.odb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.os.Handler;

import com.google.gson.Gson;

public class DatabaseInitializer {
	
	//private DatabaseHandler db;
	private final Runnable db_initialize_task;
	private final Runnable db_chkdata_manual;
	private Runnable db_getdata_task;
	private Runnable db_newinitialize_task;
	
	private final DateFormat df_Default = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
	private final DateFormat df_YearMonth = new SimpleDateFormat("yyyyMM", Locale.getDefault());
	
	public DatabaseInitializer(final DatabaseHandler db, final Handler mUI_Handler, final int lang) {
		
		//this.db = db;
		
		this.db_initialize_task = new Runnable() {
			
			private final String strDboxHttpAddr = MultiLangUtil.getDropboxAddrByLang(lang);

			@Override
			public void run() {
				
				try {
					List<String> lstToUpdate = new ArrayList<String>();
					Calendar latest_cal = null;
					
					{   // Prepare lstToUpdate List.
						if (db.getOdbsCount(lang) > 0) {
							// has old data.
							// check 

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available data month.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}
							
							latest_cal = (Calendar) cal.clone();

							for (int i = 1; i <= 3; i++) {

								Date startDate;
								Date endDate;

								if (i == 1) {

									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								} else {

									cal.add(Calendar.MONTH, -1);
									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								}

							}

						} else {
							// no data at all.

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available monthly data.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}
							
							latest_cal = (Calendar) cal.clone();

							for (int i = 1; i <= 3; i++) {
								lstToUpdate.add(df_YearMonth.format(cal.getTime()));
								cal.add(Calendar.MONTH, -1);
							}

						}
					}
					
					
					{ // Pull data from http uri.
						if(lstToUpdate.size() > 0) {
							for (String strDataMonth : lstToUpdate) {
								// in the future, we should use EnumLanguage.lang to
								// tell which http address should be access.
								String strJson = Util.getHttpContext(strDboxHttpAddr
										+ "odb_" + strDataMonth + ".json");
								
								if(strJson == null || strJson.equals("")) {
									throw new Exception("Http Error!");
								}

								odbObject[] lst_ODB;
								Gson gson = new Gson();
								lst_ODB = gson.fromJson(strJson, odbObject[].class);

								for (odbObject odb : lst_ODB) {
									db.addOdb(odb);
								}
							}
							// Initialization - ok.
							mUI_Handler.obtainMessage(EnumMsg.DB_INITIALIZE_TASK_OK, latest_cal).sendToTarget();
						} else {
							// Initialization - no data.
							mUI_Handler.obtainMessage(EnumMsg.DB_INITIALIZE_TASK_NO_DATA, latest_cal).sendToTarget();
						}
					}
				} catch (Exception e) {
					// Task interrupted.
					mUI_Handler.sendEmptyMessage(EnumMsg.DB_INITIALIZE_TASK_INTERRUPTED);
				}
				
			}
			
		};
		
		this.db_chkdata_manual = new Runnable() {
			
			private final String strDboxHttpAddr = MultiLangUtil.getDropboxAddrByLang(lang);
			
			@Override
			public void run() {
				
				try {
					List<String> lstToUpdate = new ArrayList<String>();
					
					{   // Prepare lstToUpdate List.
						if (db.getOdbsCount(lang) > 0) {
							// has old data.
							// check 

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available data month.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}

							for (int i = 1; i <= 3; i++) {

								Date startDate;
								Date endDate;

								if (i == 1) {

									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								} else {

									cal.add(Calendar.MONTH, -1);
									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								}

							}

						} else {
							// no data at all.

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available monthly data.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}

							for (int i = 1; i <= 3; i++) {
								lstToUpdate.add(df_YearMonth.format(cal.getTime()));
								cal.add(Calendar.MONTH, -1);
							}

						}
					}
					
					
					{ // Pull data from http uri.
						if(lstToUpdate.size() > 0) {
							for (String strDataMonth : lstToUpdate) {
								// in the future, we should use EnumLanguage.lang to
								// tell which http address should be access.
								String strJson = Util.getHttpContext(strDboxHttpAddr
										+ "odb_" + strDataMonth + ".json");
								
								if(strJson == null || strJson.equals("")) {
									throw new Exception("Http Error!");
								}

								odbObject[] lst_ODB;
								Gson gson = new Gson();
								lst_ODB = gson.fromJson(strJson, odbObject[].class);

								for (odbObject odb : lst_ODB) {
									db.addOdb(odb);
								}
							}
							// manual check - ok.
							mUI_Handler.sendEmptyMessage(EnumMsg.DB_CHKDATA_MANUAL_OK);
						} else {
							// manual check - no more data.
							mUI_Handler.sendEmptyMessage(EnumMsg.DB_CHKDATA_MANUAL_NO_DATA);
						}
					}
				} catch (Exception e) {
					// Task interrupted.
					mUI_Handler.sendEmptyMessage(EnumMsg.DB_CHKDATA_MANUAL_TASK_INTERRUPTED);
				}
				
			}
			
		};
		
	}
	
	public Runnable getInitialDataRunnable() {
		
		return this.db_initialize_task;
	}
	
	public Runnable getChkDataManualRunnable() {
		
		return this.db_chkdata_manual;
	}
	
	public Runnable getGetDataRunnable(final DatabaseHandler db, final Calendar cal, final Handler mUI_Handler, final int lang) {
		
		this.db_getdata_task = new Runnable() {
			
			private final String strDboxHttpAddr = MultiLangUtil.getDropboxAddrByLang(lang);
			
			@Override
			public void run() {
				
				try {
					// 0 = default, 1 = 
					int processStatus;
					
					{   // do something...
						
						Date startDate;
						Date endDate;
						
						startDate = Util.getFirstDay(cal);
						endDate = Util.getLastDay(cal);
						
						// first, check whether current db already has data or not. 
						if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
							// don't have the data that user requested.
							// next, check the data src from http exists or not.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							
							if(!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								
								// don't have data from http too.
								processStatus = EnumMsg.DB_GETDATA_NO_DATA;
								
							} else {
								// http data src do exists.
								// update data to current db.
								// in the future, we should use EnumLanguage.lang to tell which http address should be access.
								
								String strJson = Util.getHttpContext(strDboxHttpAddr + "odb_" + df_YearMonth.format(cal.getTime()) + ".json");
								
								if(strJson == null || strJson.equals("")) {
									throw new Exception("Http Error!");
								}
								
								odbObject[] lst_ODB;
								Gson gson = new Gson();
								lst_ODB = gson.fromJson(strJson, odbObject[].class);
								
								for(odbObject odb : lst_ODB) {
									db.addOdb(odb);
								}
								
								// update data.
								processStatus = EnumMsg.DB_GETDATA_UPDATED;
								
							}
							
						} else {
							// already have data.
							processStatus = EnumMsg.DB_GETDATA_ALREADY_HAS_DATA;
						}
						
					}
					
					
					{   // Done. Notify UI Thread Handler to do his work.
						switch(processStatus) {
						case EnumMsg.DB_GETDATA_NO_DATA:
							// don't have data from http too.
							mUI_Handler.sendEmptyMessage(EnumMsg.DB_GETDATA_NO_DATA);
							break;
						case EnumMsg.DB_GETDATA_UPDATED:
							// update data to current db.
							mUI_Handler.obtainMessage(EnumMsg.DB_GETDATA_UPDATED, cal).sendToTarget();
							break;
						case EnumMsg.DB_GETDATA_ALREADY_HAS_DATA:
							// already have data.
							mUI_Handler.obtainMessage(EnumMsg.DB_GETDATA_ALREADY_HAS_DATA, cal).sendToTarget();
							break;
						}
					}
				} catch (Exception e) {
					// Task interrupted.
					mUI_Handler.sendEmptyMessage(EnumMsg.DB_GETDATA_TASK_INTERRUPTED);
				}
				
			}
			
		};
		
		return this.db_getdata_task;
	}
	
	public Runnable getNewInitializeRunnable(final DatabaseHandler db, final Handler mUI_Handler, final int lang) {
		
		this.db_newinitialize_task = new Runnable() {
			
			private final String strDboxHttpAddr = MultiLangUtil.getDropboxAddrByLang(lang);

			@Override
			public void run() {
				
				try {
					List<String> lstToUpdate = new ArrayList<String>();
					Calendar latest_cal = null;
					
					{   // Prepare lstToUpdate List.
						if (db.getOdbsCount(lang) > 0) {
							// has old data.
							// check 

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available data month.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}
							
							latest_cal = (Calendar) cal.clone();

							for (int i = 1; i <= 3; i++) {

								Date startDate;
								Date endDate;

								if (i == 1) {

									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								} else {

									cal.add(Calendar.MONTH, -1);
									startDate = Util.getFirstDay(cal);
									endDate = Util.getLastDay(cal);

									if (!(db.getOdbsCount(lang, df_Default.format(startDate), df_Default.format(endDate)) > 0)) {
										lstToUpdate.add(df_YearMonth.format(startDate));
									}

								}

							}

						} else {
							// no data at all.

							Calendar cal = new GregorianCalendar();
							Date date = new Date();
							cal.setTime(date);

							// get the closest available monthly data.
							// in the future, we should use EnumLanguage.lang to tell which http address should be access.
							while (!(Util.chkUriExists(strDboxHttpAddr + "odb_"
									+ df_YearMonth.format(cal.getTime()) + ".json"))) {
								cal.add(Calendar.MONTH, -1);
							}
							
							latest_cal = (Calendar) cal.clone();

							for (int i = 1; i <= 3; i++) {
								lstToUpdate.add(df_YearMonth.format(cal.getTime()));
								cal.add(Calendar.MONTH, -1);
							}

						}
					}
					
					
					{ // Pull data from http uri.
						if(lstToUpdate.size() > 0) {
							for (String strDataMonth : lstToUpdate) {
								// in the future, we should use EnumLanguage.lang to
								// tell which http address should be access.
								String strJson = Util.getHttpContext(strDboxHttpAddr
										+ "odb_" + strDataMonth + ".json");
								
								if(strJson == null || strJson.equals("")) {
									throw new Exception("Http Error!");
								}

								odbObject[] lst_ODB;
								Gson gson = new Gson();
								lst_ODB = gson.fromJson(strJson, odbObject[].class);

								for (odbObject odb : lst_ODB) {
									db.addOdb(odb);
								}
							}
							// Initialization - ok.
							mUI_Handler.obtainMessage(EnumMsg.DB_NEWINITIALIZE_TASK_OK, latest_cal).sendToTarget();
						} else {
							// Initialization - no data.
							mUI_Handler.obtainMessage(EnumMsg.DB_NEWINITIALIZE_TASK_NO_DATA, latest_cal).sendToTarget();
						}
					}
				} catch (Exception e) {
					// Task interrupted.
					mUI_Handler.sendEmptyMessage(EnumMsg.DB_NEWINITIALIZE_TASK_INTERRUPTED);
				}
				
			}
			
		};
		
		return this.db_newinitialize_task;
	}
	
}
