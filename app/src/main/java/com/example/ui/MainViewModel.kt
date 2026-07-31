package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Notice
import com.example.data.Tutorial
import com.example.data.TutorialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ScreenTab {
    object Tutorials : ScreenTab()
    object Downloads : ScreenTab()
    object NoticeBoard : ScreenTab()
    object ContactDilshan : ScreenTab()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TutorialRepository

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow<ScreenTab>(ScreenTab.Tutorials)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Active Grade Filter: "Grade 12" or "Grade 13"
    private val _selectedGrade = MutableStateFlow("Grade 12")
    val selectedGrade: StateFlow<String> = _selectedGrade.asStateFlow()

    // Subject Filter Tag ("All", "Biology", "Chemistry", "Physics", "Combined Maths", "ICT")
    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Admin Mode State (Dilshan Sir)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _adminPin = MutableStateFlow("1234") // Default Admin secret code
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    // PDF Preview Modal (Visit option)
    private val _activePdfPreview = MutableStateFlow<Tutorial?>(null)
    val activePdfPreview: StateFlow<Tutorial?> = _activePdfPreview.asStateFlow()

    // Toast / User Status Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Database Flows
    val allNotices: StateFlow<List<Notice>>
    val downloadedTutorials: StateFlow<List<Tutorial>>
    val favoriteTutorials: StateFlow<List<Tutorial>>

    // Filtered Tutorials according to Grade, Subject, and Search Query
    val filteredTutorials: StateFlow<List<Tutorial>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TutorialRepository(database.tutorialDao(), database.noticeDao(), application)

        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }

        allNotices = repository.allNotices.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        downloadedTutorials = repository.downloadedTutorials.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        favoriteTutorials = repository.favoriteTutorials.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

        filteredTutorials = combine(
            repository.allTutorials,
            _selectedGrade,
            _selectedSubject,
            _searchQuery
        ) { tutorials, grade, subject, query ->
            tutorials.filter { tute ->
                val matchesGrade = tute.grade.equals(grade, ignoreCase = true)
                val matchesSubject = if (subject == "All") true else tute.subject.contains(subject, ignoreCase = true)
                val matchesQuery = query.isBlank() || 
                        tute.title.contains(query, ignoreCase = true) ||
                        tute.description.contains(query, ignoreCase = true) ||
                        tute.subject.contains(query, ignoreCase = true)
                matchesGrade && matchesSubject && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setScreenTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSelectedGrade(grade: String) {
        _selectedGrade.value = grade
    }

    fun setSelectedSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openPdfPreview(tutorial: Tutorial) {
        _activePdfPreview.value = tutorial
        // Increment view count
        viewModelScope.launch {
            repository.updateTutorial(tutorial.copy(viewsCount = tutorial.viewsCount + 1))
        }
    }

    fun closePdfPreview() {
        _activePdfPreview.value = null
    }

    fun toggleFavorite(tutorial: Tutorial) {
        viewModelScope.launch {
            repository.toggleFavorite(tutorial)
            showToast(if (!tutorial.isFavorite) "Favorites එකට එකතු කරන ලදී!" else "Favorites වලින් ඉවත් කරන ලදී!")
        }
    }

    fun downloadTutorial(tutorial: Tutorial) {
        viewModelScope.launch {
            showToast("'${tutorial.title}' PDF එක බාගත වීම ආරම්භ විය...")
            repository.downloadPdf(
                tutorial = tutorial,
                onProgress = { progress ->
                    // Progress handled internally in repo update
                },
                onComplete = { success, localPath ->
                    if (success) {
                        showToast("PDF එක සාර්ථකව බාගත විය! (Downloaded)")
                    } else {
                        showToast("PDF බාගත කිරීම අසාර්ථක විය. නැවත උත්සාහ කරන්න.")
                    }
                }
            )
        }
    }

    fun openLocalPdf(filePath: String) {
        repository.openDownloadedPdf(getApplication(), filePath)
    }

    // Admin Authentication
    fun unlockAdminMode(pin: String): Boolean {
        if (pin == _adminPin.value) {
            _isAdminMode.value = true
            showToast("Dilshan Sir ඇඩ්මින් මෝඩ් එක සක්‍රීය විය! (Admin Mode Unlocked)")
            return true
        } else {
            showToast("වැරදි PIN කේතයකි! නැවත උත්සාහ කරන්න.")
            return false
        }
    }

    fun lockAdminMode() {
        _isAdminMode.value = false
        showToast("ඇඩ්මින් මෝඩ් එකෙන් ඉවත් විය.")
    }

    fun updateAdminPin(newPin: String) {
        if (newPin.length >= 4) {
            _adminPin.value = newPin
            showToast("Admin PIN කේතය වෙනස් කරන ලදී.")
        } else {
            showToast("PIN කේතය අවම වශයෙන් ඉලක්කම් 4ක් විය යුතුය.")
        }
    }

    // Admin Tutorial Operations
    fun addTutorialByAdmin(
        title: String,
        description: String,
        grade: String,
        subject: String,
        pdfUrl: String
    ) {
        viewModelScope.launch {
            val newTutorial = Tutorial(
                title = title,
                description = description,
                grade = grade,
                subject = subject,
                pdfUrl = if (pdfUrl.isNotBlank()) pdfUrl else "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                author = "Dilshan Sir",
                dateAdded = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            )
            repository.insertTutorial(newTutorial)
            showToast("නව PDF නිබන්ධනය සාර්ථකව ඇඩ් කරන ලදී!")
        }
    }

    fun updateTutorialByAdmin(tutorial: Tutorial) {
        viewModelScope.launch {
            repository.updateTutorial(tutorial)
            showToast("නිබන්ධනය සංශෝධනය කරන ලදී!")
        }
    }

    fun deleteTutorialByAdmin(tutorial: Tutorial) {
        viewModelScope.launch {
            repository.deleteTutorial(tutorial)
            showToast("නිබන්ධනය ඉවත් කරන ලදී.")
        }
    }

    fun addNoticeByAdmin(title: String, content: String, gradeCategory: String) {
        viewModelScope.launch {
            val notice = Notice(
                title = title,
                content = content,
                gradeCategory = gradeCategory,
                datePosted = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            )
            repository.insertNotice(notice)
            showToast("දැන්වීම පල කරන ලදී!")
        }
    }

    fun deleteNoticeByAdmin(notice: Notice) {
        viewModelScope.launch {
            repository.deleteNotice(notice)
            showToast("දැන්වීම ඉවත් කරන ලදී.")
        }
    }

    fun showToast(msg: String) {
        _userMessage.value = msg
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
