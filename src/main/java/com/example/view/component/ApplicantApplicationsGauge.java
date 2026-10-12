package com.example.view.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;

import java.util.Locale;

public class ApplicantApplicationsGauge extends Div {

    // Active Applications Gauge Elements
    private final Div gaugeSvgContainer = new Div();
    private final Span gaugeCenterValue = new Span("0");
    private final Span gaugeCenterLabel = new Span("Active Applications");
    private final Span appliedCountSpan = new Span("0");
    private final Span screeningCountSpan = new Span("0");
    private final Span techTestCountSpan = new Span("0");
    private final Span interviewCountSpan = new Span("0");

    public ApplicantApplicationsGauge() {
        addClassName("applicant-section-card");

        // Header: Title on left, subtitle (tanpa badge In Progress redundan)
        Div header = new Div();
        header.addClassName("applicant-section-header");

        Div titleGroup = new Div();
        titleGroup.addClassName("applicant-section-title-group");

        H3 title = new H3("Active Applications");
        title.addClassName("applicant-section-title");

        Span subtitle = new Span("See where your active applications currently stand.");
        subtitle.addClassName("applicant-section-subtitle");

        titleGroup.add(title, subtitle);
        header.add(titleGroup);

        // Content layout: Left = Gauge Area (35-40%), Right = Breakdown Area (60-65%)
        Div contentLayout = new Div();
        contentLayout.addClassName("applicant-gauge-content-layout");

        // 1. Gauge Area (Left, ~35-40%)
        Div gaugeArea = new Div();
        gaugeArea.addClassName("applicant-gauge-area");

        Div gaugeBox = new Div();
        gaugeBox.addClassName("applicant-gauge-box");

        gaugeSvgContainer.addClassName("applicant-gauge-svg-container");

        Div centerTextGroup = new Div();
        centerTextGroup.addClassName("applicant-gauge-center-text");

        gaugeCenterValue.addClassName("applicant-gauge-center-number");
        gaugeCenterLabel.addClassName("applicant-gauge-center-label");

        centerTextGroup.add(gaugeCenterValue, gaugeCenterLabel);
        gaugeBox.add(gaugeSvgContainer, centerTextGroup);
        gaugeArea.add(gaugeBox);

        // 2. Breakdown Area (Right, ~60-65%, 2x2 grid)
        Div breakdownArea = new Div();
        breakdownArea.addClassName("applicant-breakdown-area");

        Div appliedItem = createLegendItem("Applied", appliedCountSpan, "legend-applied");
        Div screeningItem = createLegendItem("Screening", screeningCountSpan, "legend-screening");
        Div techTestItem = createLegendItem("Technical Test", techTestCountSpan, "legend-tech-test");
        Div interviewItem = createLegendItem("Interview", interviewCountSpan, "legend-interview");

        breakdownArea.add(appliedItem, screeningItem, techTestItem, interviewItem);

        contentLayout.add(gaugeArea, breakdownArea);
        add(header, contentLayout);
    }

    private Div createLegendItem(String stageLabel, Span countSpan, String colorClass) {
        Div item = new Div();
        item.addClassName("applicant-legend-item");

        Div dot = new Div();
        dot.addClassNames("applicant-legend-dot", colorClass);

        Span label = new Span(stageLabel);
        label.addClassName("applicant-legend-label");

        countSpan.addClassName("applicant-legend-count");

        item.add(dot, label, countSpan);
        return item;
    }

    public void updateData(
            int applied,
            int screening,
            int techTest,
            int interview,
            int inProgress
    ) {
        gaugeCenterValue.setText(String.valueOf(inProgress));
        if (inProgress == 1) {
            gaugeCenterLabel.setText("Active Application");
        } else {
            gaugeCenterLabel.setText("Active Applications");
        }

        appliedCountSpan.setText(String.valueOf(applied));
        screeningCountSpan.setText(String.valueOf(screening));
        techTestCountSpan.setText(String.valueOf(techTest));
        interviewCountSpan.setText(String.valueOf(interview));

        updateGauge(applied, screening, techTest, interview, inProgress);
    }

    private void updateGauge(int applied, int screening, int techTest, int interview, int inProgress) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 190 105\" width=\"100%\" height=\"100%\" class=\"applicant-gauge-svg\" style=\"overflow: visible;\">");

        // Panjang busur setengah lingkaran dengan radius 75: PI * 75 ~= 235.62
        double arcLength = Math.PI * 75.0;

        if (inProgress == 0) {
            // State kosong (0 active): satu busur abu-abu netral
            svg.append("<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"#E2E8F0\" stroke-width=\"11\" stroke-linecap=\"round\"/>");
        } else {
            // Track dasar abu-abu sangat muda
            svg.append("<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"#F1F5F9\" stroke-width=\"11\" stroke-linecap=\"round\"/>");

            // Hitung berapa tahap yang memiliki count > 0
            int nonZeroCount = 0;
            if (applied > 0) nonZeroCount++;
            if (screening > 0) nonZeroCount++;
            if (techTest > 0) nonZeroCount++;
            if (interview > 0) nonZeroCount++;

            // Jika lebih dari 1 tahap, buat celah 3.0px antar segmen
            double gap = (nonZeroCount > 1) ? 3.0 : 0.0;
            double totalGap = (nonZeroCount > 1) ? (nonZeroCount - 1) * gap : 0.0;
            double usableLength = arcLength - totalGap;

            boolean isSingleSegment = (nonZeroCount == 1);
            double currentOffset = 0.0;

            // 1. Applied (Biru #3B82F6)
            if (applied > 0) {
                double segLength = ((double) applied / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#3B82F6", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 2. Screening (Ungu #8B5CF6)
            if (screening > 0) {
                double segLength = ((double) screening / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#8B5CF6", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 3. Technical Test (Amber #F59E0B)
            if (techTest > 0) {
                double segLength = ((double) techTest / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#F59E0B", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }

            // 4. Interview (Cyan #06B6D4)
            if (interview > 0) {
                double segLength = ((double) interview / inProgress) * usableLength;
                svg.append(createGaugeSegmentPath("#06B6D4", currentOffset, segLength, isSingleSegment));
                currentOffset += segLength + gap;
            }
        }

        svg.append("</svg>");
        gaugeSvgContainer.getElement().setProperty("innerHTML", svg.toString());
    }

    private String createGaugeSegmentPath(String colorHex, double offset, double length, boolean isSingleSegment) {
        String linecap = isSingleSegment ? "round" : "butt";
        return String.format(
                Locale.US,
                "<path d=\"M 20 95 A 75 75 0 0 1 170 95\" fill=\"none\" stroke=\"%s\" stroke-width=\"11\" stroke-dasharray=\"0 %.2f %.2f 1000\" stroke-linecap=\"%s\"/>",
                colorHex,
                offset,
                length,
                linecap
        );
    }
}
