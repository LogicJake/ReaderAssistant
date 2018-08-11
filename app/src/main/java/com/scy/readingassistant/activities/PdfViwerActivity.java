package com.scy.readingassistant.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.scy.readingassistant.R;

import java.io.File;

import static com.scy.readingassistant.util.BookTask.addBookMark;
import static com.scy.readingassistant.util.BookTask.updatePage;


public class PdfViwerActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context = this;
    private String uid;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;

    private PDFView pdfView;
    private TextView total_page;
    private TextView current_page;
    private boolean nightMode = false;

    private ImageView mark;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            pdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private final Handler mHideHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pdf_viwer);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mark = (ImageView) findViewById(R.id.mark);
        pdfView = findViewById(R.id.pdfView);

        current_page = (TextView) findViewById(R.id.current_page);
        total_page = (TextView) findViewById(R.id.total_page);

        initPdf();
    }

    public void initPdf(){
        pdfView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        int currentpage = bundle.getInt("current_page");
        String name = bundle.getString("name");
        String path = bundle.getString("path");
        uid = bundle.getString("uid");
        setTitle(name);
        File file = new File(path);
        pdfView.fromFile(file)
                .defaultPage(currentpage-1)
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        current_page.setText(Integer.toString(pdfView.getCurrentPage()+1));
                        total_page.setText(Integer.toString(pdfView.getPageCount()));
                    }
                })
                .onPageScroll(new OnPageScrollListener() {
                    @Override
                    public void onPageScrolled(int page, float positionOffset) {
                        current_page.setText(Integer.toString(page+1));
                    }
                })
                .load();
    }

    public void onPause() {
        updatePage(context,uid,Integer.parseInt(current_page.getText().toString()),Integer.parseInt(total_page.getText().toString()));
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        pdfView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_night) {
            nightMode = !nightMode;
            pdfView.setNightMode(nightMode);
            hide();
            return true;
        }
        if (id == R.id.action_jump){
            View view = getLayoutInflater().inflate(R.layout.dialog_jump, null);
            final EditText dialog_edit = (EditText) view.findViewById(R.id.dialog_edit);
            final TextView pageNum = (TextView) view.findViewById(R.id.pageNum);
            String pageSetText = "(1-"+total_page.getText()+")";
            pageNum.setText(pageSetText);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("跳转")
                    .setView(view)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pageStr = dialog_edit.getText().toString();
                            if (pageStr.length() != 0) {
                                pdfView.jumpTo(Integer.parseInt(pageStr)-1);
                                hide();
                            }
                            dialog.dismiss();
                        }
                    })
                    .create();
            dialog.show();
            return true;
        }
        if (id == R.id.action_add_mark){
            String defaultName = current_page.getText().toString()+"/"+total_page.getText().toString();
            addBookMark(context,uid,Integer.parseInt(current_page.getText().toString()),defaultName);
            mark.setVisibility(View.VISIBLE);
            mHideHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mark.setVisibility(View.GONE);
                }
            }, 1000);
        }
        if (id == R.id.action_all_mark){

        }

        return super.onOptionsItemSelected(item);
    }

}
