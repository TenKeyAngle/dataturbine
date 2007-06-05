/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

/**
 * Extended <code>MirrorIO</code> that actually performs the mirroring
 * task.
 * <p>
 * This class is the main workhorse for the mirroring operation. It creates a
 * chain that goes from an <code>NBO</code> to collect the data to be mirrored
 * through this object via the API and on to an <code>RBO</code> to store the
 * mirrored data. Depending on the direction of the mirror, various
 * communications will be RAM-based, while the rest will use the appropriate
 * communications medium for the remote <code>Server</code>.
 * <p>
 * If the direction is <code>PULL</code>, then the <code>RBO</code> is a local
 * RAM <code>RBO</code>, while the <code>NBO</code> is on the remote
 * <code>Server</code>. Data is "pulled" from the remote and placed into the
 * local <code>Server</code>.
 * <p>
 * If the direction is <code>PUSH</code>, then the <code>NBO</code> is a local
 * RAM <code>NBO</code>, while the <code>RBO</code> is on the remote
 * <code>Server</code>. Data is "pushed" from the local side to the remote.
 * <p>
 * Actually, in both cases, the actual data transfer operation is driven from
 * the <code>NBO</code>.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 02/11/2004
 */

/*
 * Copyright 2001, 2002, 2003, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 02/11/2004  INB	Log exceptions at standard level.
 * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
 *			<code>Thread</code> and ensure that <code>Locks</code>
 *			are released.
 * 10/13/2003  INB	Use CREATE mode for LOADed sources.
 * 04/18/2001  INB	Created.
 *
 */
