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
            <span>Over Ons — StemStatistieken</span>
          </div>
          <h1 class="page-title">Wie wij zijn bij StemStatistieken</h1>
          <p class="page-description">
            StemStatistieken maakt verkiezingsdata helder en toegankelijk met moderne, betrouwbare en gebruiksvriendelijke tools.
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
          <h2>Wat we doen</h2>
          <ul class="list">
            <li>Interactieve kaarten en grafieken voor verkiezingsuitslagen</li>
            <li>Vergelijk functionaliteit tussen partijen en jaren</li>
            <li>Datakoppelingen met een moderne backend</li>
          </ul>
        </article>
        <article class="card">
          <h2>Onze waarden</h2>
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
        <ul class="contact-list">
          <li><strong>Adres:</strong> Wibauthuis, Wibautstraat 3B, 1091 GH Amsterdam</li>
          <li><strong>Telefoon:</strong> (020) 123 45 67</li>
          <li><strong>E-mail:</strong> contact@stemstatistieken.nl</li>
          <li><strong>Openingstijden:</strong> Maandag t/m vrijdag, 09:00 - 17:00</li>
        </ul>
        <p class="muted">
          Tip: gebruik de navigatie om andere onderdelen zoals Uitslagen, Kandidaten en Vergelijken te verkennen.
        </p>
      </section>
      <section class="card map-card">
        <h2>Hier vind je ons</h2>
        <p class="muted">Ons team werkt vanuit het Wibauthuis van de Hogeschool van Amsterdam.</p>
        <div class="map-wrapper">
          <iframe
            class="map-iframe"
            src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d546.7886733042398!2d4.909632646712929!3d52.35916815236507!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x47c6099a4b62ff9f%3A0xaaf1a4822a7d03c9!2sHogeschool%20van%20Amsterdam%20-%20HvA%20Wibauthuis%20(WBH)!5e0!3m2!1snl!2snl!4v1762874430364!5m2!1snl!2snl"
            allowfullscreen=""
            loading="lazy"
            referrerpolicy="no-referrer-when-downgrade"
          ></iframe>
        </div>
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

.contact {
  margin-top: 22px;
}

.contact-list {
  list-style: none;
  margin: 16px 0 0 0;
  padding: 0;
  display: grid;
  gap: 10px;
  color: #334155;
}

.contact-list strong {
  color: #1e293b;
}

.map-card {
  margin-top: 28px;
}

.map-wrapper {
  position: relative;
  padding-top: 56.25%;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.15);
  margin-top: 16px;
}

.map-iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border: 0;
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
<script setup lang="ts">
</script>
