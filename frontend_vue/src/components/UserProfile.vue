<template>
  <div class="profile-page">
    <!-- Header Banner -->
    <header class="page-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Profiel</span>
        </div>
        <div class="header-info">
          <div class="header-left">
            <div class="profile-badge">
              <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              <span>Mijn Account</span>
            </div>
            <h1 class="page-title">Mijn Profiel</h1>
            <p class="page-description">Beheer uw persoonlijke informatie en voorkeuren</p>
          </div>
        </div>
      </div>
    </header>

    <div class="profile-container">

      <div v-if="errorMessage" class="error-message">
        <svg class="error-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        <span>{{ errorMessage }}</span>
      </div>

      <div v-if="user" class="profile-info">
        <!-- Profile Display -->
        <div class="profile-section">
          <div class="profile-header">
            <div class="avatar-display">
              <div v-if="(isEditing ? editForm.profilePicture : user.profilePicture)" class="avatar-image">
                <img :src="isEditing ? editForm.profilePicture : user.profilePicture" alt="Profile Picture" />
              </div>
              <div v-else class="avatar-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
              <div v-if="isEditing" class="avatar-upload">
                <input
                  type="file"
                  ref="fileInput"
                  @change="handleFileSelect"
                  accept="image/*"
                  style="display: none"
                  id="profile-picture-input"
                />
                <label for="profile-picture-input" class="upload-button">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="15" />
                  </svg>
                  <span>Foto wijzigen</span>
                </label>
                <button
                  v-if="editForm.profilePicture"
                  @click="removeProfilePicture"
                  class="remove-button"
                  type="button"
                >
                  Verwijderen
                </button>
              </div>
            </div>
            <div class="profile-name">
              <h2>
                {{ user.firstName }}{{ user.lastName ? ' ' + user.lastName : '' }}
                <span v-if="calculateAge(user.birthDate)" class="profile-age">({{ calculateAge(user.birthDate) }} jaar)</span>
              </h2>
              <p class="profile-email">{{ user.email }}</p>
            </div>
          </div>
        </div>

        <!-- User Details -->
        <div class="details-section">
          <div class="detail-item">
            <label>Voornaam:</label>
            <span v-if="!isEditing">{{ user.firstName || 'Niet ingevuld' }}</span>
            <input
              v-else
              type="text"
              v-model="editForm.firstName"
              class="text-input"
              placeholder="Voornaam"
            />
          </div>
          <div class="detail-item">
            <label>Achternaam:</label>
            <span v-if="!isEditing">{{ user.lastName || 'Niet ingevuld' }}</span>
            <input
              v-else
              type="text"
              v-model="editForm.lastName"
              class="text-input"
              placeholder="Achternaam"
            />
          </div>
          <div class="detail-item">
            <label>Email:</label>
            <span>{{ user.email }}</span>
          </div>
          <div class="detail-item">
            <label>Geboortedatum:</label>
            <span v-if="!isEditing">{{ formatDate(user.birthDate) || 'Niet ingevuld' }}</span>
            <input
              v-else
              type="date"
              v-model="editForm.birthDate"
              :max="maxDate"
              class="date-input"
            />
          </div>
          <div class="detail-item">
            <label>Favoriete Partij:</label>
            <span v-if="!isEditing">{{ user.favoriteParty || 'Niet ingevuld' }}</span>
            <select
              v-else
              v-model="editForm.favoriteParty"
              class="select-input"
            >
              <option value="">Geen partij geselecteerd</option>
              <option v-for="party in parties" :key="party.id" :value="party.name">
                {{ party.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- Edit Button -->
        <div class="action-buttons">
          <button v-if="!isEditing" @click="startEditing" class="btn-edit">Bewerken</button>
          <div v-else class="edit-buttons">
            <button @click="saveProfile" class="btn-save" :disabled="saving">
              {{ saving ? 'Opslaan...' : 'Opslaan' }}
            </button>
            <button @click="cancelEditing" class="btn-cancel">Annuleren</button>
          </div>
        </div>

        <div v-if="successMessage" class="success-message">
          <svg class="success-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
          <span>{{ successMessage }}</span>
        </div>
      </div>

      <div v-else-if="!errorMessage" class="profile-loading">
        <div class="loading-spinner"></div>
        <p>Profiel laden...</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '../config/api.js'
import { ElectionService } from '../services/ElectionService.js'

const router = useRouter()
const user = ref(null)
const errorMessage = ref('')
const isEditing = ref(false)
const saving = ref(false)
const successMessage = ref('')
const parties = ref([])
const editForm = ref({
  firstName: '',
  lastName: '',
  birthDate: '',
  profilePicture: null
})
const fileInput = ref(null)

// Computed property voor maximale datum (vandaag)
const maxDate = computed(() => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

async function loadUser() {
  const userId = localStorage.getItem('userId')
  console.log('Loading user with ID:', userId)

  if (!userId) {
    console.log('No userId found, redirecting to login')
    router.push('/login')
    return
  }

  // Always fetch from API to get latest data from database
  try {
    const url = `${API_BASE_URL}/api/auth/user/${userId}`
    console.log('Fetching user from:', url)

    const res = await fetch(url)
    console.log('Response status:', res.status)

    if (res.ok) {
      const data = await res.json()
      console.log('User data received from database:', data)

      // Update localStorage with fresh data from database
      localStorage.setItem('userData', JSON.stringify({
        id: data.id,
        firstName: data.firstName || null,
        lastName: data.lastName || null,
        email: data.email || 'Geen email',
        birthDate: data.birthDate || null,
        favoriteParty: data.favoriteParty || null,
        profilePicture: data.profilePicture || null
      }))

      user.value = {
        id: data.id,
        firstName: data.firstName || null,
        lastName: data.lastName || null,
        email: data.email || 'Geen email',
        birthDate: data.birthDate || null,
        favoriteParty: data.favoriteParty || null,
        profilePicture: data.profilePicture || null
      }

      // Dispatch event to update navbar
      window.dispatchEvent(new Event('profileUpdated'))
    } else if (res.status === 404) {
      const errorText = await res.text()
      console.error('User not found in database:', res.status, errorText)
      errorMessage.value = `Gebruiker niet gevonden in database. Status: ${res.status}`

      // Fallback to localStorage if available
      const storedUserData = localStorage.getItem('userData')
      if (storedUserData) {
        try {
          const data = JSON.parse(storedUserData)
          console.log('Using fallback data from localStorage:', data)
          user.value = {
            id: data.id || userId,
            firstName: data.firstName || null,
            lastName: data.lastName || null,
            email: data.email || 'Geen email',
            birthDate: data.birthDate || null,
            profilePicture: data.profilePicture || null
          }
        } catch (e) {
          console.error('Error parsing stored user data:', e)
        }
      }
    } else if (res.status === 401) {
      console.log('Unauthorized, redirecting to login')
      router.push('/login')
    } else {
      const errorText = await res.text()
      console.error('Error response:', res.status, errorText)
      errorMessage.value = `Kon profiel niet laden: ${res.status}. ${errorText}`

      // Fallback to localStorage if available
      const storedUserData = localStorage.getItem('userData')
      if (storedUserData) {
        try {
          const data = JSON.parse(storedUserData)
          console.log('Using fallback data from localStorage:', data)
          user.value = {
            id: data.id || userId,
            firstName: data.firstName || null,
            lastName: data.lastName || null,
            email: data.email || 'Geen email',
            birthDate: data.birthDate || null,
            profilePicture: data.profilePicture || null
          }
        } catch (e) {
          console.error('Error parsing stored user data:', e)
        }
      }
    }
  } catch (e) {
    console.error('Exception loading user:', e)
    errorMessage.value = `Netwerkfout bij het laden van profiel: ${e.message}`

    // Fallback to localStorage if available
    const storedUserData = localStorage.getItem('userData')
    if (storedUserData) {
      try {
        const data = JSON.parse(storedUserData)
        console.log('Using fallback data from localStorage after error:', data)
        user.value = {
          id: data.id || userId,
          firstName: data.firstName || null,
          lastName: data.lastName || null,
          email: data.email || 'Geen email',
          birthDate: data.birthDate || null
        }
      } catch (parseError) {
        console.error('Error parsing stored user data:', parseError)
      }
    }
  }
}

function formatDate(dateString) {
  if (!dateString) return null
  const date = new Date(dateString)
  return date.toLocaleDateString('nl-NL', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function calculateAge(birthDate) {
  if (!birthDate) return null
  const today = new Date()
  const birth = new Date(birthDate)
  let age = today.getFullYear() - birth.getFullYear()
  const monthDiff = today.getMonth() - birth.getMonth()
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age--
  }
  return age
}

function startEditing() {
  isEditing.value = true
  // Initialize form with current user data
  editForm.value.firstName = user.value.firstName || ''
  editForm.value.lastName = user.value.lastName || ''

  if (user.value.birthDate) {
    const date = new Date(user.value.birthDate)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    editForm.value.birthDate = `${year}-${month}-${day}`
  } else {
    editForm.value.birthDate = ''
  }
  editForm.value.favoriteParty = user.value.favoriteParty || ''
  editForm.value.profilePicture = user.value.profilePicture || null
  successMessage.value = ''
}

function cancelEditing() {
  isEditing.value = false
  editForm.value.firstName = ''
  editForm.value.lastName = ''
  editForm.value.birthDate = ''
  editForm.value.favoriteParty = ''
  editForm.value.profilePicture = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  successMessage.value = ''
}

function handleFileSelect(event) {
  const file = event.target.files[0]
  if (!file) return

  // Validate file type
  if (!file.type.startsWith('image/')) {
    errorMessage.value = 'Alleen afbeeldingen zijn toegestaan'
    return
  }

  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = 'Afbeelding mag maximaal 5MB zijn'
    return
  }

  // Convert to base64
  const reader = new FileReader()
  reader.onload = (e) => {
    editForm.value.profilePicture = e.target.result
    errorMessage.value = ''
    // Dispatch event to update navbar immediately
    window.dispatchEvent(new Event('profileUpdated'))
  }
  reader.onerror = () => {
    errorMessage.value = 'Fout bij het lezen van de afbeelding'
  }
  reader.readAsDataURL(file)
}

function removeProfilePicture() {
  editForm.value.profilePicture = null
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  // Dispatch event to update navbar immediately
  window.dispatchEvent(new Event('profileUpdated'))
}

async function saveProfile() {
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const userId = localStorage.getItem('userId')
    const token = localStorage.getItem('token')

    const res = await fetch(`${API_BASE_URL}/api/auth/user/${userId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        firstName: editForm.value.firstName || null,
        lastName: editForm.value.lastName || null,
        birthDate: editForm.value.birthDate || null,
        favoriteParty: editForm.value.favoriteParty || null,
        profilePicture: editForm.value.profilePicture || null
      })
    })

    if (res.ok) {
      const data = await res.json()
      // Update user data with response
      user.value.firstName = data.firstName
      user.value.lastName = data.lastName
      user.value.birthDate = data.birthDate
      user.value.favoriteParty = data.favoriteParty
      user.value.profilePicture = data.profilePicture
      localStorage.setItem('userData', JSON.stringify({
        id: data.id,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        birthDate: data.birthDate,
        favoriteParty: data.favoriteParty,
        profilePicture: data.profilePicture
      }))
      // Dispatch event to update navbar
      window.dispatchEvent(new Event('profileUpdated'))
      isEditing.value = false
      successMessage.value = 'Profiel succesvol bijgewerkt!'
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } else {
      const errorText = await res.text()
      errorMessage.value = `Kon profiel niet bijwerken: ${errorText}`
    }
  } catch (e) {
    console.error('Error updating profile:', e)
    errorMessage.value = 'Netwerkfout. Probeer later opnieuw.'
  } finally {
    saving.value = false
  }
}

async function loadParties() {
  try {
    // Load parties from recent election
    const election = await ElectionService.getElection('TK2023')
    console.log('Election data received:', election)
    console.log('Parties in election:', election?.parties)

    if (election && election.parties && Array.isArray(election.parties)) {
      // Sort parties by name for better UX
      parties.value = election.parties
        .filter(p => p && p.id && p.name) // Filter out invalid entries
        .map(p => ({ id: p.id, name: p.name }))
        .sort((a, b) => a.name.localeCompare(b.name))
      console.log('Loaded parties:', parties.value.length, parties.value)
    } else {
      console.warn('No parties found in election data. Election structure:', election)
      parties.value = []
    }
  } catch (e) {
    console.error('Error loading parties:', e)
    // If loading fails, continue without parties list
    parties.value = []
  }
}

onMounted(() => {
  loadUser()
  loadParties()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: 5px;
  font-family: 'Nunito', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* Header Banner */
.page-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/images/banner.png') center/cover;
  opacity: 0.05;
  z-index: 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  margin-top: 0;
  padding-top: 0;
  font-size: 14px;
}

.breadcrumb-item {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: white;
}

.breadcrumb-item.active {
  color: white;
  font-weight: 600;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.5);
}

.header-info {
  color: white;
}

.header-left {
  flex: 1;
}

.profile-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  padding: 8px 16px;
  border-radius: 20px;
  margin-bottom: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.badge-icon {
  width: 20px;
  height: 20px;
  color: white;
}

.page-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 12px 0;
  color: white;
  letter-spacing: -0.5px;
}

.page-description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  margin-bottom: 60px;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: -30px;
  position: relative;
  z-index: 1;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
  font-size: 14px;
  background: #fee2e2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.error-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.profile-section {
  padding-bottom: 24px;
  border-bottom: 2px solid #e2e8f0;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-display {
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  border: 3px solid #e2e8f0;
}

.avatar-placeholder svg {
  width: 60px;
  height: 60px;
}

.avatar-image {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #e2e8f0;
  background: #f1f5f9;
}

.avatar-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.upload-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: #1e293b;
  color: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
  width: 100%;
  max-width: 200px;
}

.upload-button:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.upload-button svg {
  width: 16px;
  height: 16px;
}

.remove-button {
  padding: 8px 16px;
  background: #64748b;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
  width: 100%;
  max-width: 200px;
}

.remove-button:hover {
  background: #475569;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.3);
}

.profile-name {
  flex: 1;
}

.profile-name h2 {
  font-family: 'Nunito', sans-serif;
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.profile-age {
  font-size: 1rem;
  font-weight: 400;
  color: #64748b;
  margin-left: 8px;
}

.profile-email {
  font-family: 'Nunito', sans-serif;
  font-size: 1rem;
  color: #64748b;
  margin: 0;
}

.details-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-item {
  display: flex;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
}

.detail-item label {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  min-width: 120px;
}

.detail-item span {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  color: #1e293b;
}

.date-input,
.text-input,
.select-input{
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  padding: 8px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: white;
  color: #1e293b;
  outline: none;
  transition: border-color 0.2s;
  width: 100%;
  max-width: 300px;
}

.date-input:focus,
.text-input:focus,
.select-input:focus{
  border-color: #3b82f6;
}

.action-buttons {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.btn-edit {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  font-weight: 600;
  padding: 12px 24px;
  background: #1e293b;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit:hover {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.edit-buttons {
  display: flex;
  gap: 12px;
}

.btn-save {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  font-weight: 600;
  padding: 12px 24px;
  background: #1e293b;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover:not(:disabled) {
  background: #334155;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.3);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  font-weight: 600;
  padding: 12px 24px;
  background: #64748b;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #475569;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(100, 116, 139, 0.3);
}

.success-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-top: 24px;
  font-size: 14px;
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}

.success-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.info-note {
  padding: 16px;
  background: #eff6ff;
  border-radius: 8px;
  border-left: 4px solid #3b82f6;
  margin-top: 16px;
}

.info-note p {
  font-family: 'Nunito', sans-serif;
  font-size: 14px;
  color: #1e40af;
  margin: 0;
}

.profile-loading {
  text-align: center;
  padding: 40px;
  color: #64748b;
  font-family: 'Nunito', sans-serif;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.profile-loading p {
  margin: 0;
  font-size: 16px;
  animation: none !important;
  transform: none !important;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  animation: profile-spin 1s linear infinite;
}

@keyframes profile-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .page-header {
    padding: 32px 20px 48px;
  }

  .page-title {
    font-size: 32px;
  }

  .page-description {
    font-size: 16px;
  }

  .profile-container {
    padding: 24px;
    margin-top: -20px;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
  }

  .detail-item {
    flex-direction: column;
    gap: 8px;
  }

  .detail-item label {
    min-width: auto;
  }
}
</style>
