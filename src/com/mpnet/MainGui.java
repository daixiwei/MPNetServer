package com.mpnet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.Timer;


public class MainGui {
	private final ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
	private JFrame frame;
	private JPanel mainPanel;
	private JTextArea consoleTextArea;
	private JButton resetButton;
	MPNetServer mmpnet;
	
	MainGui(){
		
	}
	
	public void start(MPNetServer mpnet,String title){
		frame=new JFrame(title);
        mainPanel=new JPanel();
        this.mmpnet = mpnet;
        frame.addWindowListener(new WindowAdapter(){
            public final void windowClosing(WindowEvent e){
                frame.dispose();
                System.exit(-1);
            }
        });
        initGui();
		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width/3*2,screenSize.height/3*2);
        frame.setLocation((screenSize.width-frame.getWidth())/2,(screenSize.height-frame.getHeight())/2);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH); //最大化
        frame.setVisible(true);
        
        mpnet.start();
        resetButton.setEnabled(true);
	}
	
	private void initGui(){
		consoleTextArea = new JTextArea();

		PrintStream ps = new PrintStream(this.outputStream);
		System.setOut(ps);
		System.setErr(ps);

		final Timer stdOutTimer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ByteArrayOutputStream stream = outputStream;
				if (stream.size() > 0) {
					String txt = stream.toString();
					consoleTextArea.append(txt);
					stream.reset();
				}
			}
		});
		stdOutTimer.start();
		
		
		
		//JPanel leftPanel=new JPanel();
		final JPanel toolsBar=new JPanel();
        toolsBar.setLayout(new BoxLayout(toolsBar,BoxLayout.Y_AXIS));
        toolsBar.setBorder(BorderFactory.createEmptyBorder(2,5,2,5));
        resetButton=new JButton("重启服务器");
        resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mmpnet.restart();
				resetButton.setEnabled(false);
			}
		});
        resetButton.setEnabled(false);
        toolsBar.add(resetButton);
		final JSplitPane mainPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,toolsBar,new JScrollPane(consoleTextArea));
        mainPane.setOneTouchExpandable(true);
        mainPane.setResizeWeight(0.1);
        
        mainPanel=new JPanel(new BorderLayout());
        //mainPanel.add(toolsBar,BorderLayout.NORTH);
        mainPanel.add(toolsBar,BorderLayout.EAST);
        mainPanel.add(new JScrollPane(consoleTextArea),BorderLayout.CENTER);
        //mainPanel.add(statusBar,BorderLayout.SOUTH);
        frame.setContentPane(mainPanel);
	}
	
//	public static void main(String[] args) {
//		new MainGui();
//	}
}
