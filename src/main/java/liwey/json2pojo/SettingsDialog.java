package liwey.json2pojo;

import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
    private JCheckBox serializedNameCheckbox;
    private JCheckBox fluentCheckBox;

    private JRadioButton noneRadioButton;
    private JRadioButton gsonRadioButton;
    private JRadioButton jacksonRadioButton;

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

        bindConfig();
        accessorsCheckBox.addChangeListener(e -> fluentCheckBox.setEnabled(accessorsCheckBox.isSelected()));
    }

    private void bindConfig() {
        try {
            config = ConfigUtil.load();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings, using default settings: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        dataCheckbox.setSelected(config.lombokData());
        builderCheckbox.setSelected(config.lombokBuilder());
        accessorsCheckBox.setSelected(config.lombokAccessors());
        noArgsConstructorCheckBox.setSelected(config.lombokNoArgsConstructor());
        requiredArgsConstructorCheckBox.setSelected(config.lombokRequiredArgsConstructor());
        allArgsConstructorCheckBox.setSelected(config.lombokAllArgsConstructor());
        fluentCheckBox.setEnabled(config.lombokAccessors());
        fluentCheckBox.setSelected(config.lombokAccessorsFluent());
        if (config.namePolicy() == Config.NamePolicy.JACKSON)
            jacksonRadioButton.setSelected(true);
        else if (config.namePolicy() == Config.NamePolicy.GSON)
            gsonRadioButton.setSelected(true);
        else
            noneRadioButton.setSelected(true);
    }

    private void onOK() {
        if (jacksonRadioButton.isSelected())
            config.namePolicy(Config.NamePolicy.JACKSON);
        else if (gsonRadioButton.isSelected())
            config.namePolicy(Config.NamePolicy.GSON);
        else
            config.namePolicy(Config.NamePolicy.NONE);
        config.lombokData(dataCheckbox.isSelected());
        config.lombokBuilder(builderCheckbox.isSelected());
        config.lombokAccessors(accessorsCheckBox.isSelected());
        config.lombokAccessorsFluent(fluentCheckBox.isSelected());
        config.lombokNoArgsConstructor(noArgsConstructorCheckBox.isSelected());
        config.lombokRequiredArgsConstructor(requiredArgsConstructorCheckBox.isSelected());
        config.lombokAllArgsConstructor(allArgsConstructorCheckBox.isSelected());

        try {
            ConfigUtil.save(config);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load settings: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
