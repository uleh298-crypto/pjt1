package com.ssafy.ssabree.features.dday.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ssafy.ssabree.app.ApplicationClass
import com.ssafy.ssabree.app.FakeAppContainer
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.datasource.local.DdayLocalStore
import com.ssafy.ssabree.core.datasource.local.LocalDdayItem
import com.ssafy.ssabree.core.designsystem.component.SsabreeDialog
import com.ssafy.ssabree.core.ui.simpleViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import com.ssafy.ssabree.core.utils.KoreanHolidays
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

// 디데이 데이터 모델
data class DdayItem(
    val id: Int,
    val title: String,
    val date: String,
    val dDay: String,
    val icon: ImageVector,
    val iconKey: String? = null,
    val isMain: Boolean = false,
    var showOnHome: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DdayDetailScreen(
    onBackClick: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: DdayViewModel = simpleViewModel {
        DdayViewModel(container.ddayRepository)
    }
    val uiState by viewModel.uiState.collectAsState()

    val localStore = remember { DdayLocalStore(ApplicationClass.appContext) }
    val ddayMap = remember { mutableStateMapOf<String, SnapshotStateList<DdayItem>>() }
    val persistLocal: () -> Unit = {
        val localItems = ddayMap.values
            .flatten()
            .filter { it.id != -1 }
            .map { item ->
                LocalDdayItem(
                    id = item.id,
                    title = item.title,
                    date = item.date,
                    showOnHome = item.showOnHome,
                    // 우선순위: 서버에서 내려온 원본 iconKey -> 현재 아이콘의 name
                    iconKey = item.iconKey ?: ddayIconKey(item.icon)
                )
            }
        localStore.save(localItems)
    }

    LaunchedEffect(Unit) {
        val localItems = localStore.load()
        if (localItems.isNotEmpty()) {
            ddayMap.clear()
            localItems.forEach { item ->
                val mappedItem = DdayItem(
                    id = item.id,
                    title = item.title,
                    date = item.date,
                    dDay = calculateDday(item.date),
                    icon = ddayIconFromKey(item.iconKey),
                    iconKey = item.iconKey,
                    showOnHome = item.showOnHome
                )
                ddayMap.getOrPut(item.date) { mutableStateListOf() }.add(mappedItem)
            }
        } else {
            viewModel.loadDdays()
        }
    }

    LaunchedEffect(uiState.items) {
        if (uiState.items.isNotEmpty() && ddayMap.isEmpty()) {
            uiState.items.forEach { item ->
                val formattedDate = formatApiDate(item.targetDate)
                val mappedItem = DdayItem(
                    id = item.id,
                    title = item.title,
                    date = formattedDate,
                    dDay = formatDdayLabel(item.dDay),
                    icon = ddayIconFromKey(item.iconKey),
                    iconKey = item.iconKey
                )
                ddayMap.getOrPut(formattedDate) { mutableStateListOf() }.add(mappedItem)
            }
            persistLocal()
        }
    }
    val ddayList = remember {
        derivedStateOf {
            val allItems = ddayMap.values.flatten()
            allItems.sortedWith(
                compareBy<DdayItem> { if (ddayDiffDays(it.date) >= 0L) 0 else 1 }
                    .thenBy { ddayDiffDays(it.date) }
            )
        }
    }

    var itemToEdit by remember { mutableStateOf<DdayItem?>(null) }
    var showIconPickerForItem by remember { mutableStateOf<DdayItem?>(null) }

    val listState = rememberLazyListState()

    val calendarHeight = 380.dp
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage) { 1000 }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showEventDialog by remember { mutableStateOf(false) }

    if (showEventDialog) {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        val selectedDateString = sdf.format(selectedDate.time)

        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val salaryDayForMonth = getSalaryDay(year, month)
        val isSalaryDay = selectedDate.get(Calendar.DAY_OF_MONTH) == salaryDayForMonth

        val userEvents = ddayMap[selectedDateString] ?: emptyList()
        val allEvents = userEvents.toMutableList()

        if (isSalaryDay) {
            val salaryDdayItem = DdayItem(
                id = -1,
                title = "월급날",
                date = selectedDateString,
                dDay = calculateDday(selectedDateString),
                icon = Icons.Default.Payments,
                iconKey = ddayIconKey(Icons.Default.Payments),
                isMain = false
            )
            allEvents.add(0, salaryDdayItem)
        }

        EventDialog(
            date = selectedDate,
            events = allEvents,
            onDismiss = { showEventDialog = false },
            onAddDday = { title ->
                val newId = (ddayMap.values.flatten().maxOfOrNull { it.id } ?: 0) + 1
                val newDday = DdayItem(
                    id = newId,
                    title = title,
                    date = selectedDateString,
                    dDay = calculateDday(selectedDateString),
                    icon = Icons.Default.Event,
                    iconKey = ddayIconKey(Icons.Default.Event),
                )

                ddayMap.getOrPut(selectedDateString) { mutableStateListOf() }.add(newDday)
                persistLocal()
            }
        )
    }

    if (showIconPickerForItem != null) {
        IconPickerDialog(
            onDismiss = { showIconPickerForItem = null },
            onIconSelected = { newIcon ->
                val itemToUpdate = showIconPickerForItem!!
                val dateList = ddayMap[itemToUpdate.date]
                val itemIndex = dateList?.indexOfFirst { it.id == itemToUpdate.id }
                if (itemIndex != null && itemIndex != -1) {
                    dateList[itemIndex] = itemToUpdate.copy(
                        icon = newIcon,
                        iconKey = ddayIconKey(newIcon)
                    )
                    persistLocal()
                }
                showIconPickerForItem = null
            }
        )
    } else if (itemToEdit != null) {
        EditDdayDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                val originalItem = itemToEdit!!
                ddayMap[originalItem.date]?.remove(originalItem)
                if (ddayMap[originalItem.date].isNullOrEmpty()) {
                    ddayMap.remove(originalItem.date)
                }
                ddayMap.getOrPut(updatedItem.date) { mutableStateListOf() }.add(updatedItem)
                persistLocal()
                itemToEdit = null
            },
            onDelete = {
                val itemToDelete = itemToEdit!!
                ddayMap[itemToDelete.date]?.remove(itemToDelete)
                if (ddayMap[itemToDelete.date].isNullOrEmpty()) {
                    ddayMap.remove(itemToDelete.date)
                }
                persistLocal()
                itemToEdit = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("D-Day", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(calendarHeight)
                    ) {
                        DdayCalendarCard(
                            pagerState = pagerState,
                            selectedDate = selectedDate,
                            onDateClick = { date, calendar ->
                                val newSelected = calendar.clone() as Calendar
                                newSelected.set(Calendar.DAY_OF_MONTH, date)
                                selectedDate = newSelected
                                showEventDialog = true
                            },
                            isCondensed = false,
                            initialPage = initialPage,
                            ddayDates = ddayMap.keys
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text("내 디데이 목록", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("총 ${ddayList.value.size}개", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(ddayList.value, key = { it.id }) { item ->
                    DdayListItem(
                        item = item,
                        onClick = { itemToEdit = item },
                        onIconClick = { showIconPickerForItem = item }
                    )
                }
            }

        }
    }
}


// 월급날 계산 함수
fun getSalaryDay(year: Int, month: Int): Int {
    val cal = Calendar.getInstance().apply { set(year, month, 15) }
    while (KoreanHolidays.isWeekendOrHoliday(cal)) {
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return cal.get(Calendar.DAY_OF_MONTH)
}

fun calculateDday(dateString: String): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    val targetDate = Calendar.getInstance().apply {
        time = sdf.parse(dateString) ?: return "E-RR"
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = targetDate.timeInMillis - today.timeInMillis
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        diffDays == 0L -> "D-DAY"
        diffDays > 0L -> "D-${diffDays}"
        else -> "D+${-diffDays}"
    }
}

fun ddayDiffDays(dateString: String): Long {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
    val targetDate = Calendar.getInstance().apply {
        time = sdf.parse(dateString) ?: return Long.MAX_VALUE
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffMillis = targetDate.timeInMillis - today.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(diffMillis)
}

fun formatApiDate(targetDate: String): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        val output = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        val parsed = input.parse(targetDate)
        if (parsed != null) output.format(parsed) else targetDate.replace("-", ".")
    } catch (e: Exception) {
        targetDate.replace("-", ".")
    }
}

fun formatDdayLabel(dDay: Int): String {
    return when {
        dDay == 0 -> "D-DAY"
        dDay > 0 -> "D-$dDay"
        else -> "D+${-dDay}"
    }
}

fun ddayIconKey(icon: ImageVector): String = icon.name

fun ddayIconFromKey(key: String?): ImageVector {
    if (key.isNullOrBlank()) return Icons.Default.Event
    return ddayAllIcons().firstOrNull { it.name == key } ?: Icons.Default.Event
}

fun ddayAllIcons(): List<ImageVector> {
    return (ddayCodingIcons() + ddayStudyIcons() + ddayAchievementIcons() + ddayDailyIcons()).distinct()
}

fun ddayCodingIcons(): List<ImageVector> {
    return listOf(
        Icons.Default.Code, Icons.Default.Terminal, Icons.Default.DeveloperMode, Icons.Default.LaptopChromebook,
        Icons.Default.DataObject, Icons.Default.BugReport, Icons.Default.Storage, Icons.Default.Api,
        Icons.Default.Http, Icons.Default.Webhook, Icons.Default.Commit, Icons.Default.Cloud,
        Icons.Default.DeveloperBoard, Icons.Default.IntegrationInstructions, Icons.Default.Memory, Icons.Default.Router,
        Icons.Default.Dns, Icons.Default.Hub, Icons.Default.AutoAwesome, Icons.Default.Psychology, Icons.Default.RocketLaunch, Icons.Default.ModelTraining,
        Icons.Default.TipsAndUpdates, Icons.Default.Lightbulb, Icons.Default.Analytics, Icons.Default.QueryStats,
        Icons.Default.Science, Icons.Default.SmartToy, Icons.Default.SettingsEthernet, Icons.Default.Power, Icons.Default.SdStorage,
        Icons.Default.Security, Icons.Default.NetworkCheck, Icons.Default.Wifi, Icons.Default.Cable,
        Icons.Default.Scanner, Icons.Default.Print, Icons.Default.CloudUpload, Icons.Default.CloudDownload,
        Icons.Default.Sync, Icons.Default.PhoneAndroid, Icons.Default.Adb, Icons.Default.Language
    )
}

fun ddayStudyIcons(): List<ImageVector> {
    return listOf(
        Icons.Default.School, Icons.Default.Edit, Icons.Default.Book, Icons.Default.HistoryEdu,
        Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Filled.LibraryBooks, Icons.Default.Brush, Icons.Default.Draw,
        Icons.Default.SquareFoot, Icons.Default.Architecture, Icons.Default.Biotech, Icons.Default.Calculate,
        Icons.Default.Functions, Icons.Default.Translate, Icons.Default.GTranslate
    )
}

fun ddayAchievementIcons(): List<ImageVector> {
    return listOf(
        Icons.Default.LocalFireDepartment, Icons.Default.Whatshot, Icons.Default.Star, Icons.Default.WorkspacePremium,
        Icons.Default.EmojiEvents, Icons.Default.MilitaryTech, Icons.AutoMirrored.Filled.TrendingUp, Icons.Default.Leaderboard,
        Icons.Default.Verified, Icons.Default.EmojiFlags, Icons.Default.Campaign, Icons.Default.ThumbUp
    )
}

fun ddayDailyIcons(): List<ImageVector> {
    return listOf(
        Icons.Default.Event, Icons.Default.Cake, Icons.Default.Celebration, Icons.Default.Favorite,
        Icons.Default.Work, Icons.Default.Flight, Icons.Default.ShoppingCart, Icons.Default.CardGiftcard,
        Icons.Default.Build, Icons.Default.SportsEsports, Icons.Default.FitnessCenter, Icons.Default.Pets,
        Icons.Default.Restaurant, Icons.Default.LocalHospital, Icons.Default.Movie, Icons.Default.MusicNote,
        Icons.Default.Home, Icons.Default.Phone, Icons.Default.PhotoCamera, Icons.Default.Group,
        Icons.Default.Money, Icons.Default.Computer, Icons.Default.Watch, Icons.Default.Hotel,
        Icons.Default.DirectionsCar, Icons.Default.Train, Icons.Default.Fastfood, Icons.Default.LocalBar,
        Icons.Default.AirportShuttle, Icons.Default.BeachAccess, Icons.Default.BusinessCenter, Icons.Default.Casino,
        Icons.Default.ChildFriendly, Icons.Default.Spa, Icons.Default.Pool, Icons.Default.Kitchen,
        Icons.Default.Palette, Icons.Default.Public, Icons.Default.Weekend, Icons.Default.Headset, Icons.Default.Mic
    )
}

@Composable
fun MonthGrid(
    calendar: Calendar,
    selectedDate: Calendar,
    onDateClick: (Int) -> Unit,
    isCondensed: Boolean,
    ddayDates: Set<String>
) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val salaryDay = remember(year, month) { getSalaryDay(year, month) }
    val tempCal = calendar.clone() as Calendar
    tempCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val sdf = remember { SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN) }

    Column(modifier = Modifier.fillMaxWidth()) {
        var dateNum = 1
        for (i in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 7) {
                    val currentIdx = i * 7 + j
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(if (isCondensed) 2.2f else 1.0f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentIdx >= firstDayOfWeek && dateNum <= daysInMonth) {
                            val d = dateNum
                            val isSelected = selectedDate.get(Calendar.YEAR) == year &&
                                    selectedDate.get(Calendar.MONTH) == month &&
                                    selectedDate.get(Calendar.DAY_OF_MONTH) == d
                            val dateCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, d) }
                            val dateStr = sdf.format(dateCal.time)
                            CalendarDateItem(
                                date = d, isCondensed = isCondensed, isSelected = isSelected,
                                isSalaryDay = d == salaryDay, hasDday = ddayDates.contains(dateStr),
                                onClick = { onDateClick(d) }
                            )
                            dateNum++
                        }
                    }
                }
            }
            if (dateNum > daysInMonth) break
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DdayCalendarCard(
    pagerState: PagerState, selectedDate: Calendar, onDateClick: (Int, Calendar) -> Unit,
    isCondensed: Boolean, initialPage: Int, ddayDates: Set<String>
) {
    val coroutineScope = rememberCoroutineScope()
    val currentMonthCalendar by remember {
        derivedStateOf { Calendar.getInstance().apply { add(Calendar.MONTH, pagerState.currentPage - initialPage) } }
    }
    Card(
        modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val year = currentMonthCalendar.get(Calendar.YEAR)
            val month = currentMonthCalendar.get(Calendar.MONTH) + 1
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceIn(0, pagerState.pageCount - 1)) } },
                    modifier = Modifier.size(if (isCondensed) 24.dp else 32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month")
                }
                Text("${year}년 ${month}월", fontWeight = FontWeight.Bold, fontSize = if (isCondensed) 15.sp else 18.sp)
                IconButton(onClick = { coroutineScope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceIn(0, pagerState.pageCount - 1)) } },
                    modifier = Modifier.size(if (isCondensed) 24.dp else 32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month")
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = if (isCondensed) 4.dp else 12.dp)) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEachIndexed { index, day ->
                    Text(
                        text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontSize = if (isCondensed) 12.sp else 14.sp, fontWeight = FontWeight.Bold,
                        color = when (index) {
                            0 -> MaterialTheme.colorScheme.error
                            6 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (isCondensed) 4.dp else 12.dp))
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                val pageCalendar = remember(page) { Calendar.getInstance().apply { add(Calendar.MONTH, page - initialPage) } }
                MonthGrid(
                    calendar = pageCalendar, selectedDate = selectedDate,
                    onDateClick = { date -> onDateClick(date, pageCalendar) },
                    isCondensed = isCondensed,
                    ddayDates = ddayDates
                )
            }
        }
    }
}

