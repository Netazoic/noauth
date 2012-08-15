package com.netazoic.oauth;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;


public abstract class OAuth_Link extends OAuth_2{
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
	/*
	 * Steps in the process:
	 * 	1) Authorize your app
	 * 	 -- https://api.linkedin.com/uas/oauth/requestToken?scope=r_basicprofile+r_emailaddress
	 * 	 -- authenticate: https//www.linkedin.com/uas/oauth/authenticate?oauth_token=XXXXXXXXX
	 * 	 <-- oauth_verifier ??
	 *  2) get the person's json record: 
	 *    request record: http://api.linkedin.com/v1/people/~?format=JSON
	 */
	
	public String urlAccessToken;
	public String urlCallback;
	
	public enum LINK_Param {
		code,state,LINK_State, access_token,
		id,username,email,link,
		name,first_name, last_name, 
		location,work,employer,position,
		gender,
		timezone,locale,
		verified,updated_time, 
		LINK_RequestToken, authStep,
		;

	}
	



	public LINKUser linkUser;

	public class LINKUser extends OUser{
		public String username;
		public String id;
		public String firstName,lastName;
		public String emailAddress;
		public String siteStandardProfileRequest;
		public String siteStandardProfileRequest_valParam = "url";
		public String json;

	}
	
	protected OAuth_Link(HttpServletRequest request){
		urlCallback = request.getServerName() + urlCallback;
		init();
		initService();
	}

	public void init(){
		oUser = new LINKUser();

		urlOAuth =  "https://www.linkedin.com/uas/oauth/authenticate?"
				+ "client_id=" + getAppID() 
				+ "&redirect_uri=" + getUrlLoginRedirect()
				+ "&scope=email"
				+ "&state=";
		
		long nonce = System.nanoTime();
		urlToken = "https://api.linkedin.com/uas/oauth/requestToken?"
				+ "scope=r_basicprofile+r_emailaddress"
				+ "&oauth_nonce=" + nonce
				+ "&oauth_consumer_key=" + getAppID()
				+ "&oauth_signature_method=HMAC-SHA1" 
				+ "&oauth_timestamp=" + nonce
				+ "&oauth_version=1.0";

	
		/*
		oauth_callback (optional)
		oauth_consumer_key
		oauth_nonce
		oauth_signature_method
		oauth_timestamp
		oauth_version
		*/

		//TODO ??
		urlAccessToken = "https://api.linkedin.com/uas/oauth/accessToken";
		
		//http://www.linkedin.com/profile?viewProfile=&key=8219502&authToken=SqI1&authType=name&trk=api*a108281*s116823*
		urlGetUserRec = "http://www.linkedin.com/profile?viewProfile="
				+"&key=" + getAppID()
				+"&authType=name" 
				+"&authToken=";
		
		parmAccessToken = LINK_Param.access_token.name();
		parmReqState = LINK_Param.state.name();
		parmServerCheckState = LINK_Param.LINK_State.name();
		parmRecordValParam = LINK_Param.name.name();

	}
	/* *************************
	 * fieldMap Initialization *
	 * *************************
	 * initialize the fieldMap to hold assignments from fb user-record field names
     * to local app field names with a block like the following:
     * map.<fbUser.field,app.field>*/
	/*
    static {
        Map<String, String> aMap = new HashMap<String,String>();
        aMap.put("email","wuEMail");
        aMap.put("name","wuName");
        aMap.put("first_name","wuFirstName");
        aMap.put("last_name","wuLastName");
        aMap.put("employer","wuCompany");
        aMap.put("position","wuTitle");
        aMap.put("city","wuCity");
        aMap.put("state","usStateName");
        aMap.put("phone","wuTelephone");
        aMap.put("cell","wuCell");
        aMap.put("id","wuFBID");

        aMap.put("password", "wuPassword");
        fieldMap = Collections.unmodifiableMap(aMap);
    }
    */
	
	public void initService(){
		 service = new ServiceBuilder()
		.provider(LinkedInApi.class)
		.apiKey(getAppID())
		.apiSecret(getSecret())
		.scope("r_emailaddress")
		.callback(urlCallback)
		.build();
	}
	//TODO ??
	public void registerWithLINK(){
		//String p = "Please visit this URL: " + request_token.authorize_url + " in your browser and then input the numerical code you are provided here: "

	}
	/*
	 * (non-Javadoc)
	 * @see com.claresco.fcby.OAuth_2#getUserRecord(java.lang.String)
	 * Override for special user record processing
	 */
	public LINKUser getUserRecord(String accessToken) throws Exception {

		//NOT USED IN THIS OAUTH2 SUB-CLASS
		return linkUser;

	}
	
	public LINKUser getUserRecord() throws Exception{
		//Get the user record from Linked-In
		//String url = "http://api.linkedin.com/v1/people/~?format=json";
		String url = "http://api.linkedin.com/v1/people/~:(first-name,last-name,email-address,id,headline,picture-url)?format=json";
		OAuthRequest oaRequest = new OAuthRequest(Verb.GET, url);
		service.signRequest(accessToken, oaRequest);
		Response oaResponse = oaRequest.send();
		String json = oaResponse.getBody();
		super.getUserRecordFromJSON(json);

		linkUser = (LINKUser) oUser;

		return linkUser;
	}

	/*
	 * Person Response:
	 * 
{
  "firstName": "John",
  "headline": "programmer at Claresco Corp.",
  "lastName": "Moore",
  "siteStandardProfileRequest": {"url": "http://www.linkedin.com/profile?viewProfile=&key=9652496&authToken=hMaX&authType=name&trk=api*a202649*s210721*"}
}
	 */
	/*
	 * Error response:
	 * <error>
		  <status>401</status>
		  <timestamp>1343687838351</timestamp>
		  <request-id>10FMEGSWWN</request-id>
		  <error-code>0</error-code>
		  <message>[unauthorized]. Expired access token. Timestamp: 1343685790676</message>
		</error>
	 */
	
	
}


