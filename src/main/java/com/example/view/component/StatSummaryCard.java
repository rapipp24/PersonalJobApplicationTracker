package com.example.view.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.H2;


public class StatSummaryCard extends VerticalLayout {
    private H2 value = new H2("0");

    public StatSummaryCard(String label) {
        Span title = new Span(label);

        add(
            title,
            value
            );
    }

        public void setValue(long value) {
        this.value.setText(String.valueOf(value));
    }
}
