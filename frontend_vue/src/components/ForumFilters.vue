<template>
  <div class="filter-sort-container">
    <!-- Search & Filter Toggle -->
    <div class="filter-section">
      <div class="search-filter-wrapper">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>

          <input
            :value="search"
            @input="$emit('update:search', $event.target.value)"
            type="text"
            placeholder="Zoeken op titel of inhoud..."
            class="search-filter-input"
          />
        </div>

        <button @click="toggleFilters" class="filter-toggle-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
          </svg>
          Filters
          <span v-if="activeFilterCount > 0" class="filter-badge">
            {{ activeFilterCount }}
          </span>
        </button>
      </div>

      <!-- Advanced Filters -->
      <div v-if="showFilters" class="filters-panel">
        <div class="filter-group">
          <label class="filter-label">Auteur</label>
          <input
            :value="author"
            @input="$emit('update:author', $event.target.value)"
            type="text"
            class="filter-input"
            placeholder="Zoek op auteur..."
          />
        </div>

        <div class="filter-group">
          <label class="filter-label">Datum</label>
          <select
            :value="date"
            @change="$emit('update:date', $event.target.value)"
            class="filter-select"
          >
            <option value="">Alle datums</option>
            <option value="today">Vandaag</option>
            <option value="week">Deze week</option>
            <option value="month">Deze maand</option>
            <option value="year">Dit jaar</option>
          </select>
        </div>

        <div class="filter-group">
          <label class="filter-label">Minimale score</label>
          <input
            :value="minScore"
            @input="$emit('update:minScore', Number($event.target.value))"
            type="number"
            min="0"
            class="filter-input"
          />
        </div>

        <div class="filter-group">
          <label class="filter-label">Minimaal aantal reacties</label>
          <input
            :value="minComments"
            @input="$emit('update:minComments', Number($event.target.value))"
            type="number"
            min="0"
            class="filter-input"
          />
        </div>

        <div class="filter-actions">
          <button @click="$emit('clear')" class="clear-filters-btn">
            Filters wissen
          </button>
        </div>
      </div>
    </div>

    <!-- Sort Bar -->
    <div class="sort-bar">
      <button
        v-for="option in sortOptions"
        :key="option.value"
        @click="$emit('update:sort', option.value)"
        :class="['sort-btn', { active: sort === option.value }]"
      >
        <svg class="sort-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path :d="option.icon" />
        </svg>
        {{ option.label }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  search: String,
  author: String,
  date: String,
  minScore: Number,
  minComments: Number,
  sort: String,
  activeFilterCount: {
    type: Number,
    default: 0
  }
})

defineEmits([
  'update:search',
  'update:author',
  'update:date',
  'update:minScore',
  'update:minComments',
  'update:sort',
  'clear'
])

const showFilters = ref(false)

const toggleFilters = () => {
  showFilters.value = !showFilters.value
}

const sortOptions = [
  { value: 'hot', label: 'Populair', icon: 'M13 10V3L4 14h7v7l9-11h-7z' },
  { value: 'new', label: 'Nieuw', icon: 'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z' },
  { value: 'top', label: 'Top', icon: 'M5 10l7-7m0 0l7 7m-7-7v18' }
]
</script>

<style scoped>
.filter-sort-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.search-filter-wrapper {
  display: flex;
  gap: 8px;
  padding: 12px;
  align-items: center;
}

.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-input-wrapper .search-icon {
  position: absolute;
  left: 12px;
  width: 18px;
  height: 18px;
  color: #94a3b8;
  pointer-events: none;
}

.search-filter-input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  transition: all 0.2s;
}

.search-filter-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-toggle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
  position: relative;
}

.filter-toggle-btn:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
  color: #3b82f6;
}

.filter-toggle-btn svg {
  width: 18px;
  height: 18px;
}

.filter-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ef4444;
  color: white;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  text-align: center;
}

.filters-panel {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-label {
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  background: white;
  transition: all 0.2s;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-select {
  cursor: pointer;
}

.filter-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #e2e8f0;
}

.clear-filters-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}

.clear-filters-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #1e293b;
}

.sort-bar {
  display: flex;
  gap: 8px;
  background: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}

.sort-btn:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.sort-btn.active {
  background: #3b82f6;
  color: white;
}

.sort-icon {
  width: 16px;
  height: 16px;
}

@media (max-width: 768px) {
  .filter-sort-container {
    gap: 8px;
  }

  .search-filter-wrapper {
    flex-direction: column;
  }

  .filter-toggle-btn {
    width: 100%;
    justify-content: center;
  }

  .filters-panel {
    grid-template-columns: 1fr;
  }

  .sort-bar {
    flex-wrap: wrap;
  }

  .sort-btn {
    flex: 1;
    min-width: 80px;
    justify-content: center;
  }
}

</style>
