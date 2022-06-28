package com.example.composelist

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.composelist.ui.theme.ComposeListTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Sets whether the system should ensure that the navigation bar has enough contrast when a fully transparent background is requested.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            ComposeListTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    val listItems = remember {
                        mutableStateListOf<TagNode>().apply {
                            repeat(1000) { index ->
                                add(TagNode("key_$index", rangeForRandom.random()))
                            }
                        }
                    }

                    val modalBottomSheetState =
                        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
                    val scope = rememberCoroutineScope()
                    ModalBottomSheetLayout(
                        sheetContent = {
                            BottomSheetContent()
                        },
                        sheetState = modalBottomSheetState,
                        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        sheetBackgroundColor = colorResource(id = R.color.purple_200),
                        // scrimColor = Color.Red,  // Color for the fade background when you open/close the drawer
                    ) {
//                        CompositionLocalProvider(LocalWindowInsets provides windowInsets) {
//                            InsetsBasics()
//                        }
                        CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
                            Greeting(listItems = listItems) { clickedIndex ->
//                        listItems.add(TagNode("key_${listItems.size}", rangeForRandom.random()))
//                            listItems.sortBy { it.startMargin }
                                scope.launch {
                                    modalBottomSheetState.show()
                                }
//                            listItems.add(TagNode("key_${listItems.size + 1}", listItems.last().startMargin + 1))
                                false
                            }
                        }
                    }
                }
            }
        }
    }
}

@Stable
private val rangeForRandom = (16..300)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Greeting(listItems: List<TagNode>, updateCount: (Int) -> Boolean) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    LazyColumn(
        state = scrollState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = with(LocalDensity.current) {
            WindowInsets.navigationBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                .getBottom(LocalDensity.current)
                .toDp()
        }),
    ) {
        itemsIndexed(listItems, key = { _, tagNode -> tagNode.key }) { index, tagNode ->
            Row(
                Modifier
                    .clickable {
                        if (updateCount(index)) { // 最后一次添加才移动到最后
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(listItems.size)
                            }
                        }
                    }
                    .padding(top = if (index == 0) 8.dp + with(LocalDensity.current) {
                        WindowInsets.statusBars
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .getTop(LocalDensity.current)
                            .toDp()
                    } else 8.dp, bottom = 8.dp)
                    .height(30.dp)
                    .fillMaxWidth()
                    .animateItemPlacement(), verticalAlignment = Alignment.CenterVertically
            ) {
                // 偏移一个随机数，类似于子标签。
                Text(
                    text = "${index}-${tagNode.startMargin}超长文本-",
                    maxLines = 1,
                    modifier = Modifier
                        .padding(start = with(LocalDensity.current) {
                            tagNode.startMargin.toDp()
                        }, end = 0.dp)
                        .weight(0.5f, true),
                    overflow = TextOverflow.Ellipsis
                )
                if (index % 2 == 0) {
                    Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = "")
                    Image(painter = painterResource(id = R.drawable.ic_launcher_background), contentDescription = "", modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
    }
}