package ru.andreymarkelov.atlas.plugins;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

public class IssuesParentsJQL extends AbstractJqlFunction {
    /**
     * Logger.
     */
    private static Log log = LogFactory.getLog(IssuesParentsJQL.class);

    /**
     * Permission manager.
     */
    private final PermissionManager permissionManager;

    /**
     * Search service.
     */
    private final SearchService searchService;

    public IssuesParentsJQL(
            PermissionManager permissionManager,
            SearchService searchService) {
        this.permissionManager = permissionManager;
        this.searchService = searchService;
    }

    @Override
    @NotNull
    public JiraDataType getDataType() {
        return JiraDataTypes.ISSUE;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 1;
    }

    @Override
    @NotNull
    public List<QueryLiteral> getValues(
            @NotNull QueryCreationContext context,
            @NotNull FunctionOperand operand,
            @NotNull TerminalClause terminalClause) {
        List<QueryLiteral> literals = new LinkedList<QueryLiteral>();

        SearchService.ParseResult parseResult = searchService.parseQuery(context.getUser(), operand.getArgs().get(0));
        if (parseResult.isValid()) {
            SearchResults results;
            try {
                results = searchService.search(context.getUser(), parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
            } catch (SearchException e) {
                log.error("IssuesParentsJQL::getValues - searching error", e);
                return null;
            }
            List<Issue> issues = results.getIssues();
            for (Issue i : issues) {
                Issue parent = i.getParentObject();
                if (parent != null && permissionManager.hasPermission(Permissions.BROWSE, parent, context.getUser())) {
                    literals.add(new QueryLiteral(operand, parent.getId()));
                }
            }
        }

        return literals;
    }

    @Override
    @NotNull
    public MessageSet validate(
            User searcher,
            @NotNull FunctionOperand operand,
            @NotNull TerminalClause terminalClause) {
        MessageSet messages = new MessageSetImpl();

        List<String> keys = operand.getArgs();
        if (keys.size() != 1) {
            messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.jql.issuessubtasks.nooneparam"));
        } else {
            SearchService.ParseResult parseResult = searchService.parseQuery(searcher, keys.get(0));
            if (!parseResult.isValid()) {
                messages.addErrorMessage(ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("utils.jql.issuessubtasks.invalidjql"));
            }
        }

        return messages;
    }
}