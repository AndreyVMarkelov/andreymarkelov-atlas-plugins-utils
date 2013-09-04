package ru.andreymarkelov.atlas.plugins;

import java.util.Map;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlParseException;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.Validator;
import com.opensymphony.workflow.WorkflowException;

public class JqlValidator
    implements Validator
{
    private final SearchService searchService;

    /**
     * Constructor.
     */
    public JqlValidator(
        SearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    public void validate(
        Map transientVars,
        Map args,
        PropertySet ps)
    throws InvalidInputException, WorkflowException
    {
        Issue issue = (Issue) transientVars.get("issue");

        String jql = (String) args.get(Consts.JQL);

        if (!Utils.isValidStr(jql))
        {
            return;
        }

        User user = ComponentManager.getInstance().getJiraAuthenticationContext().getLoggedInUser();
        if (user == null)
        {
            return;
        }

        try
        {
            Query query = (new DefaultJqlQueryParser()).parseQuery(jql);
            query = JqlQueryBuilder.newClauseBuilder(query).and().not().issue(issue.getKey()).buildQuery();
            SearchResults results = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
            if (results != null && results.getIssues().size() > 0)
            {
                for (Issue i : results.getIssues())
                {
                    throw new InvalidInputException(
                        ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getText(
                            "utils.jqlunique.error",
                            i.getKey(),
                            i.getSummary()));
                }
            }
        }
        catch (JqlParseException ex)
        {
            return;
        }
        catch (SearchException e)
        {
            return;
        }
    }
}
