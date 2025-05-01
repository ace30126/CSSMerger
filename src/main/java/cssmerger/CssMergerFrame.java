package cssmerger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CssMergerFrame extends JFrame {

    private CssFilePanel leftPanel;
    private CssFilePanel rightPanel;
    private JButton saveButton;
    private JButton cancelButton;
    private ButtonGroup priorityGroup;

    public CssMergerFrame() {
        setTitle("CSSMerger");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        URL iconURL = getClass().getResource("/icon.png");
        if (iconURL != null) {
            ImageIcon frameIcon = new ImageIcon(iconURL);
            setIconImage(frameIcon.getImage()); // JFrame의 아이콘으로 설정
        } else {
            // 아이콘 로드 실패 시 에러 메시지 출력 (콘솔)
            System.err.println("Warning: Could not load frame icon 'icon.png' from classpath.");
        }

        initComponents();
        layoutComponents();

        // Initial call to set default state
        updateMergePreview();
    }

    private void initComponents() {
        priorityGroup = new ButtonGroup();

        // Callbacks now trigger updateMergePreview directly
        leftPanel = new CssFilePanel("CSS 파일 1", priorityGroup,
                panel -> updateMergePreview(),
                panel -> updateMergePreview());

        rightPanel = new CssFilePanel("CSS 파일 2", priorityGroup,
                panel -> updateMergePreview(),
                panel -> updateMergePreview());

        leftPanel.getPriorityCheckbox().setSelected(true); // Default priority

        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");

        saveButton.addActionListener(e -> saveMergedCss());
        cancelButton.addActionListener(e -> System.exit(0));
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- Merge Logic (Unchanged) ---
    private Map<String, CssRule> calculateMergedCss() {
        Map<String, CssRule> mergedRules = new LinkedHashMap<>();
        Map<String, CssRule> leftRules = leftPanel.getCssRules();
        Map<String, CssRule> rightRules = rightPanel.getCssRules();

        boolean leftPriority = leftPanel.getPriorityCheckbox().isSelected();
        boolean rightPriority = rightPanel.getPriorityCheckbox().isSelected();

        Map<String, CssRule> primaryRules = leftPriority ? leftRules : rightRules;
        Map<String, CssRule> secondaryRules = leftPriority ? rightRules : leftRules;

        // Add secondary first, then primary (overwrites duplicates)
        mergedRules.putAll(secondaryRules);
        mergedRules.putAll(primaryRules);

        // Handle case where neither is selected (shouldn't happen with ButtonGroup, but safe)
        if (!leftPriority && !rightPriority) {
            mergedRules.clear();
            mergedRules.putAll(leftRules);
            mergedRules.putAll(rightRules); // Default: right overwrites left
        }

        return mergedRules;
    }

    // --- Update Preview and Highlighting Logic (MODIFIED) ---
    private void updateMergePreview() {
        Map<String, CssRule> mergedRules = calculateMergedCss();
        Set<String> finalSelectors = mergedRules.keySet();

        // Get selectors from both panels to find shared ones
        Set<String> leftSelectors = leftPanel.getCssRules().keySet();
        Set<String> rightSelectors = rightPanel.getCssRules().keySet();

        // Calculate shared selectors
        Set<String> sharedSelectors;
        if (leftSelectors.isEmpty() || rightSelectors.isEmpty()) {
            sharedSelectors = Collections.emptySet(); // No overlap if one side is empty
        } else {
            sharedSelectors = new HashSet<>(leftSelectors);
            sharedSelectors.retainAll(rightSelectors); // Keep only elements present in both
        }

        // Get priority status for each panel
        boolean isLeftPrioritized = leftPanel.getPriorityCheckbox().isSelected();
        boolean isRightPrioritized = rightPanel.getPriorityCheckbox().isSelected();

        // Update highlighting in both panels, providing all necessary info
        leftPanel.updateHighlighting(finalSelectors, sharedSelectors, isLeftPrioritized);
        rightPanel.updateHighlighting(finalSelectors, sharedSelectors, isRightPrioritized);

        System.out.println("Merge preview updated. Final selectors: " + finalSelectors.size() + ", Shared: " + sharedSelectors.size());
    }
    // --- End Update Preview ---


    // --- Save Logic (Unchanged) ---
    private void saveMergedCss() {
        Map<String, CssRule> mergedRules = calculateMergedCss();
        if (mergedRules.isEmpty()) {
            JOptionPane.showMessageDialog(this, "병합할 CSS 규칙이 없습니다. 파일을 먼저 불러오세요.", "저장할 내용 없음", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("병합된 CSS 저장");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSS File (*.css)", "css"));
        fileChooser.setSelectedFile(new File("merged.css"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
             if (!fileToSave.getName().toLowerCase().endsWith(".css")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".css");
            }

            if (fileToSave.exists()) {
                int result = JOptionPane.showConfirmDialog(this,
                        "파일이 이미 존재합니다. 덮어쓰시겠습니까?\n" + fileToSave.getAbsolutePath(),
                        "덮어쓰기 확인", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write("/* CSS Merged by CSSMerger */\n\n");
                for (CssRule rule : mergedRules.values()) {
                    writer.write(rule.getFullRule());
                    writer.newLine();
                    writer.newLine();
                }
                 JOptionPane.showMessageDialog(this, "CSS 파일이 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(), "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "파일 저장 중 오류 발생:\n" + ex.getMessage(), "저장 오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void clearAll() { // Consider adding a button for this?
        leftPanel.clear();
        rightPanel.clear();
        priorityGroup.clearSelection();
        leftPanel.getPriorityCheckbox().setSelected(true); // Reset default priority
        updateMergePreview();
    }
}