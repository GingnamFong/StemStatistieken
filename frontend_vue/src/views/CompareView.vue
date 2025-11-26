<template>
  <div class="dashboard">
    <!-- Header -->
    <header class="dashboard-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active">Vergelijken</span>
        </div>
        <div class="header-info">
          <div class="header-top-row">
            <div class="header-left">
              <div class="election-badge">
                <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
                <span>Vergelijk Verkiezingen</span>
              </div>
              <h1 class="page-title">Vergelijk Uitslagen</h1>
              <p class="page-description">Vergelijk provincies, gemeentes of kieskringen tussen verschillende jaren</p>
            </div>
            <div class="header-right">
              <button class="btn-reset" @click="resetAll">
                <svg class="btn-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4v6h6M20 20v-6h-6M20 8a8 8 0 00-13.657-5.657L4 4m16 16l-2.343 2.343A8 8 0 018 20" />
                </svg>
                Reset selectie
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>

    <CompareContainer ref="compareContainerRef" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CompareContainer from '@/components/CompareContainer.vue'

const compareContainerRef = ref(null)

function resetAll() {
  if (compareContainerRef.value) {
    compareContainerRef.value.resetAll()
  }
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', sans-serif;
}

/* Header - Same as ElectionDashboardView */
.dashboard-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.dashboard-header::before {
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

.header-top-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 32px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  padding-bottom: 4px;
}

.election-badge {
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

/* Reset Button */
.btn-reset {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px 12px 16px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
  /* MATCH Kandidaten search input */
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-reset:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.2);
}

.btn-reset:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.2), 0 2px 8px rgba(0, 0, 0, 0.15);
}

.btn-reset .btn-icon {
  width: 20px;
  height: 20px;
  color: rgba(255, 255, 255, 0.9);
}

/* Responsive */

@media (max-width: 768px) {
  .dashboard-header {
    padding: 32px 20px 48px;
  }

  .page-title {
    font-size: 32px;
  }

  .page-description {
    font-size: 16px;
  }

  .header-top-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .header-right {
    width: 100%;
    padding-bottom: 0;
  }

  .btn-reset {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

}
</style>
