package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;

import hudson.model.Job;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.JobAction;
import io.jenkins.plugins.analysis.core.model.LabelProviderFactory;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.PortletTableModel;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.Result;
import io.jenkins.plugins.analysis.core.portlets.IssuesTablePortlet.TableRow;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the class {@link IssuesTablePortlet}.
 *
 * @author Ullrich Hafner
 */
class IssuesTablePortletTest {
    private static final String SPOT_BUGS_ID = "spotbugs";
    private static final String SPOT_BUGS_NAME = "SpotBugs";
    private static final String CHECK_STYLE_ID = "checkstyle";
    private static final String CHECK_STYLE_NAME = "CheckStyle";

    @Test
    void shouldShowTableWithOneJob() {
        Job<?, ?> job = createJob(1, CHECK_STYLE_ID, CHECK_STYLE_NAME);

        PortletTableModel model = createModel(asList(job));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        verifyRow(rows.get(0), job, CHECK_STYLE_ID, 1);
    }

    @Test
    void shouldShowTableWithTwoJobs() {
        Job<?, ?> firstRow = createJob(1, SPOT_BUGS_ID, SPOT_BUGS_NAME);
        Job<?, ?> secondRow = createJob(2, SPOT_BUGS_ID, SPOT_BUGS_NAME);

        PortletTableModel model = createModel(asList(firstRow, secondRow));

        assertThat(model.getToolNames()).containsExactly(SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        verifyRow(rows.get(0), firstRow, SPOT_BUGS_ID, 1);
        verifyRow(rows.get(1), secondRow, SPOT_BUGS_ID, 2);
    }

    @Test
    void shouldShowTableWithTwoTools() {
        Job job = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(asList(job));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowIconsOfTools() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setShowIcons(true);

        JenkinsFacade jenkinsFacade = mock(JenkinsFacade.class);
        when(jenkinsFacade.getImagePath("checkstyle.png")).thenReturn("/path/to/checkstyle.png");
        when(jenkinsFacade.getImagePath("spotbugs.png")).thenReturn("/path/to/spotbugs.png");
        portlet.setJenkinsFacade(jenkinsFacade);

        Job job = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(asList(job));

        assertThat(model.getToolNames()).containsExactly(
                "<img alt=\"CheckStyle\" title=\"CheckStyle\" src=\"/path/to/checkstyle.png\">",
                "<img alt=\"SpotBugs\" title=\"SpotBugs\" src=\"/path/to/spotbugs.png\">");

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(job);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 2);
        verifyResult(results.get(1), SPOT_BUGS_ID, 1);
    }

    @Test
    void shouldShowHtmlHeaders() {
        IssuesTablePortlet portlet = new IssuesTablePortlet("portlet");

        String htmlName = "<b>ToolName</b> <script>execute</script>";
        Job<?, ?> job = createJob(1, SPOT_BUGS_ID, htmlName);

        LabelProviderFactory factory = mock(LabelProviderFactory.class);
        registerTool(factory, SPOT_BUGS_ID, htmlName);

        portlet.setLabelProviderFactory(factory);

        PortletTableModel model = portlet.getModel(asList(job));
        assertThat(model.getToolNames()).containsExactly("<b>ToolName</b>");
    }

