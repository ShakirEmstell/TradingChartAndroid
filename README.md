# TradingChartAndroid
Candle stick chart like tradingview using android canvas


usage

 <shakir.bhav.android.ChartView

        android:id="@+id/tradingChart"
        android:background="#2B2B2B"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
        
        
            tradingChart.priceTop = resp?.h!!.maxOf { it }
            tradingChart.priceBottom = resp?.l!!.minOf { it }
            tradingChart.timeStart=resp.t.first()
            tradingChart.timeEnd=resp.t.last()
            tradingChart.t=resp.t
            tradingChart.h=resp.h
            tradingChart.l=resp.l
            tradingChart.c=resp.c
            tradingChart.o=resp.o
            tradingChart.invalidate()
