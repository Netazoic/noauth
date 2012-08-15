package com.netazoic.oauth;

import javax.servlet.http.HttpServletRequest;

public class OAuth_Link_Neta extends OAuth_Link {


	/*
	 * Company:
	Netazoic

	Application Name:
	Netazoic

	API Key:
	991eu7d3kl1h

	Secret Key:
	tw08x8LPBX0nVu5T

	OAuth User Token:
	e9e98530-67ff-4914-9af0-be53b7b1f912

	OAuth User Secret:
	38407e78-8c45-44e7-a653-a8e6e511ee8d
	 */
	
	private String APP_ID = "991eu7d3kl1h";
	private String SECRET = "38407e78-8c45-44e7-a653-a8e6e511ee8d";
	private String URL_CALLBACK = "/fcby?pAction=linklogin";
	
	protected OAuth_Link_Neta(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}
	public void init(){
		setAppID(APP_ID);
		setSecret(SECRET);
		urlCallback = URL_CALLBACK;
		super.init();
	}
}
