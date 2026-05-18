package me.tju244.kop.ui.effect

import android.os.Build
import androidx.annotation.RequiresApi
import top.yukonga.miuix.kmp.blur.RuntimeShader

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class BgEffectPainter {
    val runtimeShader by lazy {
        RuntimeShader(OS2_BG_FRAG).also { shader ->
            shader.setFloatUniform("uTranslateY", 0f)
            shader.setFloatUniform("uNoiseScale", 1.5f)
            shader.setFloatUniform("uPointOffset", 0.1f)
            shader.setFloatUniform("uPointRadiusMulti", 1.8f)
            shader.setFloatUniform("uAlphaMulti", 1f)
        }
    }

    private val resolution = FloatArray(2)
    private val bound = FloatArray(4)
    private var animTime = Float.NaN
    private var isDarkCached: Boolean? = null

    fun updateResolution(width: Float, height: Float) {
        if (resolution[0] == width && resolution[1] == height) return
        resolution[0] = width
        resolution[1] = height
        runtimeShader.setFloatUniform("uResolution", resolution)
    }

    fun updateAnimTime(time: Float) {
        if (animTime == time) return
        animTime = time
        runtimeShader.setFloatUniform("uAnimTime", animTime)
    }

    fun updatePresetIfNeeded(logoHeight: Float, height: Float, width: Float, isDark: Boolean) {
        if (isDarkCached == isDark) return
        val heightRatio = logoHeight / height
        if (width <= height) {
            bound[0] = 0f
            bound[1] = 1f - heightRatio
            bound[2] = 1f
            bound[3] = heightRatio
        } else {
            val aspectRatio = width / height
            val contentCenterY = 1f - heightRatio / 2f
            bound[0] = 0f
            bound[1] = contentCenterY - aspectRatio / 2f
            bound[2] = 1f
            bound[3] = aspectRatio
        }
        val preset = BgEffectConfig.get(DeviceType.PHONE, isDark)
        runtimeShader.setFloatUniform("uPoints", preset.points)
        runtimeShader.setFloatUniform("uColors", preset.colors)
        runtimeShader.setFloatUniform("uLightOffset", preset.lightOffset)
        runtimeShader.setFloatUniform("uSaturateOffset", preset.saturateOffset)
        runtimeShader.setFloatUniform("uBound", bound)
        isDarkCached = isDark
    }
}


