package com.netazoic.oauth;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;
import com.netazoic.oauth.OAuth_Link.LINK_Param;

public abstract class OAuth_2 {


	
	//App Specific Settings
	private String AppID="123456790";
	private String Secret="BigLongSecretString";
	protected String urlOAuth = "https://WhereWeGetAuthenticated";
	protected String urlLoginRedirect="http://LocalAppURLWhereToGoAfterOAuthLogin";
	protected String urlToken ="https://WhereWeGetTheAccessToken";
	protected String urlGetUserRec = "https://WhereWeGetTheUserRecord"; 
	protected String parmAccessToken = null; //e.g., FB_Param.access_token.name()
	protected String parmReqState = null; //The state parameter as returned by the OAuth provider
	protected String parmServerCheckState = null;
	protected static String parmRecordValParam = null;  //Param used in json records to identify a 
									  //value field in a map; e.g., "name"

	private String accessTokenString;
	
	public OAuthService service;
	public Token requestToken;

	protected Token accessToken;
	protected Verifier verifier;


	public OUser oUser;  //instantiate in concrete class
	
	public enum OAUTH_Param{
		oauth_verifier,
		oauth_token, oauth_token_secret;
	}
	public abstract class OUser<T>{
		public String json;
		public Map<String,Object> userMap;
		public Map<String, String> fieldMap;
		/* initialize with a block like the following:
		 * map.<fbUser.field,app.field>

	    static {
	        Map<String, String> aMap = new HashMap<String,String>();
	        aMap.put("userEmail","wuEmail");
	        aMap.put("userName","wuFirstName");
	        fieldMap = Collections.unmodifiableMap(aMap);
	    }
		 */
		
		protected OUser(){}
		public void deSerialize(String json) throws Exception{
			this.json = json;
			//HashMap<String,String> temp;
			//Map<String,?>userMap = (HashMap<String,Object>)new JSONDeserializer().deserialize(json);
			Gson gson = new Gson();
			userMap =gson.fromJson(json, HashMap.class);
			//TODO
			//should be possible to do something like this . . .
			//FBUser fbu = gson.fromJson(json, FBUser.class);
				try{
				deSerializeObj(this,userMap);
			}catch(Exception ex){
				throw new Exception(ex);
			}
		}
		public void setRequestVars(HttpServletRequest request){
			if(fieldMap == null) fieldMap = initFieldMap();
			setRequestVarsAllFields(this,request,fieldMap,true);
		}
	}

	public OAuth_2() {
		init();
		}
	public void init(){}

