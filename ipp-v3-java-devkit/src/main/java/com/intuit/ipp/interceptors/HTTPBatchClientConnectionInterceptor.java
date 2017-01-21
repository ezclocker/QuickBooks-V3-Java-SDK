package com.intuit.ipp.interceptors;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.exception.CompressionException;
import com.intuit.ipp.exception.ConfigurationException;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.net.IntuitRetryPolicyHandler;
import com.intuit.ipp.net.MethodType;
import com.intuit.ipp.util.Config;
import com.intuit.ipp.util.Logger;
import com.intuit.ipp.util.PropertyHelper;
import com.intuit.ipp.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;


import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * Interceptor to establish a HTTP connection
 * This version merges multiple entities into single request
 *
 */
public class HTTPBatchClientConnectionInterceptor implements Interceptor {

	/**
	 * logger instance
	 */
	private static final org.slf4j.Logger LOG = Logger.getLogger();

	/**
	 * variable PORT_443
	 */
	private static final int PORT_443 = 443;

	/**
	 * variable LENGTH_256
	 */
	private static final int LENGTH_256 = 256;

    /**
     * Handles HttpEntities and merges them into one HttpEntity
     */
    private EntitiesManager entitiesManager= new EntitiesManager();


    /**
     * Major executor. It is not part of interface
     * @param intuitMessages
     * @throws FMSException
     */
    public void execute(List<IntuitMessage> intuitMessages) throws FMSException {
        LOG.debug("Enter HTTPBatchClientConnectionInterceptor - batch...");
        AbstractHttpClient client = createClient();
        entitiesManager.reset();
        registerProxy(client);

        HttpRequestBase httpRequest = prepareHttpRequest(getFirst(intuitMessages).getRequestElements(), client);

        //Additional processing for POST requests
        if(httpRequest instanceof HttpPost) {
            for (IntuitMessage intuitMessage : intuitMessages) {
                execute(intuitMessage);
            }
            try {
                ((HttpPost) httpRequest).setEntity(entitiesManager.asSingleEntity());
            } catch (IOException ex) {
                LOG.debug("HTTPBatchClientConnectionInterceptor was unable to convert input into single HTTP entity");
                throw new FMSException("Unable to prepare http entity" + ex.getMessage() + "\n" + ex.getStackTrace());
            } finally {
                entitiesManager.reset();
            }


        }

        //Method to send request
        IntuitMessage intuitMessage = executeHttpRequest(httpRequest,client);

        //TODO review this implementation, since it is using one temporary instance to fill up response elements in original
        for (IntuitMessage originalMessage : intuitMessages) {
            ResponseElements originalResponseElements = originalMessage.getResponseElements();
            ResponseElements receivedResponseElements = intuitMessage.getResponseElements();

            originalResponseElements.setResponseContent(   receivedResponseElements.getResponseContent()   );
            originalResponseElements.setEncodingHeader(    receivedResponseElements.getEncodingHeader()    );
            originalResponseElements.setContentTypeHeader( receivedResponseElements.getContentTypeHeader() );
            originalResponseElements.setStatusLine(        receivedResponseElements.getStatusLine()        );
            originalResponseElements.setStatusCode(        receivedResponseElements.getStatusCode()        );

        }
        LOG.debug("Exit HTTPBatchClientConnectionInterceptor.");

    }

    /**
     * Returns first item from the list
     * @param intuitMessages
     * @return IntuitMessage
     * @throws FMSException
     */
    private IntuitMessage getFirst(List<IntuitMessage> intuitMessages) throws FMSException {
        if(intuitMessages.isEmpty()) {
            throw new FMSException("IntuitMessages list is empty. Nothing to upload.");
        }
        return intuitMessages.get(0);
    }

    /**
     * Returns HttpClient instance
     * @return DefaultHttpClient
     * @throws FMSException
     */
    private AbstractHttpClient createClient() throws FMSException {
        DefaultHttpClient client = new DefaultHttpClient();
        IntuitRetryPolicyHandler handler = getRetryHandler();
        client.setHttpRequestRetryHandler(handler);
        return client;
    }

