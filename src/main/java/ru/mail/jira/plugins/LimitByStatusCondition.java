package ru.mail.jira.plugins;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

public class LimitByStatusCondition extends AbstractJiraCondition {
    private static final String ISSUES_BY_STATUS_JQL = "assignee = currentUser() AND status = '%s'";

    private final SearchService searchService;

    public LimitByStatusCondition(SearchService searchService) {
        this.searchService = searchService;
    }

    protected int getInProgressIssuesCount(User user, String status) {
        SearchService.ParseResult parseResult = searchService.parseQuery(user, String.format(ISSUES_BY_STATUS_JQL, status));
        if (parseResult.isValid())
            try {
                SearchResults results = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter());
                if (results != null)
                    return results.getTotal();
            } catch (SearchException ignored) {
            }
        return 0;
    }

    @Override
    public boolean passesCondition(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        String status = (String) args.get(LimitByStatusConditionFactoryImpl.STATUS_KEY);
        if (status == null || "".equals(status))
            return true;

        int limit;
        try {
            String limitString = (String) args.get(LimitByStatusConditionFactoryImpl.LIMIT_KEY);
            limit = Integer.parseInt(limitString);
        } catch (NumberFormatException e) {
            return true;
        }

        User user = getCaller(transientVars, args);
        int issuesCount = getInProgressIssuesCount(user, status);

        return issuesCount < limit;
    }
}
