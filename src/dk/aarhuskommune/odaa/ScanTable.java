/**
 * 15-05-2014
 */
package dk.aarhuskommune.odaa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScanTable {
	
	private String _TABLESTOSCANSQL="select * from information_schema.tables where table_schema='public' and is_insertable_into='YES';";
		
	public String generateSQL(String tableName,int limit,int offset) {
		return "select * from \"" + tableName + "\" limit " + limit + " offset " + offset + ";";
	}
	
	public List<String> findTablesToScan(JDBCSQLAccess jdbcSqlAccess) {
		ResultSet rs=jdbcSqlAccess.executeQuery(_TABLESTOSCANSQL);
	    List<String> al = new ArrayList<String>();
        try {
			while (rs.next()) {            				
				al.add(rs.getObject("table_name").toString());				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return al;
	} //findTablesToScan
	
	public List<String> resultSetToStringArray(JDBCSQLAccess jdbcSqlAccess,String sql) {
		ResultSet rs=jdbcSqlAccess.executeQuery(sql);		
		int total_col;
		List<String> al = new ArrayList<String>();
		try {
			total_col = rs.getMetaData().getColumnCount();	        
	        while (rs.next()) {
	        	String str="";
	        	for (int i = 0; i < total_col; i++) {
	        		str=str+rs.getObject(rs.getMetaData().getColumnLabel(i+1)) + "\":\"";	        				        
		        }
	        	al.add(str);
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return al;
	} //resultSetToStringArray

}
//\d{5,6}[ -]\d{4}