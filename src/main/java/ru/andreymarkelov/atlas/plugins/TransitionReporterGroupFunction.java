package ru.andreymarkelov.atlas.plugins;

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
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.andreymarkelov.atlas.plugins.TransitionCountFunction;
import ru.andreymarkelov.atlas.plugins.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This JQL function finds all the issues that were moved to a specified status by a user, who belongs to a specified group.
 *
 * @author Andrey Molchanov
 */
public class TransitionReporterGroupFunction extends AbstractJqlFunction {
    private final static String SQL =
            "SELECT\n" +
            "    cg.issueid\n" +
            "FROM\n" +
            "    changegroup AS cg,\n" +
            "    changeitem AS ci,\n" +
            "    cwd_membership AS m\n" +
            "WHERE\n" +
            "    cg.id = ci.groupid\n" +
            "    AND ci.fieldtype = 'jira'\n" +
            "    AND ci.field = 'status'\n" +
            "    AND ci.newstring = ?\n" +
            "    AND cg.author = m.child_name\n" +
            "    AND m.parent_name = ?\n" +
            "    AND m.membership_type = 'GROUP_USER'\n" +
            "GROUP BY\n" +
            "    cg.issueid";

    private final static Log log = LogFactory.getLog(TransitionCountFunction.class);
    private final PermissionManager permissionManager;
    private final UserUtil userUtil;

    public TransitionReporterGroupFunction(PermissionManager permissionManager, UserUtil userUtil) {
        this.permissionManager = permissionManager;
        this.userUtil = userUtil;
    }

    @Override
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 2;
    }

    @Override
    public MessageSet validate(User searcher, FunctionOperand operand, TerminalClause terminalClause) {
        MessageSet messages = new MessageSetImpl();

        List<String> keys = operand.getArgs();
        if (keys.size() != 2) {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectparameters", operand.getName()));
        } else {
            String status = keys.get(0);
            String group = keys.get(1);

            Collection<Status> statuses = ComponentManager.getInstance().getConstantsManager().getStatusObjects();
            boolean correctStatus = false;
            for (Status statusObj : statuses)
                if (statusObj.getName().equals(status)) {
                    correctStatus = true;
                    break;
                }
            if (!correctStatus)
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectstatusparameter", status, operand.getName()));

            if (userUtil.getGroupObject(group) == null)
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.incorrectgroupparameter", operand.getName()));
        }

        return messages;
    }

    @Override
    public List<QueryLiteral> getValues(QueryCreationContext context, FunctionOperand operand, TerminalClause terminalClause) {
        List<String> keys = operand.getArgs();
        String status = keys.get(0);
        String group = keys.get(1);

        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        Connection conn = null;
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try {
            conn = new DefaultOfBizConnectionFactory().getConnection();

            pStmt = conn.prepareStatement(SQL);
            pStmt.setString(1, status);
            pStmt.setString(2, group);

            rs = pStmt.executeQuery();
            IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
            while (rs.next()) {
                Long l = rs.getLong(1);
                Issue issue = issueManager.getIssueObject(l);
                if (issue != null && permissionManager.hasPermission(Permissions.BROWSE, issue, context.getUser()))
                    literals.add(new QueryLiteral(operand, l));
            }
        } catch (DataAccessException e) {
            log.error("TransitionReporterGroupFunction::getValues - DataAccessException", e);
            return null;
        } catch (SQLException e) {
            log.error("TransitionReporterGroupFunction::getValues - SQLException", e);
            return null;
        } finally {
            Utils.closeResultSet(rs);
            Utils.closeStaement(pStmt);
            Utils.closeConnection(conn);
        }

        return literals;
    }
}
