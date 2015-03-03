package de.xcom.keshlib.keshlibandroiddemo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.xcom.keshlib.keshlibandroiddemo.R;

public class RegistrationWebViewActivity extends BaseActivity {

	private enum RegistrationState {
		STARTER("01"), BASIC("02"), PREMIUM_INCOMPLETE("03");

		private final String displayName;

		private RegistrationState(String s) {
			this.displayName = s;
		}

		private String getDisplayName() {
			return displayName;
		}

		public static boolean contains(RegistrationState state) {
			List<RegistrationState> list = Arrays.asList(RegistrationState.values());
			return list.contains(state);
		}

		public static RegistrationState getByType(String s) {
			for (RegistrationState state : RegistrationState.values()) {
				if (state.getDisplayName().equals(s)) {
					return state;
				}
			}
			return null;
		}

	}

	private enum RegistrationResult {
		REGISTRATION_COMPLETED_STARTER("00"), REGISTRATION_COMPLETED_BASIC("01"), REGISTRATION_ABORTED("02");

		private final String displayName;

		private RegistrationResult(String s) {
			this.displayName = s;
		}

		private String getDisplayName() {
			return displayName;
		}

		public static RegistrationResult getByType(String s) {
			for (RegistrationResult state : RegistrationResult.values()) {
				if (state.getDisplayName().equals(s)) {
					return state;
				}
			}
			return null;
		}
	}

	public static final String REGISTRATION_URL_KEY = "reg_url";

	private WebView webView;
	private String status;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.registrationwebview);

		String url = getIntent().getStringExtra(REGISTRATION_URL_KEY);

		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		// Add a suffix to the useragent, so the server knows we are a android webview
		webView.getSettings().setUserAgentString(webView.getSettings().getUserAgentString() + " KeshRegistrationIntegration");
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setAppCacheEnabled(false);
		webView.setWebViewClient(new RegistrationWebViewClient());

		webView.loadUrl(url);
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			RegistrationResult state = RegistrationResult.getByType(this.status);
			this.finish(state);
		}
	}

	private void finish(RegistrationResult state) {
		this.setResult(RESULT_OK);
		this.finish();
	}

	private class RegistrationWebViewClient extends WebViewClient {

		private static final String MOBILE_NUMBER_PARAM = "mobile_number";

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.endsWith("pdf")) {
				// Show pdf
				return true;
			} else {
				String path = null;
				try {
					URL urlToOpen = new URL(url);
					path = urlToOpen.getPath();
					if (path != null) {
						if (path.endsWith("exit")) {
							// The webview should be closed and report the provided state
							RegistrationResult result = null;

							Hashtable<String, String> table = parseQuery(urlToOpen.getQuery());
							if (table != null) {
								String state = table.get("registration_state");
								result = RegistrationResult.getByType(state);

							}
							finish(result);
							return true;
						} else if (path.endsWith("changed")) {
							Hashtable<String, String> table = parseQuery(urlToOpen.getQuery());
							customerStateChanged(table.get(MOBILE_NUMBER_PARAM), table.get("new_customer_state"));
							return true;
						}
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				return false;
			}
		}

		/**
		 * Report a change of the customer state.
		 * 
		 * @param mobile_number
		 *            Phone number of the customer
		 * @param status
		 *            New status
		 */
		private void customerStateChanged(String mobile_number, String status) {
			RegistrationWebViewActivity.this.status = status;
			RegistrationState state = RegistrationState.getByType(status);
		}

		/**
		 * Parse the query part into key value pairs.
		 * 
		 * @param query
		 *            The query to parse
		 * @return A Hashtable containing the query parameters as key value pairs
		 */
		private Hashtable<String, String> parseQuery(String query) {
			Hashtable<String, String> table = null;
			if (query != null) {
				String[] params = query.split("&");
				table = new Hashtable<String, String>();
				for (String p : params) {
					int index = p.indexOf("=");
					if (index > 0) {
						String key = p.substring(0, index);
						String value = p.substring(index + 1);
						table.put(key, value);
					}
				}
			}
			return table;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
	}
}
