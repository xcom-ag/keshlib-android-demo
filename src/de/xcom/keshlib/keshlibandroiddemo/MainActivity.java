package de.xcom.keshlib.keshlibandroiddemo;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import de.xcom.kesh.keshlib.KeshServiceManager;
import de.xcom.kesh.keshlib.communication.AbstractErrorData;
import de.xcom.kesh.keshlib.communication.OnConnectivityChangedListener;
import de.xcom.kesh.keshlib.communication.OnNotificationReceivedListener;
import de.xcom.kesh.keshlib.communication.OnResponseReceivedCallback;
import de.xcom.kesh.keshlib.communication.response_v2.body.AccountBalanceNotificationBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.AccountBalanceResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.AuthorizationRequiredNotificationBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.ChargeAccountResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.CreateMandateResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.DischargeAccountResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.FetchAvatarResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.FetchMandatePreviewResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.FetchMandateResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.FetchUserDataResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.ListTransactionsResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.LoginResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.PaymentInfoNotificationBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.RequestPaymentResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.ResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.SaveAvatarResonseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.SendMoneyResponseBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.UserUpgradedNotificationBody;
import de.xcom.kesh.keshlib.communication.response_v2.body.data.Amount;
import de.xcom.kesh.keshlib.communication.response_v2.body.data.ImageData;
import de.xcom.kesh.keshlib.util.Formatter;

public class MainActivity extends BaseActivity {

	KeshServiceManager keshServiceManager;
	AlertDialog dialog;
	EditText imageId, oldPasswd, newPasswd;
	View connectionState;
	String authToken;

