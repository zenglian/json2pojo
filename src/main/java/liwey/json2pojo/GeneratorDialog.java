package liwey.json2pojo;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import static liwey.json2pojo.ConfigUtil.config;

/**
 * A custom dialog which allows the user to input a JSON text.
 */
public class GeneratorDialog extends JDialog {
  private static final String CLASS_NAME_REGEX = "[A-Za-z][A-Za-z0-9]*";

  private final String packageName;
  private final String destPath;

  // UI
  private JButton buttonCancel;
  private JButton buttonGenerate;
  private JTextField txtClassName;
  private JPanel contentPanel;
  private JButton buttonSettings;
  private JTextArea jsonTextArea;
  private JTextField resultTextField;

  GeneratorDialog(String packageName, String destPath) {
    this.packageName = packageName;
    this.destPath = destPath;

    // Set up the main content
    setContentPane(contentPanel);
    setModal(true);
    setTitle(R.get("title"));
    setLocation(config.getWindowX(), config.getWindowY());
    setSize(config.getWindowWidth(), config.getWindowHeight());
    getRootPane().setDefaultButton(buttonGenerate);

    // Set the minimum dialog size
    setMinimumSize(new Dimension(400, 400));

    // Add button listeners
    buttonGenerate.addActionListener(e -> onGenerate());
    buttonCancel.addActionListener(e -> onCancel());

    // Call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }

      @Override
      public void windowClosed(WindowEvent e) {
        saveDialogPosAndSize();
      }
    });

    // Call onCancel() on ESCAPE
    contentPanel.registerKeyboardAction(e -> onCancel(),
          KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
          JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    // Enable/disable OK button
    buttonGenerate.setEnabled(false);
    jsonTextArea.setBorder(BorderFactory.createCompoundBorder(null, BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    txtClassName.getDocument().addDocumentListener(new TextChangedListener());
    jsonTextArea.getDocument().addDocumentListener(new TextChangedListener());

    buttonSettings.addActionListener(e -> {
      SettingsDialog dialog = new SettingsDialog();
      dialog.setTitle(R.get("settings"));
      dialog.setLocationRelativeTo(this);
      dialog.pack();
      dialog.setVisible(true);
    });
  }

  private void onCancel() {
    dispose();
  }

  private void onGenerate() {
    new Thread(() -> {
      Generator generator = new Generator(packageName, destPath, resultTextField);
      try {
        int n = generator.generateFromJson(txtClassName.getText(), jsonTextArea.getText());
        resultTextField.setForeground(Color.blue);
        resultTextField.setText(R.get("generate.result", n, packageName));
        resultTextField.setToolTipText(R.get("success"));
        //dispose();
      } catch (Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null)
          cause = e.getCause();
        resultTextField.setForeground(Color.red);
        resultTextField.setText(cause.getMessage());
        StringWriter writer = new StringWriter();
        cause.printStackTrace(new PrintWriter(writer));
        resultTextField.setToolTipText("<html>" + writer.toString().replace("\n", "<br/>") + "</html>");
      }
    }).start();
  }

  private void createUIComponents() {
  }

  private void saveDialogPosAndSize() {
    config.setWindowWidth(this.getWidth());
    config.setWindowHeight(this.getHeight());
    config.setWindowX(this.getX());
    config.setWindowY(this.getY());
    try {
      ConfigUtil.save();
    } catch (IOException e) {
      e.printStackTrace();
    }
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
      buttonGenerate.setEnabled(txtClassName.getText().matches(CLASS_NAME_REGEX) && !jsonTextArea.getText().isEmpty());
    }
  }

  public static void main(String[] args) {
    ConfigUtil.setLocale();

    GeneratorDialog dialog = new GeneratorDialog("test", System.getProperty("user.dir") + "/src/test/java");
    dialog.setLocation(config.getWindowX(), config.getWindowY());
    dialog.setSize(config.getWindowWidth(), config.getWindowHeight());
    dialog.setVisible(true);
    System.exit(0);
  }
}
