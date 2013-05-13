package com.github.inlhell.cvp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;
import com.github.inlhell.cvp.model.TextFile;
import com.github.inlhell.cvp.observer.Observer;
import com.github.inlhell.cvp.svn.SVNStatusCheckerDaemon;

@SuppressWarnings("serial")
public class BorderFrame extends SimpleFrame implements PropertyChangeListener, Observer {
	
	private File currentlyDirectory = new File(System.getProperty("user.home"));
	private LocalChangesPane localChangesPane = null;
	private final JFileChooser saveDialog = new JFileChooser(this.currentlyDirectory);
	private final JFileChooser selectDirectoryDialog = new JFileChooser(this.currentlyDirectory);
	private final TextEditorPane textEditorPane;
	
	public BorderFrame() {
		this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
		this.setMinimumSize(new Dimension(800, 640));
		
		// This panel contains text editor or tabs with editors (if we have
		// more then one open file)
		this.textEditorPane = new TextEditorPane();
		
		// Menu bar and menu items (File->New|Save|Save As...|Close)
		final JMenuBar menuBar = new JMenuBar();
		
		final JMenu fileMenu = new JMenu("File");
		final JMenuItem newMenuItem = new JMenuItem("New", null);
		newMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				BorderFrame.this.newMenuItemActionPerformed(arg0);
			}
		});
		fileMenu.add(newMenuItem);
		
		final JMenuItem openMenuItem = new JMenuItem("Open", null);
		openMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		});
		fileMenu.add(openMenuItem);
		
		final JMenuItem saveMenuItem = new JMenuItem("Save", null);
		saveMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				
				if (!CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().isStored()) {
					BorderFrame.this.saveFileAs();
				}
				else {
					BorderFrame.this.saveFile(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getAbsolutePath(),
						CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getName());
				}
				
			}
		});
		fileMenu.add(saveMenuItem);
		
		final JMenuItem saveAsMenuItem = new JMenuItem("Save As...", null);
		saveAsMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				BorderFrame.this.saveFileAs();
			}
		});
		fileMenu.add(saveAsMenuItem);
		
		final JMenuItem closeMenuItem = new JMenuItem("Close", null);
		closeMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				BorderFrame.this.saveBeforeClose();
				System.exit(0);
			}
		});
		fileMenu.add(closeMenuItem);
		
		menuBar.add(fileMenu);
		
		final JMenu editMenu = new JMenu("Edit");
		
		final JMenuItem cutMenuItem = new JMenuItem("Cut", null);
		cutMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		});
		editMenu.add(cutMenuItem);
		
		final JMenuItem copyMenuItem = new JMenuItem("Copy", null);
		copyMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		});
		editMenu.add(copyMenuItem);
		
		final JMenuItem pasteMenuItem = new JMenuItem("Paste", null);
		pasteMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		});
		editMenu.add(pasteMenuItem);
		
		menuBar.add(editMenu);
		
		this.setJMenuBar(menuBar);
		
		this.saveDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.saveDialog.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
		
		this.selectDirectoryDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.selectDirectoryDialog.setDialogTitle("Select SVN Directory");
		this.selectDirectoryDialog.setAcceptAllFileFilterUsed(false);
		if (this.selectDirectoryDialog.showDialog(this, "Select Directory") == JFileChooser.APPROVE_OPTION) {
			System.out.println("getCurrentDirectory(): " + this.selectDirectoryDialog.getCurrentDirectory());
			System.out.println("getSelectedFile() : " + this.selectDirectoryDialog.getSelectedFile());
			
			final File[] selectedFiles = this.selectDirectoryDialog.getCurrentDirectory().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(final File directory, final String fileName) {
					return fileName.endsWith(".txt");
				}
			});
			final SVNClientManager svnClientManager = SVNClientManager.newInstance();
			for (final File selectedFile : selectedFiles) {
				try {
					final SVNStatus status = svnClientManager.getStatusClient().doStatus(selectedFile, true);
					final SVNStatusType contentsStatus = status.getContentsStatus();
					
					final TextFile newlyCreatedTextFile = new TextFile();
					newlyCreatedTextFile.setAbsolutePath(selectedFile.getAbsolutePath());
					newlyCreatedTextFile.setName(selectedFile.getName());
					newlyCreatedTextFile.setChanged(false);
					newlyCreatedTextFile.setText(this.readFileAsString(selectedFile.getAbsolutePath()));
					newlyCreatedTextFile.setInitialTextState(newlyCreatedTextFile.getText());
					newlyCreatedTextFile.setSvnStatus(this.defineSVNStatus(contentsStatus));
					newlyCreatedTextFile.addPropertyChangeListener(this);
					newlyCreatedTextFile.setStored(true);
					AvailableTextFiles.getInstance().addTextFile(newlyCreatedTextFile);
					
				}
				catch (final SVNException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// Panel contains changes view
			this.localChangesPane = new LocalChangesPane();
			this.getContentPane().add(this.textEditorPane, BorderLayout.CENTER);
			this.getContentPane().add(this.localChangesPane, BorderLayout.SOUTH);
			this.localChangesPane.registerObserver(this);
			
			for (final TextFile availableTextFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
				availableTextFile.addPropertyChangeListener(this.localChangesPane);
			}
			
			// Start daemon
			new SVNStatusCheckerDaemon();
			
			for (final File selectedFile : selectedFiles) {
				System.out.println(selectedFile);
			}
			
		}
		else {
			System.out.println("No Selection ");
		}
		
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		this.setTitle("Changes View Prototype - "
									+ CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getTitleWithPath());
		this.saveDialog.setSelectedFile(new File(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getName()));
	}
	
	@Override
	public void update() {
		this.textEditorPane.createNewFile(this);
	}
	
	protected void newMenuItemActionPerformed(@SuppressWarnings("unused") final ActionEvent arg0) {
		this.textEditorPane.createNewFile(this);
	}
	
	protected void saveBeforeClose() {
	}
	
	protected void saveFile(final String absolutePath, final String fileName) {
		try {
			final FileWriter fileWriter = new FileWriter(absolutePath);
			fileWriter.write(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getText());
			fileWriter.close();
			
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(false);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setStored(true);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setName(fileName);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setAbsolutePath(absolutePath);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile()
				.setInitialTextState(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getText());
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().touch();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void saveFileAs() {
		if (this.saveDialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			this.currentlyDirectory = this.saveDialog.getCurrentDirectory();
			this.saveFile(this.saveDialog.getSelectedFile().getAbsolutePath(), this.saveDialog.getSelectedFile().getName());
		}
	}
	
	private String defineSVNStatus(final SVNStatusType contentsStatus) {
		String svnStatus = null;
		if (contentsStatus == SVNStatusType.STATUS_MODIFIED) {
			svnStatus = "MODIFIED";
		}
		else if (contentsStatus == SVNStatusType.STATUS_CONFLICTED) {
			svnStatus = "CONFLICTED";
		}
		else if (contentsStatus == SVNStatusType.STATUS_DELETED) {
			svnStatus = "DELETED";
		}
		else if (contentsStatus == SVNStatusType.STATUS_ADDED) {
			svnStatus = "ADDED";
		}
		else if (contentsStatus == SVNStatusType.STATUS_UNVERSIONED) {
			svnStatus = "UNVERSIONED";
		}
		else if (contentsStatus == SVNStatusType.STATUS_EXTERNAL) {
			svnStatus = "EXTERNAL";
		}
		else if (contentsStatus == SVNStatusType.STATUS_IGNORED) {
			svnStatus = "IGNORED";
		}
		else if ((contentsStatus == SVNStatusType.STATUS_MISSING) || (contentsStatus == SVNStatusType.STATUS_INCOMPLETE)) {
			svnStatus = "MISSING";
		}
		else if (contentsStatus == SVNStatusType.STATUS_OBSTRUCTED) {
			svnStatus = "MISSING";
		}
		else if (contentsStatus == SVNStatusType.STATUS_REPLACED) {
			svnStatus = "OBSTRUCTED";
		}
		else if ((contentsStatus == SVNStatusType.STATUS_NONE) || (contentsStatus == SVNStatusType.STATUS_NORMAL)) {
			svnStatus = "NORMAL";
		}
		return svnStatus;
	}
	
	private String readFileAsString(final String filePath) throws IOException {
		final StringBuffer fileData = new StringBuffer();
		final BufferedReader reader = new BufferedReader(new FileReader(filePath));
		final char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			final String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}
}
