<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const navigateToResults = () => {
  router.push('/dashboard')
}

const navigateToCompare = () => {
  router.push('/vergelijken')
}
</script>

<template>
  <div class="home-container">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-icon">🗳️</span>
          <span class="badge-text">Verkiezingen Nederland</span>
        </div>
        
        <h1 class="hero-title">
          Ontdek de <span class="highlight">verkiezingsuitslagen</span> van Nederland
        </h1>
        
        <p class="hero-description">
          Interactieve visualisaties en analyses van de Nederlandse verkiezingen. 
          Bekijk uitslagen per provincie, gemeente en kieskring, en vergelijk resultaten door de jaren heen.
        </p>
        
        <div class="hero-actions">
          <button @click="navigateToResults" class="btn btn-primary">
            <span>Bekijk Uitslagen</span>
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
          
          <button @click="navigateToCompare" class="btn btn-secondary">
            <span>Vergelijk Resultaten</span>
          </button>
        </div>

        <div class="stats-row">
          <div class="stat-item">
            <div class="stat-number">3</div>
            <div class="stat-label">Jaren: 2021, 2023, 2025</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">342</div>
            <div class="stat-label">Gemeenten</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">12</div>
            <div class="stat-label">Provincies</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">20</div>
            <div class="stat-label">Kieskringen</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">Functies</h2>
        <p class="section-subtitle">Alles wat je nodig hebt om verkiezingsuitslagen te analyseren</p>
      </div>

      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon" style="background: #3b82f6;">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
            </svg>
          </div>
          <h3 class="feature-title">Interactieve Kaarten</h3>
          <p class="feature-description">
            Verken verkiezingsuitslagen op een visuele manier met interactieve kaarten van Nederland.
          </p>
        </div>

        <div class="feature-card">
          <div class="feature-icon" style="background: #64748b;">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M8 7h12M8 12h12m-12 5h12M3 7h.01M3 12h.01M3 17h.01" />
            </svg>
          </div>
          <h3 class="feature-title">Vergelijk Jaren</h3>
          <p class="feature-description">
            Vergelijk verkiezingsresultaten tussen verschillende jaren om trends te ontdekken.
          </p>
        </div>

        <div class="feature-card">
          <div class="feature-icon" style="background: #475569;">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <h3 class="feature-title">Geavanceerd Zoeken</h3>
          <p class="feature-description">
            Zoek snel specifieke partijen, kandidaten of regio's met onze krachtige zoekfunctie.
          </p>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="cta-content">
        <h2 class="cta-title">Begin met analyseren</h2>
        <p class="cta-description">
          Duik in de data en ontdek interessante inzichten over de Nederlandse verkiezingen
        </p>
        <button @click="navigateToResults" class="btn btn-primary btn-large">
          <span>Start Nu</span>
          <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd" />
          </svg>
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  font-family: 'Nunito', sans-serif;
  background: #ffffff;
}

/* Hero Section */
.hero-section {
  background: #1e293b;
  padding: 100px 24px 80px;
  text-align: center;
  position: relative;
  overflow: hidden;
  margin-top: -1px;
}

.hero-section::before {
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

.hero-content {
  max-width: 900px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 8px 20px;
  border-radius: 50px;
  margin-bottom: 32px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.badge-icon {
  font-size: 20px;
}

.badge-text {
  color: white;
  font-weight: 600;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  color: white;
  margin-bottom: 24px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.highlight {
  color: #3b82f6;
}

.hero-description {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 48px;
  line-height: 1.6;
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 64px;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Nunito', sans-serif;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover {
  background: #2563eb;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: white;
  transform: translateY(-1px);
}

.btn-large {
  padding: 18px 40px;
  font-size: 18px;
}

.btn-icon {
  width: 20px;
  height: 20px;
}

/* Stats Row */
.stats-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 40px;
  font-weight: 800;
  color: white;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

/* Features Section */
.features-section {
  padding: 100px 24px;
  background: #f8fafc;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-title {
  font-size: 42px;
  font-weight: 800;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: -0.5px;
}

.section-subtitle {
  font-size: 18px;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.features-grid {
  max-width: 1000px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

@media (max-width: 992px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}

.feature-card {
  background: white;
  padding: 40px 32px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #cbd5e1;
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: white;
  background: #64748b;
}

.feature-icon svg {
  width: 32px;
  height: 32px;
}

.feature-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.feature-description {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}

/* CTA Section */
.cta-section {
  background: #f1f5f9;
  padding: 100px 24px;
  text-align: center;
  border-top: 1px solid #e2e8f0;
}

.cta-content {
  max-width: 700px;
  margin: 0 auto;
}

.cta-title {
  font-size: 48px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 20px;
  letter-spacing: -0.5px;
}

.cta-description {
  font-size: 20px;
  color: #64748b;
  margin-bottom: 40px;
  line-height: 1.6;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 18px;
  }

  .section-title {
    font-size: 32px;
  }

  .cta-title {
    font-size: 36px;
  }

  .stats-row {
    gap: 32px;
  }

  .stat-divider {
    display: none;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .hero-section {
    padding: 60px 16px;
  }

  .hero-title {
    font-size: 28px;
  }

  .stat-number {
    font-size: 32px;
  }

  .features-section {
    padding: 60px 16px;
  }

  .cta-section {
    padding: 60px 16px;
  }
}
</style>
