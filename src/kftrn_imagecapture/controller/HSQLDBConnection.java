/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kftrn_imagecapture.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author zakhiyah arsal
 */
public class HSQLDBConnection {
    public static Connection getConnection() {
		Connection con = null;
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
                        //con = DriverManager.getConnection("jdbc:hsqldb:hsql://192.9.201.200:9001/camdb;user=ROOT;password=minic123");
                        con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:9001/camdb;user=ROOT;password=minic123");	
		} catch (ClassNotFoundException | SQLException e) {	
		}
	return con;
	}
    
}
