package com.github.inlhell.cvp.svn;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

import com.github.inlhell.cvp.model.AvailableTextFiles;
import com.github.inlhell.cvp.model.TextFile;

public class SVNStatusCheckerDaemon extends Thread {
	
	public SVNStatusCheckerDaemon() {
		this.setDaemon(true);
		this.start();
	}
	
	@Override
	public void run() {
		while (true) {
			final SVNClientManager svnClientManager = SVNClientManager.newInstance();
			for (final TextFile availableTextFile : AvailableTextFiles.getInstance().getAvailableTextFiles()) {
				try {
					final SVNStatus status =
						svnClientManager.getStatusClient().doStatus(new File(availableTextFile.getAbsolutePath()), true);
					final SVNStatusType contentsStatus = status.getContentsStatus();
					final String svnStatus = this.defineSVNStatus(contentsStatus);
					if (!availableTextFile.getSvnStatus().equals(svnStatus)) {
						// Status was changed
						availableTextFile.setSvnStatus(svnStatus);
						availableTextFile.touch();
					}
				}
				catch (final SVNException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(1000);
			}
			catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
}
