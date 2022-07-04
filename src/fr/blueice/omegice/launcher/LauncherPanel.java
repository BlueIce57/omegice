package fr.blueice.omegice.launcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import javax.swing.*;

import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.util.Saver;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.event.SwingerEvent;
import fr.theshark34.swinger.event.SwingerEventListener;
import fr.theshark34.swinger.textured.STexturedButton;

@SuppressWarnings("serial")
public class LauncherPanel extends JPanel implements SwingerEventListener {
	
	private Image background = Swinger.getResource("OI.png");
	
	private Saver saver = new Saver(new File(Launcher.OI_DIR, "launcher.properties"));
	
	private JTextField usernameField = new JTextField(this.saver.get("username"));
	private JPasswordField 	passwordField = new JPasswordField();
	
	private STexturedButton playButton = new STexturedButton(Swinger.getResource("Play.png"));
	private STexturedButton quitButton = new STexturedButton(Swinger.getResource("quit.png"));
	private STexturedButton hideButton = new STexturedButton(Swinger.getResource("hide.png"));

	private SColoredBar  progressBar = new SColoredBar(new Color(0, 255, 255, 100), new Color(0, 0, 198, 175));
	private JLabel infoLabel = new JLabel("Clique sur Jouer !", SwingConstants.CENTER);
		
	public LauncherPanel() {
		this.setLayout(null);
		
		passwordField.setForeground(Color.WHITE);
		passwordField.setFont(usernameField.getFont().deriveFont(20F));
		passwordField.setCaretColor(Color.WHITE);
		passwordField.setOpaque(false);
		passwordField.setBorder(null);
		passwordField.setBounds(195, 445, 540, 33);
		this.add(passwordField);
		
		usernameField.setForeground(Color.WHITE);
		usernameField.setFont(usernameField.getFont().deriveFont(20F));
		usernameField.setCaretColor(Color.WHITE);
		usernameField.setOpaque(false);
		usernameField.setBorder(null);
		usernameField.setBounds(195, 347, 540, 33);
		this.add(usernameField);
		
		playButton.setBounds(415, 500);
		playButton.addEventListener(this);
		this.add(playButton);
		
		quitButton.setBounds(923, 18);
		quitButton.addEventListener(this);
		this.add(quitButton);
		
		hideButton.setBounds(880, 18);
		hideButton.addEventListener(this);
		this.add(hideButton);

		progressBar.setBounds(12, 620, 951, 5);
		this.add(progressBar);

		infoLabel.setForeground(Color.WHITE);
		infoLabel.setFont(usernameField.getFont());
		infoLabel.setBounds(20, 590, 951, 25);
		this.add(infoLabel);
	}
		
	@SuppressWarnings("deprecation")
	@Override
	public void onEvent (SwingerEvent e) {
		if(e.getSource() == playButton)  {
			setFieldsEnabled(false);
			
		
		if(usernameField.getText().replaceAll(" ", "").length() == 0 || passwordField.getText().length() == 0) {
			JOptionPane.showMessageDialog(this, "Erreur, veuillez entrer un pseudo et un mot de passe valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
			setFieldsEnabled(true);
			return;
		}
		
		Thread t =new Thread() {
			
			@Override
			public void run() {
				try {
				    Launcher.authMicrosoft(usernameField.getText(), passwordField.getText());
				} catch (MicrosoftAuthenticationException e) {
				    JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, Impossible de se connecter à Microsoft: " + e, "Erreur", JOptionPane.ERROR_MESSAGE);
				    setFieldsEnabled(true);
				    return;
				}
			
				saver.set("username", usernameField.getText());
				
				try {
					Launcher.update();
				} catch (Exception e) {
					Launcher.interruptThread();
					JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, Impossible de mettre le jeu a jour : " + e, "Erreur", JOptionPane.ERROR_MESSAGE);
					setFieldsEnabled(true);
					return;
				}
				
				try {
					Launcher.launch();
				} catch (LaunchException e) {
					JOptionPane.showMessageDialog(LauncherPanel.this, "Erreur, Impossible de lancer le jeu : " + e, "Erreur", JOptionPane.ERROR_MESSAGE);
					setFieldsEnabled(true);
				}
			}
		};
		t.start();
		} else if (e.getSource() == quitButton) 
			System.exit(0);
		else if(e.getSource() == hideButton)
			LauncherFrame.getInstance().setState(JFrame.ICONIFIED);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), this);

	}
	
	private void setFieldsEnabled(boolean enabled) {
		usernameField.setEnabled(enabled);
		passwordField.setEnabled(enabled);
		playButton.setEnabled(enabled);
	}
	
	public SColoredBar getProgressBar( ) {
		return progressBar;
	}
	
	public void setInfoText(String text) {
		infoLabel.setText(text);
	}
	
}