package dk.aarhuskommune.odaa;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DKAarhusKommuneODAA {
	//static String _FORNAME="com.mysql.jdbc.Driver";
	static String _FORNAME="org.postgresql.Driver";

	static int _OFFSET = 69000;
	static int _STRLENGTH=2000;
	static String _LOGFILEPATH = "";
	static String _LOGFILENAME = "PossiblyCPR.log";
		
	static String _EMAIL_TO = "***,***";
	static String _EMAIL_HOST = "localhost";
	static String message="";
				
	public static void main(String[] args) {		
		//ReadArguments ra=new ReadArguments(args);
		//ReadPropertyFiles rpf=new ReadPropertyFiles(ra.getParameter("-ini"));
		//System.out.println("path=" + rpf.getPath() + " filename=" + rpf.getFilename() +  " version=" + rpf.getParameter("version"));
		
		ReadFromPropertyFile(args);		
		
		ExecutionTimer executionTimer=new ExecutionTimer();
		executionTimer.start();
		searchForCPR_step1();
		searchForCPR_step2();
		executionTimer.end();		
		sendMail();
		
		writeToLog("Search for CPR done in", executionTimer.duration() + " ms.");		
	} //main
	
	public static void ReadFromPropertyFile(String[] args) {
		ReadArguments ra=new ReadArguments(args);		
		ReadPropertyFiles rpf=new ReadPropertyFiles(ra.getParameter("-ini"));
		
		_FORNAME=rpf.getParameter("FORNAME");
					
		_CONNECTIONSTRING = rpf.getParameter("CONNECTIONSTRING");
		_USER = rpf.getParameter("USER");
		_PASSWORD = rpf.getParameter("PASSWORD");
		_EMAIL_FROM =rpf.getParameter("EMAIL_FROM");
												
		_OFFSET = Integer.parseInt(rpf.getParameter("OFFSET"));
		_STRLENGTH=Integer.parseInt(rpf.getParameter("STRLENGTH"));
		_LOGFILEPATH = rpf.getParameter("LOGFILEPATH");
		_LOGFILENAME = rpf.getParameter("LOGFILENAME");
				
		_EMAIL_TO = rpf.getParameter("EMAIL_TO");
		_EMAIL_HOST = rpf.getParameter("EMAIL_HOST");
	}
		
	public static void searchForCPR_step1() {
		//String _TABLESTOSCANSQL="select relname from \"pg_statio_user_tables\";";
		String _TABLESTOSCANSQL="SELECT * from (SELECT relname FROM pg_statio_all_tables where " + 
							    "pg_statio_all_tables.relname not in (SELECT table_name " + 
							    "FROM cprScanned_table) and schemaname='public') AS result " + 
							    "where relname!='cprscanned_table' and relname!='modifybyckanapi';";
		
		Boolean noCPR;
		
		JDBCSQLAccess jdbcSqlAccess=new JDBCSQLAccess(_FORNAME,_CONNECTIONSTRING,_USER,_PASSWORD);		
		jdbcSqlAccess.connect();
		ResultSet tables=jdbcSqlAccess.executeQuery(_TABLESTOSCANSQL);
		ScanTable st=new ScanTable();
				
		try {
			while (tables.next()) {
				noCPR=true;
				String str="";
				String table_name=tables.getObject("relname").toString();
				int offset=0;
				JDBCSQLAccess datastoreSQLaccess=new JDBCSQLAccess(_FORNAME,_CONNECTIONSTRING,_USER,_PASSWORD);		
				datastoreSQLaccess.connect();
				do {									
					String sql=st.generateSQL(table_name,_OFFSET,offset);
					ResultSet datastore=datastoreSQLaccess.executeQuery(sql);					
					str="";
					String strToScan="";
					while (datastore.next()) {		        	
						str="\n_id=" + datastore.getObject("_id") + ", record=";
						int total_col = datastore.getMetaData().getColumnCount();
						for (int i = 0; i < total_col; i++) {
							String columnLabel=datastore.getMetaData().getColumnLabel(i+1);							
							if ((columnLabel.equals("_full_text")==false) && (columnLabel.equals("_id")==false)) {
								str=str+datastore.getObject(columnLabel) + ",";	
							}
						}
						strToScan=strToScan+str + "\n";
						if (strToScan.length()>_STRLENGTH) {
							String isPossiblyCPR=isPossiblyCPR(strToScan);
							if (isPossiblyCPR.equals("")==false) {								
								writeToLog(table_name,isPossiblyCPR);																							
								noCPR=false;																
							}
							strToScan="";
						}						
					}									
					offset=offset+_OFFSET;
				} while(str.equals("")==false);
				datastoreSQLaccess.close();
				if (noCPR==true) {
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");								
					Date date = new Date();
					jdbcSqlAccess.executeUpdateSQL("INSERT INTO cprScanned_table (table_name, dateTime) VALUES ('" + table_name + "','" + dateFormat.format(date) + "');");
					jdbcSqlAccess.executeUpdateSQL("DROP TRIGGER IF EXISTS \"onUpdateAfter\" ON \"public\".\"" + table_name + "\";CREATE TRIGGER \"onUpdateAfter\" AFTER UPDATE OR INSERT ON \"public\".\"" + table_name + "\" for EACH ROW EXECUTE PROCEDURE dlFrCPRTbl();");
				} 				
			} //while tables
		} catch (SQLException e) {
			e.printStackTrace();
		}
		jdbcSqlAccess.close();
	} //searchForCPR_step1
	
	public static void searchForCPR_step2() {
		//String _TABLESTOSCANSQL="select relname from \"pg_statio_user_tables\";";
		String _TABLESTOSCANSQL="select distinct table_name from modifyByCKANAPI;";
		
		Boolean noCPR;
		
		JDBCSQLAccess jdbcSqlAccess=new JDBCSQLAccess(_FORNAME,_CONNECTIONSTRING,_USER,_PASSWORD);		
		jdbcSqlAccess.connect();
		ResultSet tables=jdbcSqlAccess.executeQuery(_TABLESTOSCANSQL);		
				
		try {
			while (tables.next()) {
				noCPR=true;
				String str="";
				String table_name=tables.getObject("table_name").toString();
				int offset=0;
				JDBCSQLAccess datastoreSQLaccess=new JDBCSQLAccess(_FORNAME,_CONNECTIONSTRING,_USER,_PASSWORD);		
				datastoreSQLaccess.connect();
				String sql="select * from \"" + table_name + "\" where _id in (select _id from modifyByCKANAPI where table_name='" + table_name + "' and \"falseAlert\"=false);";
				
				ResultSet datastore=datastoreSQLaccess.executeQuery(sql);
				String strToScan="";
				do {																																						
					str="";					
					while (datastore.next()) {		        	
						str="\n_id=" + datastore.getObject("_id") + ", record=";
						int total_col = datastore.getMetaData().getColumnCount();
						for (int i = 0; i < total_col; i++) {
							String columnLabel=datastore.getMetaData().getColumnLabel(i+1);
							if ((columnLabel.equals("_full_text")==false) && (columnLabel.equals("_id")==false)) {
								str=str+datastore.getObject(columnLabel) + ",";
							}
						}
						strToScan=strToScan+str + "\n";
						if (strToScan.length()>_STRLENGTH) {
							String isPossiblyCPR=isPossiblyCPR(strToScan);
							if (isPossiblyCPR.equals("")==false) {								
								writeToLog(table_name,isPossiblyCPR);																							
								noCPR=false;																
							}
							strToScan="";
						}						
					}									
					offset=offset+_OFFSET;
				} while(str.equals("")==false);
				if (strToScan.length()<_STRLENGTH) {
					String isPossiblyCPR=isPossiblyCPR(strToScan);
					if (isPossiblyCPR.equals("")==false) {								
						writeToLog(table_name,isPossiblyCPR);																							
						noCPR=false;																
					}
					strToScan="";
				}
				datastoreSQLaccess.close();
				if (noCPR==true) {
					jdbcSqlAccess.executeUpdateSQL("DELETE FROM modifyByCKANAPI WHERE table_name='" + table_name + "';");					
				} 
			} //while tables
		} catch (SQLException e) {
			e.printStackTrace();
		}
		jdbcSqlAccess.close();
	} //searchForCPR_step2
			
	public static String isPossiblyCPR(String strToScan) {		
		final Matcher m = Pattern.compile("\\d{5,6}[ -]\\d{4}").matcher(strToScan);		
		String str="";
		while (m.find()) {
			int mStart=m.start();
			int mEnd=m.end();
			if (strToScan.substring(mEnd-4,mEnd).equals("0000")==false) {
				if (isValidDate(strToScan.substring(mStart,mEnd-5))==true) {	
					int lio=strToScan.lastIndexOf("\n_id=", mStart);
					int io=strToScan.indexOf("\n", mEnd);
					if (lio==-1) {			
						lio=0;
						str=str + "\n";
					}				
					str=str + strToScan.substring(lio,io) + " Possibly CPR=" + strToScan.substring(mStart,mEnd) + ", position=" + ((mStart-lio));					
				}
			}						
		}										
		return str;		
	} //isPossiblyCPR
	
	public static Boolean isValidDate(String stringToParse) {
		if (stringToParse.length()<6) {
			stringToParse='0'+stringToParse;					
		}
		int dd=Integer.parseInt(stringToParse.substring(0,2));
		int mm=Integer.parseInt(stringToParse.substring(2,4));
		//int yy=Integer.parseInt(stringToParse.substring(4,6));
		
		if ((dd>31) | (mm>12)) {
			return false;
		}
		if ((dd<1) | (mm<1)) {
			return false;
		}
		
		return true;		
	} //isValidDate
	
	public static void writeToLog(String table,String stringPossiblyCPR) {
		try{
			// Create file 		
			FileWriter fstream = new FileWriter(_LOGFILEPATH + _LOGFILENAME,true);
			BufferedWriter out = new BufferedWriter(fstream);
		
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String _message=dateFormat.format(date) + " "  + table + " " + stringPossiblyCPR + "\n";
			message=message + _message;
			out.write(_message);
//			System.out.println(dateFormat.format(date) + " "  + table + " " + stringPossiblyCPR);
			//Close the output stream
			out.flush();
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	} //writeToLog	
	
	public static void sendMail() {
		String subject="";
		if (message.equals("")==true) {
			message="Nothing to do.";
			subject=message;
		} else {
			subject="Found CPR";
		}
		Mail mail=new Mail(_EMAIL_FROM,_EMAIL_HOST);
		mail.sendMail(_EMAIL_TO,subject,message);
	}
}