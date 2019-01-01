package liwey.json2pojo;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.*;

import static liwey.json2pojo.ConfigUtil.config;

public class SettingsDialog extends JDialog {
  private JPanel contentPanel;
  private JButton buttonOK;
  private JButton buttonCancel;

  private JCheckBox dataCheckbox;
  private JCheckBox builderCheckbox;
  private JCheckBox noArgsConstructorCheckBox;
  private JCheckBox requiredArgsConstructorCheckBox;
  private JCheckBox allArgsConstructorCheckBox;
  private JCheckBox fluentCheckBox;

  private JComboBox<String> filedNameComboBox;
  private JCheckBox checkBoxPrimitive;
  private JCheckBox chainCheckBox;
  private JTextField prefixTextField;
  private JTextField warningsTextField;

  public SettingsDialog() {
    setContentPane(contentPanel);
    setModal(true);
    setResizable(false);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPanel.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
          JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    filedNameComboBox.addItem("[None]");
    filedNameComboBox.addItem("@SerializedName (gson)");
    filedNameComboBox.addItem("@JsonProperty (jackson)");

    bindConfig();
  }

  private void bindConfig() {
    setData(config);
    filedNameComboBox.setSelectedIndex(config.getFieldNameAnnotation());
  }

  private void onOK() {
    try {
      getData(config);
      config.setFieldNameAnnotation(filedNameComboBox.getSelectedIndex());
      ConfigUtil.save();
    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "Failed to load settings: " + e.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    dispose();
  }

  private void onCancel() {
    dispose();
  }

  public void setData(Config data) {
    noArgsConstructorCheckBox.setSelected(data.isLombokNoArgsConstructor());
    requiredArgsConstructorCheckBox.setSelected(data.isLombokRequiredArgsConstructor());
    allArgsConstructorCheckBox.setSelected(data.isLombokAllArgsConstructor());
    dataCheckbox.setSelected(data.isLombokData());
    builderCheckbox.setSelected(data.isLombokBuilder());
    fluentCheckBox.setSelected(data.isLombokAccessorsFluent());
    chainCheckBox.setSelected(data.isLombokAccessorsChain());
    prefixTextField.setText(data.getLombokAccessorsPrefix());
    warningsTextField.setText(data.getSuppressWarnings());
    checkBoxPrimitive.setSelected(data.isFieldTypePrimitive());
  }

  public void getData(Config data) {
    data.setLombokNoArgsConstructor(noArgsConstructorCheckBox.isSelected());
    data.setLombokRequiredArgsConstructor(requiredArgsConstructorCheckBox.isSelected());
    data.setLombokAllArgsConstructor(allArgsConstructorCheckBox.isSelected());
    data.setLombokData(dataCheckbox.isSelected());
    data.setLombokBuilder(builderCheckbox.isSelected());
    data.setLombokAccessorsFluent(fluentCheckBox.isSelected());
    data.setLombokAccessorsChain(chainCheckBox.isSelected());
    data.setLombokAccessorsPrefix(prefixTextField.getText());
    data.setSuppressWarnings(warningsTextField.getText());
    data.setFieldTypePrimitive(checkBoxPrimitive.isSelected());
  }
}
