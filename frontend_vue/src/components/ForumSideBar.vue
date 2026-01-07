<template>
  <aside class="forum-sidebar">
    <!-- About -->
    <div class="sidebar-card">
      <h3>Over r/Forum</h3>
      <p>
        Stel hier je vragen, deel je standpunten en gedachten of bespreek de
        verkiezingen en andere politieke onderwerpen.
      </p>

      <div class="sidebar-stats">
        <div class="stat">
          <span class="stat-value">{{ postsCount }}</span>
          <span class="stat-label">Posts</span>
        </div>

        <div class="stat">
          <span class="stat-value">{{ commentsCount }}</span>
          <span class="stat-label">Reacties</span>
        </div>
      </div>
    </div>

    <!-- Rules -->
    <div class="sidebar-card">
      <h3>Regels</h3>
      <ul class="rules-list">
        <li>Wees respectvol tegen elkaar</li>
        <li>Geen spam of zelfpromotie</li>
        <li>Blijf relevant bij het onderwerp</li>
        <li>Geen persoonlijke aanvallen</li>
        <li>Blijf neutraal</li>
      </ul>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  postsCount: {
    type: Number,
    required: true
  },
  commentsCount: {
    type: Number,
    required: true
  }
})
</script>

<style scoped>
.forum-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.sidebar-card h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.sidebar-card p {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.sidebar-stats {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rules-list li {
  padding: 8px 0;
  font-size: 14px;
  color: #64748b;
  border-bottom: 1px solid #f1f5f9;
}

.rules-list li:last-child {
  border-bottom: none;
}

.rules-list li::before {
  content: '✓ ';
  color: #10b981;
  font-weight: 700;
  margin-right: 8px;
}

@media (max-width: 1024px) {
  .forum-sidebar {
    order: -1;
  }
}

</style>
