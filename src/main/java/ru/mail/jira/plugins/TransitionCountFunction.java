package ru.mail.jira.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

/**
 * This JQL function finds all issues that transition was performed the <code>count</code> times.
 */
public class TransitionCountFunction extends AbstractJqlFunction {
    private final static String SQL =
            "SELECT\n" +
            "    ji.id\n" +
            "FROM\n" +
            "    jiraissue AS ji,\n" +
            "    changegroup AS cg,\n" +
            "    changeitem AS ci\n" +
            "WHERE\n" +
            "    ji.project = ?\n" +
            "    AND ji.id = cg.issueid\n" +
            "    AND ci.groupid = cg.id\n" +
            "    AND ci.fieldtype = 'jira'\n" +
            "    AND ci.field = 'status'\n" +
            "    AND ci.newstring = ?\n" +
            "GROUP BY\n" +
            "    ji.id\n" +
            "HAVING\n" +
            "    COUNT(*) %s ?";

    private final static Log log = LogFactory.getLog(TransitionCountFunction.class);
    private final PermissionManager permissionManager;

    public TransitionCountFunction(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 4;
    }

    @Override
    public List<QueryLiteral> getValues(QueryCreationContext context, FunctionOperand operand, TerminalClause terminalClause) {
        List<String> keys = operand.getArgs();
        String project = keys.get(0);
        String status = keys.get(1);
        String count = keys.get(2);
        String op = keys.get(3);

        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try {
            conn = new DefaultOfBizConnectionFactory().getConnection();

            pStmt = conn.prepareStatement(String.format(SQL, op));
            pStmt.setLong(1, ComponentManager.getInstance().getProjectManager().getProjectObjByKey(project).getId());
            pStmt.setString(2, status);
            pStmt.setLong(3, Long.parseLong(count));

            rs = pStmt.executeQuery();
            IssueManager imgr = ComponentManager.getInstance().getIssueManager();
            while (rs.next()) {
                Long l = rs.getLong(1);
                Issue issue = imgr.getIssueObject(l);
                if (issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, context.getUser()))
                    literals.add(new QueryLiteral(operand, l));
            }
        } catch (DataAccessException e) {
            log.error("TransitionCountFunction::getValues - An error occured", e);
            return null;
        } catch (SQLException e) {
            log.error("TransitionCountFunction::getValues - An error occured", e);
            return null;
        } finally {
            Utils.closeResultSet(rs);
            Utils.closeStaement(pStmt);
            Utils.closeConnection(conn);
        }

        return literals;
    }

    @Override
    public MessageSet validate(User searcher, FunctionOperand operand, TerminalClause terminalClause) {
        MessageSet messages = new MessageSetImpl();

        List<String> keys = operand.getArgs();
        if (keys.size() != 4) {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectparameters", operand.getName()));
        } else {
            String project = keys.get(0);
            String status = keys.get(1);
            String count = keys.get(2);
            String op = keys.get(3);

            if (ComponentManager.getInstance().getProjectManager().getProjectObjByKey(project) == null)
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectprojectparameter", project, operand.getName()));

            Collection<Status> statuses = ComponentManager.getInstance().getConstantsManager().getStatusObjects();
            boolean correctStatus = false;
            for (Status statusObj : statuses)
                if (statusObj.getName().equals(status)) {
                    correctStatus = true;
                    break;
                }
            if (!correctStatus)
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectstatusparameter", status, operand.getName()));

            try {
                Long.parseLong(count);
            } catch (NumberFormatException e) {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectintparameter", count, operand.getName()));
            }

            if (!Arrays.asList(">", "<", "=", ">=", "<=", "<>").contains(op))
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectoperatorparameter", op, operand.getName()));
        }

        return messages;
    }
}
