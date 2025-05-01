package cssmerger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
// Import DnD classes
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List; // Needed for DnD file list
import java.util.Map;
import java.util.Set; // Needed for highlighting logic update
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CssFilePanel extends JPanel {

    private JButton fileButton;
    private JCheckBox priorityCheckbox;
    private JList<CssRule> ruleList;
    private DefaultListModel<CssRule> listModel;
    private File currentFile;
    private Map<String, CssRule> cssRules = new LinkedHashMap<>(); // selector -> rule
    private Consumer<CssFilePanel> onFileLoadedCallback;
    private Consumer<CssFilePanel> onCheckboxChangedCallback;
    private JScrollPane scrollPane; // Make scrollPane a field to attach DropTarget

    // Simple regex: matches selector { properties } - handles basic nesting poorly
    private static final Pattern CSS_RULE_PATTERN = Pattern.compile("([^\\{]+)\\{([^\\}]+)\\}");

    public CssFilePanel(String title, ButtonGroup checkboxGroup, Consumer<CssFilePanel> onFileLoadedCallback, Consumer<CssFilePanel> onCheckboxChangedCallback) {
        this.onFileLoadedCallback = onFileLoadedCallback;
        this.onCheckboxChangedCallback = onCheckboxChangedCallback;

        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // --- Top Panel (File Name + Checkbox) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileButton = new JButton("파일 선택...");
        fileButton.setToolTipText("CSS 파일을 선택하거나 여기에 드래그 앤 드롭하세요.");
        priorityCheckbox = new JCheckBox("이 CSS 우선 적용");
        priorityCheckbox.setToolTipText("중복되는 선택자가 있을 경우 이 파일의 규칙을 사용합니다.");

        checkboxGroup.add(priorityCheckbox);

        fileButton.addActionListener(e -> chooseFile());
        priorityCheckbox.addActionListener(e -> {
            if (onCheckboxChangedCallback != null) {
                onCheckboxChangedCallback.accept(this);
            }
        });

        topPanel.add(fileButton);
        topPanel.add(priorityCheckbox);

        // --- Center Panel (Rule List) ---
        listModel = new DefaultListModel<>();
        ruleList = new JList<>(listModel);
        ruleList.setCellRenderer(new CssRuleListRenderer()); // Renderer initialization moved here
        ruleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane = new JScrollPane(ruleList); // Assign to field
        scrollPane.setBorder(new TitledBorder(title));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- Drag and Drop Setup ---
        configureDragAndDrop(scrollPane); // Apply DND to scrollpane/list area
        configureDragAndDrop(fileButton); // Also allow dropping on button
    }

    // --- Drag and Drop Handler ---
    private void configureDragAndDrop(Component component) {
         // Create the DropTarget listener
         DropTargetAdapter dropTargetListener = new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                boolean success = false;
                try {
                    // Check if the dropped data is a file list
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY); // Accept the drop

                        // Get the list of dropped files
                        @SuppressWarnings("unchecked") // Required cast for getTransferData
                        List<File> droppedFiles = (List<File>) dtde.getTransferable()
                                .getTransferData(DataFlavor.javaFileListFlavor);

                        if (droppedFiles != null && !droppedFiles.isEmpty()) {
                            // Process the first dropped file
                            File droppedFile = droppedFiles.get(0);
                            if (droppedFile.getName().toLowerCase().endsWith(".css")) {
                                loadFile(droppedFile); // Load the valid CSS file
                                success = true;
                            } else {
                                JOptionPane.showMessageDialog(CssFilePanel.this, // Parent component
                                        "CSS 파일만 드롭할 수 있습니다 (.css 확장자 필요).\n파일: " + droppedFile.getName(),
                                        "잘못된 파일 형식", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    } else {
                         dtde.rejectDrop(); // Reject other data types
                    }
                } catch (Exception ex) {
                     JOptionPane.showMessageDialog(CssFilePanel.this,
                            "파일을 드롭하는 중 오류 발생:\n" + ex.getMessage(),
                            "드롭 오류", JOptionPane.ERROR_MESSAGE);
                     ex.printStackTrace(); // Log error for debugging
                } finally {
                     // Signal drop completion status
                    dtde.dropComplete(success);
                }
            }
        };

         // Create DropTarget and associate it with the component
         new DropTarget(component, DnDConstants.ACTION_COPY, dropTargetListener, true);
    }
    // --- End Drag and Drop ---

    private void chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSS Files (*.css)", "css"));
        fileChooser.setDialogTitle("CSS 파일 선택");
        if (currentFile != null) {
            fileChooser.setCurrentDirectory(currentFile.getParentFile());
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            loadFile(fileChooser.getSelectedFile());
        }
    }

    public void loadFile(File file) {
        if (file == null || !file.exists() || !file.getName().toLowerCase().endsWith(".css")) {
             JOptionPane.showMessageDialog(this, "유효한 CSS 파일을 선택해주세요.", "파일 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.currentFile = file;
        fileButton.setText(file.getName());
        fileButton.setToolTipText(file.getAbsolutePath());

        parseCssFile(file);
        updateListModel();

        if (onFileLoadedCallback != null) {
            onFileLoadedCallback.accept(this);
        }
    }

    private void parseCssFile(File file) {
        cssRules.clear();
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
             JOptionPane.showMessageDialog(this, "파일 읽기 오류:\n" + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        String cssContent = content.toString().replaceAll("/\\*.*?\\*/", "");
        Matcher matcher = CSS_RULE_PATTERN.matcher(cssContent);
        while (matcher.find()) {
            String selector = matcher.group(1).trim();
            String properties = matcher.group(2).trim();
            String fullRule = matcher.group(0).trim();

            String[] individualSelectors = selector.split(",");
            for(String sel : individualSelectors) {
                 String trimmedSel = sel.trim();
                 if (!trimmedSel.isEmpty()) {
                     cssRules.put(trimmedSel, new CssRule(trimmedSel, properties, selector + " { " + properties + " }"));
                 }
            }
        }
        System.out.println("Parsed " + cssRules.size() + " rules from " + file.getName());
    }


    private void updateListModel() {
        listModel.clear();
        cssRules.values().forEach(listModel::addElement); // More concise way
    }

    public Map<String, CssRule> getCssRules() {
        return cssRules;
    }

    public File getCurrentFile() {
        return currentFile;
    }

     public JCheckBox getPriorityCheckbox() {
        return priorityCheckbox;
    }

     public JList<CssRule> getRuleList() {
        return ruleList;
    }

    public DefaultListModel<CssRule> getListModel() {
        return listModel;
    }

    // --- Updated Highlighting Method ---
    // Receives info needed by the renderer to make highlighting decisions
    public void updateHighlighting(Set<String> finalSelectors, Set<String> sharedSelectors, boolean isMyPanelPrioritized) {
         if (ruleList.getCellRenderer() instanceof CssRuleListRenderer) {
            CssRuleListRenderer renderer = (CssRuleListRenderer) ruleList.getCellRenderer();
            // Pass all necessary information to the renderer
            renderer.setHighlightingInfo(finalSelectors, sharedSelectors, isMyPanelPrioritized);
            ruleList.repaint(); // Trigger repaint to apply rendering changes
        }
    }
    // --- End Updated Highlighting ---

     public void clear() {
        currentFile = null;
        fileButton.setText("파일 선택...");
        fileButton.setToolTipText("CSS 파일을 선택하거나 여기에 드래그 앤 드롭하세요.");
        priorityCheckbox.setSelected(false);
        cssRules.clear();
        listModel.clear();
        if (ruleList.getCellRenderer() instanceof CssRuleListRenderer) {
             // Reset renderer state when clearing
             ((CssRuleListRenderer) ruleList.getCellRenderer()).setHighlightingInfo(null, null, false);
        }
     }
}