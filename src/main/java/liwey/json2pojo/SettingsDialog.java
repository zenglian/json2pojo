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

    private JComboBox<String> filedNameComboBox;
    private JCheckBox checkBoxPrimitive;
    private JCheckBox chainCheckBox;
    private JTextField prefixTextField;

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
        accessorsCheckBox.addChangeListener(e -> updateAccessorsState());

        filedNameComboBox.addItem("None");
        filedNameComboBox.addItem("@SerializedName (gson)");
        filedNameComboBox.addItem("@JsonProperty (jackson)");

        bindConfig();
    }

    private void bindConfig() {
        try {
            config = ConfigUtil.load();
            setData(config);
            updateAccessorsState();
            filedNameComboBox.setSelectedIndex(config.getFieldNameAnnotation());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings, using default settings: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAccessorsState(){
        fluentCheckBox.setEnabled(accessorsCheckBox.isSelected());
        chainCheckBox.setEnabled(accessorsCheckBox.isSelected());
        prefixTextField.setEnabled(accessorsCheckBox.isSelected());
    }

    private void onOK() {
        try {
            getData(config);
            config.setFieldNameAnnotation(filedNameComboBox.getSelectedIndex());
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
        builderCheckbox.setSelected(data.isLombokBuilder());
        accessorsCheckBox.setSelected(data.isLombokAccessors());
        fluentCheckBox.setSelected(data.isLombokAccessorsFluent());
        chainCheckBox.setSelected(data.isLombokAccessorsChain());
        prefixTextField.setText(data.getLombokAccessorsPrefix());
        checkBoxPrimitive.setSelected(data.isPrimitive());
    }

    public void getData(Config data) {
        data.setLombokNoArgsConstructor(noArgsConstructorCheckBox.isSelected());
        data.setLombokRequiredArgsConstructor(requiredArgsConstructorCheckBox.isSelected());
        data.setLombokAllArgsConstructor(allArgsConstructorCheckBox.isSelected());
        data.setLombokData(dataCheckbox.isSelected());
        data.setLombokBuilder(builderCheckbox.isSelected());
        data.setLombokAccessors(accessorsCheckBox.isSelected());
        data.setLombokAccessorsFluent(fluentCheckBox.isSelected());
        data.setLombokAccessorsChain(chainCheckBox.isSelected());
        data.setLombokAccessorsPrefix(prefixTextField.getText());
        data.setPrimitive(checkBoxPrimitive.isSelected());
    }
}