	protected Map initFieldMap(){
		return null;
		/* override with concrete class, e.g.,
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
		return aMap */
	}
	public boolean checkState(HttpServletRequest request) throws Exception{
		/*
		 * Check to see if the returned state key matches the session stored state key
		 * 
		 */
		boolean flgCheck = false;
		if(parmReqState == null) throw new Exception("parmReqStat not set. This is not a secure transaction.");
		String reqState = (String)request.getAttribute(parmReqState);
		String sessState = (String)getSessionVal(parmServerCheckState,request);
		if(reqState == null) return flgCheck;
		if(reqState.equals(sessState))flgCheck = true;
		return flgCheck;	
	}
	public void doOAuth(String state,HttpServletRequest request,HttpServletResponse response) {
		setSessionVal(parmServerCheckState,state,request);
		urlOAuth  += state;
		response.setStatus(response.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", urlOAuth);
	}
	
	public String doGetToken(String tokenParm) throws IOException{
		String json = (fileGetContents(urlToken));
		Gson gson = new Gson();
		HashMap<String,String> map = gson.fromJson(json, HashMap.class);
		String token = map.get(tokenParm);
		return token;
	}
	public Token getAccessToken() throws Exception{
		/*
		 * User scribe to do an accessToken request
		 */
		if(accessToken!=null) return accessToken;
		if(requestToken == null) throw new Exception("requestToken not set");
		if(verifier == null) throw new Exception ("verifier not set.");
		accessToken =  service.getAccessToken(requestToken, verifier);
		return accessToken;
	}
	
	public Token getAccessToken(String pin,Token requestToken) throws Exception{
		/*
		 * User scribe to do an accessToken request
		 */
		if(accessToken!=null) return accessToken;

		if(requestToken == null) throw new Exception("requestToken not set");
		verifier = new Verifier(pin);
		if(verifier == null) throw new Exception ("verifier not set.");
		accessToken = service.getAccessToken(requestToken, verifier);
		return accessToken;
	}

	public String getAccessToken(String code) throws Exception {
		/*
		 * Run a straight request for the access token an parse results
		 * Must define tokenParm in the concrete class
		 */

		if(parmAccessToken == null) throw new Exception("tokenParm not set.");
		urlToken  += code;

		String fbResp = fileGetContents(urlToken);
		//look through the response for an 'access_token'
		String[] args = fbResp.split("&");
		String[] parm;
		Map<String,String> map = new HashMap();
		for(String l : args){
			parm = l.split("=");
			map.put(parm[0], parm[1]);
		}
		accessTokenString = map.get(parmAccessToken);
		return accessTokenString;
	}
	
	public String getAuthorizationURL() throws Exception{
		if(requestToken == null) throw new Exception("Request Token not set.");
		return service.getAuthorizationUrl(this.requestToken);
	}
	/**
	 * @return the appID
	 */
	public String getAppID() {
		return AppID;
	}

	public Token getRequestToken(){
		if(requestToken == null)
			requestToken = service.getRequestToken();
		return requestToken;
	}
	/**
	 * @return the secret
	 */
	public String getSecret() {
		return Secret;
	}
	/**
	 * @return the urlLoginRedirect
	 */
	public String getUrlLoginRedirect() {
		return urlLoginRedirect;
	}
	
	public OUser getUserRecord(String accessToken) throws Exception {
		urlGetUserRec += accessToken;
		String json = (fileGetContents(urlGetUserRec));
		try{
			oUser.deSerialize(json);
			oUser.json = json;
		}catch(Exception ex){
			throw new Exception(ex.getMessage() + "\r\n" +json);
			//throw new Exception(ex);
			//ajaxResponse(json, response);
		}
		return oUser;
	}
	
	public OUser getUserRecordFromJSON(String json) throws Exception{
		try{
			oUser.deSerialize(json);
			oUser.json = json;
		}catch(Exception ex){
			throw new Exception(ex.getMessage() + "\r\n" +json);
			//throw new Exception(ex);
			//ajaxResponse(json, response);
		}
		return oUser;

	}
	
	public void goAuthorization(HttpServletResponse response) throws Exception{
		System.out.println(getAuthorizationURL());
		String urlAuthorize =  getAuthorizationURL();
		//Redirect user to the authorize screen
		goURL(urlAuthorize,response);

	}
	
	public Token retrieveRequestToken(HttpServletRequest req){
		return (Token)getSessionVal(LINK_Param.LINK_RequestToken.name(),req);
	}
	public void saveRequestToken(HttpServletRequest req){
		setSessionVal(LINK_Param.LINK_RequestToken.name(),requestToken,req);
	}
	/**
	 * @param appID the appID to set
	 */
	public void setAppID(String appID) {
		AppID = appID;
	}

	
	public void setVerifier(String pin){
		verifier = new Verifier(pin);
	}
	/**
	 * @param secret the secret to set
	 */
	public void setSecret(String secret) {
		Secret = secret;
	}
	/**
	 * @param urlLoginRedirect the urlLoginRedirect to set
	 */
	public void setUrlLoginRedirect(String urlLoginRedirect) {
		this.urlLoginRedirect = urlLoginRedirect;
	}
	
	/* Utility Function */
	

	static void deSerializeObj(Object obj,Map<String,?>userMap) throws IllegalAccessException{
		Field[] flds = obj.getClass().getDeclaredFields();
		String fldName;
		Object valObj;

		for(Field f : flds){
			fldName = f.getName();
			if(fldName.matches("^(fld|itr|nit)_.*"))continue;
			valObj = userMap.get(fldName);
			if(valObj==null) continue;
			setVal(obj,f,valObj,parmRecordValParam);
		}
	}
	

	public void goURL(String url,HttpServletResponse response){
		response.setStatus(response.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", url);
	}
	public static String wget(String urlString) throws IOException{
		return fileGetContents(urlString);
	}
	
	public static String fileGetContents(String urlString) throws IOException{
		byte buf[] = new byte[1024];
		URL url = new URL(urlString);
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		int bytesRead=0;
		String strFileContents = ""; 
		while( (bytesRead = bis.read(buf)) != -1){ 
			strFileContents += new String(buf, 0, bytesRead);               
		}
		bis.close();
		return strFileContents;
	}

	public void filePutContents(String urlString,String tgtFile) throws IOException{
		byte buf[] = new byte[4096];
		URL url = new URL(urlString);
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		FileOutputStream fos = new FileOutputStream(tgtFile);

		int bytesRead = 0;

		while((bytesRead = bis.read(buf)) != -1) {
			fos.write(buf, 0, bytesRead);
		}

		fos.flush();
		fos.close();
		bis.close();
	}





	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		// http://stackoverflow.com/questions/1042798/retrieving-the-inherited-attribute-names-values-using-java-reflection
		for (Field field: type.getDeclaredFields()) {
			fields.add(field);
		}

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}


	public static void setRequestVarsAllFields(Object obj,HttpServletRequest request,
			Map<String,String> fldMap,boolean flgDumbPs){
		/*
		 * At this level, the setRequestVars function can only set values on Public fields in the 
		 * extending class. If you wish to work with private or package scope fields, override
		 * this function with a copy in the local class.
		 */
		//Field[] flds = obj.getClass().getDeclaredFields();
		// To get all fields in the parent class(es) as well:
		List<Field> flds= getAllFields(new LinkedList<Field>(),obj.getClass());
		Object val;
		String fld;
		String appFld;
		for(Field f : flds){
			try{
				fld = f.getName();
				if(fld.matches("^(fld|itr|nit)_.*"))continue;
				val = f.get(obj);
				if(val == null) continue;
				request.setAttribute(fld, val.toString());
				request.setAttribute(fld.toLowerCase(), val.toString());

				if(fldMap!=null){
					appFld = (String)fldMap.get(fld);
					request.setAttribute(appFld, val.toString());
					if(flgDumbPs){
						request.setAttribute("p" + appFld, val.toString());				
					}
				}			
			}catch(Exception ex){
				//Will fail on access of private fields
				String msg = ex.getMessage();
				continue;
			}
		}
	}

	
	public static void setRequestVars(HttpServletRequest request,Object obj,
			Map<String,String> fldMap,boolean flgDumbPs){
		/*
		 * At this level, the setRequestVars function can only set values on Public fields in the 
		 * extending class. If you wish to work with private or package scope fields, override
		 * this function with a copy in the local class.
		 */
		Field[] flds = obj.getClass().getDeclaredFields();
		// To get all fields in the parent class(es) as well:
		// List<Field> flds= getAllFields(new LinkedList<Field>(),obj.getClass());
		Object val;
		String fld;
		for(Field f : flds){
			try{
				fld = f.getName();
				if(fld.matches("^(fld|itr|nit)_.*"))continue;
				val = f.get(obj);
				if(val == null) continue;
				request.setAttribute(fld, val.toString());
				request.setAttribute(fld.toLowerCase(), val.toString());
				if(flgDumbPs){
					request.setAttribute("p" + fld.toLowerCase(), val.toString());				
				}
				if(fldMap!=null){
					request.setAttribute((String)fldMap.get(fld), val.toString());
				}			
			}catch(Exception ex){
				//Will fail on access of private fields
				String msg = ex.getMessage();
				continue;
			}
		}
	}


	private static String setVal(Object tgtObject,Field f,Object valObj,String valParam) throws IllegalArgumentException, IllegalAccessException, SecurityException {
		/*
		 * Set a value into an object field
		 * Recurses on ArrayList values and HashMap values
		 * Assumes that a HashMap will be referenced into an ArrayList structure
		 * by a field name key, and will have a tgtVal parameter (e.g.,"name")
		 * which is the field value.
		 * 
		 * e.g.,  location{id:"12341312",name:"Berkeley, California"}
		 */
		String val = null;
		if(valParam == null) valParam = "name";
		if(valObj==null)return val;
		if(valObj instanceof ArrayList<?>){
			ArrayList objArrayList = (ArrayList)valObj;
			ListIterator<Integer> itr = objArrayList.listIterator();
			f.set(tgtObject, valObj);
			while(itr.hasNext()){
				Object obj= itr.next();
				setVal(tgtObject,f,obj,valParam);
			}
		}else if(valObj instanceof StringMap){
			//gson
			StringMap<String> objStringMap = (StringMap)valObj;
			val = (String)objStringMap.get(valParam);
			//if(val == null) val = objStringMap.get(objStringMap.keySet().iterator().next());
			if(val != null){
				f.set(tgtObject, val);
			}
			else{
				for(String k : objStringMap.keySet()){
					try{
						Field fld = tgtObject.getClass().getField(k);
						setVal(tgtObject,fld,objStringMap.get(k),valParam);
					}catch(NoSuchFieldException ex){
						//nada
						continue;
					}
				}
			}
		}
		else if(valObj instanceof HashMap<?,?>){
			HashMap<String,Object> objHashMap = (HashMap)valObj;
			val = (String)objHashMap.get(valParam);
			if(val != null){
				f.set(tgtObject, val);
			}
			else{
				for(String k : objHashMap.keySet()){
					try{
						Field fld = tgtObject.getClass().getField(k);
						setVal(tgtObject,fld,objHashMap.get(k),valParam);
					}catch(NoSuchFieldException ex){
						//nada
						continue;
					}
				}
			}
		}else if(valObj instanceof String){
			val = (String)valObj;
			f.set(tgtObject, val);
		}

		return val;
	}
	
	private Object getSessionVal(String key,HttpServletRequest req){
		// Get  a session-scoped value
		Object value = null;
		HttpSession session = req.getSession(false);
		if (session != null) {
		    value = session.getAttribute(key);
		}
		return value;
	}

	private void setSessionVal(String key,Object val,HttpServletRequest req){
		// Set  a session-scoped value
		HttpSession session = req.getSession(true);
		if (session != null) {
		    session.setAttribute(key, val);
		}
	}


}