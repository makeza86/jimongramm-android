package com.jimongramm.app;
import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.PermissionRequest;
import android.webkit.JavascriptInterface;
import android.view.KeyEvent;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.GeolocationPermissions;
import android.content.Intent;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String APP_URL = "https://jimongramm.com";
    private static final int SPEECH_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setGeolocationEnabled(true);
        settings.setUserAgentString(settings.getUserAgentString() + " JimonGrammApp/1.0");

        webView.addJavascriptInterface(new VoiceInterface(), "AndroidVoice");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("https://jimongramm.com") ||
                    url.startsWith("http://jimongramm.com")) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        webView.loadUrl(APP_URL);
    }

   private android.speech.SpeechRecognizer speechRecognizer;

public class VoiceInterface {
    @JavascriptInterface
    public void startListening() {
        runOnUiThread(() -> {
            if (speechRecognizer != null) {
                speechRecognizer.destroy();
            }
            speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                @Override public void onReadyForSpeech(android.os.Bundle p) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float v) {}
                @Override public void onBufferReceived(byte[] b) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onPartialResults(android.os.Bundle b) {}
                @Override public void onEvent(int t, android.os.Bundle b) {}
                @Override public void onError(int error) {
                    webView.post(() -> webView.evaluateJavascript(
                        "window.onAndroidSpeechError && window.onAndroidSpeechError(" + error + ")", null
                    ));
                }
                @Override
                public void onResults(android.os.Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String text = matches.get(0);
                        webView.post(() -> webView.evaluateJavascript(
                            "window.onAndroidSpeechResult && window.onAndroidSpeechResult('" + text.replace("'", "\\'") + "')", null
                        ));
                    }
                }
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            speechRecognizer.startListening(intent);
        });
    }
}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
