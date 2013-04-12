package ru.mail.jira.plugins.utils;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class AttachmentCommentRenderer extends AtlassianWikiRenderer {
    private final CommentManager commentManager;

    public AttachmentCommentRenderer(CommentManager commentManager, EventPublisher eventPublisher, VelocityRequestContextFactory velocityRequestContextFactory) {
        super(eventPublisher, velocityRequestContextFactory);
        this.commentManager = commentManager;
    }

    public String getRendererType() {
        return "attachment-comment-renderer";
    }

    public String render(String s, IssueRenderContext issueRenderContext) {
        s += getAttachmentLinks(issueRenderContext.getIssue(), s);
        return super.render(s, issueRenderContext);
    }

    private String getAttachmentLinks(Issue issue, String comment) {
        Collection<Attachment> attachments = issue.getAttachments();
        List<Comment> comments = commentManager.getComments(issue);

        for (Comment c : comments)
            if (c.getBody().equals(comment)) {
                Date created = c.getCreated();
                for (Attachment attachment : attachments) {
                    if (created.equals(attachment.getCreated()))
                        return "\r\n\r\n[^" + attachment.getFilename() + "]";
                }
            }

        return "";
    }
}
