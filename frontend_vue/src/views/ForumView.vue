<template>
  <div class="dashboard">
    <!-- ForumHeader, frontend in component -->
    <ForumHeader>
      <template #icon>
        <svg
          class="badge-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path
            d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
          />
        </svg>
      </template>
    </ForumHeader>

    <!-- Content Container -->
    <div class="dashboard-container">
      <div class="forum-layout">
        <!-- Main Content -->
        <div class="forum-main">

          <!-- Filter and Sort Bar, logic can be found in ForumFilters.vue -->
          <ForumFilters
            v-model:search="searchQuery"
            v-model:author="filterAuthor"
            v-model:date="filterDate"
            v-model:minScore="filterMinScore"
            v-model:minComments="filterMinComments"
            v-model:sort="selectedSort"
            :activeFilterCount="activeFilterCount"
            @clear="clearFilters"
          />


          <!-- Create Post Card, logic in component CreatePostCard.vue-->
          <CreatePostCard
            v-model:title="newPostTitle"
            :open="showPostForm"
            @open="showPostForm = true"
          >
            <template #avatar>
              <svg>...</svg>
            </template>
            <template #icon>
              <svg>...</svg>
            </template>
          </CreatePostCard>



          <!-- Post Form Modal -->
          <PostForm
            v-if="showPostForm"
            v-model:title="newPostTitle"
            v-model:content="newPostContent"
            :loading="loading"
            :error="error"
            @submit="submitPost"
            @close="closePostForm"
          />


          <!-- Filter Results Info -->
          <div v-if="activeFilterCount > 0" class="filter-results-info">
            <span class="filter-results-text">
              {{ sortedPosts.length }} van {{ posts.length }} posts getoond
            </span>
            <button @click="clearFilters" class="clear-filters-link">Filters wissen</button>
          </div>

          <!-- Posts List -->
          <div class="posts-list">
            <ForumPostCard
              v-for="post in sortedPosts"
              :key="post.id"
              :post="post"
              @vote="vote"
              @open="$router.push(`/forum/questions/${$event}`)"
            />
          </div>

        </div>

        <!-- Sidebar -->
        <div class="forum-sidebar">
          <div class="sidebar-card">
            <h3>Over r/Forum</h3>
            <p>
              Stel hier je vragen, deel je standpunten en gedachten of bespreek de verkiezingen en andere politieke onderwerpen.
            </p>
            <div class="sidebar-stats">
              <div class="stat">
                <span class="stat-value">{{ posts.length }}</span>
                <span class="stat-label">Posts</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ totalComments }}</span>
                <span class="stat-label">Reacties</span>
              </div>
            </div>
          </div>

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
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import ForumHeader from '@/components/ForumHeader.vue'

import { ref, computed, onMounted } from 'vue'
import {submitForumPost} from "@/services/ForumQuestionService.js";

const selectedSort = ref('hot')
const showPostForm = ref(false)
const newPostTitle = ref('')
const newPostContent = ref('')
const loading = ref(false)
const error = ref('')

// Filter states
const searchQuery = ref('')
ref(false);
const filterAuthor = ref('')
const filterDate = ref('')
const filterMinScore = ref(null)
const filterMinComments = ref(null)
// Dummy posts for demonstration
import { dummyPosts } from '@/data/dummyPosts.js'

// now initialize reactive posts with dummy data
const posts = ref([...dummyPosts])


// Computed property for active filter count
// Backend logic can be found in ForumFilters.js
import { useForumFilters } from '@/composables/ForumFilters.js'

const { sortedPosts, activeFilterCount } = useForumFilters(
  posts,
  {
    searchQuery,
    filterAuthor,
    filterDate,
    filterMinScore,
    filterMinComments
  },
  selectedSort
)

// Apply filters function (can be called manually if needed)
const totalComments = computed(() => {
  return posts.value.reduce((sum, post) => sum + (post.comments || 0), 0)
})

// fetchForumPosts
import { fetchForumPosts } from '@/services/ForumQuestionService.js'


// Async for loading a post
import { runAsyncWithState } from '@/composables/AsyncHandler.js'

async function loadPosts() {
  await runAsyncWithState(async () => {
    posts.value = await fetchForumPosts()
  }, { loading, error })
}


// VotePost logic replaced to ForumQuestionService.js
import { votePost } from '@/services/ForumQuestionService.js'

function vote(postId, voteType) {
  votePost(posts, postId, voteType)
}

// Piece of time
import ForumPostCard from "@/components/ForumPostCard.vue";
import CreatePostCard from "@/components/CreatePostCard.vue";
import ForumFilters from "@/components/ForumFilters.vue";
import PostForm from "@/components/PostForm.vue";


function closePostForm() {
  showPostForm.value = false
  newPostTitle.value = ''
  newPostContent.value = ''
}


// new backend added, new logic in ForumQuestionService.js
async function submitPost() {
  error.value = ''
  loading.value = true

  try {
    await submitForumPost(
      newPostTitle.value,
      newPostContent.value
    )

    await loadPosts()
    closePostForm()
  } catch (e) {
    console.error(e)
    error.value =
      e.message || 'Er is een fout opgetreden bij het opslaan van je post.'
  } finally {
    loading.value = false
  }
}


