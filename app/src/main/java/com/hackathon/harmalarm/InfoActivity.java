package com.hackathon.harmalarm;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private RecyclerView mInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GraphView graphView = (GraphView) findViewById(R.id.graph);
        DataPoint[] dataPointArray = new DataPoint[MainActivity.sTemperatureEntries.size()];
        for (int i = MainActivity.sTemperatureEntries.size() - 1; i >= 0; i--) {
            dataPointArray[i] = new DataPoint(i, MainActivity.sTemperatureEntries.get(i).getTemperature());
        }
        LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<DataPoint>(dataPointArray);
        lineGraphSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(InfoActivity.this, dataPoint.getY() + "\u00B0F", Toast.LENGTH_SHORT).show();
            }
        });

        graphView.addSeries(lineGraphSeries);
        graphView.getGridLabelRenderer().setVerticalAxisTitle("°F");
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(12);
    }

}