    /**
     * Configures proxy if this is applicable to connection
     * @param client
     * @param <T>
     * @throws FMSException
     */
    private <T extends AbstractHttpClient> void registerProxy(T client) throws FMSException {
        HttpHost proxy = getProxy();

        if (proxy != null) {
            setProxyAuthentication(client);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            Scheme sch = getSSLScheme();
            if (sch != null) {
                client.getConnectionManager().getSchemeRegistry().register(sch);
            }
        }
    }

    /**
     * Returns httpRequest instance with configured fields
     * @param intuitRequest
     * @param client
     * @param <T>
     * @return
     * @throws FMSException
     */
    private <T extends AbstractHttpClient> HttpRequestBase prepareHttpRequest(RequestElements intuitRequest, T client ) throws FMSException
    {
        setTimeout(client, intuitRequest.getContext());
        HttpRequestBase httpRequest =  extractMethod(intuitRequest, extractURI(intuitRequest));

        // populate the headers to HttpRequestBase
        populateRequestHeaders(httpRequest, intuitRequest.getRequestHeaders());

        // authorize the request
        authorizeRequest(intuitRequest.getContext(), httpRequest);

        LOG.debug("Request URI : " + httpRequest.getURI());
        LOG.debug("Http Method : " + httpRequest.getMethod());

        return httpRequest;
    }

    /**
     * Returns URI instance which will be used as a connection source
     * @param intuitRequest
     * @return URI
     * @throws FMSException
     */
    private URI extractURI(RequestElements intuitRequest) throws FMSException
    {
        URI uri = null;

        try {
            uri = new URI(intuitRequest.getRequestParameters().get(RequestElements.REQ_PARAM_RESOURCE_URL));
        } catch (URISyntaxException e) {
            throw new FMSException("URISyntaxException", e);
        }
        if(null == uri) {
            throw new FMSException("URI is empty");
        }
        return uri;
    }

    /**
     * Returns instance of HttpGet or HttpPost type, depends from request parameters
     * @param intuitRequest
     * @param uri
     * @return HttpRequestBase
     * @throws FMSException
     */
    private HttpRequestBase extractMethod(RequestElements intuitRequest, URI uri) throws FMSException {
        String method = intuitRequest.getRequestParameters().get(RequestElements.REQ_PARAM_METHOD_TYPE);

        if (method.equals(MethodType.GET.toString())) {
            return new HttpGet(uri);
        } else if (method.equals(MethodType.POST.toString())) {
            return new HttpPost(uri);
        }
        throw new FMSException("Unexpected HTTP method");
    }

    /**
     * Executes communication with remote host
     * @param httpRequest
     * @param client
     * @return IntuitMessage
     * @throws FMSException
     */
    private  IntuitMessage executeHttpRequest(HttpRequestBase httpRequest, AbstractHttpClient client) throws FMSException {
        HttpResponse httpResponse = null;
        IntuitMessage intuitMessage = new IntuitMessage();


        try {
            // prepare HttpHost object
            HttpHost target = new HttpHost(httpRequest.getURI().getHost(), -1, httpRequest.getURI().getScheme());
            httpResponse = client.execute(target, httpRequest);
            LOG.debug("Connection status : " + httpResponse.getStatusLine());
            // set the response elements before close the connection
            setResponseElements(intuitMessage, httpResponse);
            return intuitMessage;
        } catch (ClientProtocolException e) {
            throw new ConfigurationException("Error in Http Protocol definition", e);
        } catch (IOException e) {
            throw new FMSException(e);
        } finally {
            if (client != null) {
                try {
                    client.getConnectionManager().shutdown();
                } catch (Exception e) {
                    LOG.warn("Unable to close HttpClient connection.", e);
                }
            }
        }
    }



