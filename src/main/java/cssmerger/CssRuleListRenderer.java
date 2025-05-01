package cssmerger;

import javax.swing.*;
import java.awt.*;
import java.util.Collections; // For emptySet
import java.util.HashSet;
import java.util.Set;

public class CssRuleListRenderer extends DefaultListCellRenderer {

    // Store all info needed for highlighting logic
    private Set<String> finalSelectors = Collections.emptySet();
    private Set<String> sharedSelectors = Collections.emptySet();
    private boolean isMyPanelPrioritized = false;

    // Method to update all highlighting info at once
    public void setHighlightingInfo(Set<String> finalSelectors, Set<String> sharedSelectors, boolean isMyPanelPrioritized) {
        this.finalSelectors = (finalSelectors != null) ? finalSelectors : Collections.emptySet();
        this.sharedSelectors = (sharedSelectors != null) ? sharedSelectors : Collections.emptySet();
        this.isMyPanelPrioritized = isMyPanelPrioritized;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        // Let the default renderer configure the basics
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof CssRule) {
            CssRule rule = (CssRule) value;

            // --- Refined Highlighting Logic ---
            boolean ruleIsInFinal = finalSelectors.contains(rule.getSelector());
            boolean ruleIsShared = sharedSelectors.contains(rule.getSelector());
            boolean shouldHighlight = false;

            if (ruleIsInFinal) {
                if (ruleIsShared) {
                    // Highlight shared rule ONLY if this panel has priority
                    shouldHighlight = isMyPanelPrioritized;
                } else {
                    // Highlight unique rule if it's in the final set
                    shouldHighlight = true;
                }
            }
            // --- End Refined Logic ---

            if (shouldHighlight) {
                // Included in final output for THIS panel: Bold + Green
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                c.setForeground(isSelected ? Color.WHITE : new Color(0, 128, 0)); // Dark Green
            } else {
                 // Not included OR included but via the OTHER panel: Regular font
                 c.setFont(c.getFont().deriveFont(Font.PLAIN));
                 // Reset foreground to default (important if previously highlighted)
                 c.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            }
        } else {
             // Reset to default if not a CssRule
             c.setFont(c.getFont().deriveFont(Font.PLAIN));
             c.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        }

        // Keep default selection background handling
        if (!isSelected) {
            c.setBackground(list.getBackground());
        } else {
             c.setBackground(list.getSelectionBackground());
        }

        return c;
    }
}