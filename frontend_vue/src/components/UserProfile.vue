<template>
  <div class="profile-page">
    <div class="profile-container">
      <h1>Mijn Profiel</h1>

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
              <div class="avatar-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </div>
            </div>
            <div class="profile-name">
              <h2>{{ user.firstName }} {{ user.lastName || '' }}</h2>
              <p class="profile-email">{{ user.email }}</p>
            </div>
          </div>
        </div>

        <!-- User Details -->
        <div class="details-section">
          <div class="detail-item">
            <label>Voornaam:</label>
            <span>{{ user.firstName || 'Niet ingevuld' }}</span>
          </div>
          <div class="detail-item">
            <label>Achternaam:</label>
            <span>{{ user.lastName || 'Niet ingevuld' }}</span>
          </div>
          <div class="detail-item">
            <label>Email:</label>
            <span>{{ user.email }}</span>
          </div>
        </div>

        <div class="info-note">
          <p>Profiel bewerken functionaliteit komt binnenkort beschikbaar.</p>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '../config/api.js'

const router = useRouter()
const user = ref(null)
const errorMessage = ref('')

async function loadUser() {
  const userId = localStorage.getItem('userId')
  console.log('Loading user with ID:', userId)
  
  if (!userId) {
    console.log('No userId found, redirecting to login')
    router.push('/login')
    return
  }

  // Try to get user data from localStorage first (from login response)
  const storedUserData = localStorage.getItem('userData')
  if (storedUserData) {
    try {
      const data = JSON.parse(storedUserData)
      console.log('Using stored user data:', data)
      user.value = {
        id: data.id || userId,
        firstName: data.firstName || 'Onbekend',
        lastName: data.lastName || '',
        email: data.email || 'Geen email'
      }
      return
    } catch (e) {
      console.error('Error parsing stored user data:', e)
    }
  }

  // Try to fetch from API
  try {
    const url = `${API_BASE_URL}/api/auth/user/${userId}`
    console.log('Fetching user from:', url)
    
    const res = await fetch(url)
    console.log('Response status:', res.status)
    
    if (res.ok) {
      const data = await res.json()
      console.log('User data received:', data)
      // Store in localStorage for next time
      localStorage.setItem('userData', JSON.stringify(data))
      user.value = {
        id: data.id,
        firstName: data.firstName || 'Onbekend',
        lastName: data.lastName || '',
        email: data.email || 'Geen email'
      }
    } else if (res.status === 404) {
      // Endpoint doesn't exist yet, use basic info
      console.log('Endpoint not found, using basic info')
      user.value = {
        id: userId,
        firstName: 'Gebruiker',
        lastName: '',
        email: localStorage.getItem('userEmail') || 'Email niet beschikbaar'
      }
    } else if (res.status === 401) {
      console.log('Unauthorized, redirecting to login')
      router.push('/login')
    } else {
      const errorText = await res.text()
      console.error('Error response:', res.status, errorText)
      errorMessage.value = `Kon profiel niet laden: ${res.status}. Probeer de pagina te verversen.`
    }
  } catch (e) {
    console.error('Exception loading user:', e)
    // Use basic info if fetch fails
    user.value = {
      id: userId,
      firstName: 'Gebruiker',
      lastName: '',
      email: localStorage.getItem('userEmail') || 'Email niet beschikbaar'
    }
  }
}

onMounted(() => {
  loadUser()
})
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 64px);
  padding: 40px 20px;
  background: #f8fafc;
  margin-top: 64px;
}

.profile-container {
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h1 {
  font-family: 'Nunito', sans-serif;
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 32px;
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
  .profile-container {
    padding: 24px;
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
