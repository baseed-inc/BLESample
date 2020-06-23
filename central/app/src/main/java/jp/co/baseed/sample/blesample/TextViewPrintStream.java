package jp.co.baseed.sample.blesample;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class TextViewPrintStream extends PrintStream {
    private Handler handler = new Handler(Looper.getMainLooper());
    TextView target;

    @Override
    public synchronized void print(final String s) {
        super.print(s);
        handler.post(new Runnable() {
            @Override
            public void run() {
                target.append(s);
            }
        });
    }
    private void scrollDown() {
        final ScrollView parent = (ScrollView)target.getParent();
        parent.post(new Runnable() {
            @Override
            public void run() {
                parent.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public TextViewPrintStream(OutputStream out, TextView target) {
        super(out);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(OutputStream out, boolean autoFlush, TextView target) {
        super(out, autoFlush);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(OutputStream out, boolean autoFlush, String encoding, TextView target) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(String fileName, TextView target) throws FileNotFoundException {
        super(fileName);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(String fileName, String csn, TextView target) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(File file, TextView target) throws FileNotFoundException {
        super(file);
        this.target = target;
        scrollDown();
    }

    public TextViewPrintStream(File file, String csn, TextView target) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
        this.target = target;
        scrollDown();
    }
}