	/**
	 * This methods exists mostly for compatibility with interface
     * {@inheritDoc}}
	 */
	@Override
	public void execute(IntuitMessage intuitMessage) throws FMSException {
        entitiesManager.add(populateEntity(intuitMessage.getRequestElements()));
	}



    /**
	 * Method to populate the HTTP request headers by reading it from the requestHeaders Map
	 *
	 * @param httpRequest the http request
	 * @param requestHeaders the request headers
	 */
	private void populateRequestHeaders(HttpRequestBase httpRequest, Map<String, String> requestHeaders) {

		Set<String> keySet = requestHeaders.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		while (keySetIterator.hasNext()) {
			String key = keySetIterator.next();
			String value = requestHeaders.get(key);
			httpRequest.addHeader(key, value);
		}

		PropertyHelper propertyHelper = PropertyHelper.getInstance();
		String requestSource = propertyHelper.getRequestSource() + propertyHelper.getVersion();
		if(propertyHelper.getRequestSourceHeader() != null){
			httpRequest.addHeader(propertyHelper.getRequestSourceHeader(), requestSource);
		}

	}

	/**
	 * Method to authorize the given HttpRequest
	 *
	 * @param context the context
	 * @param httpRequest the http request
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private void authorizeRequest(Context context, HttpRequestBase httpRequest) throws FMSException {
		context.getAuthorizer().authorize(httpRequest);
	}

	/**
	 * Method to get the retry handler which is used to retry to establish the HTTP connection
	 *
	 * @return returns the IntuitRetryPolicyHandler
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private IntuitRetryPolicyHandler getRetryHandler() throws FMSException {
		IntuitRetryPolicyHandler handler = null;
		String policy = Config.getProperty(Config.RETRY_MODE);
		if (policy.equalsIgnoreCase("fixed")) {
			String retryCountStr = Config.getProperty(Config.RETRY_FIXED_COUNT);
			String retryIntervalStr = Config.getProperty(Config.RETRY_FIXED_INTERVAL);
			try {
				handler = new IntuitRetryPolicyHandler(Integer.parseInt(retryCountStr),
						Integer.parseInt(retryIntervalStr));
			} catch (NumberFormatException e) {
				throw new ConfigurationException(e);
			}

		} else if (policy.equalsIgnoreCase("incremental")) {
			String retryCountStr = Config.getProperty(Config.RETRY_INCREMENTAL_COUNT);
			String retryIntervalStr = Config.getProperty(Config.RETRY_INCREMENTAL_INTERVAL);
			String retryIncrementStr = Config.getProperty(Config.RETRY_INCREMENTAL_INCREMENT);
			try {
				handler = new IntuitRetryPolicyHandler(Integer.parseInt(retryCountStr),
						Integer.parseInt(retryIntervalStr), Integer.parseInt(retryIncrementStr));
			} catch (NumberFormatException e) {
				throw new ConfigurationException(e);
			}

		} else if (policy.equalsIgnoreCase("exponential")) {
			String retryCountStr = Config.getProperty(Config.RETRY_EXPONENTIAL_COUNT);
			String minBackoffStr = Config.getProperty(Config.RETRY_EXPONENTIAL_MIN_BACKOFF);
			String maxBackoffStr = Config.getProperty(Config.RETRY_EXPONENTIAL_MAX_BACKOFF);
			String deltaBackoffStr = Config.getProperty(Config.RETRY_EXPONENTIAL_DELTA_BACKOFF);

			try {
				handler = new IntuitRetryPolicyHandler(Integer.parseInt(retryCountStr),
						Integer.parseInt(minBackoffStr), Integer.parseInt(maxBackoffStr),
						Integer.parseInt(deltaBackoffStr));
			} catch (NumberFormatException e) {
				throw new ConfigurationException(e);
			}
		}

		return handler;
	}

	/**
	 * Method to get the SSL scheme
	 *
	 * @return returns Scheme
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	public Scheme getSSLScheme() throws FMSException {
		String path = Config.getProperty(Config.PROXY_KEYSTORE_PATH);
		String pass = Config.getProperty(Config.PROXY_KEYSTORE_PASSWORD);
		Scheme scheme = null;
		FileInputStream instream = null;
		KeyStore trustStore = null;
		SSLSocketFactory factory = null;

		if (path != null && pass != null) {
			try {
				trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				instream = new FileInputStream(new File(path));
				trustStore.load(instream, pass.toCharArray());
				factory = new SSLSocketFactory(trustStore);
				scheme = new Scheme("https", PORT_443, factory);
			} catch (KeyStoreException e) {
				LOG.error("KeyStoreException", e);
				throw new FMSException(e);
			} catch (FileNotFoundException e) {
				LOG.error("FileNotFoundException", e);
				throw new FMSException(e);
			} catch (NoSuchAlgorithmException e) {
				LOG.error("NoSuchAlgorithmException", e);
				throw new FMSException(e);
			} catch (CertificateException e) {
				LOG.error("CertificateException", e);
				throw new FMSException(e);
			} catch (IOException e) {
				LOG.error("IOException", e);
				throw new FMSException(e);
			} catch (KeyManagementException e) {
				LOG.error("KeyManagementException", e);
				throw new FMSException(e);
			} catch (UnrecoverableKeyException e) {
				LOG.error("UnrecoverableKeyException", e);
				throw new FMSException(e);
			} finally {
				try {
					instream.close();
				} catch (IOException e) {
					LOG.error("IOException", e);
					throw new FMSException(e);
				}
			}
		}
		return scheme;
	}

	/**
	 * Method to get proxy
	 *
	 * @return returns HttpHost
	 */
	public HttpHost getProxy() {
		String host = Config.getProperty(Config.PROXY_HOST);
		String port = Config.getProperty(Config.PROXY_PORT);
		HttpHost proxy = null;
		if (StringUtils.hasText(host) && StringUtils.hasText(port)) {
			proxy = new HttpHost(host, Integer.parseInt(port));
		}
		return proxy;
	}

