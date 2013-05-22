package com.github.inlhell.cvp;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.github.inlhell.cvp.model.CurrentlyOpenTextFile;

public class TextAreaChangesListener implements DocumentListener {
	
	private final JTextArea textArea;
	private final TextModificationIgnorable textModificationIgnorable;
	
	public TextAreaChangesListener(final JTextArea textArea, final TextModificationIgnorable textModificationIgnorable) {
		this.textArea = textArea;
		this.textModificationIgnorable = textModificationIgnorable;
	}
	
	@Override
	public void changedUpdate(final DocumentEvent e) {
		this.analyzeTextAreaModification();
	}
	
	@Override
	public void insertUpdate(final DocumentEvent e) {
		this.analyzeTextAreaModification();
	}
	
	@Override
	public void removeUpdate(final DocumentEvent e) {
		this.analyzeTextAreaModification();
	}
	
	private void analyzeTextAreaModification() {
		
		final boolean wasTextModifiedComparedWithInitState =
			!(this.textArea.getText().equals(CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile()
				.getInitialTextState()));
		
		if (wasTextModifiedComparedWithInitState) {
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(true);
			this.textModificationIgnorable.setTextModificationIgnored(true);
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setText(this.textArea.getText());
			this.textModificationIgnorable.setTextModificationIgnored(false);
		}
		else {
			CurrentlyOpenTextFile.getInstance().getCurrentlyOpenTextFile().setChanged(false);
		}
	}
}
