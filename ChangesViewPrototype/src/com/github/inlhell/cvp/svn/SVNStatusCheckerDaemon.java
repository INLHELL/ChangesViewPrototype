package com.github.inlhell.cvp.svn;

public class SVNStatusCheckerDaemon extends Thread {
	
	public SVNStatusCheckerDaemon() {
		this.setDaemon(true);
		this.start();
	}
	
	@Override
	public void run() {
		while (true) {
			SVNStatusChecker.getInstance().checkStatus();
			try {
				Thread.sleep(1000);
			}
			catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