class MirrorController
    extends com.rbnb.api.MirrorIO
    implements java.lang.Runnable,
	       com.rbnb.api.MirrorHandler
{
    /**
     * the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private ServerHandler local = null;

    /**
     * the <code>Sink</code> used to retrieve the data.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #source
     * @since V2.0
     * @version 05/11/2001
     */
    private Sink sink = null;

    /**
     * the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */
    private Thread thread = null;

    /**
     * Class constructor.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    MirrorController() {
	super();
    }

    /**
     * Class constructor to build a <code>MirrorController</code> running on
     * the specified <code>ServerHandler</code> to run the specified
     * <code>Mirror</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param localI  the local <code>ServerHandler</code>.
     * @param mirrorI the <code>Mirror</code>.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is an addressing problem.
     * @exception java.lang.IllegalStateException
     *		  thrown if mirror is not supported by the
     *		  <code>License</code>.
     * @since V2.0
     * @version 11/14/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code>.
     * 04/18/2001  INB	Created.
     *
     */
    MirrorController(ServerHandler localI,Mirror mirrorI)
	throws com.rbnb.api.AddressException
    {
	/*
	License license = ((RBNB) localI).getLicense();
	if (!license.mirrors()) {
	    throw new java.lang.IllegalStateException
		("This license (" +
		 license.version() + " #" + license.serialNumber() +
		 ") does not support mirroring.");
	}
	*/

	setLocal(localI);
	setDirection(mirrorI.getDirection());
	Address rAddress = Address.newAddress
	    (mirrorI.getRemote().getAddress());
	mirrorI.getRemote().setAddress(rAddress.getAddress());

	if (mirrorI.getRemote().getAddress().compareTo
	    (getLocal().getAddress()) == 0) {
	    setRemote((Server) localI);
	} else {
	    setRemote(mirrorI.getRemote());
	}
	setRequest(mirrorI.getRequest());
	setSource(mirrorI.getSource());

	setThread(new ThreadWithLocks(this,
				      ("_MC." +
				       localI.getAddress() +
				       ((getDirection() == PUSH) ?
					"->" :
					"<-") +
				       getRemote().getAddress())));
	getThread().start();
    }

    /**
     * Creates the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectSink()
     * @since V2.0
     * @version 02/21/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void createSink()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server snkServer = ((getDirection() == PULL) ?
			    getRemote() :
			    (Server) getLocal()),
	       srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote());

	if (snkServer instanceof ServerHandler) {
	    snkServer = ((ServerHandler) snkServer).getClientSide();
	    setSink(snkServer.createRAMSink
		    ("_Mirror." + getSource().getName()));
	} else {
	    setSink(snkServer.createSink("_Mirror." + getSource().getName()));
	}
	getSink().setType(Client.MIRROR);
	getSink().setRemoteID(srcServer.getAddress() +
			      Rmap.PATHDELIMITER +
			      getSource().getName());
	getSink().start();
    }

    /**
     * Disconnects the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @see #createSink()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void disconnectSink()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSink().stop();
    }

    /**
     * Disconnects the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.IllegalStateException
     *		  thrown if the <code>Client</code> is not running.
     * @exception java.lang.InterruptedException
     *		  thrown if the terminate is interrupted.
     * @see #initializeSource()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void disconnectSource()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSource().stop();
    }

    /**
     * Gets the <code>Log</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Log</code>.
     * @since V2.0
     * @version 11/19/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/19/2002  INB	Created.
     *
     */
    public final Log getLog() {
	return (getLocal().getLog());
    }

    /**
     * Gets the log class mask for this <code>MirrorController</code>.
     * <p>
     * Log messages for this class use this mask.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the class mask.
     * @see #getLogLevel()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final long getLogClass() {
	return (Log.CLASS_MIRROR_CONTROLLER);
    }

    /**
     * Gets the base log level for this <code>MirrorController</code>.
     * <p>
     * Log messages for this class are at or above this level.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the level value.
     * @see #getLogClass()
     * @since V2.0
     * @version 01/11/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2002  INB	Created.
     *
     */
    public final byte getLogLevel() {
	return (Log.STANDARD);
    }

    /**
     * Gets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the local <code>ServerHandler</code>.
     * @see #setLocal(com.rbnb.api.ServerHandler)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final ServerHandler getLocal() {
	return (local);
    }

    /**
     * Gets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Sink</code>.
     * @see #setSink(com.rbnb.api.Sink)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final Sink getSink() {
	return (sink);
    }

    /**
     * Gets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the thread.
     * @see #setThread(Thread)
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final Thread getThread() {
	return (thread);
    }

    /**
     * Initializes the <code>Source</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #disconnectSource()
     * @since V2.0
     * @version 10/13/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 10/13/2003  INB	Use CREATE mode for LOADed sources.
     * 04/18/2001  INB	Created.
     *
     */
    private final void initializeSource()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Server srcServer = ((getDirection() == PULL) ?
			    (Server) getLocal() :
			    getRemote()),
	       snkServer = ((getDirection() == PULL) ?
			    getRemote() :
			    (Server) getLocal());
	Source lSource = getSource();

	if (srcServer instanceof ServerHandler) {
	    srcServer = ((ServerHandler) srcServer).getClientSide();
	    setSource(srcServer.createRAMSource(lSource.getName()));
	} else {
	    setSource(srcServer.createSource(lSource.getName()));
	}
	getSource().setAframes(lSource.getAframes());
	if (lSource.getAmode() != Source.ACCESS_LOAD) {
	    getSource().setAmode(lSource.getAmode());
	} else {
	    getSource().setAmode(Source.ACCESS_CREATE);
	}
	//	getSource().setAsize(lSource.getAsize());
	if (lSource.getCframes() > 0) {
	    getSource().setCframes(lSource.getCframes());
	}
	//	getSource().setCsize(lSource.getCsize());
	if (lSource.getNfs() > 0) {
	    getSource().setNfs(lSource.getNfs());
	}
	getSource().setType(Client.MIRROR);
	getSource().setRemoteID(snkServer.getAddress() +
				Rmap.PATHDELIMITER +
				"_Mirror." + getSource().getName());
	getSource().start();
    }

    /**
     * Issues the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #loopRequest()
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void issueRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	getSink().addChild(getRequest());
	getSink().initiateRequestAt(0);
    }

    /**
     * Loops on the request.
     * <p>
     *
     * @author Ian Brown
     *
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if the request stream ends prematurely.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @see #issueRequest()
     * @since V2.0
     * @version 11/17/2003
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/18/2001  INB	Created.
     *
     */
    private final void loopRequest()
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	Rmap response = null;

	while ((response = getSink().fetch(Sink.FOREVER)) != null) {
	    if (response instanceof EndOfStream) {
		if (response.getNchildren() == 1) {
		    Rmap child = response.getChildAt(0);
		    response.removeChild(child);
		    post(child);
		}
		break;
	    } else {
		post(response);
	    }

	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "MirrorController.loopRequest",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    }
	}
    }

    /**
     * Posts a data response.
     * <p>
     * This method strips off the <code>Server</code> and <code>Source</code>
     * information from the input response before passing it to the target.
     * <p>
     *
     * @author Ian Brown
     *
     * @param responseI  the response <code>Rmap</code> to post.
     * @exception com.rbnb.api.AddressException
     *		  thrown if there is a problem with an address.
     * @exception com.rbnb.api.EndOfStreamException
     *		  thrown if the request stream ends prematurely.
     * @exception com.rbnb.api.SerializeException
     *		  thrown if there is a problem with the serialization.
     * @exception java.io.EOFException
     *		  thrown if the end of the input stream is reached.
     * @exception java.io.IOException
     *		  thrown if there is an error during I/O.
     * @exception java.lang.InterruptedException
     *		  thrown if the operation is interrupted.
     * @since V2.0
     * @version 05/11/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void post(Rmap responseI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException
    {
	// Strip out the <code>Server</code> and <code>Source</code>
	// information from the response. In addition, strip out unnecessary
	// unnamed <code>Rmaps</code>.
	Rmap response = responseI,
	     level;
	boolean foundServer = false,
		foundSource = false,
		hasInfo = false;

	for (level = responseI;
	     !hasInfo ||!foundServer || !foundSource;
	     level = level.getChildAt(0)) {
	    boolean hadInfo = hasInfo;

	    if (!hasInfo && (level.getParent() != null)) {
		response = level;
	    }

	    if (!foundServer) {
		if (level instanceof Server) {
		    level.setName(null);
		    foundServer = true;
		} else if (level instanceof Client) {
		    throw new java.lang.IllegalArgumentException
			("Mirror request produced an Rmap that cannot be " +
			 "properly placed into the output." +
			 responseI);
		}
	    } else if (!foundSource) {
		if (level instanceof Client) {
		    level.setName(null);
		    foundSource = true;
		}
	    }

	    if (!level.isNamelessTimeless()) {
		hasInfo = true;
	    }
	    if (!foundServer || !foundSource) {
		if (level.getNchildren() > 1) {
		    throw new java.lang.IllegalArgumentException
			("Mirror request produced an Rmap that cannot be " +
			 "properly placed into the output.\n" +
			 responseI);
		}
	    } else {
		hasInfo = hasInfo || (level.getNchildren() > 1);
	    }

	    if (hasInfo && foundServer && foundSource) {
		if (!hadInfo) {
		    response = response.getParent();
		}
		break;
	    }
	}

	if (response != responseI) {
	    response.getParent().removeChild(response);
	}

	// Send the result to our <code>Source</code>.
	getSource().addChild(response);
    }

    /**
     * Runs the mirror.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 02/11/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 02/11/2004  INB	Log exceptions at standard level.
     * 11/14/2003  INB	Use <code>ThreadWithLocks</code> rather than
     *			<code>Thread</code> and ensure that <code>Locks</code>
     *			are released.
     * 04/18/2001  INB	Created.
     *
     */
    public final void run() {

	try {
	    // Initialize the destination <code>Source</code>.
	    initializeSource();

	    // Create the <code>Sink</code>.
	    createSink();

	    // Issue the request.
	    issueRequest();

	    // Loop until the request completes.
	    loopRequest();

	} catch (java.lang.Exception e) {
	    try {
		String name;
		if (getDirection() == PULL) {
		    name = getLocal().getFullName();
		} else {
		    name = getRemote().getFullName();
		}
		name += "<-" + getSource().getFullName();
		for (int idx = 0; idx < name.length(); ++idx) {
		    if (name.charAt(idx)  == '/') {
			name = (name.substring(0,idx) +
				"_" +
				name.substring(idx + 1));
		    }
		}

		getLog().addException
		    (Log.STANDARD,
		     getLogClass(),
		     name,
		     e);
	    } catch (java.lang.Exception e1) {
	    }

	} finally {
	    try {
		disconnectSource();
	    } catch (java.lang.Exception e) {
	    }
	    try {
		disconnectSink();
	    } catch (java.lang.Exception e) {
	    }
	    if (getThread() != null) {
		((ThreadWithLocks) getThread()).ensureLocksCleared
		    (toString(),
		     "MirrorController.run",
		     getLog(),
		     getLogLevel(),
		     getLogClass());
	    }
	    setThread(null);
	}
    }

    /**
     * Sets the local <code>ServerHandler</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param localI  the local <code>ServerHandler</code>.
     * @see #getLocal()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setLocal(ServerHandler localI) {
	local = localI;
    }

    /**
     * Sets the <code>Sink</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @param sinkI  the <code>Sink</code>.
     * @see #getSink()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setSink(Sink sinkI) {
	sink = sinkI;
    }

    /**
     * Sets the thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @param threadI  the thread.
     * @see #getThread()
     * @since V2.0
     * @version 05/22/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/18/2001  INB	Created.
     *
     */
    private final void setThread(Thread threadI) {
	thread = threadI;
    }
}

