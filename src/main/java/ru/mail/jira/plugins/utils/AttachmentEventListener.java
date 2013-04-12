package ru.mail.jira.plugins.utils;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.util.AttachmentUtils;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import ru.mail.jira.plugins.Utils;

import java.io.File;
import java.net.URLEncoder;
import java.util.*;

public class AttachmentEventListener implements InitializingBean, DisposableBean {
    private final Log log = LogFactory.getLog(AttachmentEventListener.class);
    private final EventPublisher eventPublisher;

    public AttachmentEventListener(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
    }
 
    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }

    private boolean isEnabled() {
        return true;
    }

    private boolean isAttachmentEvent(IssueEvent issueEvent) {
        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId()))
            if (issueEvent.getChangeLog() != null)
                try {
                    // todo remove attachment ??
                    for (GenericValue changeItem : issueEvent.getChangeLog().getRelated("ChildChangeItem"))
                        if ("Attachment".equals(changeItem.get("field")))
                            return true;
                } catch (GenericEntityException ignored) {
                }
        return false;
    }

    @SuppressWarnings("unused")
    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        if (isEnabled() && isAttachmentEvent(issueEvent) && issueEvent.getComment() == null) {
            // Add empty comment

        }
    }
}
