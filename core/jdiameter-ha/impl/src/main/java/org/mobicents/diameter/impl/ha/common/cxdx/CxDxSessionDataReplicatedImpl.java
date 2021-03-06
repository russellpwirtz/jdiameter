/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.diameter.impl.ha.common.cxdx;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.jboss.cache.Fqn;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.Request;
import org.jdiameter.client.api.IContainer;
import org.jdiameter.client.api.IMessage;
import org.jdiameter.client.api.parser.IMessageParser;
import org.jdiameter.client.api.parser.ParseException;
import org.jdiameter.common.api.app.cxdx.CxDxSessionState;
import org.jdiameter.common.api.app.cxdx.ICxDxSessionData;
import org.mobicents.cluster.MobicentsCluster;
import org.mobicents.diameter.impl.ha.common.AppSessionDataReplicatedImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public abstract class CxDxSessionDataReplicatedImpl extends AppSessionDataReplicatedImpl implements ICxDxSessionData {

  private static final Logger logger = LoggerFactory.getLogger(CxDxSessionDataReplicatedImpl.class);

  private static final String STATE = "STATE";
  private static final String BUFFER = "BUFFER";
  private static final String TS_TIMERID = "TS_TIMERID";

  private IMessageParser messageParser;

  /**
   * @param nodeFqn
   * @param mobicentsCluster
   * @param iface
   */
  public CxDxSessionDataReplicatedImpl(Fqn<?> nodeFqn, MobicentsCluster mobicentsCluster, IContainer container) {
    super(nodeFqn, mobicentsCluster);
    this.messageParser = container.getAssemblerFacility().getComponentInstance(IMessageParser.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdiameter.common.api.app.cxdx.ICxDxSessionData#setCxDxSessionState(org.jdiameter.common.api.app.cxdx.CxDxSessionState)
   */
  @Override
  public void setCxDxSessionState(CxDxSessionState state) {
    if (exists()) {
      getNode().put(STATE, state);
    }
    else {
      throw new IllegalStateException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdiameter.common.api.app.cxdx.ICxDxSessionData#getCxDxSessionState()
   */
  @Override
  public CxDxSessionState getCxDxSessionState() {
    if (exists()) {
      return (CxDxSessionState) getNode().get(STATE);
    }
    else {
      throw new IllegalStateException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdiameter.common.api.app.cxdx.ICxDxSessionData#getTsTimerId()
   */
  @Override
  public Serializable getTsTimerId() {
    if (exists()) {
      return (Serializable) getNode().get(TS_TIMERID);
    }
    else {
      throw new IllegalStateException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jdiameter.common.api.app.cxdx.ICxDxSessionData#setTsTimerId(java.io.Serializable)
   */
  @Override
  public void setTsTimerId(Serializable tid) {
    if (exists()) {
      getNode().put(TS_TIMERID, tid);
    }
    else {
      throw new IllegalStateException();
    }
  }

  public Request getBuffer() {
    byte[] data = (byte[]) getNode().get(BUFFER);
    if (data != null) {
      try {
        return (Request) this.messageParser.createMessage(ByteBuffer.wrap(data));
      }
      catch (AvpDataException e) {
        logger.error("Unable to recreate message from buffer.");
        return null;
      }
    }
    else {
      return null;
    }
  }

  public void setBuffer(Request buffer) {
    if (buffer != null) {
      try {
        byte[] data = this.messageParser.encodeMessage((IMessage) buffer).array();
        getNode().put(BUFFER, data);
      }
      catch (ParseException e) {
        logger.error("Unable to encode message to buffer.");
      }
    }
    else {
      getNode().remove(BUFFER);
    }
  }
}
