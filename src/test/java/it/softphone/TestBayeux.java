package it.softphone;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.cometd.bayeux.Channel;
import org.eclipse.jetty.util.ajax.JSON;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesforce.emp.connector.BayeuxParameters;
import com.salesforce.emp.connector.EmpConnector;
import com.salesforce.emp.connector.TopicSubscription;
import com.salesforce.emp.connector.example.LoggingListener;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TestBayeux {
	private final static String login = "https://login.salesforce.com";
	//private final static String token = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiIzTVZHOUxCSkxBcGVYX1BEVTBYT3hfeTJreVBlTXk2WUR6OGh6TkJ5QnY2akdEeXFJN1ZyMFpqNmk5WEVsSXp0V0htcXJ2dnFWMkFadkhTWmxWbEJnIiwic3ViIjoiYWxlc3NhbmRyby5jYXNvbGxhLm9tbmlAc29mdHBob25lLml0IiwiZXhwIjoxNTc1NTg3NTI0LCJhdWQiOiJodHRwczovL2xvZ2luLnNhbGVzZm9yY2UuY29tIn0.fOrFpDAf-IpbVJj4OVe_KrBX7DUBkhQTr8GBEkkvxysdboVCi1B08-KrOW__JquK0EWyLNemjuqCIQFtPGuPZ5gfhKtRXbssNWxAWJvmXgsOTYnzUp0zasrJJdRsuCWEZIHGzC0maOrku4AlvSof6HsLCq1-02oemkL4-ZCvGgPz3PUefkmS9mA5btCs0ew5i6-KIgfbDWYKNpf6Dv6rYsLCiIaKF-pUf0XHDkOS0Fh0C4vNY9VcYqHVOOjfdyK43UaUacbkcNdNzBHa9G79GbY2n9r-yihahmqqhQMUyx5Kr38TTPUw5cnb4NJOnJPhR1j_fzRr6f4H_uB9HjzEsg";
	private final static String topic = "/topic/ExternalRoutingPSR";
	private final static long replayFrom = EmpConnector.REPLAY_FROM_EARLIEST;

	private final static String bayeux_login = "https://cti-connector-test.my.salesforce.com";
	
	private final static int JWT_TOKEN_VALIDITY = 100;
	private final static String audience = "https://login.salesforce.com";
	private final static String subject = "alessandro.casolla.omni@softphone.it";
	private final static String issuer = "3MVG9LBJLApeX_PDU0XOx_y2kyPeMy6YDz8hzNByBv6jGDyqI7Vr0Zj6i9XElIztWHmqrvvqV2AZvHSZlVlBg";
	
	private final static String jksPath = "/Users/ale/Documents/certificates/salesforce/tomcat.keystore";
	private final static String jksPassword = "password";
	private final static String jksKey = "tomcat";

	private static final Logger logger = LoggerFactory.getLogger(TestBayeux.class);

	@Test
	public void token()  {
		
		
		try {
			final String token = getToken();
			
		 BayeuxParameters params = new BayeuxParameters() {

	            @Override
	            public String bearerToken() {
	                return token;
	            }

	            @Override
	            public URL host() {
	                try {
	                    return new URL(bayeux_login);
	                } catch (MalformedURLException e) {
	                    throw new IllegalArgumentException(String.format("Unable to create url: %s", login), e);
	                }
	            }
	        };

	        Consumer<Map<String, Object>> consumer = event -> logger.info(String.format("Received:\n%s", JSON.toString(event)));
	        EmpConnector connector = new EmpConnector(params);

	        connector.addListener(Channel.META_CONNECT, new LoggingListener(true, true))
	        .addListener(Channel.META_DISCONNECT, new LoggingListener(true, true))
	        .addListener(Channel.META_HANDSHAKE, new LoggingListener(true, true));

	        connector.start().get(5, TimeUnit.SECONDS);

	        TopicSubscription subscription = connector.subscribe(topic, replayFrom, consumer).get(5, TimeUnit.SECONDS);
	        
	        logger.info(String.format("Subscribed: %s", subscription));
		}catch(Exception e ) {
			logger.error("error ",e);
		}
	}
	
	
	
	public String getToken() throws Exception {
		
			Date exp = new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000);
		    
		    KeyStore keystore = KeyStore.getInstance("JKS");
		             keystore.load(new FileInputStream(jksPath), jksPassword.toCharArray());
		    PrivateKey privateKey = (PrivateKey) 
		             keystore.getKey(jksKey, 
		            		 jksPassword.toCharArray());
		String jwt = Jwts.builder() //
		      .setIssuer(issuer) // identifies principal that issued the JWT
		      .setSubject(subject) // identifies the subject of the JWT
		      .setExpiration(exp) // identifies the expiration time on or 
		                          // after which the JWT must not be
		                          // accepted for processing
		      .setAudience(audience) // identifies the recipients that the 
		                        // JWT is intended for
		      .signWith(SignatureAlgorithm.RS256, privateKey) //
		      .compact();
		     logger.info("token :\n{}",jwt); 
		   return jwt;
		  //   return "00D6g000000GwPe!AQoAQGwjPFOJ2iwI4MbSrIVtJ_2FdbVT95ZHqjwjBfc6UZDfoT66mBsgliQODtJAIKPpOj.sZQh0WiFL2_mOeepfm68SAjDs";
	}

}
