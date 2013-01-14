/*
 * Created by Andrey Markelov 11-01-2013.
 * Copyright Mail.Ru Group 2013. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

/**
 * This JQL function finds all issues that was commented by logged user in the last time.
 * 
 * @author Andrey Markelov
 */
public class TransitionReporterFunction
    extends AbstractJqlFunction
{
    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(TransitionReporterFunction.class);

    /**
     * Processed SQL.
     */
    private final static String SQL = "SELECT ISSUEID FROM jiraaction WHERE ACTIONTYPE = 'comment' AND UPDATED > ? AND UPDATEAUTHOR = ? ORDER BY UPDATED DESC";

    /**
     * Constructor.
     */
    public TransitionReporterFunction() {}

    @Override
    @NotNull
    public JiraDataType getDataType()
    {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments()
    {
        return 0;
    }

    @Override
    @NotNull
    public List<QueryLiteral> getValues(
        @NotNull QueryCreationContext context,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause terminalClause)
    {
        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = new DefaultOfBizConnectionFactory().getConnection();
            pStmt = conn.prepareStatement(SQL);
            rs = pStmt.executeQuery();
            while (rs.next())
            {
                Long l = rs.getLong(1);
                literals.add(new QueryLiteral(operand, l));
            }
        }
        catch (DataAccessException e)
        {
            log.error("MyCommentedIssuesJqlFunction::getValues - An error occured", e);
            return null;
        }
        catch (SQLException e)
        {
            log.error("MyCommentedIssuesJqlFunction::getValues - An error occured", e);
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
        if (keys.size() != 1)
        {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectparameters", operand.getName()));
        }
        else
        {
            String status = keys.get(0);
            String count = keys.get(1);
            String op = keys.get(2);

            try
            {
                ComponentManager.getInstance().getJiraDurationUtils().parseDuration(time, ComponentManager.getInstance().getJiraAuthenticationContext().getLocale());
            }
            catch (InvalidDurationException e)
            {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrecttimeparameter", operand.getName()));
            }
        }

        return messages;
    }
}