    @Test
    void shouldShowTableWithTwoToolsAndTwoJobs() {
        Job first = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        Job second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(asList(first, second));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        TableRow firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        verifyResult(firstRowResults.get(0), CHECK_STYLE_ID, 2);
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        TableRow secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 4);
        verifyResult(secondRowResults.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldFilterZeroIssuesJobs() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setHideCleanJobs(true);

        Job first = createJobWithActions(
                createAction(0, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(0, CHECK_STYLE_ID, CHECK_STYLE_NAME));
        Job second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(asList(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldFilterNonActionJobs() {
        IssuesTablePortlet portlet = createPortlet();
        portlet.setHideCleanJobs(true);

        Job first = createJobWithActions();
        Job second = createJobWithActions(
                createAction(3, SPOT_BUGS_ID, SPOT_BUGS_NAME),
                createAction(4, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = portlet.getModel(asList(first, second));

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(1);

        TableRow actualRow = rows.get(0);
        assertThat(actualRow.getJob()).isSameAs(second);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(2);

        verifyResult(results.get(0), CHECK_STYLE_ID, 4);
        verifyResult(results.get(1), SPOT_BUGS_ID, 3);
    }

    @Test
    void shouldShowTableWithTwoJobsWithDifferentTools() {
        Job first = createJobWithActions(
                createAction(1, SPOT_BUGS_ID, SPOT_BUGS_NAME));
        Job second = createJobWithActions(
                createAction(2, CHECK_STYLE_ID, CHECK_STYLE_NAME));

        PortletTableModel model = createModel(asList(first, second));

        assertThat(model.getToolNames()).containsExactly(CHECK_STYLE_NAME, SPOT_BUGS_NAME);

        List<TableRow> rows = model.getRows();
        assertThat(rows).hasSize(2);

        TableRow firstRow = rows.get(0);
        assertThat(firstRow.getJob()).isSameAs(first);

        List<Result> firstRowResults = firstRow.getResults();
        assertThat(firstRowResults).hasSize(2);

        verifyEmptyResult(firstRowResults.get(0), CHECK_STYLE_ID);
        verifyResult(firstRowResults.get(1), SPOT_BUGS_ID, 1);

        TableRow secondRow = rows.get(1);
        assertThat(secondRow.getJob()).isSameAs(second);

        List<Result> secondRowResults = secondRow.getResults();
        assertThat(secondRowResults).hasSize(2);

        verifyResult(secondRowResults.get(0), CHECK_STYLE_ID, 2);
        verifyEmptyResult(secondRowResults.get(1), SPOT_BUGS_ID);
    }

    private PortletTableModel createModel(final List<Job<?, ?>> jobs) {
        IssuesTablePortlet portlet = createPortlet();

        return portlet.getModel(jobs);
    }

    private void verifyRow(final TableRow actualRow,
            final Job<?, ?> expectedJob, final String expectedId, final int expectedSize) {
        assertThat(actualRow.getJob()).isSameAs(expectedJob);

        List<Result> results = actualRow.getResults();
        assertThat(results).hasSize(1);

        verifyResult(results.get(0), expectedId, expectedSize);
    }

    private void verifyEmptyResult(final Result result, final String expectedId) {
        assertThat(result.getTotal()).isEmpty();
    }

    private void verifyResult(final Result result, final String expectedId, final int expectedSize) {
        assertThat(result.getUrl()).isEqualTo(url(expectedId));
        assertThat(result.getTotal()).isNotEmpty();
        assertThat(result.getTotal().getAsInt()).isEqualTo(expectedSize);
    }

    private String url(final String id) {
        return "job/build/" + id;
    }

    private IssuesTablePortlet createPortlet() {
        IssuesTablePortlet portlet = new IssuesTablePortlet("portlet");

        LabelProviderFactory factory = mock(LabelProviderFactory.class);
        registerTool(factory, CHECK_STYLE_ID, CHECK_STYLE_NAME);
        registerTool(factory, SPOT_BUGS_ID, SPOT_BUGS_NAME);
        portlet.setLabelProviderFactory(factory);

        return portlet;
    }

    private void registerTool(final LabelProviderFactory factory, final String id, final String name) {
        StaticAnalysisLabelProvider tool = mock(StaticAnalysisLabelProvider.class);
        when(factory.create(id, name)).thenReturn(tool);
        when(factory.create(id)).thenReturn(tool);
        when(tool.getSmallIconUrl()).thenReturn(id + ".png");
        when(tool.getName()).thenReturn(name);
        when(tool.getLinkName()).thenReturn(name);
    }

    private List<Job<?, ?>> asList(final Job<?, ?>... analysisJobs) {
        List<Job<?, ?>> jobs = new ArrayList<>();
        Collections.addAll(jobs, analysisJobs);
        return jobs;
    }

    private Job createJobWithActions(final JobAction... actions) {
        Job job = mock(Job.class);

        when(job.getActions(JobAction.class)).thenReturn(Lists.fixedSize.of(actions));

        return job;
    }

    private Job<?, ?> createJob(final int size, final String id, final String name) {
        Job job = mock(Job.class);
        JobAction jobAction = createAction(size, id, name);

        when(job.getActions(JobAction.class)).thenReturn(Collections.singletonList(jobAction));

        return job;
    }

    private JobAction createAction(final int size, final String id, final String name) {
        JobAction jobAction = mock(JobAction.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(jobAction.getLatestAction()).thenReturn(Optional.of(resultAction));
        when(jobAction.getId()).thenReturn(id);

        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getTotalSize()).thenReturn(size);

        when(resultAction.getResult()).thenReturn(result);
        when(resultAction.getId()).thenReturn(id);
        when(resultAction.getName()).thenReturn(name);
        when(resultAction.getRelativeUrl()).thenReturn(url(id));

        return jobAction;
    }
}