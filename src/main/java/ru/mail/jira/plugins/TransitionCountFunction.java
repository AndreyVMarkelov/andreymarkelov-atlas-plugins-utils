/*
 * Created by Andrey Markelov 11-01-2013.
 * Copyright Mail.Ru Group 2013. All rights reserved.
 */
package ru.mail.jira.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

/**
 * This JQL function finds all issues that transition was performed the <code>count</code> times.
 * 
 * @author Andrey Markelov
 */
public class TransitionCountFunction
    extends AbstractJqlFunction
{
    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(TransitionCountFunction.class);

    /**
     * Processed SQL with '>'.
     */
    private final static String SQL = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) > ?";

    /**
     * Processed SQL with '<'.
     */
    private final static String SQL1 = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) < ?";

    /**
     * Processed SQL with '='.
     */
    private final static String SQL2 = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) = ?";

    /**
     * Processed SQL with '<='.
     */
    private final static String SQL3 = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) <= ?";

    /**
     * Processed SQL with '>='.
     */
    private final static String SQL4 = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) >= ?";

    /**
     * Processed SQL with '<>'.
     */
    private final static String SQL5 = "SELECT CG.ISSUEID FROM changeitem CI INNER JOIN changegroup CG ON CI.GROUPID = CG.ID WHERE CI.FIELDTYPE = 'jira' AND CI.FIELD = 'status' AND CI.NEWSTRING = ? GROUP BY CG.ISSUEID HAVING COUNT(CI.NEWSTRING) <> ?";

    /**
     * Permission manager.
     */
    private final PermissionManager permissionManager;

    /**
     * Constructor.
     */
    public TransitionCountFunction(
        PermissionManager permissionManager)
    {
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
        return 3;
    }

    @Override
    @NotNull
    public List<QueryLiteral> getValues(
        @NotNull QueryCreationContext context,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause terminalClause)
    {
        List<String> keys = operand.getArgs();
        String status = keys.get(0);
        String count = keys.get(1);
        String op = keys.get(2);

        String sql;
        if (op.equals(">"))
        {
            sql = SQL;
        }
        else if (op.equals("<"))
        {
            sql = SQL1;
        }
        else if (op.equals("="))
        {
            sql = SQL2;
        }
        else if (op.equals("<="))
        {
            sql = SQL3;
        }
        else if (op.equals(">="))
        {
            sql = SQL4;
        }
        else
        {
            sql = SQL5;
        }

        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try
        {
            conn = new DefaultOfBizConnectionFactory().getConnection();
            pStmt = conn.prepareStatement(sql);
            pStmt.setString(1, status);
            pStmt.setLong(2, Long.parseLong(count));
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
            log.error("TransitionCountFunction::getValues - An error occured", e);
            return null;
        }
        catch (SQLException e)
        {
            log.error("TransitionCountFunction::getValues - An error occured", e);
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
        if (keys.size() != 3)
        {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectparameters", operand.getName()));
        }
        else
        {
            String status = keys.get(0);
            String count = keys.get(1);
            String op = keys.get(2);

            Collection<Status> statuses = ComponentManager.getInstance().getConstantsManager().getStatusObjects();
            boolean correctStatus = false;
            for (Status statusObj : statuses)
            {
                if (statusObj.getName().equals(status))
                {
                    correctStatus = true;
                    break;
                }
            }

            if (!correctStatus)
            {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectstatusparameter", status, operand.getName()));
            }

            try
            {
                Long.parseLong(count);
            }
            catch (NumberFormatException nex)
            {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectintparameter", count, operand.getName()));
            }

            List<String> ops = new ArrayList<String>(6);
            ops.add(">");
            ops.add("<");
            ops.add("=");
            ops.add(">=");
            ops.add("<=");
            ops.add("<>");
            if (!ops.contains(op))
            {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectoperatorparameter", op, operand.getName()));
            }
        }

        return messages;
    }
}
