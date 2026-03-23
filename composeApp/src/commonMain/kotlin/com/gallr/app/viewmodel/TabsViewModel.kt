package com.gallr.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gallr.shared.data.model.AppLanguage
import com.gallr.shared.data.model.Exhibition
import com.gallr.shared.data.model.ExhibitionMapPin
import com.gallr.shared.data.model.FilterState
import com.gallr.shared.data.model.MapDisplayMode
import com.gallr.shared.data.model.toMapPin
import com.gallr.shared.repository.BookmarkRepository
import com.gallr.shared.repository.ExhibitionRepository
import com.gallr.shared.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ExhibitionListState {
    data object Loading : ExhibitionListState()
    data class Success(val exhibitions: List<Exhibition>) : ExhibitionListState()
    data class Error(val message: String) : ExhibitionListState()
}

class TabsViewModel(
    private val exhibitionRepository: ExhibitionRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val languageRepository: LanguageRepository,
) : ViewModel() {

    // ── Language ──────────────────────────────────────────────────────────────

    val language: StateFlow<AppLanguage> =
        languageRepository.observeLanguage()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppLanguage.KO)

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { languageRepository.setLanguage(lang) }
    }

    fun toggleLanguage() {
        val current = language.value
        setLanguage(if (current == AppLanguage.KO) AppLanguage.EN else AppLanguage.KO)
    }

    // ── Raw data ────────────────────────────────────────────────────────────

    private val _featuredState =
        MutableStateFlow<ExhibitionListState>(ExhibitionListState.Loading)
    val featuredState: StateFlow<ExhibitionListState> = _featuredState

    private val _allExhibitions =
        MutableStateFlow<ExhibitionListState>(ExhibitionListState.Loading)

    // ── Filter state ────────────────────────────────────────────────────────

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    fun updateFilter(update: FilterState.() -> FilterState) {
        _filterState.value = _filterState.value.update()
    }

    // ── Filtered exhibitions ─────────────────────────────────────────────────

    val filteredExhibitions: StateFlow<ExhibitionListState> =
        combine(_allExhibitions, _filterState) { state, filter ->
            when (state) {
                is ExhibitionListState.Loading -> ExhibitionListState.Loading
                is ExhibitionListState.Error -> state
                is ExhibitionListState.Success ->
                    ExhibitionListState.Success(state.exhibitions.filter { filter.matches(it) })
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExhibitionListState.Loading,
        )

    // ── Bookmarks ────────────────────────────────────────────────────────────

    val bookmarkedIds: StateFlow<Set<String>> =
        bookmarkRepository.observeBookmarkedIds()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleBookmark(exhibitionId: String) {
        viewModelScope.launch {
            if (bookmarkRepository.isBookmarked(exhibitionId)) {
                bookmarkRepository.removeBookmark(exhibitionId)
            } else {
                bookmarkRepository.addBookmark(exhibitionId)
            }
        }
    }

    // ── Map display mode + pins ──────────────────────────────────────────────

    private val _mapDisplayMode = MutableStateFlow(MapDisplayMode.MY_LIST)
    val mapDisplayMode: StateFlow<MapDisplayMode> = _mapDisplayMode

    fun setMapDisplayMode(mode: MapDisplayMode) {
        _mapDisplayMode.value = mode
    }

    val myListMapPins: StateFlow<List<ExhibitionMapPin>> =
        combine(_allExhibitions, bookmarkedIds, language) { state, bookmarked, lang ->
            (state as? ExhibitionListState.Success)
                ?.exhibitions
                ?.filter { it.id in bookmarked }
                ?.mapNotNull { it.toMapPin(lang) }
                ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allMapPins: StateFlow<List<ExhibitionMapPin>> =
        combine(_allExhibitions, language) { state, lang ->
            (state as? ExhibitionListState.Success)
                ?.exhibitions
                ?.mapNotNull { it.toMapPin(lang) }
                ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Data loading ────────────────────────────────────────────────────────

    fun loadFeaturedExhibitions() {
        viewModelScope.launch {
            _featuredState.value = ExhibitionListState.Loading
            exhibitionRepository.getFeaturedExhibitions()
                .onSuccess { _featuredState.value = ExhibitionListState.Success(it) }
                .onFailure {
                    val msg = it.message ?: "Unknown error"
                    println("ERROR [TabsViewModel] loadFeaturedExhibitions: $msg")
                    _featuredState.value = ExhibitionListState.Error(msg)
                }
        }
    }

    fun loadAllExhibitions() {
        viewModelScope.launch {
            _allExhibitions.value = ExhibitionListState.Loading
            exhibitionRepository.getExhibitions(FilterState())
                .onSuccess { _allExhibitions.value = ExhibitionListState.Success(it) }
                .onFailure {
                    val msg = it.message ?: "Unknown error"
                    println("ERROR [TabsViewModel] loadAllExhibitions: $msg")
                    _allExhibitions.value = ExhibitionListState.Error(msg)
                }
        }
    }

    init {
        loadFeaturedExhibitions()
        loadAllExhibitions()
    }

    // ── Factory ─────────────────────────────────────────────────────────────

    companion object {
        fun factory(
            exhibitionRepository: ExhibitionRepository,
            bookmarkRepository: BookmarkRepository,
            languageRepository: LanguageRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TabsViewModel(exhibitionRepository, bookmarkRepository, languageRepository)
            }
        }
    }
}
