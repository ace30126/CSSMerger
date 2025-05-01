package cssmerger;

import javax.swing.*;

public class CssMergerApp {

    public static void main(String[] args) {
        // Set Look and Feel (Optional, makes it look more modern)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel.");
        }

        // Ensure UI creation happens on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            CssMergerFrame frame = new CssMergerFrame();
            frame.setVisible(true);
        });
    }
}