package me.tju244.kop.lake.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.tju244.kop.RebuildApplication

class LakeViewModelFactory(
    private val app: RebuildApplication,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LakeViewModel::class.java)) {
            return LakeViewModel(
                lakeTokenRepository = app.lakeTokenRepository,
                lakeRepository = app.lakeRepository,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

