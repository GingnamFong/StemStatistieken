<template>
  <section class="about-page">
    <header class="about-header">
      <div class="header-content">
        <div class="breadcrumb">
          <router-link to="/" class="breadcrumb-item">Home</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="breadcrumb-item active" aria-current="page">Over ons</span>
        </div>
        <div class="header-info">
          <div class="election-badge">
            <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 8a4 4 0 100 8 4 4 0 000-8zm0-6a1 1 0 011 1v2a1 1 0 11-2 0V3a1 1 0 011-1zm0 18a1 1 0 011 1v2a1 1 0 11-2 0v-2a1 1 0 011-1zm-9-7a1 1 0 011-1h2a1 1 0 110 2H4a1 1 0 01-1-1zm16 0a1 1 0 011-1h2a1 1 0 110 2h-2a1 1 0 01-1-1z" />
            </svg>
            <span>Over Ons</span>
          </div>
          <h1 class="page-title">Wie wij zijn</h1>
          <p class="page-description">
            We maken verkiezingsdata helder en toegankelijk met moderne, betrouwbare en gebruiksvriendelijke tools.
          </p>
        </div>
      </div>
    </header>

    <main class="dashboard-container">
      <section class="card mission">
        <h2>Onze missie</h2>
        <p>
          Onze missie is om burgers, studenten en onderzoekers te helpen bij het begrijpen van uitslagen,
          trends en verschillen tussen partijen en regio’s. Dat doen we met heldere visualisaties, betrouwbare data
          en een toegankelijke gebruikerservaring.
        </p>
      </section>

      <section class="grid">
        <article class="card">
          <h3>Wat we doen</h3>
          <ul class="list">
            <li>Interactieve kaarten en grafieken voor verkiezingsuitslagen</li>
            <li>Vergelijk functionaliteit tussen partijen en jaren</li>
            <li>Datakoppelingen met een moderne backend</li>
          </ul>
        </article>
        <article class="card">
          <h3>Onze waarden</h3>
          <ul class="list">
            <li>Betrouwbaarheid en transparantie</li>
            <li>Toegankelijkheid en performance</li>
            <li>Gebruiksvriendelijkheid en duidelijkheid</li>
          </ul>
        </article>
      </section>

      <section class="card contact">
        <h2>Contact</h2>
        <p>
          Heb je feedback of wil je meedenken? Laat het ons weten.
        </p>
        <p class="muted">
          Tip: gebruik de navigatie om andere onderdelen zoals Uitslagen, Kandidaten en Vergelijken te verkennen.
        </p>
      </section>
    </main>
  </section>
</template>

<style scoped>
.about-page {
  padding-top: 0;
}

.about-header {
  background: #1e293b;
  padding: 40px 32px 60px;
  position: relative;
  overflow: hidden;
  margin: 0;
  margin-top: -1px;
}

.about-header::before {
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

.content {
  padding-top: 28px;
  padding-bottom: 48px;
}

.dashboard-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  margin-top: 20px;
}

.card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

.card h2, .card h3 {
  margin-top: 0;
  color: #1e293b;
}

.card p {
  margin: 8px 0 0 0;
  color: #334155;
}

.list {
  padding-left: 18px;
  margin: 8px 0 0 0;
  color: #334155;
}

.muted {
  color: #64748b;
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
<script setup lang="ts">
</script>
