package de.xcom.keshlib.keshlibandroiddemo;

import java.util.Arrays;
import java.util.List;

import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.xcom.keshlib.keshlibandroiddemo.R;

public class UpgradeWebViewActivity extends BaseActivity {
	public static final String UPGRADE_URL_KEY = "upgr_url";
	public static final String AUTH_TOKEN_KEY = "token";

	private String status;
	private WebView webView;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.registrationwebview);

		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new UpgradeJavaScriptInterface(), "mobile_interface");
		webView.setWebViewClient(new UpgradeWebViewClient());
		webView.getSettings().setSavePassword(false);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setAppCacheEnabled(false);
		String authToken = getIntent().getStringExtra(AUTH_TOKEN_KEY);
		String url = getIntent().getStringExtra(UPGRADE_URL_KEY) + "?sessionId=" + authToken + "&target=upgrade";
		webView.loadUrl(url);
	}

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

		// public static boolean contains(RegistrationResult state) {
		// List<RegistrationResult> list = Arrays.asList(RegistrationResult.values());
		// return list.contains(state);
		// }

		public static RegistrationResult getByType(String s) {
			for (RegistrationResult state : RegistrationResult.values()) {
				if (state.getDisplayName().equals(s)) {
					return state;
				}
			}
			return null;
		}
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

	public class UpgradeJavaScriptInterface {

		// dieser tag wird benötigt, damit es auch auf neueren
		// android-versionen noch funktioniert
		@JavascriptInterface
		public void customerStateChanged(String mobile_number, String status) {

			UpgradeWebViewActivity.this.status = status;
			RegistrationState state = RegistrationState.getByType(status);

		}

		// dieser tag wird benötigt, damit es auch auf neueren
		// android-versionen noch funktioniert
		@JavascriptInterface
		public void exitKeshRegistration(String mobile_number, String registration_state, boolean cancel) {
			RegistrationResult state = RegistrationResult.getByType(registration_state);
			UpgradeWebViewActivity.this.finish(state);
		}

	}

	private void finish(RegistrationResult state) {
		this.setResult(RESULT_OK);
		this.finish();
	}

	private class UpgradeWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.endsWith("pdf")) {
				// show pdf
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
	}
}
