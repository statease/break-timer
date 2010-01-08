import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class BreakTimer extends JPanel implements ActionListener {

	private Timer timer;
	private int currentSeconds, currentMinutes, startSeconds, startMinutes, buttonSize;
	private String timeString, buttonFont, player, switches;
	private File nextFile;
	private JButton toggleButton;
	private JButton stopButton;
	private JLabel timeLabel;

	private static final int DEFAULT_MINUTES = 15;
	private static final int DEFAULT_SECONDS = 0;

	public BreakTimer() {
		
		buttonSize = 370;
		startSeconds = DEFAULT_SECONDS;
		startMinutes = DEFAULT_MINUTES;
		nextFile = new File("ringinx3.wav");
		
		try {
			readConfig();
		} catch(FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, "Can't read config.ini", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch(IOException ioe) {
			JOptionPane.showMessageDialog(null, "Can't read config.ini", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch(NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "config.ini malformed", "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		
		File playerFile = new File(player);
		
		if(!playerFile.exists()) {
			JOptionPane.showMessageDialog(null, "The player you have selected does not exist!\nPlease choose another.", "Player Not Available", JOptionPane.ERROR_MESSAGE);
			
			setNewPlayer();
		}

		currentSeconds = startSeconds;
		currentMinutes = startMinutes;
				
		JPanel timerPanel = new JPanel();
	
		toggleButton = new JButton(timeString);
		toggleButton.setActionCommand("toggle");
		toggleButton.addActionListener(this);
		toggleButton.setFont(new Font(buttonFont, Font.BOLD, buttonSize));
		
		updateTimeLabel();
		
		timerPanel.add(toggleButton);

		add(timerPanel, BorderLayout.PAGE_START);
		
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				if(currentMinutes == 0 && currentSeconds == 0) {
					
					updateTimeLabel();
					//using wav.exe for now...
					try {
						java.lang.Runtime.getRuntime().exec(player + " \"" + nextFile.getAbsolutePath() + "\" " + switches);
						nextFile = getNextFile(nextFile);
						try {
							writeConfig();
						} catch(IOException ioe) {
							JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
						}
					} catch(IOException ioe) {}
					
					toggleButton.setForeground(new Color(0xff0000));
					timer.stop();
				} else if(currentMinutes > 0 && currentSeconds == 0) {
					
					currentMinutes--;	
					currentSeconds = 59;
				} else {
					
					currentSeconds--;
				}
				
				if(timer.isRunning()) {
					toggleButton.setForeground(new Color(0x000000));
					updateTimeLabel();
				}
			}
		});
	}
	
	private File getNextFile(File currentFile) {
		
		File[] fileList = currentFile.getParentFile().listFiles();
		for(int i = 0; i < (fileList.length - 1); i++) 
			if(fileList[i].getName().equals(currentFile.getName()))
					return fileList[i + 1];			
		return fileList[0];
	}
	
	private void readConfig() throws FileNotFoundException, IOException, NumberFormatException {
		
		BufferedReader inputFileReader;
		inputFileReader = new BufferedReader(new FileReader("./config.ini"));	
	
		startMinutes = Integer.parseInt(inputFileReader.readLine());
		startSeconds = Integer.parseInt(inputFileReader.readLine());
		buttonSize = Integer.parseInt(inputFileReader.readLine());	
		buttonFont = inputFileReader.readLine();
		nextFile = new File(inputFileReader.readLine());
		player = inputFileReader.readLine();
		switches = inputFileReader.readLine();
	}
	
	private void writeConfig() throws IOException {
		
		File configFile = new File("config.ini");
		configFile.delete();
		configFile.createNewFile();
		FileWriter out = new FileWriter(configFile);
		
		out.write("" + startMinutes + "\n");
		out.write("" + startSeconds + "\n");
		out.write("" + buttonSize + "\n");
		out.write("" + buttonFont + "\n");
		out.write("" + nextFile.getAbsolutePath() + "\n");
		out.write(player + "\n");
		out.write(switches + "\n");
				
		out.close();				
	}
	
	private void updateTimeLabel() {
		if(currentMinutes < 10)
			timeString = "0" + currentMinutes;
		else
			timeString = "" + currentMinutes;
		timeString += ":";
		if(currentSeconds < 10)
			timeString += "0" + currentSeconds;
		else
			timeString += currentSeconds;
							
		toggleButton.setText(timeString);
		toggleButton.setForeground(new Color(0x000000));
	}
	
	public JMenuBar buildTimerMenu() {
		
		JMenuBar timerMenuBar;
		JMenu commandsMenu, settingsMenu;
		JMenuItem restart, start, stop, setMinutes, setSeconds, setFile, setPlayer, setFlags, saveSettings;
		
		timerMenuBar = new JMenuBar();
		
		commandsMenu = new JMenu("Commands");
		commandsMenu.setMnemonic(KeyEvent.VK_C);
		commandsMenu.getAccessibleContext().setAccessibleDescription(
                "Timer commands.");                
		timerMenuBar.add(commandsMenu);
		
		settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		settingsMenu.getAccessibleContext().setAccessibleDescription(
                "Timer settings.");                
		timerMenuBar.add(settingsMenu);
		
		start = new JMenuItem("Start", KeyEvent.VK_S);
		start.getAccessibleContext().setAccessibleDescription(
                "Starts the timer.");
		start.setActionCommand("start");
		start.addActionListener(this);
      commandsMenu.add(start);
      
      stop = new JMenuItem("Stop", KeyEvent.VK_T);
		stop.getAccessibleContext().setAccessibleDescription(
                "Stops the timer.");
		stop.setActionCommand("stop");
		stop.addActionListener(this);
      commandsMenu.add(stop);
		
		restart = new JMenuItem("Restart", KeyEvent.VK_R);
		restart.getAccessibleContext().setAccessibleDescription(
                "Restarts the timer.");
		restart.setActionCommand("restart");
		restart.addActionListener(this);
      commandsMenu.add(restart);
      
      setMinutes = new JMenuItem("Minutes...", KeyEvent.VK_M);
		setMinutes.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the minutes for the timer.");
		setMinutes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				int input = currentMinutes;
				String sinput = (String) JOptionPane.showInputDialog(null, "Enter minutes", "Minutes", JOptionPane.PLAIN_MESSAGE, null, null, "" + currentMinutes);
				
				if(sinput != null) {
					try  {
						input = Integer.parseInt(sinput);
					} catch (NumberFormatException nfe) {
						
						JOptionPane.showMessageDialog(null, "Not an integer", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					if(input > 99)
						input = 99;
						
					startMinutes = currentMinutes = input;
					
					updateTimeLabel();
				}
			}			
		});
      settingsMenu.add(setMinutes);
      
      setSeconds = new JMenuItem("Seconds...", KeyEvent.VK_C);
		setSeconds.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the seconds for the timer.");
		setSeconds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				int input = currentSeconds;
				String sinput = (String) JOptionPane.showInputDialog(null, "Enter seconds", "Seconds", JOptionPane.PLAIN_MESSAGE, null, null, "" + startSeconds);
				
				if(sinput != null) {
					try  {
						input = Integer.parseInt(sinput);
					} catch (NumberFormatException nfe) {
						
						JOptionPane.showMessageDialog(null, "Not an integer", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					if(input > 59)
						input = 59;
						
					startSeconds = currentSeconds = input;
					
					updateTimeLabel();
				}
			}			
		});
      settingsMenu.add(setSeconds);
      
      setSeconds = new JMenuItem("Size...", KeyEvent.VK_Z);
		setSeconds.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the seconds for the timer.");
		setSeconds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				int input = currentSeconds;
				String sinput = (String) JOptionPane.showInputDialog(null, "Enter font size", "Size", JOptionPane.PLAIN_MESSAGE, null, null, "" + buttonSize);
				
				if(sinput != null) {
					try  {
						input = Integer.parseInt(sinput);
					} catch (NumberFormatException nfe) {
						
						JOptionPane.showMessageDialog(null, "Not an integer", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					if(input < 10)
						input = 10;
						
					buttonSize = input;
					
					try {
						writeConfig();
					} catch(IOException ioe) {
						JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}			
		});
      settingsMenu.add(setSeconds);
      
      setSeconds = new JMenuItem("Font...", KeyEvent.VK_N);
		setSeconds.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the seconds for the timer.");
		setSeconds.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				int input = currentSeconds;
				String sinput = (String) JOptionPane.showInputDialog(null, "Enter font name", "Font", JOptionPane.PLAIN_MESSAGE, null, null, buttonFont);
				
				if(sinput != null) {
					
					buttonFont = sinput;
					
					try {
						writeConfig();
					} catch(IOException ioe) {
						JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}			
		});
      settingsMenu.add(setSeconds);
      
      setFile = new JMenuItem("Current Media File...", KeyEvent.VK_W);
		setFile.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the file played on finish.");
		setFile.setActionCommand("browse");
		setFile.addActionListener(this);
      settingsMenu.add(setFile);
      
      setPlayer = new JMenuItem("Media Player...", KeyEvent.VK_P);
		setPlayer.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the media player.");
		setPlayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				setNewPlayer();
			}			
		});
      settingsMenu.add(setPlayer);
      
      setFlags = new JMenuItem("Player Flags...", KeyEvent.VK_F);
		setFlags.getAccessibleContext().setAccessibleDescription(
                "Dialog to set the media player flags.");
		setFlags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				
				int input = currentSeconds;
				String sinput = (String) JOptionPane.showInputDialog(null, "Enter flags", "Flags", JOptionPane.PLAIN_MESSAGE, null, null, switches);
				
				if(sinput != null) {
					switches = sinput;
					try {
						writeConfig();
					} catch(IOException ioe) {
						JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}			
		});
      settingsMenu.add(setFlags);
      
      /*
      saveSettings = new JMenuItem("Save Settings", KeyEvent.VK_V);
      saveSettings.getAccessibleContext().setAccessibleDescription(
                "Saves your settings to config.ini");
		saveSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					writeConfig();
				} catch(IOException ioe) {
					JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}			
		});
      settingsMenu.add(saveSettings);		
		*/
		
		return timerMenuBar;
	}
	
	public void actionPerformed(ActionEvent evt) {
		
		if(evt.getActionCommand().equals("start")) {
			if(!timer.isRunning())
				timer.start();
		} else if(evt.getActionCommand().equals("stop")) {	
			if(timer.isRunning())
				timer.stop();
		}	else if(evt.getActionCommand().equals("restart")) {
			
			currentMinutes = startMinutes;
			currentSeconds = startSeconds;
			
			updateTimeLabel();
		}	else if(evt.getActionCommand().equals("toggle")) {		
			if(timer.isRunning())
				timer.stop();
			else if(currentMinutes == 0 && currentSeconds == 0) {
				currentMinutes = startMinutes;
				currentSeconds = startSeconds;
				
				updateTimeLabel();
				timer.start();
			} else
				timer.start();
		} else if(evt.getActionCommand().equals("browse")) {
			
			JFileChooser chooser = new JFileChooser();
			chooser.setSelectedFile(nextFile);
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				nextFile = chooser.getSelectedFile();
			}
			
			try {
				writeConfig();
			} catch(IOException ioe) {
				JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void setNewPlayer() {
		
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(new File(player));
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			player = (chooser.getSelectedFile()).getAbsolutePath();
		}
		
		try {
			writeConfig();
		} catch(IOException ioe) {
			JOptionPane.showMessageDialog(null, "Can't write config.ini", "Error", JOptionPane.ERROR_MESSAGE);
		}	
	}

	private static void displayGUI() {

		JFrame timerFrame = new JFrame("Break Timer");
		timerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		timerFrame.setIconImage(new ImageIcon("orangeclock.gif").getImage());
		BreakTimer bt = new BreakTimer();
		bt.setOpaque(true);	
		timerFrame.setContentPane(bt);
		timerFrame.setJMenuBar(bt.buildTimerMenu());
			
		//Display the window.
		timerFrame.pack();
		timerFrame.setVisible(true);
	}

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayGUI();
			}
		});
	}
}
