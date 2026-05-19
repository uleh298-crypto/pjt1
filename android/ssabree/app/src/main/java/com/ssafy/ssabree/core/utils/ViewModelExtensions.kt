package com.ssafy.ssabree.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel



//
//@Composable
//inline fun <reified VM : ViewModel> simpleViewModel(
//    crossinline create: () -> VM
//): VM {
//    return viewModel<VM>(
//        factory = object : ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                return create() as T
//            }
//        }
//    )
//}
