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
 * Server-side representation of the list of all servers known to a particular
 * RBNB server.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 09/29/2004
 */

/*
 * Copyright 2001, 2004 Creare Inc.
 * All Rights Reserved
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 09/29/2004  JPW	In order to compile under J#, need to explicitly
 *			add a declaration for the clone method in this class.
 * 05/09/2001  INB	Created.
 *
 */
interface RoutingMapHandler
    extends com.rbnb.api.GetLogInterface,
	    com.rbnb.api.GetServerHandlerInterface,
	    com.rbnb.api.Interruptable,
	    com.rbnb.api.IOMetricsInterface,
	    com.rbnb.api.NotificationFrom,
	    com.rbnb.api.PathFinder,
	    com.rbnb.api.RegisteredInterface,
	    com.rbnb.api.RoutedTarget,
	    com.rbnb.api.RoutingMapInterface
{

    /**
     * Clones this object.
     * <p>
     * This same abstract declaration is also included in RmapInterface.java,
     * but for some unknown reason J# gives a compiler error if it is not also
     * included here.
     *
     * @author John Wilson
     *
     * @return the clone.
     * @see java.lang.Cloneable
     * @since V2.5
     * @version 09/29/2004
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 09/29/2004  JPW	Created.
     *
     */
    public abstract Object clone();

    /**
     * Creates a <code>PeerServer</code> based on the input hierarchy.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerHierarchyI the <code>PeerServer's</code> hierarchy.
     * @param peerI	     an existing <code>PeerServer</code> object to
     *			     tie in at the "correct" level.
     * @return the <code>PeerServer</code> created.
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
     * @since V2.0
     * @version 01/14/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/18/2001  INB	Created.
     *
     */
    public abstract PeerServer createPeer(Rmap peerHierarchyI,
					  PeerServer peerI)
	throws com.rbnb.api.AddressException,
	       com.rbnb.api.SerializeException,
	       java.io.EOFException,
	       java.io.IOException,
	       java.lang.InterruptedException;

    /**
     * Gets the <code>Door</code> for controlling the creation of
     * <code>PeerServers</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the <code>Door</code>.
     * @since V2.0
     * @version 12/21/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/21/2001  INB	Created.
     *
     */
    public abstract Door getPeerDoor();

    /**
     * Starts the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #stop()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract void start();

    /**
     * Stops the peer updates thread.
     * <p>
     *
     * @author Ian Brown
     *
     * @see #start()
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract void stop();

    /**
     * Updates the information stored about a <code>PeerServer</code> of the
     * local <bold>RBNB</bold> server.
     * <p>
     *
     * @author Ian Brown
     *
     * @param peerUpdateI the <code>PeerUpdate</code> message.
     * @since V2.0
     * @version 12/20/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 12/20/2001  INB	Created.
     *
     */
    public abstract void updatePeer(PeerUpdate peerUpdateI);
}
