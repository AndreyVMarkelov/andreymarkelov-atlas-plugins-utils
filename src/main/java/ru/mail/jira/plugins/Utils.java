/*
 * Created by Andrey Markelov 02-10-2012.
 * Copyright Mail.Ru Group 2012. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class contains utility methods.
 * 
 * @author Andrey Markelov
 */
public class Utils
{
    /**
     * Close connection.
     */
    public static void closeConnection(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                //--> nothing
            }
        }
    }

    /**
     * Close result set.
     */
    public static void closeResultSet(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                //--> nothing
            }
        }
    }

    /**
     * Close statement.
     */
    public static void closeStaement(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                //--> nothing
            }
        }
    }

    /**
     * Check string on not null and not empty.
     */
    public static boolean isValidStr(String str)
    {
        return (str != null && str.length() > 0);
    }

    /**
     * Private constructor.
     */
    private Utils() {}
}