function clearFilters() {
  searchQuery.value = ''
  filterAuthor.value = ''
  filterDate.value = ''
  filterMinScore.value = null
  filterMinComments.value = null
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Nunito', sans-serif;
}

/* Header - Same as other pages */
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

/* Dashboard Container */
.dashboard-container {
  max-width: 1400px;
  margin: -40px auto 0;
  padding: 0 32px 40px;
  position: relative;
  z-index: 2;
}

/* Forum Layout */
.forum-layout {
  display: grid;
  grid-template-columns: 1fr 312px;
  gap: 24px;
  margin-top: 24px;
}

.forum-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* Filter and Sort Container */
.filter-sort-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Filter Section */
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

/* Filters Panel */
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

/* Sort Bar */
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

/* Create Post Card */
.create-post-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
}

.create-post-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.create-post-avatar svg {
  width: 20px;
  height: 20px;
}

.create-post-input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  background: #f8fafc;
  transition: all 0.2s;
}

.create-post-input:focus {
  outline: none;
  border-color: #3b82f6;
  background: white;
}

.create-post-btn {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.create-post-btn:hover {
  background: #f1f5f9;
  border-color: #3b82f6;
  color: #3b82f6;
}

.create-post-btn svg {
  width: 20px;
  height: 20px;
}

/* Post Form Modal */
.post-form-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.post-form-card {
  background: white;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.post-form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.post-form-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.close-btn {
  background: none;
  border: none;
  font-size: 32px;
  color: #64748b;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.post-form-body {
  padding: 24px;
}

.post-form-title {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  font-family: 'Nunito', sans-serif;
  margin-bottom: 16px;
  transition: all 0.2s;
}

.post-form-title:focus {
  outline: none;
  border-color: #3b82f6;
}

.post-form-content {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  font-family: 'Nunito', sans-serif;
  resize: vertical;
  margin-bottom: 20px;
  transition: all 0.2s;
}

.post-form-content:focus {
  outline: none;
  border-color: #3b82f6;
}

.post-form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.cancel-btn,
.submit-btn {
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
  border: none;
}

.cancel-btn {
  background: #f1f5f9;
  color: #64748b;
}

.cancel-btn:hover {
  background: #e2e8f0;
}

.submit-btn {
  background: #3b82f6;
  color: white;
}

.submit-btn:hover {
  background: #2563eb;
}

.error-text {
  margin-top: 8px;
  font-size: 14px;
  color: #dc2626;
}

/* Posts List */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  display: flex;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.2s;
}

.post-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.post-votes {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 4px;
  background: #f8fafc;
  min-width: 40px;
}

.vote-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.vote-btn:hover {
  background: #e2e8f0;
}

.vote-btn svg {
  width: 20px;
  height: 20px;
}

.vote-btn.upvote.active {
  color: #ef4444;
}

.vote-btn.downvote.active {
  color: #3b82f6;
}

.post-content {
  flex: 1;
  padding: 12px 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.post-author {
  font-weight: 600;
  color: #3b82f6;
}

.post-time {
  color: #94a3b8;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.post-text {
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.post-footer {
  display: flex;
  gap: 16px;
}

.post-action {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  padding: 6px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-family: 'Nunito', sans-serif;
}

.post-action:hover {
  background: #f1f5f9;
  color: #3b82f6;
}

.post-action svg {
  width: 16px;
  height: 16px;
}

.empty-state {
  text-align: center;
  color: #64748b;
  font-size: 14px;
  margin-top: 8px;
}

.filter-results-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  font-size: 14px;
}

.filter-results-text {
  color: #1e40af;
  font-weight: 500;
}

.clear-filters-link {
  background: none;
  border: none;
  color: #3b82f6;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  font-family: 'Nunito', sans-serif;
  padding: 0;
  transition: color 0.2s;
}

.clear-filters-link:hover {
  color: #2563eb;
}

/* Sidebar */
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

.rules-list li:before {
  content: '✓ ';
  color: #10b981;
  font-weight: 700;
  margin-right: 8px;
}

/* Responsive */
@media (max-width: 1024px) {
  .forum-layout {
    grid-template-columns: 1fr;
  }

  .forum-sidebar {
    order: -1;
  }
}

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

  .dashboard-container {
    padding: 0 20px 40px;
  }

  .forum-layout {
    gap: 16px;
  }

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

  .post-form-modal {
    padding: 10px;
  }

  .post-form-card {
    max-height: 95vh;
  }
}

@media (max-width: 480px) {
  .dashboard-header {
    padding: 24px 16px 40px;
  }

  .page-title {
    font-size: 28px;
  }

  .dashboard-container {
    padding: 0 16px 32px;
  }

  .create-post-input {
    font-size: 13px;
  }

  .create-post-btn {
    padding: 6px 10px;
  }

  .post-card {
    border-radius: 6px;
  }

  .post-votes {
    min-width: 36px;
    padding: 6px 2px;
  }

  .vote-btn svg {
    width: 18px;
    height: 18px;
  }

  .post-content {
    padding: 10px 12px;
  }

  .post-title {
    font-size: 16px;
  }

  .post-action {
    padding: 4px 6px;
    font-size: 11px;
  }

  .post-action svg {
    width: 14px;
    height: 14px;
  }
}
</style>


