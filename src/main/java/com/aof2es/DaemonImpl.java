package com.aof2es;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

/**
 * @author Roland Quast (rquast@rolandquast.com)
 *
 */
public class DaemonImpl implements Daemon {

    private Main main;

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
	Main.init(context);
    }

    @Override
    public void start() throws Exception {
	if (this.main == null) {
	    this.main = new Main();
	}
    }

    @Override
    public void stop() throws Exception {
	if (this.main == null) {
	    this.main.stop();
	}
    }

    @Override
    public void destroy() {
	if (this.main == null) {
	    this.main.destroy();
	}
    }

}
