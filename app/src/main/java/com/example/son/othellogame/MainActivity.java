package com.example.son.othellogame;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.son.othellogame.adapter.ChessBroadAdapter;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView chessBroad = (RecyclerView) findViewById(R.id.chessBroad);
        chessBroad.setLayoutManager(new GridLayoutManager(this, 8));
        chessBroad.setAdapter(new ChessBroadAdapter(this));
    }
}
