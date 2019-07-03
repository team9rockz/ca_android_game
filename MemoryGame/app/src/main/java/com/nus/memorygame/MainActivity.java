package com.nus.memorygame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nus.memorygame.adapter.ImageListAdapter;
import com.nus.memorygame.model.ImageFile;
import com.nus.memorygame.service.DownloadService;
import com.nus.memorygame.util.Const;
import com.nus.memorygame.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    private String[] permissions;
    private String[] unGranted;
    private int count = 0;
    private LinearLayout progressLayout = null;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private GridView gridView;
    private ArrayList<ImageFile> images = new ArrayList<>();
    private TextView emptyView;
    private EditText urlView;
    private Button clearBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button fetchBtn = (Button) findViewById(R.id.btn_fetch);
        progressLayout = (LinearLayout) findViewById(R.id.progress_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressTextView = (TextView) findViewById(R.id.progress);
        emptyView = (TextView) findViewById(R.id.empty_view);
        clearBtn = (Button) findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);

        urlView = (EditText) findViewById(R.id.et_url);
        urlView.addTextChangedListener(this);

        showEmptyView(images.isEmpty());
        initAdapter();

        fetchBtn.setOnClickListener(this);
        initPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.STOP_PROGRESS);
        intentFilter.addAction(Const.DOWNLOAD_PROGRESS);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    private void checkForUnGrantedPermissions(String[] perms) {
        List<String> unGrantedList = new ArrayList<>(perms.length);
        for (String permission : perms) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                unGrantedList.add(permission);
            }
        }

        unGranted = (!unGrantedList.isEmpty()) ? unGrantedList.toArray(new String[unGrantedList.size()]) : null;
    }

    private void initPermissions() {
        permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        checkForUnGrantedPermissions(permissions);
        if (unGranted != null && unGranted.length > 0) {
            requestPermissions(unGranted, Const.PERMISSIONS_REQ_CODE);
        } else {
            populateImages(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Const.PERMISSIONS_REQ_CODE) {
            checkForUnGrantedPermissions(unGranted);
            if (unGranted != null && unGranted.length > 0) {
                requestPermissions(unGranted, Const.PERMISSIONS_REQ_CODE);
                Toast.makeText(this, getResources().getText(R.string.permission_req_msg), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_clear:
                urlView.setText(null);
                clearBtn.setVisibility(View.GONE);
                break;
            case R.id.btn_fetch:
                doProcess();
                break;
        }

    }

    private void doProcess() {
        stopDownloadService();
        String url = urlView.getText().toString();

        if(url.isEmpty()) {
            Util.showToast(this, "Enter url");
            return;
        }

        if(!url.contains("www")) {
            url = "www." + url;
        }

        if(!url.contains("http")) {
            url = "https://" + url;
        }
        startDownloadService(url);
    }

    private void startDownloadService(String url) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra(Const.URL, url);
        startService(intent);
    }

    private void stopDownloadService() {
        hideProgressLayout();
        Intent intent = new Intent(this, DownloadService.class);
        stopService(intent);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Const.DOWNLOAD_PROGRESS:
                    doReceive(intent);
                    break;
                case Const.STOP_PROGRESS:
                    hideProgressLayout();
                    unregisterReceiver(receiver);
                    break;
            }
        }
    };

    private void doReceive(Intent intent) {
        Bundle args = intent.getExtras();
        String basePath = args.getString("base_path");
        int total = args.getInt("total");
        if(args.getBoolean("progress")) {
            int num = args.getInt("num");
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setProgress(num * 100 / total);
            progressTextView.setText(getResources().getString(R.string.download_progress, num, total));
        } else {
            images.clear();
            showEmptyView(true);
            hideProgressLayout();
            boolean status = args.getBoolean("status");
            if(total != 0) {
                if (status) {
                    showEmptyView(false);
                    Util.showToast(MainActivity.this, "Download Complete");
                    populateImages(basePath);
                } else {
                    Util.showToast(MainActivity.this, "Download Failed!!!");
                }
            }

            if(gridView != null) {
                updateGridViewList();
            }
        }
    }

    private void hideProgressLayout() {
        progressBar.setProgress(0);
        progressTextView.setText(null);
        progressLayout.setVisibility(View.GONE);
    }

    private void updateGridViewList() {
        ((ImageListAdapter)gridView.getAdapter()).updateItems(images);
    }

    private void populateImages(String path) {

        images = Util.getImages(path, this);
        showEmptyView(images.isEmpty());
        updateGridViewList();

    }

    private void showEmptyView(boolean sure) {
        if(emptyView != null)
            emptyView.setVisibility(sure ? View.VISIBLE : View.GONE);
    }

    private void initAdapter() {
        gridView = (GridView) findViewById(R.id.grid_view);
        final ImageListAdapter adapter = new ImageListAdapter(this, images);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                final ImageView selected = (ImageView) view.findViewById(R.id.select);

                images.get(i).setChecked(!images.get(i).isChecked());
                if(images.get(i).isChecked()) {
                    count++;
                    selected.setVisibility(View.VISIBLE);
                } else {
                    count--;
                    selected.setVisibility(View.GONE);
                }

                adapterView.invalidate();

                Util.showToast(getApplicationContext(), getString(R.string.img_selection, count, Const.MAX_PAIRS));

                if(count == 6) {
                    passSelectedImages(images);
                }
            }
        });
    }

    private void passSelectedImages(ArrayList<ImageFile> imageFiles) {
        ArrayList<String> fileNames = new ArrayList<>();
        for(ImageFile img : imageFiles) {
            if(img.isChecked()) {
                fileNames.add(img.getFileName());
            }
        }
        postToGame(fileNames);
    }

    private void postToGame(ArrayList<String> fileNames) {
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("file_names", fileNames);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(charSequence.toString().length() > 0) {
            clearBtn.setVisibility(View.VISIBLE);
        } else {
            clearBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
