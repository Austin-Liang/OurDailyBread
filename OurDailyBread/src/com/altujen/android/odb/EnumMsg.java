package com.altujen.android.odb;

public class EnumMsg {
	
	public static final String DEVICE_MODEL = android.os.Build.MODEL;
	
	public static final int DB_INITIALIZE_TASK_INTERRUPTED = -1;
	public static final int DB_NEWINITIALIZE_TASK_INTERRUPTED = -2;
	public static final int DB_GETDATA_TASK_INTERRUPTED = -3;
	public static final int DB_CHKDATA_MANUAL_TASK_INTERRUPTED = -4;
	
	public static final int STOP_SIMULATE_THREAD = 0;
	public static final int STOP_SIMULATE_THREAD_ONPOSTCREATE = 1;
	public static final int DB_INITIALIZE_TASK_OK = 2;
	public static final int DB_INITIALIZE_TASK_NO_DATA = 3;
	public static final int DB_NEWINITIALIZE_TASK_OK = 4;
	public static final int DB_NEWINITIALIZE_TASK_NO_DATA = 5;
	public static final int DB_GETDATA_NO_DATA = 6;
	public static final int DB_GETDATA_UPDATED = 7;
	public static final int DB_GETDATA_ALREADY_HAS_DATA = 8;
	public static final int DB_CHKDATA_MANUAL_OK = 9;
	public static final int DB_CHKDATA_MANUAL_NO_DATA = 10;

}
