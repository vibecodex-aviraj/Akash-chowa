package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SyllabusReferenceScreen
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun AkashChowaApp(
    viewModel: ChatViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    // BackHandler: Return to CHAT tab if currently on other tabs
    BackHandler(enabled = selectedTab != AppTab.CHAT) {
        viewModel.onSelectTab(AppTab.CHAT)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_bottom_nav_bar")
            ) {
                // Tab 1: Chat Tutor (Gemini Chat Bot)
                NavigationBarItem(
                    selected = selectedTab == AppTab.CHAT,
                    onClick = { viewModel.onSelectTab(AppTab.CHAT) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Gemini শিক্ষক"
                        )
                    },
                    label = { Text("Gemini শিক্ষক") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_chat")
                )

                // Tab 2: Syllabus
                NavigationBarItem(
                    selected = selectedTab == AppTab.SYLLABUS,
                    onClick = { viewModel.onSelectTab(AppTab.SYLLABUS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.SYLLABUS) Icons.Filled.Book else Icons.Outlined.Book,
                            contentDescription = "পাঠ্যসূচি"
                        )
                    },
                    label = { Text("পাঠ্যসূচি") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_syllabus")
                )

                // Tab 3: Chapter Quizzes & Mock Test
                NavigationBarItem(
                    selected = selectedTab == AppTab.QUIZ,
                    onClick = { viewModel.onSelectTab(AppTab.QUIZ) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.QUIZ) Icons.Filled.Quiz else Icons.Outlined.Quiz,
                            contentDescription = "কুইজ টেস্ট"
                        )
                    },
                    label = { Text("কুইজ টেস্ট") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_quiz")
                )

                // Tab 4: Projects & Homework
                NavigationBarItem(
                    selected = selectedTab == AppTab.PROJECTS,
                    onClick = { viewModel.onSelectTab(AppTab.PROJECTS) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == AppTab.PROJECTS) Icons.Filled.Create else Icons.Outlined.Create,
                            contentDescription = "প্রজেক্ট"
                        )
                    },
                    label = { Text("প্রজেক্ট গাইড") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_projects")
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            AppTab.CHAT -> {
                ChatScreen(
                    viewModel = viewModel,
                    onOpenSyllabus = { viewModel.onSelectTab(AppTab.SYLLABUS) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            AppTab.SYLLABUS -> {
                SyllabusReferenceScreen(
                    onAskTopic = { prompt ->
                        viewModel.onSelectTab(AppTab.CHAT)
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            AppTab.QUIZ -> {
                QuizScreen(
                    onAskAiQuestion = { prompt ->
                        viewModel.onSelectTab(AppTab.CHAT)
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
            AppTab.PROJECTS -> {
                ProjectsScreen(
                    onDraftProject = { prompt ->
                        viewModel.onSelectTab(AppTab.CHAT)
                        viewModel.sendMessage(prompt)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                )
            }
        }
    }
}
