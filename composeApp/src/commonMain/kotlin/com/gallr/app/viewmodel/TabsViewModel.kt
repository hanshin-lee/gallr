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
import kotlinx.coroutines.flow.map
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

    // ── City filter ──────────────────────────────────────────────────────────

    private val _selectedCity = MutableStateFlow<String?>(null) // null = all cities, otherwise cityKo
    val selectedCity: StateFlow<String?> = _selectedCity

    fun setCity(cityKo: String?) {
        _selectedCity.value = cityKo
    }

    val distinctCities: StateFlow<List<Pair<String, String>>> =
        _allExhibitions.map { state ->
            (state as? ExhibitionListState.Success)
                ?.exhibitions
                ?.map { it.cityKo to it.cityEn }
                ?.distinct()
                ?.sortedBy { it.first }
                ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── My List filter ────────────────────────────────────────────────────────

    private val _showMyListOnly = MutableStateFlow(false)
    val showMyListOnly: StateFlow<Boolean> = _showMyListOnly

    fun setShowMyListOnly(enabled: Boolean) {
        _showMyListOnly.value = enabled
    }

    fun clearAllFilters() {
        _filterState.value = FilterState()
        _selectedCity.value = null
    }

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

    fun clearAllBookmarks() {
        viewModelScope.launch { bookmarkRepository.clearAll() }
    }

    // ── Filtered exhibitions ─────────────────────────────────────────────────

    val filteredExhibitions: StateFlow<ExhibitionListState> =
        combine(
            _allExhibitions, _filterState, _selectedCity, _showMyListOnly, bookmarkedIds,
        ) { values ->
            val state = values[0] as ExhibitionListState
            val filter = values[1] as FilterState
            val city = values[2] as String?
            @Suppress("UNCHECKED_CAST")
            val myListOnly = values[3] as Boolean
            @Suppress("UNCHECKED_CAST")
            val bookmarked = values[4] as Set<String>
            when (state) {
                is ExhibitionListState.Loading -> ExhibitionListState.Loading
                is ExhibitionListState.Error -> state
                is ExhibitionListState.Success -> {
                    val filtered = state.exhibitions
                        .filter { city == null || it.cityKo == city }
                        .filter { filter.matches(it) }
                        .filter { !myListOnly || it.id in bookmarked }
                    ExhibitionListState.Success(filtered)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExhibitionListState.Loading,
        )

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

    // ── Exhibition lookup ───────────────────────────────────────────────────

    fun findExhibitionById(id: String): Exhibition? =
        (_allExhibitions.value as? ExhibitionListState.Success)
            ?.exhibitions
            ?.firstOrNull { it.id == id }

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
            exhibitionRepository.getExhibitions()
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
