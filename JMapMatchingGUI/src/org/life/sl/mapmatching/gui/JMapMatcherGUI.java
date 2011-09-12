package org.life.sl.mapmatching.gui;

import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import org.life.sl.graphs.PathSegmentGraph;
import org.life.sl.mapmatching.JMapMatcher;
import org.life.sl.readers.osm.OSMFileReader;
import org.openstreetmap.josm.io.IllegalDataException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JProgressBar;
import javax.swing.JCheckBox;

/**
 * GUI for the JMapMatcher
 * @author besn
 */
public class JMapMatcherGUI extends JPanel implements Runnable, 
									   				  ActionListener {
	private static final long serialVersionUID = 1L;
	
	private JFrame frame;
	private JTextField txtXxxx;

	private JFileChooser pointShapeChooser;
	private JFileChooser oSMFileChooser;
	
	private JTextField txtPleaseSelectANetworkFile;
	private JTextField txtPleaseSelectAGpxFile;
	private JTextField txtPleaseSelectAnOsmFile;

	private JFileChooser gPXFileChooser;
	
	private JButton btnOpenPointThemeDlg;
	private JButton btnOpenGPSFileDlg;
	private JButton btnOsmFileDlg;
	private JButton btnOpenNetworkFileDlg;

	private JProgressBar progressBar;
	private JTextField txtPleaseCreateA;
	private JTextField txtPleaseCreateA_1;
	
	private class JMapMatcherThread extends Thread implements Runnable {
		// parameters determining the data source:
		private static final boolean DATASOURCE_FROM_OSM = true;
		private static final boolean DATASOURCE_FROM_SHAPEFILE = !DATASOURCE_FROM_OSM;
		
		private PathSegmentGraph psg;
		JMapMatcher jmg;
		private JMapMatcherGUI jmmg;

		@Override
		public void run() {
			this.launch_match();
		}

		public JMapMatcherThread(JMapMatcherGUI jmmg) {
			this.jmmg = jmmg;
		}

		public void launch_match() {
			// load data:
			if (DATASOURCE_FROM_OSM) {
				String filename = "testdata/testnet.osm";
				OSMFileReader osm_reader = new OSMFileReader();
				try {
					osm_reader.readOSMFile(filename);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				psg = osm_reader.asLineMergeGraph();
			} else if (DATASOURCE_FROM_SHAPEFILE) {
				try {
					psg = new PathSegmentGraph("testdata/Sparse_bigger0.shp");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			jmmg.setProgressBar(50);
			
			// instantiate the JMapMatcher object:
			jmg = new JMapMatcher(psg);
			// start the matching algorithm:
			try {
				jmg.match("testdata/GPS_Points.shp");	// TODO: filename should be taken from the GUI
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jmmg.setProgressBar(100);	// finished!
		}

	}

	public JMapMatcherGUI() {
		initialize();
	}
	
	/**
	 * set status of progress bar
	 * @param i value in percent (0...100)
	 */
	public void setProgressBar(int i) {
		this.progressBar.setValue(i);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==btnOpenPointThemeDlg)  {

			pointShapeChooser = new JFileChooser();
			pointShapeChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnValue = pointShapeChooser.showOpenDialog(JMapMatcherGUI.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				txtXxxx.setText(pointShapeChooser.getSelectedFile().toString());
			}
			// fc.addActionListener(this);
		}
		
		if (e.getSource() == btnOpenGPSFileDlg) {
			gPXFileChooser = new JFileChooser();
			gPXFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnValue = gPXFileChooser.showOpenDialog(JMapMatcherGUI.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				txtPleaseSelectAGpxFile.setText(gPXFileChooser.getSelectedFile().toString());
			}
		}
		
		if (e.getSource() == btnOpenNetworkFileDlg) {
			gPXFileChooser = new JFileChooser();
			gPXFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnValue = gPXFileChooser.showOpenDialog(JMapMatcherGUI.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				txtPleaseSelectANetworkFile.setText(gPXFileChooser.getSelectedFile().toString());
			}
		}		
		
		if (e.getSource() == btnOsmFileDlg) {
			oSMFileChooser = new JFileChooser();
			oSMFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnValue = oSMFileChooser.showOpenDialog(JMapMatcherGUI.this);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				txtPleaseSelectAnOsmFile.setText(oSMFileChooser.getSelectedFile().toString());
			}
		}
	}
    
	/**
	 * GUI initialization
	 */
    private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 723, 517);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnMatch = new JButton("MATCH !");
		btnMatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				(new Thread(new JMapMatcherThread(JMapMatcherGUI.this))).start();
			}
		});
		btnMatch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		txtXxxx = new JTextField();
		txtXxxx.setText("please select a shapefile...");
		txtXxxx.setColumns(10);
		
		btnOpenPointThemeDlg = new JButton(".shp");
		btnOpenPointThemeDlg.addActionListener(this);
		
		JLabel lblNewLabel = new JLabel("1B Point Theme:");
		
		JLabel lblNewLabel_1 = new JLabel("2A Road Network Theme:");
		
		txtPleaseSelectANetworkFile = new JTextField();
		txtPleaseSelectANetworkFile.setText("please select a shapefile...");
		txtPleaseSelectANetworkFile.setColumns(10);
		
		btnOpenNetworkFileDlg = new JButton(".shp");
		btnOpenNetworkFileDlg.addActionListener(this);
		
		JLabel lblGpxFile = new JLabel("1A GPX FIle:");
		
		txtPleaseSelectAGpxFile = new JTextField();
		txtPleaseSelectAGpxFile.setText("please select a GPX File");
		txtPleaseSelectAGpxFile.setColumns(10);
		
		btnOpenGPSFileDlg = new JButton(".gpx");
		btnOpenGPSFileDlg.addActionListener(this);
		
		txtPleaseSelectAnOsmFile = new JTextField();
		txtPleaseSelectAnOsmFile.setText("please select an OSM file");
		txtPleaseSelectAnOsmFile.setColumns(10);
		
		btnOsmFileDlg = new JButton(".osm");
		btnOsmFileDlg.addActionListener(this);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("2 B Pull data directly from Open Streetmap");
		
		JSeparator separator = new JSeparator();
		
		txtPleaseCreateA = new JTextField();
		txtPleaseCreateA.setText("please create a GPX file");
		txtPleaseCreateA.setColumns(10);
		
		JButton btngpx = new JButton(".gpx");
		
		txtPleaseCreateA_1 = new JTextField();
		txtPleaseCreateA_1.setText("please create a Shapefile");
		txtPleaseCreateA_1.setColumns(10);
		
		JButton btnshp = new JButton(".shp");
		
		JLabel lblResultPolylineFeature = new JLabel("Result Polyline Feature");
		
		JLabel lblcOpenstreetmapFile = new JLabel("2C OpenStreetmap File");
		
		JLabel label_1 = new JLabel("Result Polyline Feature");
		
		JCheckBox chckbxSingleFileOutput = new JCheckBox("Single File Output");
		chckbxSingleFileOutput.setToolTipText("Tick if you want chosen as well as nonchosen routes in one file.");

		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap(618, Short.MAX_VALUE)
							.addComponent(btnMatch))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap(642, Short.MAX_VALUE)
							.addComponent(btnOpenNetworkFileDlg, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap(68, Short.MAX_VALUE)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblGpxFile, GroupLayout.PREFERRED_SIZE, 82, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED, 269, Short.MAX_VALUE)
											.addComponent(txtPleaseSelectAGpxFile, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblNewLabel)
											.addPreferredGap(ComponentPlacement.RELATED, 250, Short.MAX_VALUE)
											.addComponent(txtXxxx, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)))
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(btnOpenPointThemeDlg)
										.addComponent(btnOpenGPSFileDlg, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblcOpenstreetmapFile)
									.addPreferredGap(ComponentPlacement.RELATED, 208, Short.MAX_VALUE)
									.addComponent(txtPleaseSelectAnOsmFile, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(btnOsmFileDlg, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(lblResultPolylineFeature, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.UNRELATED)
											.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE))
										.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 275, Short.MAX_VALUE))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(txtPleaseCreateA)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btngpx, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(txtPleaseCreateA_1, GroupLayout.PREFERRED_SIZE, 204, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(btnshp, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
										.addComponent(chckbxSingleFileOutput, Alignment.LEADING)))
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
									.addComponent(lblNewLabel_1)
									.addPreferredGap(ComponentPlacement.RELATED, 192, Short.MAX_VALUE)
									.addComponent(txtPleaseSelectANetworkFile, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
									.addGap(87))
								.addComponent(chckbxNewCheckBox))))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(30)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtPleaseSelectAGpxFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnOpenGPSFileDlg)
						.addComponent(lblGpxFile))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnOpenPointThemeDlg)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(txtXxxx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblNewLabel)))
					.addGap(21)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOpenNetworkFileDlg)
						.addComponent(txtPleaseSelectANetworkFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxNewCheckBox)
					.addPreferredGap(ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblcOpenstreetmapFile)
						.addComponent(btnOsmFileDlg)
						.addComponent(txtPleaseSelectAnOsmFile, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(47)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
									.addComponent(txtPleaseCreateA, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(btngpx)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnshp)
								.addComponent(txtPleaseCreateA_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(label_1)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(52)
							.addComponent(lblResultPolylineFeature)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(chckbxSingleFileOutput)
					.addGap(47)
					.addComponent(btnMatch)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		frame.getContentPane().setLayout(groupLayout);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnNewMenu.add(mntmAbout);
		
		JMenuItem mntmCloseApplication = new JMenuItem("Close Application");
		mnNewMenu.add(mntmCloseApplication);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
	}
	
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JMapMatcherGUI window = new JMapMatcherGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void run() {
		// TODO Auto-generated method stub
		
	}
}


