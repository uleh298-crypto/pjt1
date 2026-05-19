package com.ssafy.ssabree.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Creates a [ViewModel] without a [androidx.lifecycle.SavedStateHandle].
 *
 * This helper is suitable for ViewModels that only require injected
 * dependencies (e.g. repositories, use cases) and do not depend on
 * navigation arguments or state restoration.
 *
 * @param key Optional key to scope multiple ViewModels of the same type
 *            within the same composition.
 * @param create Factory lambda used to instantiate the ViewModel.
 *
 */
@Composable
inline fun <reified VM : ViewModel> simpleViewModel(
    key: String? = null,
    crossinline create: () -> VM
): VM {
    val factory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == VM::class.java) {
                    "Expected ${VM::class.java}, but got $modelClass"
                }
                @Suppress("UNCHECKED_CAST")
                return create() as T
            }
        }
    }
    return viewModel(key = key, factory = factory)
}
