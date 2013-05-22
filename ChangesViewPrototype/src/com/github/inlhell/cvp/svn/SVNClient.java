package com.github.inlhell.cvp.svn;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class SVNClient {
	
	SVNWCClient svnwcClient;
	
	private SVNClient() {
		final SVNClientManager svnClientManager = SVNClientManager.newInstance();
		this.svnwcClient = svnClientManager.getWCClient();
	}
	
	private static class Handler {
		
		private static SVNClient instance = new SVNClient();
	}
	
	public static SVNClient getInstance() {
		return Handler.instance;
	}
	
	public boolean revert(final File file) {
		try {
			this.svnwcClient.doRevert(new File[] {
				file
			}, SVNDepth.UNKNOWN, null);
		}
		catch (final SVNException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean delete(final File file) {
		try {
			this.svnwcClient.doDelete(file, true, false);
		}
		catch (final SVNException e) {
			e.printStackTrace();
		}
		return true;
	}
}
