package shakir.bhav.android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import shakir.bhav.common.milliFileDateTime
import shakir.bhav.common.milliToHMS
import shakir.bhav.common.printFloat
import kotlin.math.absoluteValue

class ChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    var priceBottom = 0f
    var priceTop = 0f

//    var timeStartIndex = 0
//    var timeEndIndex = 0

    var timeStart=0L
    var timeEnd=0L

//    var timeOffset = 0f

    var t = arrayListOf<Long>()
    var h = arrayListOf<Float>()
    var l = arrayListOf<Float>()
    var c = arrayListOf<Float>()
    var o = arrayListOf<Float>()


    var canvasWidth: Float = 0f
    var canvasHeight: Float = 0f
    var priceLineWidth: Float = 0f
    var priceLineX: Float = 0f

    val timeLineHeight = AppApplication.instance.resources.getDimension(R.dimen._20sdp)
    var timeLineY: Float = 0f

    var coordinatePriceRatio: Float = 0f
    var coordinateTimeRatio: Float = 0f


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvasWidth = canvas.width.toFloat()
            canvasHeight = canvas.height.toFloat()

            priceLineWidth = (canvasWidth / 6f)
            priceLineX = canvasWidth - priceLineWidth
            timeLineY = canvasHeight - timeLineHeight






            canvas?.drawLine(canvasWidth - priceLineWidth, 0f, canvasWidth - priceLineWidth, canvasHeight, paintLineGrey)
            canvas?.drawLine(0f, canvasHeight - timeLineHeight, canvasWidth, canvasHeight - timeLineHeight, paintLineGrey)


            if (t.isNotEmpty()) {




                val priceGridUnit = (priceTop - priceBottom) / 10f
                var timeGridUnit = (timeEnd-timeStart) / 3L
                coordinatePriceRatio = canvasHeight / (priceTop - priceBottom)
                coordinateTimeRatio = (canvasWidth) / (timeStart - timeEnd)




                drawPriceGridAndTexts(canvas, priceGridUnit)
                drawTimeGridsAndTexts(canvas, timeGridUnit)
//                drawPriceLine(canvas)

                drawCandles(canvas)
            }




        }


    }


    fun optimizeTimeOffsetAndTimeIndex() {


     /*   timeStartIndex++
        timeEndIndex++
        if (timeStartIndex>timeEndIndex)
            timeEndIndex=timeStartIndex*/


        postInvalidate()

    }

    fun drawPriceGridAndTexts(canvas: Canvas, pUnit: Float) {
        var i = priceBottom
        while (i <= priceTop) {
            val p = (canvasHeight - ((i - priceBottom) * coordinatePriceRatio))
            canvas?.drawLine(0f, p, canvasWidth - priceLineWidth, p, paintLineGrids)
            val rect = Rect()
            val textString = i.printFloat()
            priceTextPaint.getTextBounds(textString, 0, textString.length, rect)
            canvas.drawText(textString, canvasWidth + (priceTextPaint.ascent()), p - rect.exactCenterY(), priceTextPaint)
            i += pUnit
        }
    }

    fun drawTimeGridsAndTexts(canvas: Canvas, tIndexUnit: Long) {
        println("drawTimeGridsAndTexts $tIndexUnit")


        var k = timeStart
        while (k<=timeEnd) {
            if (k>=t.first()&&k<=t.last()) {

                val p = (((timeStart-k ) * coordinateTimeRatio))
                println("drawTimeGridsAndTexts $tIndexUnit ${milliToHMS(k.times(1000))} $p")
                canvas?.drawLine(p, 0f, p, canvasHeight - timeLineHeight, paintLineGrids)
                val rect = Rect()
                val textString = milliToHMS(k.times(1000)).toString()
                timeTextPaint.getTextBounds(textString, 0, textString.length, rect)
                canvas.drawText(textString, p, canvasHeight - timeLineHeight - (timeTextPaint.ascent()) + (timeLineHeight / 3), timeTextPaint)

            }
            k+=tIndexUnit

        }
    }

