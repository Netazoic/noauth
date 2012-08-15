package com.netazoic.oauth;

import java.util.ArrayList;


public abstract class OAuth_FB extends OAuth_2 {
	public enum FB_Param {
		code,state,FB_State, access_token,
		id,username,email,link,
		name,first_name, last_name, 
		location,work,employer,position,
		gender,
		timezone,locale,
		verified,updated_time;
	}


	public FBUser fbUser;

	public class FBUser extends OUser{
		FBUser() {
			super();
		}
		public String username;
		public String id;
		public String name,first_name,last_name;
		public String email;
		public String cell;
		public String phone;
		public String location;
		public String state;
		public String city;
		public String countryCode;
		public String stateCode;
		public String postalCode;
		public String employer;
		public String position;
		public String locale;
		public String timezone;
		public String gender;
		public ArrayList work;

	}

	public void init(){
		oUser = new FBUser();
		urlOAuth =  "https://www.facebook.com/dialog/oauth?"
				+ "client_id=" + getAppID() 
				+ "&redirect_uri=" + getUrlLoginRedirect()
				+ "&scope=email"
				+ "&state=";
		
		urlToken = "https://graph.facebook.com/oauth/access_token?"
				+ "client_id=" + getAppID()
				+ "&redirect_uri=" + getUrlLoginRedirect()
				+ "&client_secret=" + getSecret()
				+ "&code=";
	
		urlGetUserRec = "https://graph.facebook.com/me?access_token="; 
		
		parmAccessToken = FB_Param.access_token.name();
		parmReqState = FB_Param.state.name();
		parmServerCheckState = FB_Param.FB_State.name();
		parmRecordValParam = FB_Param.name.name();

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
	
	/*
	 * (non-Javadoc)
	 * @see com.claresco.fcby.OAuth_2#getUserRecord(java.lang.String)
	 * Override for special user record processing
	 */
	public FBUser getUserRecord(String accessToken) throws Exception {
		super.getUserRecord(accessToken);
		fbUser = (FBUser)oUser;
		try{
			if(fbUser.location != null){
				fbUser.city = fbUser.location.substring(0,fbUser.location.indexOf(","));
				fbUser.state = fbUser.location.substring(fbUser.location.indexOf(",")+1);
				//fbUser.stateCode = getStateCode(fbUser.state);
			}
		}catch(Exception ex){
			throw new Exception(ex.getMessage() + "\r\n" +fbUser.json);
			//throw new Exception(ex);
			//ajaxResponse(json, response);
		}
		return fbUser;

	}






}
