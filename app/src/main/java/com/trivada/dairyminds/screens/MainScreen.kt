package com.trivada.dairyminds.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.Room
import com.trivada.dairyminds.R
import com.trivada.dairyminds.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.text.contains


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController){
    val context = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    val showDialog = remember { mutableStateOf(false) }
    val db = remember { Room.databaseBuilder(context, AppDatabase::class.java, "database").build() }
    val dairyList by db.dairyDao().getAll().collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchIconClicked by remember { mutableStateOf(false) }

    val filteredList = if (searchText.isEmpty()) {
        dairyList
    } else {
        dairyList.filter { it.title.contains(searchText, ignoreCase = true) }
    }
    LaunchedEffect(key1 = isSearchIconClicked) {
        if (isSearchIconClicked) {
            focusRequester.requestFocus()
            keyboardController?.show()
            isSearchIconClicked = false
        }
    }
    BackHandler { context.finishAffinity() }
    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        Image(painter = painterResource(R.drawable.lightfon),
            contentDescription = "fon_image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(modifier = Modifier.fillMaxSize()){
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)){
                Row(modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically){
                    TextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.weight(1f).focusRequester(focusRequester).onKeyEvent {
                                if (it.key == Key.Enter) {
                                    keyboardController?.hide()
                                    true
                                } else {
                                    false
                                }
                            },
                        placeholder = { Text("Поиск", color = colorResource(R.color.darkred)) },
                        leadingIcon = {   Icon(Icons.Default.Search, contentDescription = "search",
                            tint = colorResource(R.color.darkred), modifier = Modifier.clickable {
                                isSearchIconClicked = true
                            }
                        ) },
                        trailingIcon = {  if (searchText.isNotEmpty()) {
                            Icon(Icons.Default.Close, contentDescription = "close",
                                tint = colorResource(R.color.darkred), modifier = Modifier.clickable {
                                    searchText = ""
                                    focusRequester.freeFocus()
                                    keyboardController?.hide()
                                }
                            )
                        } },
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = colorResource(R.color.lightgray),
                            cursorColor = Color.Black,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = colorResource(R.color.darkred), fontSize = 20.sp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = {
                            keyboardController?.hide()
                        })
                    )
                }
                Image(painter = painterResource(R.drawable.baseline_note_add_24),
                    contentDescription = "add_new",
                    modifier = Modifier.size(45.dp).clickable {
                            navController.navigate("DetailsScreen/newtitle/newcontent")
                        },
                    colorFilter = ColorFilter.tint(colorResource(R.color.darkred))
                )
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(filteredList){ item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 4.dp),
                        shape = RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        onClick = { navController.navigate("DetailsScreen/${item.title}/${item.content}") }){
                        Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = item.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.Delete, contentDescription = "delete_item",
                                    tint = colorResource(R.color.darkred), modifier = Modifier.clickable {
                                        showDialog.value = true
                                    }
                                )
                                if (showDialog.value) {
                                    AlertDialog(
                                        onDismissRequest = { showDialog.value = false },
                                        containerColor = colorResource(id = R.color.white),
                                        title = { Text(text = "Подтверждение!", color = colorResource(id = R.color.darkred),
                                            fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                                        text = {
                                            Text(
                                                text = "Вы действительно хотите удалить элемент из списка?",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorResource(R.color.gray),
                                            )
                                        },
                                        confirmButton = {
                                            Button(colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = colorResource(id = R.color.gray)),
                                                onClick = {
                                                    scope.launch(Dispatchers.IO) {
                                                        db.dairyDao().delete(item)
                                                    }
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
                            }
                            Text(text = item.content,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp, end = 4.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End){
                                Text(text = "${item.date}г.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(text = item.time,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}