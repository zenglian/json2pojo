package liwey.json2pojo;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * A custom dialog which allows the user to input a JSON text.
 */
public class JsonInputDialog extends JDialog {
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    /**
     * A listener to be invoked when the user has clicked the OK button.
     */
    interface OnOkListener {
        /**
         * A callback to be invoked when the user has clicked the OK button.
         *
         * @param className the class name entered into the dialog.
         * @param jsonText  the JSON text entered into the dialog.
         */
        void onOk(String className, String jsonText);
    }

    private static final String CLASS_NAME_REGEX = "[A-Za-z][A-Za-z0-9]*";

    // Data / State
    private OnOkListener okListener;

    // UI
    private JButton buttonCancel;
    private JButton buttonOK;
    private JTextField txtClassName;
    private JPanel contentPanel;
    private RSyntaxTextArea jsonTextArea;
    private JButton buttonSettings;

    JsonInputDialog(OnOkListener listener) {
        // Set the listener
        okListener = listener;

        // Set up the main content
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        // Set the minimum dialog size
        setMinimumSize(new Dimension(420, 200));

        // Add button listeners
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // Call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // Call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Enable/disable OK button
        buttonOK.setEnabled(false);
        txtClassName.getDocument().addDocumentListener(new TextChangedListener());
        jsonTextArea.getDocument().addDocumentListener(new TextChangedListener());

        // Set up syntax highlighting
        jsonTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonTextArea.setCodeFoldingEnabled(false);
        buttonSettings.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog();
            dialog.setTitle("Json2Pojo Settings");
            dialog.setLocationRelativeTo(this);
            dialog.pack();
            dialog.setVisible(true);
        });
    }

    private void onCancel() {
        dispose();
    }

    private void onOK() {
        okListener.onOk(txtClassName.getText(), jsonTextArea.getText());
        dispose();
    }

    /**
     * Gets called when the JSON text or root class text has changed.
     */
    private class TextChangedListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            validate();
        }

        /**
         * Validates the class name and JSON text and enables the OK button if validation passes.
         */
        private void validate() {
            String className = txtClassName.getText();
            String jsonText = jsonTextArea.getText();

            if (className.matches(CLASS_NAME_REGEX) && !jsonText.isEmpty()) {
                buttonOK.setEnabled(true);
            } else {
                buttonOK.setEnabled(false);
            }
        }
    }

    public static void main(String[] args) {
        JsonInputDialog dialog = new JsonInputDialog((className, jsonText) -> {

        });
        dialog.pack();
        dialog.setTitle("Test");
        dialog.setVisible(true);
        System.exit(0);
    }
}
