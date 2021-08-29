package com.example.tooltip_with_arrow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.use
import androidx.core.view.setPadding

/**
 * A tooltip with a downward pointing arrow and a shadow. This is useful for drawing on a Google
 * Map which has no concept of View elevation-based shadows.
 */
class TooltipLayout : FrameLayout {
    constructor(context: Context) : super(context) {
        initialize(context)
    }
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        initialize(context, attributeSet)
    }
    constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attributeSet, defStyleAttr) {
        initialize(context, attributeSet, defStyleAttr)
    }

    // Attributes
    private var shadowSize = 0f
    private var arrowSize = 0f
    private var bodyCornerRadius = 0f
    private var arrowCornerRadius = 0f
    private var shadowColor = 0
    private var shadowDeltaY = 0f

    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = Color.WHITE
            setShadowLayer(shadowSize, 0f, shadowDeltaY, shadowColor)
        }
    }
    private val tooltipBody by lazy {
        // Body should not include shadows/arrow
        RectF(shadowSize, shadowSize, width - shadowSize, height - shadowSize - arrowSize)
    }
    private val tooltipPath = Path()
    private val tooltipArrowPath = Path()
    private val roundedCorners by lazy {
        FloatArray(8) { bodyCornerRadius }
    }

    private fun initialize(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) {
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.TooltipLayout,
            defStyleAttr,
            0
        ).use { typedArray ->
            shadowSize = typedArray.getDimension(R.styleable.TooltipLayout_shadowSize, 2.dp(context))
            shadowColor = typedArray.getColor(R.styleable.TooltipLayout_shadowColor, Color.parseColor("#80888888"))
            shadowDeltaY = typedArray.getDimension(R.styleable.TooltipLayout_shadowDeltaY, 1.dp(context))
            arrowSize = typedArray.getDimension(R.styleable.TooltipLayout_arrowSize, 8.dp(context))
            arrowCornerRadius = typedArray.getDimension(R.styleable.TooltipLayout_arrowCornerRadius, 1.dp(context))
            bodyCornerRadius = typedArray.getDimension(R.styleable.TooltipLayout_bodyCornerRadius, 8.dp(context))
        }

        // Necessary for onDraw(Canvas) callback to be invoked in ViewGroups.
        setWillNotDraw(false)
        // Don't let children be drawn outside the bounds of the main tooltip body and/or over the
        // tooltip shadows/arrow.
        setPadding(shadowSize.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val currentHeight = MeasureSpec.getSize(measuredHeight)
        // Increase the current height to accommodate the tooltip arrow.
        val heightWithArrow = currentHeight + arrowSize
        val measureSpecHeightWithArrow = MeasureSpec.makeMeasureSpec(heightWithArrow.toInt(), MeasureSpec.EXACTLY)
        setMeasuredDimension(measuredWidth, measureSpecHeightWithArrow)
    }

    override fun onDraw(canvas: Canvas) {
        // Trace the body
        tooltipPath.addRoundRect(tooltipBody, roundedCorners, Path.Direction.CW)
        // Trace the arrow
        with(tooltipArrowPath) {
            val tooltipCenterX = tooltipBody.centerX()
            moveTo(tooltipCenterX + arrowSize, tooltipBody.bottom)
            lineTo(tooltipCenterX + arrowCornerRadius, tooltipBody.bottom + arrowSize - arrowCornerRadius)
            // Curve bottom arrow tip
            quadTo(
                tooltipCenterX, tooltipBody.bottom + arrowSize,
                tooltipCenterX - arrowCornerRadius, tooltipBody.bottom + arrowSize - arrowCornerRadius
            )
            lineTo(tooltipCenterX - arrowSize, tooltipBody.bottom)
            close()
        }
        // Combine
        tooltipPath.addPath(tooltipArrowPath)
        canvas.drawPath(tooltipPath, paint)
    }

    private fun Int.dp(context: Context): Float {
        return this * context.resources.displayMetrics.density
    }
}
