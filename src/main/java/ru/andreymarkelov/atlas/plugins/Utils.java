/*
 * Created by Andrey Markelov 02-10-2012. Copyright Mail.Ru Group 2012. All
 * rights reserved.
 */
package ru.andreymarkelov.atlas.plugins;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;


/**
 * This class contains utility methods.
 * 
 * @author Andrey Markelov
 */
public class Utils
{
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    private static final SearchService searchService = ComponentManager
        .getComponentInstanceOfType(SearchService.class);

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
                // --> nothing
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
                // --> nothing
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
                // --> nothing
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
     * Executes JQL Query
     */
    public static List<Issue> executeJQLQuery(String jqlQuery)
    {
        List<Issue> result = null;

        User user = ComponentManager.getInstance()
            .getJiraAuthenticationContext().getLoggedInUser();
        SearchService.ParseResult parseResult = searchService.parseQuery(user,
            jqlQuery);

        if (parseResult.isValid())
        {
            Query query = parseResult.getQuery();
            try
            {
                SearchResults results = searchService.search(user, query,
                    PagerFilter.getUnlimitedFilter());
                result = results.getIssues();
            }
            catch (SearchException e)
            {
                log.error("Utils::search exception during executing JQL", e);
            }
        }

        return result;
    }

    /**
     * Private constructor.
     */
    private Utils()
    {
    }
}
