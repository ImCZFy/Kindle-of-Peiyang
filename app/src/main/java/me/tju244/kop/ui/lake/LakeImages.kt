package me.tju244.kop.ui.lake

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import me.tju244.kop.ui.ImageMemoryCache
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URL
import java.util.UUID
import kotlin.math.sqrt

internal val ImageLoadSemaphore = Semaphore(permits = 4)
internal val LocalDeferImageLoading = staticCompositionLocalOf { false }

internal fun String.toLakeImageUrl(size: String): String {
    val value = trim()
    if (value.isBlank()) return ""
    if (value.startsWith("http://") || value.startsWith("https://")) return value
    return "https://qnhdpic.twt.edu.cn/download/$size/${value.trimStart('/')}"
}

internal fun imageCacheKey(imageUrl: String, maxDecodeSize: Int): String = "$imageUrl@$maxDecodeSize"

internal suspend fun loadImageBitmap(imageUrl: String, maxDecodeSize: Int): ImageBitmap? {
    if (imageUrl.isBlank()) return null
    val cacheKey = imageCacheKey(imageUrl, maxDecodeSize)
    ImageMemoryCache.get(cacheKey)?.let { return it }
    val fromDisk = ImageMemoryCache.decodeFromDisk(cacheKey, maxDecodeSize)
    if (fromDisk != null) {
        ImageMemoryCache.put(cacheKey, fromDisk)
        return fromDisk
    }
    return ImageLoadSemaphore.withPermit {
        withContext(Dispatchers.IO) {
            runCatching {
                val bytes = URL(imageUrl).openStream().use { it.readBytes() }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, bounds)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inSampleSize = calculateImageSampleSize(bounds.outWidth, bounds.outHeight, maxDecodeSize)
                }
                val bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(bytes), null, options)?.asImageBitmap()
                if (bitmap != null) {
                    ImageMemoryCache.putBytesDisk(cacheKey, bytes)
                }
                bitmap
            }.getOrNull()
        }
    }?.also { bitmap ->
        ImageMemoryCache.put(cacheKey, bitmap)
    }
}

internal fun calculateImageSampleSize(width: Int, height: Int, maxSize: Int): Int {
    if (width <= 0 || height <= 0 || maxSize <= 0) return 1
    var sample = 1
    var sampledWidth = width
    var sampledHeight = height
    while (sampledWidth / 2 >= maxSize || sampledHeight / 2 >= maxSize) {
        sample *= 2
        sampledWidth /= 2
        sampledHeight /= 2
    }
    return sample.coerceAtLeast(1)
}

