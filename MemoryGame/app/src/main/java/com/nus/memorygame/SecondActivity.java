package com.nus.memorygame;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.nus.memorygame.adapter.GameAdapter;
import com.nus.memorygame.model.ImageFile;
import com.nus.memorygame.util.Const;
import com.nus.memorygame.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SecondActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ImageView lastView = null;
    private int matchPairsCount = 0;
    private int lastPosition = -1;
    private ArrayList<ImageFile> images = null;
    private TextView pairView = null;
    private TextView timeView = null;
    private Handler handler = new Handler();
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        pairView = (TextView) findViewById(R.id.pair_count_view);
        timeView = (TextView) findViewById(R.id.time_view);
        timer = countDown();

        Bundle args = getIntent().getExtras();

        if(args != null) {
            ArrayList<String> fileNames = args.getStringArrayList("file_names");
            loadImages(fileNames);
        }

        GridView gridView = (GridView) findViewById(R.id.game_grid);
        GameAdapter adapter = new GameAdapter(this, images);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
        timer.start();
    }

    private void loadImages(ArrayList<String> fileNames) {
        images = Util.getImagesByFileNames(fileNames, this);
        images.addAll(images);
        Collections.shuffle(images);
    }

    private CountDownTimer countDown() {

        return new CountDownTimer(Const.MAX_GAME_TIME, 1000) {
            @Override
            public void onTick(long l) {
                String text = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(l) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(l) % 60);
                timeView.setText(text);
            }

            @Override
            public void onFinish() {
                Util.showToast(SecondActivity.this, "You lost!!!");
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                builder.setTitle("Play again!");
                builder.setPositiveButton(R.string.label_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        goHome();
                    }
                }).create().show();
            }
        };
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, long l) {
        ImageFile image = (ImageFile) adapterView.getItemAtPosition(i);
        final ImageView crntView = (ImageView) view.findViewById(R.id.image_view);
        crntView.setImageBitmap(image.getBitmap());

        if(image.isChecked()) return;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastPosition == -1) {
                    lastPosition = i;
                    lastView = crntView;
                } else {
                    if (i == lastPosition) {
                        crntView.setImageDrawable(getDrawable(R.drawable.bg_image));
                    } else if (((ImageFile) crntView.getTag()).equals((ImageFile) lastView.getTag())) {
                        ((ImageFile) adapterView.getItemAtPosition(lastPosition)).setChecked(true);
                        ((ImageFile) adapterView.getItemAtPosition(i)).setChecked(true);
                        matchPairsCount++;
                        updateCount();
                    } else {
                        crntView.setImageDrawable(getDrawable(R.drawable.bg_image));
                        lastView.setImageDrawable(getDrawable(R.drawable.bg_image));
                    }
                    lastPosition = -1;
                    lastView = null;

                    adapterView.invalidate();

                    if (matchPairsCount == Const.MAX_PAIRS) {
                        Util.showToast(getApplicationContext(), "You are great");
                        timer.cancel();
                        goHome();
                    }
                }
            }

        }, 1000);
    }

    private void goHome() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SecondActivity.this, MainActivity.class));
            }
        }, 3000);
    }

    private void updateCount() {
        pairView.setText(getString(R.string.pair_match, matchPairsCount, Const.MAX_PAIRS));
    }
}
