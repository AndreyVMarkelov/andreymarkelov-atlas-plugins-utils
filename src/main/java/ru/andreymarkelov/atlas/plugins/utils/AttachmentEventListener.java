package ru.andreymarkelov.atlas.plugins.utils;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.MutableComment;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;

public class AttachmentEventListener implements InitializingBean, DisposableBean {
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

    @SuppressWarnings("unused")
    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        if (EventType.ISSUE_UPDATED_ID.equals(issueEvent.getEventTypeId())) {
            Issue issue = issueEvent.getIssue();
            User user = issueEvent.getUser();
            GenericValue changeLog = issueEvent.getChangeLog();
            if (issue == null || user == null || changeLog == null)
                return;

            AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();
            CommentManager commentManager = ComponentAccessor.getCommentManager();
            String baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
            String arrowUrl = String.format("%s/images/icons/link_attachment_7.gif", baseUrl);

            StringBuilder filesSb = new StringBuilder();
            StringBuilder imagesSb = new StringBuilder();
            try {
                for (GenericValue changeItem : changeLog.getRelated("ChildChangeItem")) {
                    Object field = changeItem.get("field");
                    Object newValue = changeItem.get("newvalue");
                    if ("Attachment".equals(field) && newValue != null) {
                        Long attachmentId = Long.parseLong(newValue.toString());
                        Attachment attachment = attachmentManager.getAttachment(attachmentId);
                        if (attachment != null) {
                            String attachmentUrl = String.format("%s/secure/attachment/%d/%d_%s", baseUrl, attachmentId, attachmentId, attachment.getFilename());
                            String attachmentTitle = String.format("%s attached to %s", attachment.getFilename(), issueEvent.getIssue().getKey());

                            if (Arrays.asList("image/jpeg", "image/png", "image/gif").contains(attachment.getMimetype())) {
                                String thumbnailUrl = String.format("%s/secure/thumbnail/%d/%d__thumb_%d.png", baseUrl, attachmentId, attachmentId, attachmentId);
                                imagesSb.append("[!").append(thumbnailUrl).append("!|").append(attachmentUrl).append("|").append(attachmentTitle).append("] ");
                            } else
                                filesSb.append("[").append(attachment.getFilename()).append("|").append(attachmentUrl).append("|").append(attachmentTitle).append("]^!").append(arrowUrl).append("!^\n");
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            } catch (GenericEntityException ignored) {
            }

            String footer = filesSb.toString() + imagesSb.toString().trim();
            if (!footer.isEmpty()) {
                Comment currentComment = issueEvent.getComment();

                if (currentComment != null) {
                    MutableComment mutableComment = commentManager.getMutableComment(currentComment.getId());
                    mutableComment.setBody(mutableComment.getBody() + "\n" + footer);
                    commentManager.update(mutableComment, false);
                } else
                    commentManager.create(issue, user.getName(), footer, null, null, issueEvent.getTime(), false);
            }
        }
    }
}