@Composable
fun CalendarDateItem(
    date: Int, isCondensed: Boolean, isSelected: Boolean, isSalaryDay: Boolean, hasDday: Boolean, onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Surface(
                color = MaterialTheme.colorScheme.primary, shape = CircleShape,
                modifier = Modifier.size(if (isCondensed) 26.dp else 34.dp)
            ) {}
        }
        Text(
            text = "$date", fontSize = if (isCondensed) 13.sp else 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
        if (!isSelected) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                if (hasDday) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = if (isCondensed) 6.dp else 8.dp)
                            .width(if (isCondensed) 14.dp else 18.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                if (isSalaryDay) {
                    if (isCondensed) {
                        Box(modifier = Modifier.padding(bottom = 8.dp).size(4.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                    } else {
                        Text(
                            "월급",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventDialog(
    date: Calendar,
    events: List<DdayItem>,
    onDismiss: () -> Unit,
    onAddDday: (String) -> Unit
) {
    val dateFormatter = SimpleDateFormat("d", Locale.KOREAN)
    val dayOfWeekFormatter = SimpleDateFormat("E요일", Locale.KOREAN)
    var newEventText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier
                .padding(24.dp)
                .heightIn(min = 250.dp, max = 500.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        dateFormatter.format(date.time),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        dayOfWeekFormatter.format(date.time),
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "일정이 없습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(events) { event ->
                                EventListItem(item = event)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newEventText,
                        onValueChange = { newEventText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("추가하고 싶은 일정 입력", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (newEventText.isNotBlank()) { onAddDday(newEventText); newEventText = "" } },
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp),
                        contentPadding = PaddingValues(0.dp),
                        enabled = newEventText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Event",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventListItem(item: DdayItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp
            )
            Text(
                item.date,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
        }
        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
            Text(
                calculateDday(item.date),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DdayListItem(
    item: DdayItem,
    onClick: () -> Unit,
    onIconClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (item.isMain) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
        border = if (item.isMain) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) else null,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = onIconClick,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp), modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icon, contentDescription = "D-day icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(item.date, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp)) {
                Text(calculateDday(item.date), modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDdayDialog(
    item: DdayItem,
    onDismiss: () -> Unit,
    onSave: (DdayItem) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var dateString by remember { mutableStateOf(item.date) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dayFormatter = SimpleDateFormat("M월 d", Locale.KOREAN)
    val dialogTitle = remember(item.date) {
        try {
            val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
            val date = sdf.parse(item.date)
            "${dayFormatter.format(date)}일 일정"
        } catch (e: Exception) {
            "일정 수정"
        }
    }

    if (showDeleteConfirm) {
        SsabreeDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = "삭제 확인",
            message = "'${item.title}' 일정을 삭제하시겠습니까?",
            confirmText = "삭제",
            dismissText = "취소",
            onConfirm = { onDelete(); showDeleteConfirm = false },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showDatePicker) {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREAN)
        val cal = Calendar.getInstance().apply { time = sdf.parse(dateString) ?: Calendar.getInstance().time }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = cal.timeInMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selectedCal = Calendar.getInstance().apply { timeInMillis = it }
                        dateString = sdf.format(selectedCal.time)
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(dialogTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(24.dp))

                Text("제목", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("날짜", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Date", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateString, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("삭제")
                    }
                    Row {
                        TextButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) { Text("취소") }
                        TextButton(
                            onClick = {
                                val updatedItem = item.copy(
                                    title = title,
                                    date = dateString,
                                    dDay = calculateDday(dateString)
                                )
                                onSave(updatedItem)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { Text("저장") }
                    }
                }
            }
        }
    }
}

sealed class IconCategory(val title: String) {
    object All : IconCategory("전체")
    object Coding : IconCategory("코딩")
    object Study : IconCategory("공부")
    object Achievement : IconCategory("성취")
    object Daily : IconCategory("일상")
}

@Composable
fun IconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (ImageVector) -> Unit
) {
    val codingIcons = ddayCodingIcons()
    val studyIcons = ddayStudyIcons()
    val achievementIcons = ddayAchievementIcons()
    val dailyIcons = ddayDailyIcons()
    val allIcons = ddayAllIcons()

    val categories = listOf(IconCategory.All, IconCategory.Coding, IconCategory.Study, IconCategory.Achievement, IconCategory.Daily)
    var selectedTabIndex by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column {
                Text(
                    "아이콘 선택",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    categories.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(category.title, fontSize = 13.sp) },
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }

                val iconsToShow = when (categories[selectedTabIndex]) {
                    IconCategory.All -> allIcons
                    IconCategory.Coding -> codingIcons
                    IconCategory.Study -> studyIcons
                    IconCategory.Achievement -> achievementIcons
                    IconCategory.Daily -> dailyIcons
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 48.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(iconsToShow) { icon ->
                        IconButton(
                            onClick = { onIconSelected(icon); onDismiss() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DdayDetailScreenPreview() {
    CompositionLocalProvider(LocalAppContainer provides FakeAppContainer()) {
        DdayDetailScreen()
    }
}
