package com.ssafy.ssabree.features.mygroup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.ssafy.ssabree.app.LocalAppContainer
import com.ssafy.ssabree.core.repository.MyGroupRepository
import com.ssafy.ssabree.core.repository.PortfolioRepository
import com.ssafy.ssabree.core.repository.ProjectRepository
import com.ssafy.ssabree.core.repository.model.MyPagePortfolioSummaryModel
import com.ssafy.ssabree.core.repository.model.PortfolioModel
import com.ssafy.ssabree.core.repository.model.ProjectModel
import com.ssafy.ssabree.core.ui.simpleViewModel
import com.ssafy.ssabree.core.utils.RetrofitClient
import com.ssafy.ssabree.features.group.model.GroupKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.ImageVector

private data class ApplicantPortfolioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val applicationTitle: String? = null,
    val applicationMessage: String? = null,
    val portfolio: PortfolioModel? = null,
    val projects: List<ProjectModel> = emptyList()
)

private class ApplicantPortfolioViewModel(
    private val myGroupRepository: MyGroupRepository,
    private val portfolioRepository: PortfolioRepository,
    private val projectRepository: ProjectRepository,
    private val portfolioId: Long,
    private val applicationId: Long,
    private val groupKind: GroupKind?
) : ViewModel() {
    private val _uiState = MutableStateFlow(ApplicantPortfolioUiState())
    val uiState: StateFlow<ApplicantPortfolioUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        if (applicationId > 0L && groupKind != null) {
            myGroupRepository.getApplicationDetail(groupKind, applicationId)
                .onSuccess { application ->
                    _uiState.update {
                        it.copy(
                            applicationTitle = application.title,
                            applicationMessage = application.message
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
        portfolioRepository.getPortfolio(portfolioId)
            .onSuccess { portfolio ->
                _uiState.update { it.copy(isLoading = false, portfolio = portfolio) }
                loadProjects(portfolioId)
            }
            .onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
    }

    private fun loadProjects(portfolioId: Long) = viewModelScope.launch {
        projectRepository.getProjectsByPortfolio(portfolioId)
            .onSuccess { projects ->
                _uiState.update { it.copy(projects = projects) }
            }
            .onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicantPortfolioScreen(
    portfolioId: Long,
    applicationId: Long = -1L,
    groupKind: GroupKind? = null,
    onBackClick: () -> Unit = {},
    onDetailClick: (Long) -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: ApplicantPortfolioViewModel = simpleViewModel(key = "applicant-portfolio-$portfolioId-$applicationId") {
        ApplicantPortfolioViewModel(
            container.myGroupRepository,
            container.portfolioRepository,
            container.projectRepository,
            portfolioId,
            applicationId,
            groupKind
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val summary = remember(uiState.portfolio, uiState.projects) {
        uiState.portfolio?.toSummary(uiState.projects)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = uiState.portfolio?.memberName
                    Text(
                        text = if (name.isNullOrBlank()) "포트폴리오" else "${name}님의 포트폴리오",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (!uiState.isLoading && uiState.portfolio == null) {
                Text("등록된 포트폴리오가 없습니다.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!uiState.applicationTitle.isNullOrBlank() || !uiState.applicationMessage.isNullOrBlank()) {
                ApplicationSummaryCard(
                    title = uiState.applicationTitle,
                    message = uiState.applicationMessage
                )
            }

            if (summary != null) {
                ApplicantPortfolioSummaryCard(
                    summary = summary,
                    applicantName = uiState.portfolio?.memberName ?: "지원자",
                    onDetailClick = { onDetailClick(portfolioId) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ApplicantPortfolioDetailScreen(
    portfolioId: Long,
    onBackClick: () -> Unit = {}
) {
    val container = LocalAppContainer.current
    val viewModel: ApplicantPortfolioViewModel = simpleViewModel(key = "applicant-portfolio-detail-$portfolioId") {
        ApplicantPortfolioViewModel(
            container.myGroupRepository,
            container.portfolioRepository,
            container.projectRepository,
            portfolioId,
            -1L,
            null
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val portfolio = uiState.portfolio

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = portfolio?.memberName
                    Text(
                        text = if (name.isNullOrBlank()) "포트폴리오" else "${name}님의 포트폴리오",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (uiState.isLoading) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            if (!uiState.isLoading && portfolio == null) {
                Text("등록된 포트폴리오가 없습니다.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            portfolio?.let { data ->
                PortfolioInfoCard {
                    PortfolioSection(title = "제목", icon = Icons.Default.Badge) {
                        Text(text = if (data.title.isBlank()) "-" else data.title, fontSize = 15.sp)
                    }
                }

                PortfolioInfoCard {
                    PortfolioSection(title = "한 줄 소개", icon = Icons.Default.Subject) {
                        Text(text = if (data.description.isBlank()) "-" else data.description, fontSize = 14.sp)
                    }
                }

                PortfolioInfoCard {
                    PortfolioSection(title = "자기소개", icon = Icons.Default.Person) {
                        Text(text = if (data.introduction.isBlank()) "-" else data.introduction, fontSize = 14.sp)
                    }
                }

                PortfolioInfoCard {
                    PortfolioSection(title = "기술 스택", icon = Icons.Default.Code) {
                        if (data.stacks.isEmpty()) {
                            Text("-", fontSize = 14.sp)
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                data.stacks.forEach { stack ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (!stack.stackImgUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = stack.stackImgUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    imageLoader = rememberSvgImageLoader()
                                                )
                                            }
                                            Text(
                                                text = "${stack.stackName} (${toExpertLevelLabel(stack.expertLevel)})",
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                PortfolioInfoCard {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PortfolioSection(title = "SW 역량", icon = Icons.Default.Psychology) {
                            Text(text = data.swTestRank?.ifBlank { "-" } ?: "-", fontSize = 14.sp)
                        }
                        PortfolioSection(title = "Solved.ac 티어", icon = Icons.Default.EmojiEvents) {
                            val tierName = data.solvedAcInfo?.tierName?.ifBlank { data.solvedacRank ?: "-" }
                                ?: (data.solvedacRank ?: "-")
                            val solved = data.solvedAcInfo?.solvedCount?.toString() ?: "-"
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("티어", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(tierName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("푼 문제", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(solved, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                PortfolioInfoCard {
                    PortfolioSection(title = "관련 링크", icon = Icons.Default.Link) {
                        if (data.urls.isEmpty()) {
                            Text("-", fontSize = 14.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                data.urls.forEach { url ->
                                    Text(url.url, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                PortfolioSection(title = "프로젝트 경험", icon = Icons.Default.WorkOutline) {
                    if (uiState.projects.isEmpty()) {
                        Text("-", fontSize = 14.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.projects.forEach { project ->
                                ApplicantProjectExperienceCard(
                                    project = project,
                                    onImageClick = {},
                                    onOpenUrl = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ApplicantPortfolioSummaryCard(
    summary: MyPagePortfolioSummaryModel,
    applicantName: String,
    onDetailClick: () -> Unit
) {
    val techStacks = summary.techStack.entries.toList()
    val swRating = summary.ssafySwRating ?: "-"
    val solvedAcRank = summary.solvedAcRank ?: "-"
    val solvedAcTierName = summary.solvedAcTierName?.ifBlank { solvedAcRank } ?: solvedAcRank
    val solvedAcSolvedCount = summary.solvedAcSolvedCount?.toString() ?: "-"
    val links = summary.links
    val projects = summary.projects

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("포트폴리오 요약", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(applicantName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.clickable { onDetailClick() }
                ) {
                    Text(
                        "상세보기",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SummaryRow(icon = Icons.Default.Code, label = "기술 스택") {
                if (techStacks.isEmpty()) {
                    Text("등록된 기술이 없습니다.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        techStacks.forEach { entry ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${entry.key} (${entry.value})",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(icon = Icons.Default.Psychology, label = "SW 역량", value = swRating)
            SummaryRow(icon = Icons.Default.EmojiEvents, label = "Solved.ac") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("티어: $solvedAcTierName", fontSize = 12.sp)
                    Text("푼 문제: $solvedAcSolvedCount", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(icon = Icons.Default.Link, label = "관련 링크") {
                if (links.isEmpty()) {
                    Text("-", fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        links.forEach { url ->
                            Text(url, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(icon = Icons.Default.WorkOutline, label = "프로젝트 경험") {
                if (projects.isEmpty()) {
                    Text("-", fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        projects.forEachIndexed { index, title ->
                            Text("${index + 1}. $title", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ApplicationSummaryCard(
    title: String?,
    message: String?
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "지원서 제목",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = title?.ifBlank { "-" } ?: "-",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "지원서 내용",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message?.ifBlank { "-" } ?: "-",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: ImageVector,
    label: String,
    value: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            if (value != null) {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            content?.invoke()
        }
    }
}

@Composable
private fun PortfolioSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().padding(start = 28.dp)) {
            content()
        }
    }
}

@Composable
private fun PortfolioInfoCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

@Composable
private fun ApplicantProjectExperienceCard(
    project: ProjectModel,
    onOpenUrl: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.WorkOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "제목",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = project.title.ifBlank { "-" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            ProjectField(
                icon = Icons.Default.Subject,
                label = "한 줄 소개",
                value = project.introduction?.ifBlank { "-" } ?: "-"
            )
            ProjectField(
                icon = Icons.Default.Description,
                label = "상세 내용",
                value = project.description?.ifBlank { "-" } ?: "-"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "사용된 기술 스택",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (project.techStacks.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        project.techStacks.forEach { stack ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(stack) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("링크", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                if (project.urls.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        project.urls.forEach { url ->
                            val target = normalizeUrl(url)
                            Text(
                                text = if (target.isBlank()) "-" else target,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    if (target.isNotBlank()) onOpenUrl(target)
                                }
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("이미지", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                if (project.imageUrls.isEmpty()) {
                    Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                } else {
                    val filtered = project.imageUrls.filter { it.isNotBlank() }.map { normalizeImageUrl(it) }
                    if (filtered.isEmpty()) {
                        Text("-", fontSize = 14.sp, fontWeight = FontWeight.Normal)
                    } else {
                        ImageCarousel(
                            imageUrls = filtered,
                            onImageClick = onImageClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectField(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Normal)
        }
    }
}

@Composable
private fun ImageCarousel(
    imageUrls: List<String>,
    onImageClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val currentIndex by remember {
        derivedStateOf {
            val visible = listState.layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) {
                0
            } else {
                val first = visible.first()
                val next = visible.getOrNull(1)
                val offset = listState.firstVisibleItemScrollOffset
                if (next != null && offset > first.size / 2) {
                    next.index
                } else {
                    first.index
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(imageUrls.size) { index ->
                val imageUrl = imageUrls[index]
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "이미지",
                    modifier = Modifier
                        .width(220.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onImageClick(imageUrl) }
                )
            }
        }
        CarouselIndicator(count = imageUrls.size, currentIndex = currentIndex)
    }
}

@Composable
private fun CarouselIndicator(
    count: Int,
    currentIndex: Int
) {
    if (count <= 1) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { index ->
            val isSelected = index == currentIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

private fun normalizeUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }
    return "https://$trimmed"
}

@Composable
private fun rememberSvgImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
}

private fun toExpertLevelLabel(raw: String?): String {
    return when (raw?.trim()?.lowercase()) {
        "high" -> "상"
        "mid" -> "중"
        "low" -> "하"
        "" -> "-"
        null -> "-"
        else -> raw
    }
}

private fun normalizeImageUrl(rawUrl: String): String {
    val trimmed = rawUrl.trim()
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed
    }
    val normalized = trimmed.replace("\\", "/")
    val uploadsIndex = normalized.indexOf("/uploads/")
    if (uploadsIndex >= 0) {
        val relative = normalized.substring(uploadsIndex)
        return RetrofitClient.SERVER_URL.trimEnd('/') + relative
    }
    return RetrofitClient.SERVER_URL.trimEnd('/') + "/uploads/" + normalized.trimStart('/')
}

private fun PortfolioModel.toSummary(projects: List<ProjectModel>): MyPagePortfolioSummaryModel {
    return MyPagePortfolioSummaryModel(
        techStack = stacks.associate { it.stackName to (it.expertLevel ?: "-") },
        ssafySwRating = swTestRank,
        solvedAcRank = solvedacRank,
        solvedAcHandle = bojHandle,
        solvedAcTierName = solvedAcInfo?.tierName,
        solvedAcTierImageUrl = solvedAcInfo?.tierImageUrl,
        solvedAcSolvedCount = solvedAcInfo?.solvedCount,
        links = urls.map { it.url },
        projects = projects.map { it.title }
    )
}