//    fun drawPriceLine(canvas: Canvas) {
//        var prevX: Float? = null
//        var prevY: Float? = null
//        c.forEachIndexed { index, close ->
//            val x = (index * coordinateTimeRatio)
//            val y = (canvasHeight - ((close - priceBottom) * coordinatePriceRatio))
//            if (prevX != null && prevY != null) {
//                if (y > prevY!!)
//                    canvas?.drawLine(prevX!!, prevY!!, x, y, paintLineRed)
//                else
//                    canvas?.drawLine(prevX!!, prevY!!, x, y, paintLineGreen)
//            }
//            prevX = x
//            prevY = y
//        }
//    }

    fun drawCandles(canvas: Canvas) {
        val x0=  (timeStart- t[0]) * coordinateTimeRatio
        val x1=  (timeStart- t[1]) * coordinateTimeRatio
        val candleWidthHalf = ((x0-x1) / 3f).absoluteValue
        println("candleCLF $candleWidthHalf")
        t.forEachIndexed { index, time ->




            val isGreen = c[index] >= o[index]
            val x = (timeStart- t[index]) * coordinateTimeRatio
            val hY = (canvasHeight - ((h[index] - priceBottom) * coordinatePriceRatio))
            val lY = (canvasHeight - ((l[index] - priceBottom) * coordinatePriceRatio))
            val cy = (canvasHeight - ((c[index] - priceBottom) * coordinatePriceRatio))
            val oy = (canvasHeight - ((o[index] - priceBottom) * coordinatePriceRatio))


            canvas?.drawLine(x, hY, x, lY, if (isGreen) paintLineGreen else paintLineRed)
            canvas.drawRect(x - candleWidthHalf, oy, x + candleWidthHalf, cy, if (isGreen) paintLineGreen else paintLineRed)


        }

    }


    var priceLineZoomStartedY = -1f
    var zoomMax = -1f
    var priceLineZoomStartedTop = -1f
    var priceLineZoomStartedBottom = -1f


    var dragStartedX = -1f
    var dragStartedY = -1f
    var dragStartedPriceTop = -1f
    var dragStartedPriceBottom = -1f
    var dragStartedStartTime=0L
    var dragStartedEndTime=0L
    var pinchZoomStarted = false
    var pinchX0: Float? = null
    var pinchY0: Float? = null
    var pinchX1: Float? = null
    var pinchY1: Float? = null
    var pinchStartedDistance: Float? = null
    var pinchStartedTimeStart = 0L
    var pinchStartedTimeEnd = 0L


    fun onPinch(event: MotionEvent) {

        if (event.pointerCount == 1) {

            if (event.getPointerId(0) == 0) {
                pinchX0 = event.getX(0)
                pinchY0 = event.getY(0)
            }


            if (event.getPointerId(0) == 1) {
                pinchX1 = event.getX(0)
                pinchY1 = event.getY(0)
            }

        }
        if (event.pointerCount == 2) {
            if (event.getPointerId(0) == 0) {
                pinchX0 = event.getX(0)
                pinchY0 = event.getY(0)
            }

            if (event.getPointerId(0) == 1) {
                pinchX1 = event.getX(0)
                pinchY1 = event.getY(0)
            }


            if (event.getPointerId(1) == 0) {
                pinchX0 = event.getX(1)
                pinchY0 = event.getY(1)
            }

            if (event.getPointerId(1) == 1) {
                pinchX1 = event.getX(1)
                pinchY1 = event.getY(1)
            }


        }


        if (pinchX0 != null && pinchX1 != null && pinchY0 != null && pinchY1 != null) {
            // d = √[(x2 x 2 − x1 x 1 )2 + (y2 y 2 − y1 y 1 )2].
            val xd = (pinchX1!! - pinchX0!!).toDouble()
            val yd = (pinchY1!! - pinchY0!!).toDouble()
            val d = Math.sqrt(((xd * xd) + (yd * yd)))

            if (pinchStartedDistance == null) {
                pinchStartedDistance = d.toFloat()
                pinchStartedTimeStart = timeStart
                pinchStartedTimeEnd = timeEnd
            } else {



                val ratio = pinchStartedDistance!! / d
                var timeGridUnit = (pinchStartedTimeEnd-pinchStartedTimeStart) / 3L
                println("ratio $ratio $coordinateTimeRatio ${coordinateTimeRatio*ratio} $timeGridUnit ${milliFileDateTime(timeStart.times(1000))} ${milliFileDateTime(timeEnd.times(1000))}")



               val scroll= (ratio*timeGridUnit).toLong()-timeGridUnit

                timeStart = pinchStartedTimeStart - scroll
                timeEnd = pinchStartedTimeEnd + scroll
                println("ratio $ratio ${milliFileDateTime(timeStart.times(1000))} ${milliFileDateTime(timeEnd.times(1000))}")

                postInvalidate()


            }


        }


    }


    fun touchActionDone() {
        priceLineZoomStartedY = -1f
        dragStartedX = -1f
        dragStartedY = -1f
        dragStartedEndTime = -1L
        dragStartedStartTime = -1L
        pinchX0 = null
        pinchX1 = null
        pinchY0 = null
        pinchY1 = null
        pinchZoomStarted = false
        pinchStartedDistance = null

        postInvalidate()
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            if (pinchZoomStarted) {
                if (event.action == 1) {
                    touchActionDone()
                } else {
                    onPinch(event)
                }

            } else if (event.pointerCount > 1) {
                pinchZoomStarted = true
                onPinch(event)
            } else if (event.action == 0 && event.x < priceLineX) {
                dragStartedPriceTop = priceTop
                dragStartedPriceBottom = priceBottom
                dragStartedStartTime=timeStart
                dragStartedEndTime=timeEnd
                dragStartedX = event.x
                dragStartedY = event.y

            } else if (event.action == 0 && event.x >= priceLineX) {
                priceLineZoomStartedTop = priceTop
                priceLineZoomStartedBottom = priceBottom
                priceLineZoomStartedY = event.y
                zoomMax = priceTop - ((priceTop + priceBottom) / 2)


            } else if (priceLineZoomStartedY >= 0) {
                val zoom = (priceLineZoomStartedY - event.y) / canvasHeight
                if (zoom >= 0) {
                    priceTop = priceLineZoomStartedTop - (zoom.absoluteValue * zoomMax)
                    priceBottom = priceLineZoomStartedBottom + (zoom.absoluteValue * zoomMax)
                } else {
                    priceTop = priceLineZoomStartedTop + (zoom.absoluteValue * zoomMax)
                    priceBottom = priceLineZoomStartedBottom - (zoom.absoluteValue * zoomMax)
                }
                postInvalidate()
                if (event.action == 1) {
                    touchActionDone()
                }
            } else if (dragStartedX >= 0f && dragStartedY >= 0f) {

                priceTop = dragStartedPriceTop - ((dragStartedY - event.y) / coordinatePriceRatio)
                priceBottom = dragStartedPriceBottom - ((dragStartedY - event.y) / coordinatePriceRatio)

                timeStart=dragStartedStartTime - ((dragStartedX - event.x) / coordinateTimeRatio).toLong()
                timeEnd=dragStartedEndTime - ((dragStartedX - event.x) / coordinateTimeRatio).toLong()



                postInvalidate()
                if (event.action == 1) {
                    touchActionDone()
                }
            }
        }




        return true


    }


    private val paintTextGrey1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        color = Color.parseColor("#C1E1AD")
        textSize = AppApplication.instance.resources.getDimension(R.dimen.dp16)
        setTextAlign(Paint.Align.CENTER)


    }

    private val priceTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        color = Color.parseColor("#C1E1AD")
        textSize = AppApplication.instance.resources.getDimension(R.dimen._10sdp)
        setTextAlign(Paint.Align.RIGHT)


    }

    private val timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {

        color = Color.parseColor("#C1E1AD")
        textSize = AppApplication.instance.resources.getDimension(R.dimen._10sdp)
        setTextAlign(Paint.Align.CENTER)


    }

    private val paintLineGrey = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C1E1AD")

    }

    private val paintLineGrids = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3C3F41")

    }

    private val paintLineBlue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE

    }
    private val paintLineGreen = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN

    }
    private val paintLineRed = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED

    }


}
