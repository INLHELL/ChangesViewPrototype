package com.github.inlhell.cvp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
import com.github.inlhell.cvp.model.TextFileUtil;
import com.github.inlhell.cvp.observer.Observer;
import com.github.inlhell.cvp.svn.SVNStatusChecker;
import com.github.inlhell.cvp.svn.SVNStatusCheckerDaemon;

@SuppressWarnings("serial")
public class BorderFrame extends SimpleFrame implements PropertyChangeListener, Observer {
	
	private File currentlyDirectory = new File(System.getProperty("user.home"));
	private LocalChangesPanel localChangesPanel = null;
	private final JFileChooser saveDialog = new JFileChooser(this.currentlyDirectory);
	private final JFileChooser selectDirectoryDialog = new JFileChooser(this.currentlyDirectory);
	private final TextEditorPane textEditorPane;
	
	public BorderFrame() {
		this.setExtendedState(this.getExtendedState() | Frame.MAXIMIZED_BOTH);
		this.setMinimumSize(new Dimension(800, 640));
		
		// This panel contains text editor or tabs with editors (if we have
		// more then one open file)
		this.textEditorPane = new TextEditorPane();
		
		this.createMenuBar();
		
		this.saveDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
		this.saveDialog.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
		
		this.selectDirectoryDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		this.selectDirectoryDialog.setDialogTitle("Select SVN Directory");
		this.selectDirectoryDialog.setAcceptAllFileFilterUsed(false);
		if (this.selectDirectoryDialog.showDialog(this, "Select Directory") == JFileChooser.APPROVE_OPTION) {
			this.checkSelectedDirectory();
		}
		else {
			System.exit(0);
		}
		
	}
	
	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (this.isChangeCorrespondsToTitle(evt)) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Changes View Prototype - ");
			stringBuffer.append(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getTitleWithPath());
			this.setTitle(stringBuffer.toString());
			this.saveDialog
				.setSelectedFile(new File(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getName()));
		}
	}
	
	private boolean isChangeCorrespondsToTitle(final PropertyChangeEvent evt) {
		boolean isChangeCorrespondsToTitle = false;
		
		final boolean isCurrentlyOpenTextFileNotNull =
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile() != null;
		final boolean isEventOfNeededType =
			evt.getPropertyName().equals("stored") || evt.getPropertyName().equals("changed")
				|| evt.getPropertyName().equals("touch");
		boolean isComingEventFromCurrentlyOpenFile = false;
		if (isCurrentlyOpenTextFileNotNull) {
			isComingEventFromCurrentlyOpenFile =
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().equals(evt.getSource());
		}
		
		if (isCurrentlyOpenTextFileNotNull && isComingEventFromCurrentlyOpenFile && isEventOfNeededType) {
			isChangeCorrespondsToTitle = true;
		}
		return isChangeCorrespondsToTitle;
	}
	
	@Override
	public void update() {
		this.textEditorPane.createNewFile(this);
	}
	
	protected void newMenuItemActionPerformed(@SuppressWarnings("unused") final ActionEvent arg0) {
		this.textEditorPane.createNewFile(this);
	}
	
	protected void saveAllBeforeClose() {
		for (final TextFile textFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
			if (textFile.isChanged()) {
				this.saveFile(textFile.getAbsolutePath(), textFile.getName(), textFile);
			}
		}
	}
	
	protected void saveFile(final String absolutePath, final String fileName, final TextFile textFile) {
		try {
			// Write actual file state to the file
			final FileWriter fileWriter = new FileWriter(absolutePath);
			fileWriter.write(textFile.getText());
			fileWriter.close();
			
			// File was stored this means it is not changed
			textFile.setChanged(false);
			textFile.setStored(true);
			// If the new name was specified we set it to the file
			textFile.setName(fileName);
			// If name was changed or the file was stored in sub directory it has a
			// new absolute path
			textFile.setAbsolutePath(absolutePath);
			// An initial state of the file will be reset to its actual contetnt
			textFile.setInitialTextState(textFile.getText());
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected void saveFileAs() {
		if (this.saveDialog.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			this.currentlyDirectory = this.saveDialog.getCurrentDirectory();
			this.saveFile(this.saveDialog.getSelectedFile().getAbsolutePath(), this.saveDialog.getSelectedFile().getName(),
				CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile());
		}
	}
	
	private void createMenuBar() {
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
		
		final JMenuItem saveMenuItem = new JMenuItem("Save", null);
		saveMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				
				if (!CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().isStored()) {
					BorderFrame.this.saveFileAs();
				}
				else {
					BorderFrame.this.saveFile(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getAbsolutePath(),
						CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().getName(), CurrentlyOpenTextFile
							.getInstance().getCurrentlyOpenTextFile());
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
		
		final JMenuItem exitMenuItem = new JMenuItem("Exit", null);
		exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				BorderFrame.this.saveAllBeforeClose();
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		
		menuBar.add(fileMenu);
		
		this.setJMenuBar(menuBar);
	}
	
	@SuppressWarnings("unused")
	private void checkSelectedDirectory() {
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
				
				this.createNewTextFile(selectedFile, contentsStatus);
				
			}
			catch (final SVNException e) {
				e.printStackTrace();
				System.exit(0);
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
		
		// Panel contains changes view
		this.localChangesPanel = new LocalChangesPanel();
		this.getContentPane().add(this.textEditorPane, BorderLayout.CENTER);
		this.getContentPane().add(this.localChangesPanel, BorderLayout.SOUTH);
		this.localChangesPanel.registerObserver(this);
		
		for (final TextFile availableTextFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
			availableTextFile.addPropertyChangeListener(this.localChangesPanel);
		}
		
		// Start daemon
		new SVNStatusCheckerDaemon();
		
		for (final File selectedFile : selectedFiles) {
			System.out.println(selectedFile);
		}
	}
	
	private void createNewTextFile(final File selectedFile, final SVNStatusType contentsStatus) throws IOException {
		final TextFile newlyCreatedTextFile = new TextFile();
		newlyCreatedTextFile.setAbsolutePath(selectedFile.getAbsolutePath());
		newlyCreatedTextFile.setName(selectedFile.getName());
		newlyCreatedTextFile.setText(TextFileUtil.getInstance().readFileAsString(selectedFile.getAbsolutePath()));
		newlyCreatedTextFile.setInitialTextState(newlyCreatedTextFile.getText());
		newlyCreatedTextFile.setSvnStatus(SVNStatusChecker.getInstance().defineSVNStatus(contentsStatus));
		newlyCreatedTextFile.addPropertyChangeListener(this);
		AvailableTextFiles.getInstance().addTextFile(newlyCreatedTextFile);
	}
}
