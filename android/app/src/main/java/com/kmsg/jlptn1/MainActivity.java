package com.kmsg.jlptn1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView web;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private final List<Voice> jaVoices = new ArrayList<>();
    private Voice male = null, female = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setTextZoom(100);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);

        tts = new TextToSpeech(this, status -> {
            ttsReady = (status == TextToSpeech.SUCCESS);
            if (ttsReady) { tts.setLanguage(Locale.JAPAN); pickVoices(); }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) { done(id); }
            @Override public void onError(String id) { done(id); }
            @Override public void onStop(String id, boolean interrupted) {}
        });
        web.addJavascriptInterface(new TtsBridge(), "AndroidTTS");
        web.loadUrl("file:///android_asset/www/index.html");
    }

    private void done(final String id) {
        runOnUiThread(() -> web.evaluateJavascript("window.__ttsDone&&window.__ttsDone('" + id.replace("'", "") + "')", null));
    }

    private void pickVoices() {
        try {
            Set<Voice> all = tts.getVoices();
            if (all == null) return;
            for (Voice v : all) {
                if (v.getLocale() != null && "ja".equals(v.getLocale().getLanguage())
                        && (v.getFeatures() == null || !v.getFeatures().contains("notInstalled"))) jaVoices.add(v);
            }
            for (Voice v : jaVoices) {
                String n = v.getName().toLowerCase(Locale.ROOT);
                // Google TTS 일본어: jab/htm = 여성, jac/jad = 남성
                if (male == null && (n.contains("jac") || n.contains("jad") || n.contains("male") && !n.contains("female"))) male = v;
                if (female == null && (n.contains("jab") || n.contains("htm") || n.contains("female"))) female = v;
            }
        } catch (Exception ignored) {}
    }

    class TtsBridge {
        @JavascriptInterface
        public void speakJa(final String text, final String speaker, final float rate, final String id) {
            runOnUiThread(() -> {
                if (tts == null || !ttsReady) { done(id); return; }
                float pitch = 1.0f;
                Voice v = null;
                if ("M".equals(speaker)) { v = male; if (v == null) pitch = 0.75f; }
                else if ("F".equals(speaker)) { v = female; if (v == null) pitch = 1.15f; }
                else { v = female != null ? female : male; }
                if (v != null) tts.setVoice(v); else tts.setLanguage(Locale.JAPAN);
                tts.setPitch(pitch);
                tts.setSpeechRate(rate * 0.95f);
                Bundle b = new Bundle();
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, b, id);
            });
        }

        @JavascriptInterface
        public void stop() { runOnUiThread(() -> { if (tts != null) tts.stop(); }); }

        @JavascriptInterface
        public String info() {
            if (!ttsReady) return "준비 중이거나 사용할 수 없음";
            if (jaVoices.isEmpty()) return "일본어 음성 없음 — 설정 › 텍스트 음성 변환에서 일본어를 설치하세요";
            return "일본어 " + jaVoices.size() + "개 (남성 " + (male != null ? "있음" : "없음") + ", 여성 " + (female != null ? "있음" : "없음") + ")";
        }
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.appBack && window.appBack()) ? 'y' : 'n'", v -> {
            if (v == null || !v.contains("y")) MainActivity.super.onBackPressed();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
