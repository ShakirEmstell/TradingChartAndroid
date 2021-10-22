# TradingChartAndroid
Candle stick chart like tradingview using android canvas




[![Screenshot](https://github.com/ShakirEmstell/TradingChartAndroid/raw/main/device-2021-10-21-200822.png)](#features)

usage

<shakir.bhav.android.ChartView
 android:id="@+id/tradingChart"
 android:background="#2B2B2B"
 android:layout_width="match_parent"
 android:layout_height="match_parent" />
        
        
        
            tradingChart.priceTop = response?.h!!.maxOf { it }
            tradingChart.priceBottom = response?.l!!.minOf { it }
            tradingChart.timeStart=response.t.first()
            tradingChart.timeEnd=response.t.last()
            tradingChart.t=response.t
            tradingChart.h=response.h
            tradingChart.l=response.l
            tradingChart.c=response.c
            tradingChart.o=response.o
            tradingChart.invalidate()
