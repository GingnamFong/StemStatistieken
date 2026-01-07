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

          <!-- Post Form Modal, logic can be found in ForumPostCard.vue -->
          <PostForm
            v-if="showPostForm"
            v-model:title="newPostTitle"
            v-model:content="newPostContent"
            :loading="loading"
            :error="error"
            @submit="submitPost"
            @close="closePostForm"
          />

          <!-- Filter Results Info, logic can be found in ForumFilters.vue -->
          <div v-if="activeFilterCount > 0" class="filter-results-info">
            <span class="filter-results-text">
              {{ sortedPosts.length }} van {{ posts.length }} posts getoond
            </span>
            <button @click="clearFilters" class="clear-filters-link">Filters wissen</button>
          </div>

          <!-- Posts List, logic can be found in ForumPostCard.vue -->
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

        <!-- Sidebar, logic can be found in ForumSideBar.vue -->
        <ForumSideBar
          :postsCount="posts.length"
          :commentsCount="totalComments"
        />

      </div>
    </div>
  </div>
</template>

<script setup>
// Service and other javascript imports
import {submitForumPost} from "@/services/ForumQuestionService.js"; // fetchForumPosts
import { fetchForumPosts } from '@/services/ForumQuestionService.js' // Async for loading a post
import { runAsyncWithState } from '@/composables/AsyncHandler.js'
import { dummyPosts } from '@/data/dummyPosts.js' // Dummy posts for demonstration
import { useForumFilters } from '@/composables/ForumFilters.js'// Computed property for active filter count, Backend logic can be found in ForumFilters.js
import { votePost } from '@/services/ForumQuestionService.js' // VotePost logic replaced to ForumQuestionService.js

// Vue imports
import { ref, computed, onMounted } from 'vue'
import ForumPostCard from "@/components/ForumPostCard.vue"; // Forum post card
import CreatePostCard from "@/components/CreatePostCard.vue"; // Creating a post
import ForumFilters from "@/components/ForumFilters.vue"; // Filters
import PostForm from "@/components/PostForm.vue"; // Post
import ForumSideBar from "@/components/ForumSideBar.vue"; // Sidebar
import ForumHeader from '@/components/ForumHeader.vue' // Header

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

// now initialize reactive posts with dummy data
const posts = ref([...dummyPosts])

// ForumFilters.vue
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

// ForumFilters.vue
function clearFilters() {
  searchQuery.value = ''
  filterAuthor.value = ''
  filterDate.value = ''
  filterMinScore.value = null
  filterMinComments.value = null
}

// AsyncHandler.js
async function loadPosts() {
  await runAsyncWithState(async () => {
    posts.value = await fetchForumPosts()
  }, { loading, error })
}

// ForumQuestionService.js
function vote(postId, voteType) {
  votePost(posts, postId, voteType)
}

// PostForm.vue
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
.badge-icon {
  width: 20px;
  height: 20px;
  color: white;
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

/* Filter and Sort Container
 Filter Section */

.filter-toggle-btn svg {
  width: 18px;
  height: 18px;
}

/* Create Post Card */
.create-post-avatar svg {
  width: 20px;
  height: 20px;
}

.create-post-btn svg {
  width: 20px;
  height: 20px;
}

/* Post Form Modal */
.post-form-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

/* Posts List */
.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.vote-btn svg {
  width: 20px;
  height: 20px;
}

.post-action svg {
  width: 16px;
  height: 16px;
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

}

@media (max-width: 768px) {

  .dashboard-container {
    padding: 0 20px 40px;
  }

  .forum-layout {
    gap: 16px;
  }

}

@media (max-width: 480px) {

  .dashboard-container {
    padding: 0 16px 32px;
  }

  .vote-btn svg {
    width: 18px;
    height: 18px;
  }

  .post-action svg {
    width: 14px;
    height: 14px;
  }
}
</style>


