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
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView webView;
    private static final String APP_URL = "https://jimongramm.com";
    private static final int RECORD_AUDIO_PERMISSION_CODE = 1001;
    private android.speech.SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

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
        webView.addJavascriptInterface(new ShareInterface(), "AndroidShare");
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
                request.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_AUDIO_PERMISSION_CODE);
        }

        webView.loadUrl(APP_URL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    public class VoiceInterface {
        @JavascriptInterface
        public void startListening() {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_CODE);
                webView.post(() -> webView.evaluateJavascript(
                    "window.onAndroidSpeechError && window.onAndroidSpeechError(9)", null
                ));
                return;
            }

            runOnUiThread(() -> {
                if (speechRecognizer != null) {
                    speechRecognizer.destroy();
                }
                speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                    @Override public void onReadyForSpeech(android.os.Bundle p) {
                        webView.post(() -> webView.evaluateJavascript(
                            "document.getElementById('voice-transcript') && (document.getElementById('voice-transcript').textContent = 'Слушаю...')", null
                        ));
                    }
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
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L);
                speechRecognizer.startListening(intent);
            });
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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }

    class ShareInterface {

        @android.webkit.JavascriptInterface
        public void share(String text) {
            runOnUiThread(() -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                startActivity(android.content.Intent.createChooser(intent, "Поделиться"));
            });
        }

        @android.webkit.JavascriptInterface
        public void shareWithImage(String text, String imageUrl) {
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(imageUrl);
                    java.io.InputStream input = url.openStream();
                    java.io.File file = new java.io.File(getCacheDir(), "share_image.png");
                    java.io.FileOutputStream output = new java.io.FileOutputStream(file);
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
                    output.close();
                    input.close();

                    android.net.Uri imageUri = androidx.core.content.FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".provider",
                        file
                    );

                    runOnUiThread(() -> {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                        intent.putExtra(android.content.Intent.EXTRA_STREAM, imageUri);
                        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(android.content.Intent.createChooser(intent, "Поделиться"));
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                        startActivity(android.content.Intent.createChooser(intent, "Поделиться"));
                    });
                }
            }).start();
        }

        @android.webkit.JavascriptInterface
public void downloadFile(String fileUrl, String fileName) {
    new Thread(() -> {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            int fileSize = connection.getContentLength();
            java.io.InputStream input = connection.getInputStream();

            String mimeType = fileUrl.endsWith(".mp4") ? "video/mp4" : fileUrl.endsWith(".pdf") ? "application/pdf" : "image/png";
            String folder = fileUrl.endsWith(".mp4") ?
                android.os.Environment.DIRECTORY_MOVIES :
                fileUrl.endsWith(".pdf") ?
                android.os.Environment.DIRECTORY_DOWNLOADS :
                android.os.Environment.DIRECTORY_PICTURES;
            android.net.Uri contentUri = fileUrl.endsWith(".mp4") ?
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI :
                fileUrl.endsWith(".pdf") ?
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI :
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, folder + "/JimonGramm");
            android.net.Uri uri = getContentResolver().insert(contentUri, values);
            java.io.OutputStream output = getContentResolver().openOutputStream(uri);

            // Прогресс-бар в статус-баре
            android.app.NotificationManager notifManager = (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
            String channelId = "jimongramm_download";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, "Загрузки JimonGramm", android.app.NotificationManager.IMPORTANCE_LOW);
                notifManager.createNotificationChannel(channel);
            }
            androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(MainActivity.this, channelId)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("JimonGramm")
                .setContentText("Загрузка...")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setProgress(100, 0, fileSize <= 0);
            int notifId = (int) System.currentTimeMillis();
            notifManager.notify(notifId, builder.build());

            byte[] buffer = new byte[4096];
            int n;
            long downloaded = 0;
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                downloaded += n;
                if (fileSize > 0) {
                    int progress = (int) (downloaded * 100 / fileSize);
                    builder.setProgress(100, progress, false)
                        .setContentText("Загрузка " + progress + "%");
                    notifManager.notify(notifId, builder.build());
                }
            }
            output.close();
            input.close();
            connection.disconnect();

            // Завершение
            builder.setContentText("✅ Сохранено")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done);
            notifManager.notify(notifId, builder.build());
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> notifManager.cancel(notifId), 3000);

            runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this,
                "✅ Сохранено в загрузки", android.widget.Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this,
                "Ошибка сохранения: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
        }
    }).start();
}

@android.webkit.JavascriptInterface
public void sharePdf(String fileUrl, String fileName) {
    new Thread(() -> {
        try {
            java.net.URL url = new java.net.URL(fileUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();
            java.io.InputStream input = connection.getInputStream();
            java.io.File file = new java.io.File(getCacheDir(), fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int n;
            while ((n = input.read(buffer)) != -1) fos.write(buffer, 0, n);
            fos.close();
            input.close();
            connection.disconnect();

            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                MainActivity.this,
                getPackageName() + ".provider",
                file
            );
            runOnUiThread(() -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                intent.setType("application/pdf");
                intent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(android.content.Intent.createChooser(intent, "Поделиться каталогом"));
            });
        } catch (Exception e) {
            runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this,
                "Ошибка: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
        }
    }).start();
   }
 }
}
