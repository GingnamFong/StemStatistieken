<template>
  <div class="selection-card">
    <div class="card-number">{{ columnNumber }}</div>
    <div class="selection-content">
      <button
        v-if="showRemove"
        @click="$emit('remove')"
        class="remove-btn"
        title="Verwijder kolom"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <div class="form-group">
        <label class="form-label">Type</label>
        <select
          :value="modelValue.type"
          @change="handleTypeChange"
          class="form-select"
          :disabled="disabled"
        >
          <option value="">Kies type...</option>
          <option value="provincie">Provincie</option>
          <option value="gemeente">Gemeente</option>
          <option value="kieskring">Kieskring</option>
          <option value="stembureau">Stembureau (postcode)</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">Jaar</label>
        <select
          :value="modelValue.year"
          @change="handleYearChange"
          class="form-select"
          :disabled="!modelValue.type"
        >
          <option value="">Kies jaar...</option>
          <option value="2021">2021</option>
          <option value="2023">2023</option>
          <option value="2025">2025</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">
          {{ modelValue.type === 'provincie' ? 'Provincie' : modelValue.type === 'gemeente' ? 'Gemeente' : modelValue.type === 'kieskring' ? 'Kieskring'  : modelValue.type === 'stembureau'
          ? 'Postcode': 'Selectie' }}

        </label>

        <!-- STEMBUREAU: gemeente + stembureau -->
        <template v-if="modelValue.type === 'stembureau'">
          <!-- 1. Gemeente kiezen -->
          <select
            class="form-select"
            :value="modelValue.municipality"
            @change="handleMunicipalityChange"
            :disabled="!modelValue.year || !availableSelections.length"
          >
            <option value="">Kies gemeente...</option>
            <option
              v-for="gem in availableSelections"
              :key="gem.id"
              :value="gem.id"
            >
              {{ gem.name }}
            </option>
          </select>

          <!-- 2. Stembureau binnen gekozen gemeente (met postcode in label) -->
          <select
            class="form-select"
            :value="modelValue.selection"
            @change="handleSelectionChange"
            :disabled="!modelValue.municipality || !stembureausInSelectedMunicipality.length"
          >
            <option value="">Kies stembureau...</option>
            <option
              v-for="sb in stembureausInSelectedMunicipality"
              :key="sb.id"
              :value="sb.id"
            >
              <!-- Label: "9461 BH – Stembureau Gemeentehuis Gieten" -->
              {{ sb.postalCode ? sb.postalCode + ' – ' : '' }}{{ sb.name }}
            </option>
          </select>
        </template>

        <!-- Alle andere types -->
        <select
          v-else
          :value="modelValue.selection"
          @change="handleSelectionChange"
          class="form-select"
          :disabled="!modelValue.year || !availableSelections.length"
        >
          <option value="">Kies {{ modelValue.type === 'provincie' ? 'provincie' : modelValue.type === 'gemeente' ? 'gemeente' : 'kieskring' }}...</option>
          <option
            v-for="item in availableSelections"
            :key="item.id || item.naam"
            :value="item.id || item.naam"
          >
            {{ item.name || item.naam }}
          </option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from "vue";

const props = defineProps({
  columnNumber: {
    type: Number,
    required: true
  },
  modelValue: {
    type: Object,
    required: true
  },
  availableSelections: {
    type: Array,
    default: () => []
  },
  selectedType: {
    type: String,
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showRemove: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'type-change', 'year-change', 'selection-change', 'remove'])

function handleTypeChange(e) {
  emit('update:modelValue', { ...props.modelValue, type: e.target.value })
  emit('type-change', e.target.value)
}

function handleYearChange(e) {
  emit('update:modelValue', { ...props.modelValue, year: e.target.value })
  emit('year-change', e.target.value)
}

function handleSelectionChange(e) {
  emit('update:modelValue', { ...props.modelValue, selection: e.target.value })
  emit('selection-change', e.target.value)
}
// 👇 Nieuw: gemeente-change handler
function handleMunicipalityChange(e) {
  const municipality = e.target.value
  // reset stembureau als gemeente verandert
  emit('update:modelValue', {
    ...props.modelValue,
    municipality,
    selection: ''
  })
}

// 👇 Alle stembureaus voor de gekozen gemeente
const stembureausInSelectedMunicipality = computed(() => {
  if (!props.modelValue.municipality) return []
  const gemeente = props.availableSelections.find(
    gem => gem.id === props.modelValue.municipality
  )
  // pollingStations komt uit handleYearChange in de parent
  return gemeente?.pollingStations || []
})
function handlePostalInput(e) {
  const clean = e.target.value.replace(/\s+/g, '').toUpperCase()
  emit('update:modelValue', { ...props.modelValue, selection: clean })
  emit('selection-change', clean)
}
</script>

<style scoped>
.selection-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  position: relative;
  transition: all 0.3s ease;
}

.selection-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.card-number {
  position: absolute;
  top: -12px;
  left: 20px;
  background: #3b82f6;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.selection-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 10;
}

.remove-btn:hover {
  background: #dc2626;
  transform: scale(1.1);
}

.remove-btn svg {
  width: 16px;
  height: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-select {
  padding: 10px 12px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 15px;
  font-family: 'Nunito', sans-serif;
  color: #1f2937;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.form-select:hover:not(:disabled) {
  border-color: #3b82f6;
}

.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-select:disabled {
  background: #f9fafb;
  color: #9ca3af;
  cursor: not-allowed;
}
</style>

