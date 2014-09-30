package com.netazoic.noauth;

import javax.servlet.http.HttpServletRequest;

public class OAuth_Link_Neta extends OAuth_Link {


	/*
	 *  This is an example of an insantiating class for the Linkedin
	 *  client.

	 * Company:
	Netazoic

	Application Name:
	Netazoic

	API Key:
	991bcd232alkj223

	Secret Key:
	tw0823232323u5T

	OAuth User Token:
	e9e89530-9999-4319-9bo0-be32a9ckf912

	OAuth User Secret:
	38407e78-9999-4319-a653-ab96e512ef8d
	 */
	
	private String APP_ID = "991bcd232alkj223";
	private String SECRET ="tw0823232323u5T";
	private String URL_CALLBACK = "/fcby?pAction=linklogin";
	
	protected OAuth_Link_Neta(HttpServletRequest request) {
		super(request);
	}
	public void init(){
		setAppID(APP_ID);
		setSecret(SECRET);
		urlCallback = URL_CALLBACK;
		super.init();
	}
}
