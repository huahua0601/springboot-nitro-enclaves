package com.github.mrgatto.host.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.mrgatto.host.socketpool.ConnectionManager;
import com.github.mrgatto.host.socketpool.ConnectionManagerSingleton;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mrgatto.network.SocketTLV;

import solutions.cloudarchitects.vsockj.VSock;
import solutions.cloudarchitects.vsockj.VSockAddress;

public class VSockHostClient extends AbstractSocketHostClient {

	private static final Logger LOG = LoggerFactory.getLogger(VSockHostClient.class);

	private final Integer cid;

	public VSockHostClient(Integer port, Integer cid, SocketTLV socketTLV) {
		super(port, socketTLV);
		this.cid = cid;
	}

	@Override
	public byte[] send(byte[] content) {
		VSock clientSocket = null;
		InputStream in = null;
		OutputStream out = null;
		ConnectionManager instance = ConnectionManagerSingleton.getInstance(new VSockAddress(5, 5000));
		try {
			clientSocket = instance.getConnection();

			in = clientSocket.getInputStream();
			out = clientSocket.getOutputStream();

			this.socketTLV.sendContent(content, out);

			byte[] rcvd = this.socketTLV.receiveContent(in);
			LOG.info("Received {} bytes", rcvd.length);

			return rcvd;

		} catch (Exception e) {
			LOG.error("VSock error", e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				out.flush();
				in = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (clientSocket != null)
				instance.releaseConnection(clientSocket);
		}
	}

}
