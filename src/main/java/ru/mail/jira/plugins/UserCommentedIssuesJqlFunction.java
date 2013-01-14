/*
 * Created by Andrey Markelov 11-01-2013.
 * Copyright Mail.Ru Group 2013. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

/**
 * This JQL function finds all issues that was commented by the user in the last time.
 * 
 * @author Andrey Markelov
 */
public class UserCommentedIssuesJqlFunction
    extends AbstractJqlFunction
{
    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(UserCommentedIssuesJqlFunction.class);

    /**
     * Processed SQL.
     */
    private final static String SQL = "SELECT ISSUEID FROM jiraaction WHERE ACTIONTYPE = 'comment' AND UPDATED > ? AND UPDATEAUTHOR = ? ORDER BY UPDATED DESC";

    /**
     * User utils.
     */
    private final UserUtil userUtil;

    /**
     * Permission manager.
     */
    private final PermissionManager permissionManager;

    /**
     * Constructor.
     */
    public UserCommentedIssuesJqlFunction(
        UserUtil userUtil,
        PermissionManager permissionManager)
    {
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
    }

    @Override
    @NotNull
    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments()
    {
        return 2;
    }

    @Override
    @NotNull
    public List<QueryLiteral> getValues(
        @NotNull QueryCreationContext context,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause terminalClause)
    {
        List<String> keys = operand.getArgs();
        String user = keys.get(0);
        String time = keys.get(1);

        long lastFindTime = System.currentTimeMillis();
        if (time.equals("startOfWeek"))
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            lastFindTime = cal.getTimeInMillis();
        }
        else if (time.equals("startOfDay"))
        {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);
            lastFindTime = cal.getTimeInMillis();
        }
        else
        {
            try
            {
                long diffTime = ComponentManager.getInstance().getJiraDurationUtils().parseDuration(time, ComponentManager.getInstance().getJiraAuthenticationContext().getLocale());
                lastFindTime -= (diffTime * 1000);
            }
            catch (InvalidDurationException e)
            {
                return null;
            }
        }

        User userObj = userUtil.getUserObject(user);
        if (userObj == null)
        {
            return null;
        }

        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = new DefaultOfBizConnectionFactory().getConnection();
            pStmt = conn.prepareStatement(SQL);
            pStmt.setTimestamp(1, new Timestamp(lastFindTime));
            pStmt.setString(2, userObj.getName());
            rs = pStmt.executeQuery();
            IssueManager imgr = ComponentManager.getInstance().getIssueManager();
            while (rs.next())
            {
                Long l = rs.getLong(1);
                Issue issue = imgr.getIssueObject(l);
                if (issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, context.getUser()))
                {
                    literals.add(new QueryLiteral(operand, l));
                }
            }
        }
        catch (DataAccessException e)
        {
            log.error("UserCommentedIssuesJqlFunction::getValues - An error occured", e);
            return null;
        }
        catch (SQLException e)
        {
            log.error("UserCommentedIssuesJqlFunction::getValues - An error occured", e);
            return null;
        }
        finally
        {
            Utils.closeResultSet(rs);
            Utils.closeStaement(pStmt);
            Utils.closeConnection(conn);
        }

        return literals;
    }

    @Override
    @NotNull
    public MessageSet validate(
        User searcher,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause terminalClause)
    {
        MessageSet messages = new MessageSetImpl();

        List<String> keys = operand.getArgs();
        if (keys.size() != 2)
        {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectparameters", operand.getName()));
        }
        else
        {
            String user = keys.get(0);
            String time = keys.get(1);

            User userObj = userUtil.getUserObject(user);
            if (userObj == null)
            {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectuserparameter", operand.getName()));
            }
            else
            {
                if (time != null && (time.equals("startOfWeek") || time.equals("startOfDay")))
                {
                    //--> nothing
                }
                else
                {
                    try
                    {
                        ComponentManager.getInstance().getJiraDurationUtils().parseDuration(time, ComponentManager.getInstance().getJiraAuthenticationContext().getLocale());
                    }
                    catch (InvalidDurationException e)
                    {
                        messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrecttimeparameter", operand.getName()));
                    }
                }
            }
        }

        return messages;
    }
}
