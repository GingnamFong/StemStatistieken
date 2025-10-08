<script setup>
defineProps({
  backgroundImageUrl: { type: String, default: '/banner.jpg' },
  years: { type: Array, default: () => ['2021', '2023', '2025'] }
})
</script>

<template>
  <div class="banner-header">
    <h1 class="banner-header__title">
      Bekijk snel en gemakkelijk<br />
      de verkiezingsresultaten<br />
      en <a href="#" class="banner-header__link">vergelijk!</a>
    </h1>
  </div>
  <section class="banner" :style="{ backgroundImage: `url(${backgroundImageUrl})` }">
    <div class="banner__overlay" />
    <div class="banner__inner">
      <div class="banner__grid">
        <button class="year-card" type="button">
          <span class="year-card__year">{{ years[0] }}</span>
        </button>
        <button class="year-card" type="button">
          <span class="year-card__year">{{ years[1] }}</span>
        </button>
        <div class="year-card year-card--disabled">
          <span class="year-card__year">{{ years[2] }}</span>
          <span class="year-card__subtitle">Binnenkort beschikbaar…</span>
        </div>
      </div>
    </div>
  </section>

</template>

<style scoped>
:global(html), :global(body) { overflow-x: hidden; }

.banner-header {
  width: 100%;
  padding: 56px 16px 28px;
  margin: 0;
}

.banner-header__title {
  max-width: 1100px;
  margin: 0 auto;
  font-size: clamp(26px, 3.6vw, 44px);
  line-height: 1.25;
}

.banner-header__link { color: #3b5ad0; text-decoration: underline; }

.banner {
  position: relative;
  background-image: none;
  background-size: cover;
  background-position: center;
  border-radius: 0;
  overflow: hidden;
  min-height: 320px;
  width: 100vw;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
}

.banner__overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0.25) 100%);
}

.banner__inner {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px 12px;
  min-height: inherit;
}

.banner__grid {
  width: 100%;
  max-width: 1200px;
  display: grid;
  grid-template-columns: repeat(3, minmax(190px, 1fr));
  gap: 24px;
  padding: 0 10px;
}

.year-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  background: #ffffff;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  box-shadow: 0 6px 16px rgba(0,0,0,0.08);
  transition: transform 180ms ease, box-shadow 180ms ease;
  cursor: pointer;
}

.year-card:hover,
.year-card:focus-visible {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(0,0,0,0.15);
}

.year-card__year {
  font-size: clamp(18px, 2.2vw, 22px);
  color: #1f2937;
}

.year-card--disabled {
  background: #e5e7eb;
  color: #4b5563;
  border-color: #e5e7eb;
  cursor: default;
}

.year-card--disabled .year-card__year {
  color: #374151;
}

.year-card__subtitle {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

@media (max-width: 1024px) {
  .banner__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .banner__grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }
}
</style>


