package com.SEGroup.UI.Views;

import com.vaadin.flow.component.Component;

/**
 * Interface for admin section views.
 * Each admin functionality should implement this interface to ensure consistent behavior.
 */
public interface AdminSection {
    /**
     * Get the title of this admin section.
     * @return The section title
     */
    String getTitle();

    /**
     * Get the main component for this admin section.
     * @return The main component to display
     */
    Component getContent();

    /**
     * Called when the section is activated.
     * Use this to refresh data or perform initialization.
     */
    void onActivate();

    /**
     * Called when the section is deactivated.
     * Use this to clean up resources or save state.
     */
    void onDeactivate();
}