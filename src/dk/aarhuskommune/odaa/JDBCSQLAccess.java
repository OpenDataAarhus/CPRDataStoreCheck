package dk.aarhuskommune.odaa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCSQLAccess {
	private Statement statement = null;
	private Connection connect = null;
	private ResultSet resultSet = null;
	private String for_name=null;
	private String url=null;
	private String user=null;
	private String password=null;
	
	public JDBCSQLAccess(String for_name,String url, String user, String password) {
		super();
		this.for_name=for_name;
		this.url = url;
		this.user = user;
		this.password = password;		
	}

	public Statement connect() {
		try {
			Class.forName (for_name).newInstance ();
			//Class.forName ("org.postgresql.Driver").newInstance ();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			connect = DriverManager.getConnection (url, user, password);
			statement=connect.createStatement();			
			// statements allow to issue SQL queries to the database		    
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return statement;
	} //connect
	
	public ResultSet executeQuery(String sql) {
		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultSet;
	} //executeQuery
	
	public void writeResultSet() throws SQLException {
	    // resultSet is initialised before the first data set
	    while (resultSet.next()) {	      
	    	System.out.println("ID: " + resultSet.getString("ID"));
	    	System.out.println("Name: " + resultSet.getString("Name"));
	    	System.out.println("CountryCode: " + resultSet.getString("CountryCode"));
	    	System.out.println("District: " + resultSet.getString("District"));
	    	System.out.println("Population: " +resultSet.getString("Population"));
	    }
	} //writeResultSet
	
	public void executeUpdateSQL(String sql) {				
		Statement cprScanned_table = null;
		Connection con;
		try {
			con = DriverManager.getConnection(url, user, password);
			cprScanned_table = con.createStatement();
			cprScanned_table.executeUpdate(sql);
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}								
	} //executeUpdateSQL
	
	public void close() {
	    try {
			resultSet.close();
			statement.close();
			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	    	    		
	} //close
}
