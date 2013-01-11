package ru.mail.jira.plugins;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import java.util.LinkedList;
import java.util.List;
import org.ofbiz.core.entity.GenericEntityException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

public class TransitionDateFunction
    extends AbstractJqlFunction
{
    /**
     * Issue manager.
     */
    private final IssueManager issueMgr;

    /**
     * Constructor.
     */
    public TransitionDateFunction(
        IssueManager issueMgr)
    {
        this.issueMgr = issueMgr;
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
        return 1;
    }

    @Override
    @NotNull
    public List<QueryLiteral> getValues(
        @NotNull QueryCreationContext queryCreationContext,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause termClause)
    {
        notNull("queryCreationContext", queryCreationContext);
        final List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        try
        {
            List<Issue> issues = issueMgr.getVotedIssues(queryCreationContext.getQueryUser());
            for (Issue issue : issues)
            {
                literals.add(new QueryLiteral(operand, issue.getId()));
            }
        }
        catch (GenericEntityException e)
        {
            e.printStackTrace();
        }

        return literals;
    }

    @Override
    @NotNull
    public MessageSet validate(
        User searcher,
        @NotNull FunctionOperand operand,
        @NotNull TerminalClause termClause)
    {
        List<String> projectKeys = operand.getArgs();
        MessageSet messages = new MessageSetImpl();

        return messages;
    }
}
