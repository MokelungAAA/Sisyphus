package com.mokelab.sisyphus.feature.subject

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubjectScreen(
    viewModel: SubjectViewModel = koinViewModel(),
    onSubjectClick: (Long) -> Unit = {}
) {
    SubjectListScreen(
        viewModel = viewModel,
        onSubjectClick = onSubjectClick
    )
}
