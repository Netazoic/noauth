package com.netazoic.oauth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;


public abstract class OAuth_Link extends OAuth_2{
	/*
	 * This is a very simple wrapper around the Scribe oauth 1.0 client for Linkedin
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


	public String urlCallback,urlCallbackQuery;

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

	public class LINKUser extends OUser<LINKUser>{
		public String userName;
		public String id;
		public String firstName,lastName;
		public String emailAddress;
		String company;
		String title;
		String industry;
		String location;
		//Location location;
		//Position[] positions;
		class Location{
			String code;
			String name;
		}
		class Position{
			String company;
			String title;
			String industry;
		}
		
		LINKUser(){
			super();
		}
	}
	


	protected OAuth_Link(HttpServletRequest request){
		init();
		urlCallback = request.getRequestURL() + urlCallbackQuery;
		initService();

	}

	public void init(){
		oUser = new LINKUser();

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

	public LINKUser getUserRecord() throws Exception{
		//Get the user record from Linked-In
		//String url = "http://api.linkedin.com/v1/people/~?format=json";
		String url = "http://api.linkedin.com/v1/people/~:(first-name,last-name,email-address,id,headline,picture-url,location,positions)?format=json";
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
  "emailAddress": "jmoore@claresco.com",
  "firstName": "John",
  "headline": "programmer at Claresco Corp.",
  "id": "slslslslsl",
  "lastName": "Moore",
  "location": {
    "country": {"code": "us"},
    "name": "San Francisco Bay Area"
  },
  "pictureUrl": "http://m3.licdn.com/mpr/mprx/0_sMe3tGFMvPWhh5hAsUUOt8FJzng826TA4YyYt8NJoPWK1Cnl9xZDphhcR4jx738jUsE0gFo__qRn",
  "positions": {
    "_total": 2,
    "values": [
      {
        "company": {
          "industry": "Information Technology and Services",
          "name": "Free Burma Rangers"
        },
        "id": 156081627,
        "isCurrent": true,
        "startDate": {"year": 2003},
        "summary": "",
        "title": "web monkey"
      },
      {
        "company": {
          "industry": "Information Technology and Services",
          "name": "Claresco Corp."
        },
        "id": 13393405,
        "isCurrent": true,
        "startDate": {"year": 1999},
        "summary": "",
        "title": "programmer"
      }
    ]
  }
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