	/**
	 * Method to set proxy authentication
	 *
	 * @param httpclient the http client
	 */
	public <T extends AbstractHttpClient> void setProxyAuthentication(T httpclient) {
		String username = Config.getProperty(Config.PROXY_USERNAME);
		String password = Config.getProperty(Config.PROXY_PASSWORD);

		if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
			String host = Config.getProperty(Config.PROXY_HOST);
			String port = Config.getProperty(Config.PROXY_PORT);
			if (StringUtils.hasText(host) && StringUtils.hasText(port)) {
				httpclient.getCredentialsProvider().setCredentials(new AuthScope(host, Integer.parseInt(port)), new UsernamePasswordCredentials(username, password));
			}
		}
	}

	/**
	 * Method to set the response elements by reading the values from HttpResponse
	 *
	 * @param intuitMessage the intuit message object
	 * @param httpResponse the http response object
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private void setResponseElements(IntuitMessage intuitMessage, HttpResponse httpResponse) throws FMSException {
		ResponseElements responseElements = intuitMessage.getResponseElements();
		if(httpResponse.getLastHeader(RequestElements.HEADER_PARAM_CONTENT_ENCODING) != null)
		{
		responseElements.setEncodingHeader(httpResponse.getLastHeader(RequestElements.HEADER_PARAM_CONTENT_ENCODING).getValue());
		}
		else
		{
			responseElements.setEncodingHeader(null);
		}
		if(httpResponse.getLastHeader(RequestElements.HEADER_PARAM_CONTENT_TYPE) !=null)
		{
		responseElements.setContentTypeHeader(httpResponse.getLastHeader(RequestElements.HEADER_PARAM_CONTENT_TYPE).getValue());
		}
		else
		{
			responseElements.setContentTypeHeader(null);
		}
		responseElements.setStatusLine(httpResponse.getStatusLine());
		responseElements.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        try {
            responseElements.setResponseContent(getCopyOfResponseContent(httpResponse.getEntity().getContent()));
        } catch (IllegalStateException e) {
            LOG.error("IllegalStateException while get the content from HttpRespose.", e);
            throw new FMSException(e);
        } catch (Exception e) {
            LOG.error("IOException in HTTPClientConnectionInterceptor while reading the entity from HttpResponse.", e);
            throw new FMSException(e);
        }
	}

	/**
	 * Method to create the copy of the input stream of response body. This is required while decompress the original content
	 *
	 * @param is the input stream of the response body
	 * @return InputStream the copy of response body
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private InputStream getCopyOfResponseContent(InputStream is) throws FMSException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream copyIs = null; 
		try {
			byte[] bbuf = new byte[LENGTH_256];
			while (true) {
				int r = is.read(bbuf);
				if (r < 0) {
					break;
				}
				baos.write(bbuf, 0, r);
			}
			copyIs = new ByteArrayInputStream(baos.toByteArray());
			return copyIs;
		} catch (IOException ioe) {
			LOG.error("IOException while decompress the data using GZIP compression.", ioe);
			throw new CompressionException(ioe);
		} finally {
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					LOG.error("Unable to close ByteArrayOutputStream.");
				}
			}
		}
	}
	
	/**
	 * Method to set the connection and request timeouts by reading from the configuration file or Context object
	 * 
	 * @param client the DefaultHttpClient
	 */
	private <T extends AbstractHttpClient> void setTimeout(T client, Context context) {
        // special logic for Amex. if the customerRequestTimeout is set, use it. otherwise use the timeout in
        //the config file.
        if ( context.getCustomerRequestTimeout() != null) {
            client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, context.getCustomerRequestTimeout());
        } else {
            String reqTimeout = Config.getProperty(Config.TIMEOUT_REQUEST);
            if (StringUtils.hasText(reqTimeout)) {
                client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(reqTimeout.trim()));
            }
        }

		String connTimeout = Config.getProperty(Config.TIMEOUT_CONNECTION);
		if (StringUtils.hasText(connTimeout)) {
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(connTimeout.trim()));
		}

	}

    /**
     * Creates HttpEntity depends from type of content
     * @param intuitRequest
     * @return
     * @throws FMSException
     */
    private HttpEntity populateEntity(RequestElements intuitRequest) throws FMSException {
        byte[] compressedData = intuitRequest.getCompressedData();

        if (null == compressedData) {
            // use postString to create httpEntity
            try {
                return new StringEntity(intuitRequest.getPostString());
            } catch (UnsupportedEncodingException e) {
                throw new FMSException("UnsupportedEncodingException", e);
            }
        }

        // use compressed data to create httpEntity
        return new InputStreamEntity( new ByteArrayInputStream(compressedData) , compressedData.length);
    }

    /**
     * Manages HTTP entities
     */
    private class EntitiesManager {
        private List<HttpEntity> entities = new ArrayList<HttpEntity>();
        private final static int MIN_SIZE = 1;
        private final static int MIN_SIZE_INDEX = 0;

        /**
         * Returns true if there is no entities
         * @return
         */
        public boolean isEmpty()
        {
            return entities.isEmpty();
        }

        /**
         * Clears existing list
         */
        public void reset()
        {
            if(!isEmpty()) {
                entities.clear();
            }
        }

        /**
         * Add new entity into list
         * @param entity
         */
        public void add (HttpEntity entity)
        {
            entities.add(entity);
        }

        /**
         * Merges http entities in the list into one
         * @return InputStreamEntity
         * @throws IOException
         */
        public HttpEntity asSingleEntity() throws IOException
        {
            if(MIN_SIZE == entities.size()) { return entities.get(MIN_SIZE_INDEX); }

            byte[] buffer = new byte[0];
            for(HttpEntity entity : entities) {
                InputStream stream = entity.getContent();
                buffer = ArrayUtils.addAll(buffer,IOUtils.toByteArray(stream));
            }
            return new InputStreamEntity( new ByteArrayInputStream(buffer) , buffer.length);
        }

    }
}