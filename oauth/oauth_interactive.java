package com.netazoic.oauth;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.claresco.servlet.ClarescoActionHandler;
import com.claresco.servlet.ClarescoException;
import com.claresco.servlet.Session;
import com.claresco.util.ConnectionInUseException;

/*
 * Handle a login coming from LinkedIn
 */
public class oauth_interactive{

	oauth_interactive(HttpServletRequest request,
			HttpServletResponse response, Session userSession)
					throws ClarescoException, IOException, ConnectionInUseException{
		//NOT CURRENTLY SUPPORTED
		/*
		 * Roll-your-own oauth on Linked-IN is a total PITA.
		 * If you want to try it again, try reading this:
		 * 
		 * http://stackoverflow.com/questions/7961095/how-do-i-get-a-linkedin-request-token
		 * 
		 * Or,use Scribe
		 */

		OAuthService service = new ServiceBuilder()
		.provider(LinkedInApi.class)
		//These are Netazoic keys
		.apiKey("991eu7d3kl1h")
		.apiSecret("tw08x8LPBX0nVu5T")
		.build();

		Scanner in = new Scanner(System.in);

		// Obtain the Request Token
		System.out.println("Fetching the Request Token...");
		Token requestToken = service.getRequestToken();
		System.out.println("Got the Request Token!");
		System.out.println();

		System.out.println("Now go and authorize Scribe here:");
		System.out.println(service.getAuthorizationUrl(requestToken));
		System.out.println("And paste the verifier here");
		System.out.print(">>");
		Verifier verifier = new Verifier(in.nextLine());
		System.out.println();

		// Trade the Request Token and Verfier for the Access Token
		System.out.println("Trading the Request Token for an Access Token...");
		Token accessToken = service.getAccessToken(requestToken, verifier);
		System.out.println("Got the Access Token!");
		System.out.println("(if your curious it looks like this: " + accessToken + " )");
		System.out.println();

		/*
		// Now let's go and ask for a protected resource!
		System.out.println("Now we're going to access a protected resource...");
		OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println("Got it! Lets see what we found...");
		System.out.println();
		System.out.println(response.getBody());

		System.out.println();
		System.out.println("Thats it man! Go and build something awesome with Scribe! :)");
		 */
	}
}