@Composable
internal fun AvatarImage(avatar: String, author: String, size: androidx.compose.ui.unit.Dp = 36.dp) {
    val deferImageLoading = LocalDeferImageLoading.current
    val avatarUrl = remember(avatar) { avatar.toLakeImageUrl("origin") }
    val cacheKey = remember(avatarUrl) { imageCacheKey(avatarUrl, 160) }
    val cachedImage = remember(cacheKey) { ImageMemoryCache.get(cacheKey) }
    val image by produceState<ImageBitmap?>(initialValue = cachedImage, avatarUrl, deferImageLoading) {
        if (cachedImage == null && !deferImageLoading) {
            value = loadImageBitmap(avatarUrl, maxDecodeSize = 160)
        }
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(LakeColors.cardAlt),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(author.ifBlank { "?" }.take(1), color = LakeColors.text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun NetworkLakeImage(path: String, size: String, modifier: Modifier = Modifier, contentScale: ContentScale = ContentScale.Crop) {
    val deferImageLoading = LocalDeferImageLoading.current
    val imageUrl = remember(path, size) { path.toLakeImageUrl(size) }
    val maxDecodeSize = remember(size, contentScale) {
        when {
            size == "thumb" -> 520
            contentScale == ContentScale.Fit -> 2200
            else -> 1400
        }
    }
    val cacheKey = remember(imageUrl, maxDecodeSize) { imageCacheKey(imageUrl, maxDecodeSize) }
    val cachedImage = remember(cacheKey) { ImageMemoryCache.get(cacheKey) }
    val image by produceState<ImageBitmap?>(initialValue = cachedImage, imageUrl, deferImageLoading) {
        if (cachedImage == null && !deferImageLoading) {
            value = loadImageBitmap(imageUrl, maxDecodeSize = maxDecodeSize)
        }
    }
    Box(modifier = modifier.background(LakeColors.cardAlt), contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}

@Composable
internal fun LocalUriImage(uri: Uri, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    Box(modifier = modifier.background(LakeColors.cardAlt), contentAlignment = Alignment.Center) {
        if (image != null) {
            Image(bitmap = image!!, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
internal fun PostImagePreview(imageUrls: List<String>, useThumb: Boolean, onOpenImages: (List<String>, Int) -> Unit) {
    val urls = imageUrls.filter { it.isNotBlank() }
    if (urls.isEmpty()) return
    val displayUrls = urls.take(3)
    if (displayUrls.size == 1) {
        NetworkLakeImage(
            path = displayUrls.first(),
            size = if (useThumb) "thumb" else "origin",
            modifier = Modifier
                .fillMaxWidth()
                .height(if (useThumb) 132.dp else 180.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenImages(urls, 0) },
        )
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            displayUrls.forEachIndexed { index, path ->
                Box(modifier = Modifier.weight(1f)) {
                    NetworkLakeImage(
                        path = path,
                        size = "thumb",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenImages(urls, index) },
                    )
                    if (index == displayUrls.lastIndex && urls.size > displayUrls.size) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.40f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("+${urls.size - displayUrls.size}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun SelectedImageStrip(images: List<Uri>, onPickImages: () -> Unit, onRemoveImage: (Uri) -> Unit, maxText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onPickImages,
            modifier = Modifier.fillMaxWidth().height(46.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("添加图片 (${images.size}/9)")
                Icon(
                    imageVector = MiuixIcons.Add,
                    contentDescription = "添加图片",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(maxText, color = LakeColors.muted)
        if (images.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(images) { uri ->
                    Box {
                        LocalUriImage(
                            uri = uri,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable { onRemoveImage(uri) }
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text("×", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CommunityImageStrip(imageUrls: List<String>, onOpenImages: (List<String>, Int) -> Unit) {
    val urls = imageUrls.filter { it.isNotBlank() }
    val displayUrls = urls.take(4)
    if (displayUrls.isEmpty()) return
    if (displayUrls.size == 1) {
        NetworkLakeImage(
            path = displayUrls.first(),
            size = "thumb",
            modifier = Modifier
                .width(120.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable { onOpenImages(urls, 0) },
        )
        return
    }
    Row(modifier = Modifier.fillMaxWidth().height(78.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        displayUrls.forEachIndexed { index, path ->
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                NetworkLakeImage(
                    path = path,
                    size = "thumb",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .clickable { onOpenImages(urls, index) },
                )
                if (index == displayUrls.lastIndex && urls.size > displayUrls.size) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${urls.size - displayUrls.size}+", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ImageViewerOverlay(urls: List<String>, initialIndex: Int, onDismiss: () -> Unit) {
    ImmersiveSystemBars()
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, urls.lastIndex),
        pageCount = { urls.size },
    )
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    LaunchedEffect(pagerState.currentPage) {
        scale = 1f
        offset = Offset.Zero
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = scale <= 1.01f,
            modifier = Modifier
                .fillMaxSize(),
        ) { page ->
            NetworkLakeImage(
                path = urls[page],
                size = "origin",
                modifier = Modifier
                    .fillMaxSize()
                    .imagePreviewGestures(
                        scale = scale,
                        onScaleChange = { scale = it },
                        offset = offset,
                        onOffsetChange = { offset = it },
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit,
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.38f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text("${pagerState.currentPage + 1}/${urls.size}", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ImmersiveSystemBars() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        if (window == null) {
            onDispose { }
        } else {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            onDispose {
                controller.show(WindowInsetsCompat.Type.systemBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }
}

@Composable
private fun Modifier.imagePreviewGestures(
    scale: Float,
    onScaleChange: (Float) -> Unit,
    offset: Offset,
    onOffsetChange: (Offset) -> Unit,
): Modifier {
    val latestScale by rememberUpdatedState(scale)
    val latestOffset by rememberUpdatedState(offset)
    val latestOnScaleChange by rememberUpdatedState(onScaleChange)
    val latestOnOffsetChange by rememberUpdatedState(onOffsetChange)
    return pointerInput(Unit) {
    awaitEachGesture {
        var lastCentroid: Offset? = null
        var lastDistance = 0f
        var gestureScale = latestScale
        var gestureOffset = latestOffset
        while (true) {
            val event = awaitPointerEvent()
            val pressed = event.changes.filter { it.pressed }
            if (pressed.isEmpty()) break
            if (pressed.size >= 2) {
                val centroid = pressed.centroid()
                val distance = pressed.averageDistanceTo(centroid)
                val previousCentroid = lastCentroid
                if (previousCentroid != null && lastDistance > 0f && distance > 0f) {
                    val zoomChange = distance / lastDistance
                    val nextScale = (gestureScale * zoomChange).coerceIn(1f, 5f)
                    gestureScale = nextScale
                    latestOnScaleChange(nextScale)
                    val pan = (centroid - previousCentroid) * 1.45f
                    gestureOffset = if (nextScale <= 1.01f) Offset.Zero else gestureOffset + pan
                    latestOnOffsetChange(gestureOffset)
                }
                lastCentroid = centroid
                lastDistance = distance
                pressed.forEach { it.consume() }
            } else if (gestureScale > 1.01f) {
                val change = pressed.first()
                val pan = change.positionChange() * 1.55f
                gestureOffset += pan
                latestOnOffsetChange(gestureOffset)
                change.consume()
            }
        }
    }
    }
}

private fun List<PointerInputChange>.centroid(): Offset {
    var x = 0f
    var y = 0f
    forEach {
        x += it.position.x
        y += it.position.y
    }
    return Offset(x / size, y / size)
}

private fun List<PointerInputChange>.averageDistanceTo(center: Offset): Float {
    var sum = 0f
    forEach {
        val dx = it.position.x - center.x
        val dy = it.position.y - center.y
        sum += sqrt(dx * dx + dy * dy)
    }
    return sum / size
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

internal fun Context.createComposeCameraImageUri(): Uri? {
    return runCatching {
        val authority = "$packageName.fileprovider"
        val dir = File(cacheDir, "compose_camera").apply { mkdirs() }
        val file = File(dir, "compose_${UUID.randomUUID()}.jpg")
        FileProvider.getUriForFile(this, authority, file)
    }.getOrNull()
}

