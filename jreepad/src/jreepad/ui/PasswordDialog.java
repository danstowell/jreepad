package jreepad.ui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class PasswordDialog extends JDialog implements PropertyChangeListener
{
    private JOptionPane optionPane;
    private JPasswordField pwdField1;
    private JPasswordField pwdField2;

    private String password = null;

    public PasswordDialog(String message, boolean confirm)
    {
        super((Frame)null, "Enter password", true);

        pwdField1 = new JPasswordField();
        Object[] array;
        if (confirm)
        {
            pwdField2 = new JPasswordField();
            array = new Object[] { new JLabel(message), pwdField1, new JLabel("confirm:"), pwdField2 };
        }
        else
        {
            array = new Object[] { new JLabel(message), pwdField1 };
        }

        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        optionPane.addPropertyChangeListener(this);
        setContentPane(optionPane);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
            }
        });
    }

    /**
     * Dialog button was pressed.
     */
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();

        if (isVisible() && JOptionPane.VALUE_PROPERTY.equals(prop))
        {
            Object value = optionPane.getValue();
            if (value == JOptionPane.UNINITIALIZED_VALUE)
                return;
            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            if (value.equals(Integer.valueOf(JOptionPane.OK_OPTION)))
            {
                String pwd1 = new String(pwdField1.getPassword());
                if (pwdField2 != null)
                {
                    String pwd2 = new String(pwdField2.getPassword());
                    if (!pwd1.equals(pwd2))
                    {
                        JOptionPane.showMessageDialog(this, "The passwords do not match");
                        return;
                    }
                }
                password = new String(pwdField1.getPassword());
            }
            setVisible(false);
        }
    }

    /**
     * Returns entered password or null if Cancel was pressed.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Shows password dialog.
     * @param message  message to show in the dialog (e.g. "Please enter password:")
     * @param confirm  whether to show second confirmation password field
     * @return  enetered password or null if Cancel was pressed
     */
    public static String showPasswordDialog(String message, boolean confirm)
    {
        PasswordDialog dialog = new PasswordDialog(message, confirm);
        dialog.setVisible(true);
        dialog.dispose(); // We don't need the dialog anymore
        return dialog.getPassword();
    }

    public static String showPasswordDialog(String message)
    {
        return showPasswordDialog(message, false);
    }

    public static String showPasswordDialog(boolean confirm)
    {
        return showPasswordDialog("Please enter password:", confirm);
    }

    public static String showPasswordDialog()
    {
        return showPasswordDialog(false);
    }
}
