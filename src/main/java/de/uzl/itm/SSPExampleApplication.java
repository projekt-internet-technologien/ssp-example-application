/**
 * Copyright (c) 2016, Oliver Kleine, Institute of Telematics, University of Luebeck
 * All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *  - Redistributions of source messageCode must retain the above copyright notice, this list of conditions and the following
 *    disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  - Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 *    products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.uzl.itm;


import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import de.uzl.itm.ncoap.application.client.CoapClient;
import de.uzl.itm.ncoap.application.server.CoapServer;
import de.uzl.itm.ncoap.communication.blockwise.BlockSize;
import de.uzl.itm.ncoap.examples.client.callback.SimpleCallback;
import de.uzl.itm.ncoap.examples.server.LoggingConfiguration;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.MessageCode;
import de.uzl.itm.ncoap.message.MessageType;
import de.uzl.itm.ncoap.message.options.OptionValue;

/**
 * This is a simple application to showcase how to use nCoAP for servers
 *
 * @author Oliver Kleine
 */
public class SSPExampleApplication extends CoapServer {

	@Option(name = "--host", usage = "Host of the SSP (ip or domain)")
	private String SSP_HOST = "141.83.151.196";

	@Option(name = "--port", usage = "Port of the SSP")
	private int SSP_PORT = 5683;
	
	public SSPExampleApplication(String[] args) {
    	
    	// The args4j command line parser
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);

		// Parse the arguments
		try {
			parser.parseArgument(args);
			// configure logging
			LoggingConfiguration.configureDefaultLogging();
			
			// create server and register resources
			SSPExampleApplication server = new SSPExampleApplication(BlockSize.SIZE_64, BlockSize.SIZE_64);
			server.registerSimpleNotObservableWebresource();
			server.registerSimpleObservableTimeResource();
			server.registerAtSSP(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}
	
    private void registerSimpleNotObservableWebresource() {
        // create initial status (one string per paragraph)
        String[] status = new String[10];
        for (int i = 0; i < status.length; i++) {
            status[i] = "This is paragraph #" + (i + 1);
        }

        // register resource at server
        this.registerWebresource(new SimpleNotObservableWebresource(
                "/simple", status, OptionValue.MAX_AGE_MAX, this.getExecutor()
        ));
    }

    private void registerSimpleObservableTimeResource() {
        // register resource at server
        this.registerWebresource(new SimpleObservableTimeService("/utc-time", 1, this.getExecutor()));
    }

    public SSPExampleApplication(BlockSize block1Size, BlockSize block2Size) {
        super(block1Size, block2Size);
    }

    public void registerAtSSP() throws URISyntaxException {
    	
        URI resourceURI = new URI ("coap", null, SSP_HOST, SSP_PORT, "/registry", null, null);
        System.out.println(resourceURI.toString());
        CoapRequest coapRequest = new CoapRequest(MessageType.CON, MessageCode.POST, resourceURI);
        InetSocketAddress remoteSocket = new InetSocketAddress(SSP_HOST, SSP_PORT);

        CoapClient c = new CoapClient();
        SimpleCallback callback = new SimpleCallback();
        c.sendCoapRequest(coapRequest, remoteSocket, callback);
}    
    
    public static void main(String[] args) {
    	new SSPExampleApplication(args);
    }
}
