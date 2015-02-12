package client.gui;

import client.controller.DataValidation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Dialog pre pripojenie k databaze
 * @author Tomas Kubovcik <xkubov02@stud.fit.vutbr.cz>
 */
public class LogInDialog extends JDialog implements ActionListener, FocusListener
{
    private static final int 	WIDTH = 350;
    private static final int 	HEIGHT = 150;
    private static final String	dialogTitle = "Please insert your account information";
    
    private static JTextField 		login_tfield;
    private static JPasswordField	pwd_pfield;
    private static JLabel			login_label;
    private static JLabel			pwd_label;
    private static JLabel			wrong_login_label;
    private static JButton			login_btn;
    private static JButton			cancel_btn;
    
    private boolean 		        wrong_login_flag = false;
    
    //constructors
    public LogInDialog(JFrame parent)
    {
        //set size
        setPreferredSize(new Dimension(WIDTH,HEIGHT));
        setLocationRelativeTo(parent);

        //set dialog's title
        this.setTitle(dialogTitle);

        //create new panel
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
        
        cs.fill = GridBagConstraints.HORIZONTAL;
        
        //set username label position
        login_label = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(login_label, cs);
        
        //set username textfield position
        login_tfield = new JTextField("xvesel60", 20);
        //login_tfield = new JTextField("xkolac11", 20);
        //login_tfield = new JTextField("xkubov02", 20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(login_tfield, cs);
        
        //set password label position
        pwd_label = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(pwd_label, cs);

        //set passwrod textfield position

        //pwd_pfield = new JPasswordField("4g9532aq", 20);
        pwd_pfield = new JPasswordField("khprgui6", 20);
        //pwd_pfield = new JPasswordField("hgnwkqgg", 20);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(pwd_pfield, cs);
              
        //create buttons and their click listeners
        login_btn = new JButton("Login");
        cancel_btn = new JButton("Cancel");

        login_btn.addActionListener(this);
        cancel_btn.addActionListener(this);

        //create focus listeners for textfields to remove on focus
        login_tfield.addFocusListener(this);
        pwd_pfield.addFocusListener(this);
        
        //create panel for warning message
        JPanel lp = new JPanel();
        
        //create label and attach it to panel
        wrong_login_label = new JLabel("Incorrect username or password format!");
        cs.gridx = 0;
        cs.gridy = 0;
		cs.gridwidth = 1;
        lp.add(wrong_login_label);

        wrong_login_label.setForeground(Color.RED);
        wrong_login_label.setVisible(false);
        
        JPanel bp = new JPanel();
        bp.add(login_btn);
        bp.add(cancel_btn);
        
        getContentPane().add(lp, BorderLayout.PAGE_START);
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        getRootPane().setDefaultButton(login_btn);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * Getter for username input in textfield
     * @return username
     */
    protected String getStringFromTextField(){
    	return login_tfield.getText();
    }

    /**
     * Getter for password input in pwdtextfield
     * @return password
     */
    protected String getStringFromPwdField()
    {
        return new String(pwd_pfield.getPassword());
    }

    //Action handlers for clicks
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        //Login button click action
    	if(actionEvent.getSource() == login_btn)
        {
    		String user = getStringFromTextField();
    		String pwd = getStringFromPwdField();


			if(	DataValidation.validateUsername(user) &&
				DataValidation.validatePassword(pwd))
            {
				wrong_login_flag = false;
				this.setVisible(false);
				dispose();
			}
			else
            {
				wrong_login_flag = true;
				wrong_login_label.setVisible(true);
			}
    	}

    	//Cancel button click action
    	else if(actionEvent.getSource() == cancel_btn)
        {
			wrong_login_flag = false;
			setVisible(false);
			dispose();
    	}
    }

    //Action handlers for focuses
    @Override
    public void focusGained(FocusEvent focusEvent)
    {
        //Username & password field focus action
        if(	focusEvent.getSource() == login_tfield || 
			focusEvent.getSource() == pwd_pfield){

			if(wrong_login_flag){
				wrong_login_label.setVisible(false);
				
				//login_tfield.setText("");
				pwd_pfield.setText("");    

				wrong_login_flag = false;
			}
		}
    }

    //Action handlers for focus lost
    @Override
    public void focusLost(FocusEvent focusEvent)
    {
        // TODO Implement this method
    }

    /**
     * Returns login and password to parent 
     * @return (username, password);
     */
    public String[] getLoginInfo()
    {
    	setVisible(true);

    	String[] login_info = new String[2];
    	login_info[0] = getStringFromTextField();
    	login_info[1] = getStringFromPwdField();

    	return login_info;
    }
}
