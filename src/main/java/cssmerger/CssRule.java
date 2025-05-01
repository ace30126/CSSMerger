package cssmerger;

import java.util.Objects;

// Simple class to represent a CSS rule
public class CssRule {
    private final String selector;
    private final String properties;
    private final String fullRule; // Store the original full text for display

    public CssRule(String selector, String properties, String fullRule) {
        this.selector = selector.trim();
        this.properties = properties.trim();
        this.fullRule = fullRule.trim();
    }

    public String getSelector() {
        return selector;
    }

    public String getProperties() {
        return properties;
    }

    public String getFullRule() {
        return fullRule;
    }

    @Override
    public String toString() {
        // This is what JList will display by default if no renderer is used
        return getFullRule();
    }

    // Equality based on selector for merging logic
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CssRule cssRule = (CssRule) o;
        return Objects.equals(selector, cssRule.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selector);
    }
}