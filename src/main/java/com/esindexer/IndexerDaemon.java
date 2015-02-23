package com.esindexer;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * @author Roland Quast (roland@formreturn.com)
 *
 */
public class IndexerDaemon implements Daemon {
	
	private Main main;

	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
		Main.init(context);
	}

	public void start() throws Exception {
		if ( this.main == null ) {
			this.main = new Main(new String[]{});
		}
	}

	public void stop() throws Exception {
		if ( this.main == null ) {
			this.main.stop();
		}
	}

	public void destroy() {
		if ( this.main == null ) {
			this.main.destroy();
		}
	}

}
