/*
 * Copyright (C) 2010 The Froyo Term Project 
 */
package com.TrackTrackTrack;

import org.achartengine.*;
import org.achartengine.chart.*;
import org.achartengine.model.*;
import org.achartengine.renderer.*;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.ViewGroup.*;
import android.widget.*;

/**
 * @author 김 현구
 * @version 1.1
 * 
 * 고도 정보에 따른 그래프를 그려주는 Activity의 구성을 담당하는 class이다.
 * achartengine-0.5.0.jar Libraries를 사용하여 개발 하였다.
 */
public class GraphActivity extends Activity {
  public static final String TYPE = "type";
  /**
 */
private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /**
 */
private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  public static XYSeries mCurrentSeries = null;
  /**
 */
private XYSeriesRenderer mCurrentRenderer;
  /**
 */
private String mDateFormat;
  public static GraphicalView mChartView;

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
    super.onRestoreInstanceState(savedState);
    mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
    mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
    mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
    mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
    mDateFormat = savedState.getString("date_format");
  }
  
  public static GraphicalView getGraphicalView(){
	  return mChartView;
  }
  
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("dataset", mDataset);
    outState.putSerializable("renderer", mRenderer);
    outState.putSerializable("current_series", mCurrentSeries);
    outState.putSerializable("current_renderer", mCurrentRenderer);
    outState.putString("date_format", mDateFormat);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.graph);
    
    String seriesTitle = "Altitude";
    XYSeries series = new XYSeries(seriesTitle);
    mDataset.addSeries(series);
    mCurrentSeries = series;
    XYSeriesRenderer renderer = new XYSeriesRenderer();
    mRenderer.addSeriesRenderer(renderer);
    renderer.setColor(Color.YELLOW);
    renderer.setPointStyle(PointStyle.DIAMOND);
    mRenderer.setShowGrid(true);
    //mRenderer.setChartTitle("Altitude Graph");
    mRenderer.setXTitle("Time(second)");
    mRenderer.setYTitle("Altitude(m)");
    mRenderer.setLabelsTextSize(20);
    mRenderer.setAxisTitleTextSize(20);
    mRenderer.setChartTitleTextSize(30);
    mRenderer.setLabelsColor(Color.GREEN);
    mRenderer.setAxesColor(Color.GRAY);
    
    //setShowGrid(true);
    mCurrentRenderer = renderer;

    if (mChartView != null) {
          mChartView.repaint();
        }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mChartView == null) {
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      boolean enabled = mDataset.getSeriesCount() > 0;
    } else {
      mChartView.repaint();
    }
  }
}
