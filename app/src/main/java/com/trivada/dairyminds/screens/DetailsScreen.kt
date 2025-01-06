package com.trivada.dairyminds.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.room.Room
import com.trivada.dairyminds.R
import com.trivada.dairyminds.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(navController: NavController, title : String, content : String){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { Room.databaseBuilder(context, AppDatabase::class.java, "database").build() }
    val sharedPreferences = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
    var textSize by remember { mutableStateOf(sharedPreferences.getInt("textSize", 20)) }
    var selectedSize by remember { mutableStateOf(sharedPreferences.getInt("selectedSize", 20)) }
    val tT = remember { mutableStateOf(false) }
    var currentText = rememberSaveable { mutableStateOf(if(title == "newtitle") "" else title) }
    var currentText2 = rememberSaveable { mutableStateOf(if(content == "newcontent") "" else content) }
    val showDialog = remember { mutableStateOf(false) }
    val showDialogPdf = remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    val currentDate = dateFormat.format(calendar.time).replace(".", "")
    val currentTime = timeFormat.format(calendar.time)

    LaunchedEffect(key1 = textSize, key2 = selectedSize) {
        snapshotFlow { textSize }
            .collect { newTextSize ->
                sharedPreferences.edit().putInt("textSize", newTextSize).apply()
            }
        snapshotFlow { selectedSize }
            .collect { newSelectedSize ->
                sharedPreferences.edit().putInt("selectedSize", newSelectedSize).apply()
            }
    }
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding(), contentAlignment = Alignment.CenterEnd) {
        Image(painter = painterResource(R.drawable.lightfon),
            contentDescription = "fon_image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(modifier = Modifier.fillMaxSize().imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End) {
                Image(painter = painterResource(R.drawable.venik), contentDescription = "venik",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable { showDialog.value = true },
                    colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        containerColor = colorResource(id = R.color.white),
                        title = { Text(text = "Подтверждение!", color = colorResource(id = R.color.darkred),
                            fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Text(
                                text = "Вы действительно хотите удалить весь текст?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.gray),
                            )
                        },
                        confirmButton = {
                            Button(colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.gray)),
                                onClick = {
                                   currentText.value = ""
                                   currentText2.value = ""
                                    showDialog.value = false
                                }) { Text("Да", color = colorResource(id = R.color.white), fontSize = 16.sp)
                            }
                        },
                        dismissButton = { Button(colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.gray)
                        ),
                            onClick = { showDialog.value = false }) {
                            Text("Нет", color = colorResource(id = R.color.white), fontSize = 16.sp)
                        }
                        })
                }
                Image(painter = painterResource(R.drawable.baseline_text_fields_24), contentDescription = "font_size",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable { tT.value = !tT.value },
                    colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
                Image(painter = painterResource(R.drawable.baseline_share_24), contentDescription = "share",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable {
                        val maxLength = 1000
                        val textToShare = currentText2.value
                        if (textToShare.length > maxLength) {
                            val parts = textToShare.chunked(maxLength)
                            scope.launch {
                                for (part in parts) {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, part)
                                        type = "text/plain"
                                    }
                                    context.startActivity(
                                        Intent.createChooser(sendIntent, "Поделиться через...")
                                    )
                                    delay(1000)
                                }
                            }
                        } else {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, textToShare)
                                type = "text/plain"
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Поделиться через...")
                            )
                        }
                    },
                     colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
                fun createPdf(context: Context, text: String, filename: String) {
                    scope.launch(Dispatchers.IO) {
                        val pdfDocument = PdfDocument()
                        val paint = TextPaint()
                        paint.color = android.graphics.Color.BLACK
                        paint.textSize = 12f
                        val pageWidth = 300
                        val pageHeight = 600
                        val marginHorizontal = 10
                        val marginVertical = 50
                        val textWidth = pageWidth - 2 * marginHorizontal
                        val staticLayoutBuilder = StaticLayout.Builder.obtain(
                            text, 0, text.length, paint, textWidth
                        ).setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(1.0f, 0.0f)
                        val textLayout = staticLayoutBuilder.build()
                        var currentY = 0f
                        var currentLine = 0
                        val lineSpacing = 18f
                        while (currentLine < textLayout.lineCount) {
                            val pageInfo = PdfDocument.PageInfo.Builder(
                                pageWidth,
                                pageHeight,
                                pdfDocument.pages.size + 1
                            ).create()
                            val page = pdfDocument.startPage(pageInfo)
                            val canvas = page.canvas
                            canvas.save()
                            canvas.translate(marginHorizontal.toFloat(), marginVertical.toFloat())
                            var lineDrawnOnPage = 0
                            while (currentLine < textLayout.lineCount) {
                                val lineTop = textLayout.getLineTop(currentLine)
                                if (currentLine + 1 < textLayout.lineCount) {
                                    if (currentY + lineSpacing > pageHeight - marginVertical * 2) {
                                        canvas.restore()
                                        pdfDocument.finishPage(page)
                                        currentY = 0f
                                        lineDrawnOnPage = 0
                                        break
                                    }
                                }
                                val lineStart = textLayout.getLineStart(currentLine)
                                val lineEnd = textLayout.getLineEnd(currentLine)
                                val lineText = text.substring(lineStart, lineEnd)
                                canvas.drawText(
                                    lineText,
                                    0f,
                                    currentY + textLayout.getLineBaseline(currentLine)
                                        .toFloat() - lineTop,
                                    paint
                                )
                                currentY += lineSpacing
                                currentLine++
                                lineDrawnOnPage++
                            }
                            if (lineDrawnOnPage > 0) {
                                canvas.restore()
                                pdfDocument.finishPage(page)
                            }
                        }
                        val downloadsDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        val file = File(downloadsDir, "$filename.pdf")
                        try {
                            if (!file.exists()) {
                                file.createNewFile()
                            }
                            if (downloadsDir.exists() && downloadsDir.canWrite()) {
                                pdfDocument.writeTo(FileOutputStream(file))
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(uri, "application/pdf")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                context.startActivity(intent)
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "downloads_directory_not_accessible", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TAG", "Error saving PDF", e)
                        } finally {
                            pdfDocument.close()
                        }
                    }
                }
                fun saveTextAsPdf(context: Context, text: String, filename: String) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        createPdf(context, text, filename)
                        Toast.makeText(context, "Файл pdf создан!", Toast.LENGTH_LONG).show()
                    }
                }
                Image(painter = painterResource(R.drawable.baseline_picture_as_pdf_24), contentDescription = "pdf",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable { showDialogPdf.value = true },
                     colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
                if (showDialogPdf.value) {
                    AlertDialog(
                        onDismissRequest = { showDialogPdf.value = false },
                        containerColor = colorResource(id = R.color.white),
                        title = { Text(text = "Подтверждение!", color = colorResource(id = R.color.darkred),
                            fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        text = {
                            Text(
                                text = "Вы действительно хотите создать файл pdf?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.gray),
                            )
                        },
                        confirmButton = {
                            Button(colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.gray)),
                                onClick = {
                                    saveTextAsPdf(context, currentText2.value, "${currentText.value} $currentDate")
                                    showDialogPdf.value = false
                                }) { Text("Да", color = colorResource(id = R.color.white), fontSize = 16.sp)
                            }
                        },
                        dismissButton = { Button(colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.gray)
                        ),
                            onClick = { showDialogPdf.value = false }) {
                            Text("Нет", color = colorResource(id = R.color.white), fontSize = 16.sp)
                        }
                        })
                }
                Image(painter = painterResource(R.drawable.baseline_save_24), contentDescription = "save",
                    modifier = Modifier.size(30.dp).padding(end = 8.dp).clickable {
                            scope.launch {
                                db.dairyDao().deleteByTitle(title)
                                db.dairyDao().upsertText(title = currentText.value, content = currentText2.value,
                                    date = currentDate, time = currentTime)
                            }
                            navController.navigate("MainScreen")
                        },
                     colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
            }
            TextField(placeholder = { Text("Введите заголовок...") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    cursorColor = colorResource(R.color.darkred),
                    unfocusedIndicatorColor = colorResource(id = R.color.darkred),
                    focusedIndicatorColor = colorResource(id = R.color.darkred),
                ),
                value = currentText.value,
                onValueChange = { newValue ->
                    currentText.value = newValue
                },
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gray)
                ),
            )
            TextField(placeholder = { Text("Введите основной текст...") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    cursorColor = colorResource(R.color.darkred),
                    unfocusedIndicatorColor = colorResource(id = R.color.darkred),
                    focusedIndicatorColor = colorResource(id = R.color.darkred),
                ),
                value = currentText2.value,
                onValueChange = { newValue ->
                    currentText2.value = newValue
                },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(4.dp),
                textStyle = TextStyle(
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gray)
                ),
            )
        }
        val offset by animateDpAsState(
            targetValue = if (tT.value) 0.dp else 70.dp, // Смещение вправо, когда скрыто
            animationSpec = tween(durationMillis = 300), label = ""
        )
        Column(modifier = Modifier.fillMaxHeight().width(70.dp).offset(x = offset).background(
            brush = Brush.horizontalGradient(listOf(colorResource(R.color.gray), colorResource(R.color.white)))
        ).verticalScroll(rememberScrollState()).zIndex(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly) {
            if(tT.value){
                listOf(10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30).forEach { size ->
                    Text( text = "$size",
                        color = if (selectedSize == size) colorResource(R.color.darkred) else colorResource(R.color.gray),
                        fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp).border(
                            width = if (selectedSize == size) 2.dp else 0.dp,
                            color = colorResource(R.color.darkred),
                            shape = RoundedCornerShape(4.dp)).clickable {
                            selectedSize = size
                            textSize = size
                            sharedPreferences.edit().putInt("selectedSize", size).apply()
                        }.padding(8.dp)
                    )
                }
            }
        }
    }
}