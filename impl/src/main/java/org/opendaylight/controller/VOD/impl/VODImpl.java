/*
 * Copyright © 2017 BNI, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.controller.VOD.impl;

import org.opendaylight.controller.ted.impl.LinkProperty;
import org.opendaylight.controller.ted.impl.LinkPropertyService;

import org.opendaylight.controller.LPM.impl.SetupManagerService;

import java.util.concurrent.Future;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vod.rev150105.VODService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vod.rev150105.SendCommandInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vod.rev150105.SendCommandOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vod.rev150105.SendCommandOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.opendaylight.controller.VOD.impl.AlgorithmsImpl;

import java.util.*;
import java.lang.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VODImpl implements VODService {

	private static final Logger LOG = LoggerFactory.getLogger(VODImpl.class);
	
	private final LinkPropertyService linkPropertyService;
	private final SetupManagerService setupManagerService;
	public TopoResource topoResource;

	/**
	 * @param linkPropertyService
	 * @param setupManagerService
	 */
	public VODImpl(SetupManagerService setupManagerService, LinkPropertyService linkPropertyService) {
		this.linkPropertyService = linkPropertyService;
		this.setupManagerService = setupManagerService;
	}

	@Override
	public Future<RpcResult<SendCommandOutput>> sendCommand(SendCommandInput input) {
		SendCommandOutputBuilder sendCommandBuilder = new SendCommandOutputBuilder();

		TopoResource topoResource = new TopoResource(linkPropertyService);
		if (topoResource.initTopoResource()){

			if (input.getOperation().equals("startVOD")) {
				AlgorithmsImpl al = new AlgorithmsImpl(topoResource);
				if (al.startSetupVodPath()) {
					if (setupManagerService.startSetupVodPath(al.srcRRU1,al.srcRRU2, al.dstBBU1, al.dstBBU2, al.connectNe, al.connectWss)) {
						Set<String> nbLogTopoShow = topoResource.getLogicalTopoShow();
						String nima = new String();
						int j = 1;
						for(String s : nbLogTopoShow) {
							nima += "<" + j++ + ">" + s;
						}
						sendCommandBuilder.setResult("The LogicalTopo after startVOD: "+ nima);
					}
				}
			}
		}

		return RpcResultBuilder.success(sendCommandBuilder.build()).buildFuture();
	}
}