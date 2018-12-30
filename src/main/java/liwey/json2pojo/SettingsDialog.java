package liwey.json2pojo;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.*;

public class SettingsDialog extends JDialog {
    private Config config = new Config();

    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JCheckBox dataCheckbox;
    private JCheckBox builderCheckbox;
    private JCheckBox accessorsCheckBox;
    private JCheckBox noArgsConstructorCheckBox;
    private JCheckBox requiredArgsConstructorCheckBox;
    private JCheckBox allArgsConstructorCheckBox;
    private JCheckBox fluentCheckBox;

    private JComboBox<String> comboBoxFieldName;
    private JCheckBox checkBoxPrimitive;

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

        comboBoxFieldName.addItem("None");
        comboBoxFieldName.addItem("@SerializedName (gson)");
        comboBoxFieldName.addItem("@JsonProperty (jackson)");
        fluentCheckBox.setEnabled(accessorsCheckBox.isSelected());

        bindConfig();
        accessorsCheckBox.addChangeListener(e -> fluentCheckBox.setEnabled(accessorsCheckBox.isSelected()));
    }

    private void bindConfig() {
        try {
            config = ConfigUtil.load();
            setData(config);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings, using default settings: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onOK() {
        try {
            getData(config);
            ConfigUtil.save(config);
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
        accessorsCheckBox.setSelected(data.isLombokAccessors());
        fluentCheckBox.setSelected(data.isLombokAccessorsFluent());
        builderCheckbox.setSelected(data.isLombokBuilder());
        checkBoxPrimitive.setSelected(data.isPrimitive());
        comboBoxFieldName.setSelectedIndex(data.getFieldNameAnnotation());
    }

    public void getData(Config data) {
        data.setLombokNoArgsConstructor(noArgsConstructorCheckBox.isSelected());
        data.setLombokRequiredArgsConstructor(requiredArgsConstructorCheckBox.isSelected());
        data.setLombokAllArgsConstructor(allArgsConstructorCheckBox.isSelected());
        data.setLombokData(dataCheckbox.isSelected());
        data.setLombokAccessors(accessorsCheckBox.isSelected());
        data.setLombokAccessorsFluent(fluentCheckBox.isSelected());
        data.setLombokBuilder(builderCheckbox.isSelected());
        data.setPrimitive(checkBoxPrimitive.isSelected());
        data.setFieldNameAnnotation(comboBoxFieldName.getSelectedIndex());
    }

    public boolean isModified(Config data) {
        if (noArgsConstructorCheckBox.isSelected() != data.isLombokNoArgsConstructor()) return true;
        if (requiredArgsConstructorCheckBox.isSelected() != data.isLombokRequiredArgsConstructor()) return true;
        if (allArgsConstructorCheckBox.isSelected() != data.isLombokAllArgsConstructor()) return true;
        if (dataCheckbox.isSelected() != data.isLombokData()) return true;
        if (accessorsCheckBox.isSelected() != data.isLombokAccessors()) return true;
        if (fluentCheckBox.isSelected() != data.isLombokAccessorsFluent()) return true;
        if (builderCheckbox.isSelected() != data.isLombokBuilder()) return true;
        if (checkBoxPrimitive.isSelected() != data.isPrimitive()) return true;
        if (comboBoxFieldName.getSelectedIndex() != data.getFieldNameAnnotation()) return true;
        return false;
    }
}