	OnConnectivityChangedListener connectionListener = new OnConnectivityChangedListener() {
		@Override
		public void onConnectivityChanged(final int i) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateConnectionIndicator(i);
				}
			});
		}
	};

	// The callback used to register for notifications
	OnNotificationReceivedListener notificationListener = new OnNotificationReceivedListener() {
		@Override
		public void onAccountBalanceNotificationReceived(AccountBalanceNotificationBody arg0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("Notification received", "AccountBalanceNotification");
				}
			});
		}

		@Override
		public void onPaymentInfoNotificationReceived(PaymentInfoNotificationBody arg0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("Notification received", "PaymentInfoNotification");
				}
			});
		}

		@Override
		public void onAuthorizationRequiredNotificationReceived(AuthorizationRequiredNotificationBody arg0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("Notification received", "AuthorizationRequiredNotification");
				}
			});
		}

		@Override
		public void onUserUpgradedNotificationReceived(UserUpgradedNotificationBody arg0) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("Notification received", "UserUpgradedNotification");
				}
			});
			
		}
	};

	OnResponseReceivedCallback<LoginResponseBody> loginCallback = new OnResponseReceivedCallback<LoginResponseBody>() {

		@Override
		public void onRequestFinishedSuccessful(LoginResponseBody response) {
			final LoginResponseBody data = response;
			authToken = data.getSessionToken();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("LoginUserRequest finished", "Sessiontoken: " + data.getSessionToken());
				}
			});
		}

		@Override
		public void onRequestFinishedWithError(final AbstractErrorData error) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("error", error.getMessage());
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageId = (EditText) findViewById(R.id.image_id_edittext);
		oldPasswd = (EditText) findViewById(R.id.old_pw_edittext);
		newPasswd = (EditText) findViewById(R.id.new_pw_edittext);
		connectionState = findViewById(R.id.connection_indicator);
		keshServiceManager = KeshServiceManager.getInstance();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		keshServiceManager.registerConnectionStateChangedListener(connectionListener);
		keshServiceManager.registerNotificationListener(notificationListener);
		updateConnectionIndicator(keshServiceManager.getConnectionState());
	}

	@Override
	protected void onPause() {
		super.onPause();
		keshServiceManager.unregisterConnectionStateChangedListener(connectionListener);
		keshServiceManager.unregisterNotificationListener(notificationListener);
		if (dialog != null) {
			dialog.dismiss();
		}
		if (isTaskRoot() && isFinishing()) {
			// cleanup manager if last activity finishes
			keshServiceManager.cleanup();
		}
	}

	public void connectButtonClicked(View view) {
		String serverUrl = "https://demo1.kesh.de:443"; // demo1
		// String serverUrl = "https://demo1.kesh.de:743"; // demo1 with client key store
		X509Certificate cert = null;
		// KeyStore clientKeyStore = null;
		// KeyStore clientKeyStore = loadClientKeyStore("storename.p12", "passwd"); // example
		try {
			cert = loadCertificatFromFile("demo1.kesh.de.crt"); // demo1.kesh.de cert
		} catch (Exception e) {
			e.printStackTrace();
			cert = null;
		}
		// Connect with optional server certificate and client store
		// keshServiceManager.connectToServer(serverUrl, cert, clientKeyStore, passwd); // connect, using the key store to authenticate the client
		// Connect with optional server certificate
		keshServiceManager.connectToServer(serverUrl, cert);
	}

	public void disconnectButtonClicked(View view) {
		keshServiceManager.disconnect();
	}

	public void balanceButtonClicked(View view) {
		keshServiceManager.sendAccountBalanceRequest(new OnResponseReceivedCallback<AccountBalanceResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(final AccountBalanceResponseBody response) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog("AccountBalanceRequest finished", "Balance: " + Formatter.formatAmount(response.getBalance()));
					}
				});
			}

			@Override
			public void onRequestFinishedWithError(final AbstractErrorData error) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog("error", error.getMessage());
					}
				});
			}
		});
	}

	public void loginButtonClicked(View view) {
		keshServiceManager.sendLoginRequest("01522223333", "123456", loginCallback);
	}

	public void logoutButtonClicked(View view) {
		keshServiceManager.sendLogoutRequest(new DefaultResponseCallback());
	}

	public void userdataButtonClicked(View view) {
		keshServiceManager.sendFetchUserDataRequest(new OnResponseReceivedCallback<FetchUserDataResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(FetchUserDataResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void sendMoneyButtonClicked(View view) {
		keshServiceManager.sendMoneyRequest("6000000846", "0150123456", new Amount(10.0, "EUR"), "A short description for the transaction.",
				new OnResponseReceivedCallback<SendMoneyResponseBody>() {
					@Override
					public void onRequestFinishedSuccessful(SendMoneyResponseBody arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestFinishedWithError(AbstractErrorData arg0) {
						// TODO Auto-generated method stub

					}
				});
	}

	public void requestMoneyButtonClicked(View view) {
		keshServiceManager.sendRequestPaymentRequest("6000000846", "0150123456", new Amount(10.0, "EUR"), "A short description for the transaction.",
				new OnResponseReceivedCallback<RequestPaymentResponseBody>() {

					@Override
					public void onRequestFinishedSuccessful(RequestPaymentResponseBody arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestFinishedWithError(AbstractErrorData arg0) {
						// TODO Auto-generated method stub

					}
				});
	}

	public void ownAvatarButtonClicked() {
		keshServiceManager.sendFetchAvatarRequest(KeshServiceManager.AVATAR_SIZE_FULL, new OnResponseReceivedCallback<FetchAvatarResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(FetchAvatarResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void avatarButtonClicked(View view) {
		int id = -1;
		Editable edit = imageId.getText();
		if (edit != null && edit.length() > 0) {
			try {
				id = Integer.parseInt(edit.toString());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (id >= 0) {
				keshServiceManager.sendFetchAvatarRequest(id, KeshServiceManager.AVATAR_SIZE_FULL, new OnResponseReceivedCallback<FetchAvatarResponseBody>() {

					@Override
					public void onRequestFinishedSuccessful(FetchAvatarResponseBody arg0) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRequestFinishedWithError(AbstractErrorData arg0) {
						// TODO Auto-generated method stub

					}
				});
			} else {
				showDialog("ImageID", "Bitte eine ImageId eingeben");
			}
		} else {
			ownAvatarButtonClicked();
		}
	}

	public void changePasswordButtonClicked(View view) {
		keshServiceManager.sendChangePasswordRequest(oldPasswd.getText().toString(), newPasswd.getText().toString(), new DefaultResponseCallback());
	}

	public void chargeButtonClicked(View view) {
		keshServiceManager.sendChargeAccountRequest(new Amount(10.0, "EUR"), new OnResponseReceivedCallback<ChargeAccountResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(ChargeAccountResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void dischargeButtonClicked(View view) {
		keshServiceManager.sendDischargeAccountRequest(new Amount(10.0, "EUR"), new OnResponseReceivedCallback<DischargeAccountResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(DischargeAccountResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void transactionsButtonClicked(View view) {
		keshServiceManager.sendListTransactionsRequest(null, new OnResponseReceivedCallback<ListTransactionsResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(ListTransactionsResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void fetchMandateButtonClicked(View view) {
		keshServiceManager.sendFetchMandateRequest(new OnResponseReceivedCallback<FetchMandateResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(FetchMandateResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void createMandateButtonClicked(View view) {
		keshServiceManager.sendCreateMandateRequest(new OnResponseReceivedCallback<CreateMandateResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(CreateMandateResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void fetchPreviewButtonClicked(View view) {
		keshServiceManager.sendFetchMandatePreviewRequest(new OnResponseReceivedCallback<FetchMandatePreviewResponseBody>() {

			@Override
			public void onRequestFinishedSuccessful(FetchMandatePreviewResponseBody arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRequestFinishedWithError(AbstractErrorData arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void saveAvatarButtonClicked(View view) {
		String imgData = "78247987b83287837e4f8332d490328a499834f";
		String mime = "image/jpg";
		keshServiceManager.sendSaveAvatarRequest(new ImageData(imgData, mime), new OnResponseReceivedCallback<SaveAvatarResonseBody>() {

			@Override
			public void onRequestFinishedSuccessful(final SaveAvatarResonseBody response) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog("Request finished", "ResponseData classname: " + response.getClass().getSimpleName());
					}
				});
			}

			@Override
			public void onRequestFinishedWithError(final AbstractErrorData error) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showDialog("error", error.getMessage());
					}
				});
			}
		});
	}

	public void confirmCentButtonClicked(View view) {
		keshServiceManager.sendConfirmCentTransactionRequest("thiscouldbeyouractivationcode", new DefaultResponseCallback());
	}

	public void registerButtonClicked(View view) {
		Intent intent = new Intent(MainActivity.this, RegistrationWebViewActivity.class);
		intent.putExtra(RegistrationWebViewActivity.REGISTRATION_URL_KEY, "https://demo1.kesh.de:444/kesh_mobile_oke/registrierung");

		startActivity(intent);
	}

	public void upgradeButtonClicked(View view) {
		Intent intent = new Intent(MainActivity.this, UpgradeWebViewActivity.class);
		intent.putExtra(UpgradeWebViewActivity.UPGRADE_URL_KEY, "https://demo1.kesh.de:444/kesh_mobile_oke/registrierung");
		intent.putExtra(UpgradeWebViewActivity.AUTH_TOKEN_KEY, authToken);
		startActivity(intent);
	}

	private void showDialog(String title, String message) {
		if (dialog != null) {
			dialog.dismiss();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle(title);
		builder.setMessage(message);
		dialog = builder.create();
		dialog.show();
	}

	private void updateConnectionIndicator(int state) {
		switch (state) {
			case KeshServiceManager.CONNECTION_STATE_CONNECTING:
				connectionState.setBackgroundColor(0xFFFF990A);
				break;
			case KeshServiceManager.CONNECTION_STATE_NO_NETWORK:
				connectionState.setBackgroundColor(0xFF444444);
				break;
			case KeshServiceManager.CONNECTION_STATE_ESTABLISHED:
				connectionState.setBackgroundColor(0xFF00FF00);
				break;
			default:
				connectionState.setBackgroundColor(0xFFFF0000);
		}

	}

	private X509Certificate loadCertificatFromFile(String filePath) throws Exception {
		BufferedInputStream bis = null;

		CertificateFactory certFact = null;
		X509Certificate _cert = null;
		try {
			bis = new BufferedInputStream(getAssets().open(filePath));
			certFact = CertificateFactory.getInstance("X.509"); //$NON-NLS-1$
			_cert = (X509Certificate) certFact.generateCertificate(bis);
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException("Could not locate file at '" + filePath); //$NON-NLS-1$
		} catch (Exception e) {
			throw new Exception(" Exception in readFromFileToString() -> '" + filePath + "' ->", e); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return _cert;
	}

	private KeyStore loadClientKeyStore(String filePath, String passwd) {
		BufferedInputStream bis = null;
		KeyStore store = null;

		try {
			char[] pw = null;
			if (passwd != null) {
				pw = passwd.toCharArray();
			}
			bis = new BufferedInputStream(getAssets().open(filePath));
			store = KeyStore.getInstance("PKCS12");

			store.load(bis, pw);
		} catch (Exception e) {
			store = null;
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return store;
	}

	class DefaultResponseCallback implements OnResponseReceivedCallback<ResponseBody> {
		@Override
		public void onRequestFinishedSuccessful(final ResponseBody response) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("Request finished", "ResponseData classname: " + response.getClass().getSimpleName());
				}
			});

		}

		@Override
		public void onRequestFinishedWithError(final AbstractErrorData error) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showDialog("error", error.getMessage());
				}
			});
		}
	}
}
