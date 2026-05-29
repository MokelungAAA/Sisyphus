package com.mokelab.sisyphus.feature.subject

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun SubjectScreen(
    viewModel: SubjectViewModel = koinViewModel()
) {
    SubjectListScreen(viewModel = viewModel)
}
