package com.mpnet.controllers.system;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.Response;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.controllers.BaseControllerCommand;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.core.MPConstants;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPErrorCode;
import com.mpnet.exceptions.MPRequestValidationException;
import com.mpnet.util.CryptoUtils;

/**
 * 
 * @ClassName: Handshake 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:43:23 
 *
 */
public class Handshake extends BaseControllerCommand {
	public static final String KEY_BIN_FLAG = "bin";
	public static final String KEY_API = "api";
	public static final String KEY_TOKEN = "tk";
	public static final String KEY_COMPRESSION_THRESHOLD = "ct";
	public static final String KEY_RECONNECTION_TOKEN = "rt";
	public static final String KEY_CLIENT_TYPE = "cl";
	public static final String KEY_MAX_MESSAGE_SIZE = "ms";

	public Handshake() {
		super(SystemRequest.Handshake);
	}

	public void execute(IRequest request) throws Exception {
		ISession sender = request.getSender();

		IMPObject reqObj = (IMPObject) request.getContent();
		String apiVersionStr = reqObj.getUtfString(KEY_API);
		String reconnectionToken = reqObj.getUtfString(KEY_RECONNECTION_TOKEN);
		IMPObject resObj = MPObject.newInstance();

		if (!isApiVersionOk(apiVersionStr)) {

			resObj.putShort(KEY_ERROR_CODE, MPErrorCode.HANDSHAKE_API_OBSOLETE.getId());
			resObj.putUtfString(KEY_ERROR_PARAMS, apiVersionStr+""+formatVersionNumber(mpserver.getMinClientApiVersion()));
		} else {
			String sessionToken = null;

			if (reconnectionToken != null) {
				ISession resumedSession = mpserver.getSessionManager().reconnectSession(sender, reconnectionToken);

				if (resumedSession == null) {
					return;
				}

				sender = resumedSession;
				sessionToken = sender.getHashId();

				User user = mpserver.getUserManager().getUserBySession(sender);

				if (user == null) {
					logger.warn("User not found at reconnection time. " + sender);
				} else {
					user.updateLastRequestTime();
					logger.info("Reconnected USER: " + user + ", logged: " + sender.isLoggedIn());
				}

			} else {
				sessionToken = CryptoUtils.getUniqueSessionToken(sender);
			}

			sender.setSystemProperty(MPConstants.SESSION_CLIENT_TYPE,reqObj.containsKey(KEY_CLIENT_TYPE) ? reqObj.getUtfString(KEY_CLIENT_TYPE) : "Unknown");

			sender.setHashId(sessionToken);
			resObj.putUtfString(KEY_TOKEN, sessionToken);
			resObj.putInt(KEY_COMPRESSION_THRESHOLD,mpserver.getConfigurator().getServerSettings().protocolCompressionThreshold);
			resObj.putInt(KEY_MAX_MESSAGE_SIZE, mpserver.getConfigurator().getServerSettings().maxIncomingRequestSize);
		}

		IResponse response = new Response();
		response.setId(Short.valueOf(getId()));
		response.setRecipients(sender);
		response.setContent(resObj);
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);

		response.write();
	}

	private String formatVersionNumber(int ver) {
		String unformatted = String.valueOf(ver);
		int additionalZeros = 3 - unformatted.length();

		StringBuffer sb = new StringBuffer();

		if (additionalZeros > 0) {
			for (int j = 0; j < additionalZeros; j++) {
				sb.append('0');
			}
		}

		sb.append(unformatted);

		int bottomPos = sb.length() - 1;
		sb.insert(bottomPos, '.');
		bottomPos--;
		sb.insert(bottomPos, '.');

		return sb.toString();
	}

	public boolean validate(IRequest request) throws MPRequestValidationException {
		IMPObject reqObj = (IMPObject) request.getContent();
		ISession sender = request.getSender();

		if (!reqObj.containsKey(KEY_API)) {
			throw new MPRequestValidationException("Missing 'api' flag in Handshake Request. Sender: " + sender);
		}
		String clientType = reqObj.getUtfString(KEY_CLIENT_TYPE);
		if ((clientType != null) && (clientType.length() > 512)) {
			throw new MPRequestValidationException("Illegal ClientType field length (> 512 chars). Sender: " + sender);
		}

		return true;
	}

	private boolean isApiVersionOk(String apiVersionStr) {
		boolean ok = false;

		apiVersionStr = apiVersionStr.replace(".", "");

		int apiVersionNumber = -1;
		try {
			apiVersionNumber = Integer.parseInt(apiVersionStr);
		} catch (NumberFormatException localNumberFormatException) {
		}
		if (apiVersionNumber >= mpserver.getMinClientApiVersion()) {
			ok = true;
		}
		return ok;
	}
}