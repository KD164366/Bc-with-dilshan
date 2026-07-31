package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Tutorial
import com.example.ui.MainViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.AddNoticeDialog
import com.example.ui.components.AddTutorialDialog
import com.example.ui.components.AdminUnlockDialog
import com.example.ui.components.PdfViewerDialog
import com.example.ui.screens.ContactDilshanScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.NoticeBoardScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.theme.BCWithDilshanTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryGold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BCWithDilshanTheme {
                BCWithDilshanApp()
            }
        }
    }
}

@Composable
fun BCWithDilshanApp(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activePdfPreview by viewModel.activePdfPreview.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val selectedGrade by viewModel.selectedGrade.collectAsStateWithLifecycle()

    var showAdminUnlockDialog by remember { mutableStateOf(false) }
    var showAddTuteDialog by remember { mutableStateOf(false) }
    var editingTutorial by remember { mutableStateOf<Tutorial?>(null) }
    var showAddNoticeDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                // Tutes Tab (නිබන්ධන)
                NavigationBarItem(
                    selected = currentTab is ScreenTab.Tutorials,
                    onClick = { viewModel.setScreenTab(ScreenTab.Tutorials) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Tutorials",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("නිබන්ධන (Tutes)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = Color.White,
                        indicatorColor = SecondaryGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag("nav_tab_tutorials")
                )

                // Downloads Tab (බාගන්න)
                NavigationBarItem(
                    selected = currentTab is ScreenTab.Downloads,
                    onClick = { viewModel.setScreenTab(ScreenTab.Downloads) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DownloadDone,
                            contentDescription = "Downloads",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("බාගත කිරීම්", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = Color.White,
                        indicatorColor = SecondaryGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag("nav_tab_downloads")
                )

                // Notice Board Tab (දැන්වීම්)
                NavigationBarItem(
                    selected = currentTab is ScreenTab.NoticeBoard,
                    onClick = { viewModel.setScreenTab(ScreenTab.NoticeBoard) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Notices",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("දැන්වීම්", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = Color.White,
                        indicatorColor = SecondaryGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag("nav_tab_notices")
                )

                // Contact Dilshan Sir Tab
                NavigationBarItem(
                    selected = currentTab is ScreenTab.ContactDilshan,
                    onClick = { viewModel.setScreenTab(ScreenTab.ContactDilshan) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Contact",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Dilshan Sir", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = Color.White,
                        indicatorColor = SecondaryGold,
                        unselectedIconColor = Color.White.copy(alpha = 0.7f),
                        unselectedTextColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.testTag("nav_tab_contact")
                )
            }
        }
    ) { innerPadding ->
        when (currentTab) {
            is ScreenTab.Tutorials -> TutorialsScreen(
                viewModel = viewModel,
                onOpenAdminUnlock = { showAdminUnlockDialog = true },
                onOpenAddTute = { showAddTuteDialog = true },
                onEditTute = { tute ->
                    editingTutorial = tute
                    showAddTuteDialog = true
                }
            )
            is ScreenTab.Downloads -> DownloadsScreen(viewModel = viewModel)
            is ScreenTab.NoticeBoard -> NoticeBoardScreen(
                viewModel = viewModel,
                onOpenAddNotice = { showAddNoticeDialog = true }
            )
            is ScreenTab.ContactDilshan -> ContactDilshanScreen(
                viewModel = viewModel,
                onOpenAdminUnlock = { showAdminUnlockDialog = true }
            )
        }

        // PDF Viewer Modal (Visit option)
        activePdfPreview?.let { tute ->
            PdfViewerDialog(
                tutorial = tute,
                onDismiss = { viewModel.closePdfPreview() },
                onDownload = { viewModel.downloadTutorial(it) }
            )
        }

        // Admin Unlock Dialog
        if (showAdminUnlockDialog) {
            AdminUnlockDialog(
                onDismiss = { showAdminUnlockDialog = false },
                onUnlock = { pin ->
                    if (viewModel.unlockAdminMode(pin)) {
                        showAdminUnlockDialog = false
                    }
                }
            )
        }

        // Add or Edit Tutorial Dialog (Dilshan Sir)
        if (showAddTuteDialog) {
            AddTutorialDialog(
                initialGrade = selectedGrade,
                existingTutorial = editingTutorial,
                onDismiss = {
                    showAddTuteDialog = false
                    editingTutorial = null
                },
                onSubmit = { title, description, grade, subject, pdfUrl ->
                    if (editingTutorial != null) {
                        viewModel.updateTutorialByAdmin(
                            editingTutorial!!.copy(
                                title = title,
                                description = description,
                                grade = grade,
                                subject = subject,
                                pdfUrl = pdfUrl
                            )
                        )
                    } else {
                        viewModel.addTutorialByAdmin(title, description, grade, subject, pdfUrl)
                    }
                    showAddTuteDialog = false
                    editingTutorial = null
                }
            )
        }

        // Add Notice Dialog (Dilshan Sir)
        if (showAddNoticeDialog) {
            AddNoticeDialog(
                onDismiss = { showAddNoticeDialog = false },
                onSubmit = { title, content, gradeCategory ->
                    viewModel.addNoticeByAdmin(title, content, gradeCategory)
                    showAddNoticeDialog = false
                }
            )
        }
    }
}
