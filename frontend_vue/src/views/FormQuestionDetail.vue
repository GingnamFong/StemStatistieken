 <template>
  <div class="page">
    <button class="back" @click="$router.back()">← Terug</button>

    <p v-if="loading && !post" class="muted">Laden...</p>
    <p v-if="error" class="err">{{ error }}</p>

    <!-- Post -->
    <div v-if="post" class="post-card">
      <div class="post-votes" aria-hidden="true">
        <button class="vote-btn" type="button" disabled aria-label="Upvote">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M7 14l5-5 5 5z" /></svg>
        </button>

        <div class="score">{{ post.score ?? 0 }}</div>

        <button class="vote-btn" type="button" disabled aria-label="Downvote">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M17 10l-5 5-5-5z" /></svg>
        </button>
      </div>

      <div class="post-content">
        <div class="post-header">
          <span class="post-author">u/{{ authorName(post.author) }}</span>
          <span class="post-time">· {{ formatTime(post.createdAt) }}</span>
        </div>

        <h2 class="post-title">{{ title }}</h2>
        <p v-if="content" class="post-text">{{ content }}</p>

        <div class="post-footer">
          <span class="pill">💬 {{ comments.length }} reacties</span>
          <button class="ghost-btn" type="button" disabled>↗ Delen</button>
          <button class="ghost-btn" type="button" disabled>🔖 Opslaan</button>
        </div>
      </div>
    </div>

    <p v-if="!loading && !post && !error" class="err">Post niet gevonden.</p>

    <!-- Comments -->
    <div v-if="post" class="comments">
      <div class="comments-header">
        <h3>Reacties</h3>
        <span class="count">{{ comments.length }}</span>
      </div>

      <!-- Add comment -->
      <div class="comment-box">
        <textarea
          v-model="newComment"
          placeholder="Schrijf een reactie..."
          rows="4"
          :disabled="submitting"
        />

        <div class="comment-actions">
          <span v-if="!isLoggedIn" class="muted">Log in om te reageren.</span>
          <span v-else class="muted">Wees respectvol 🙂</span>

          <button
            class="primary-btn"
            @click="submitComment"
            :disabled="submitting || !isLoggedIn || !newComment.trim()"
            type="button"
          >
            {{ submitting ? 'Plaatsen...' : 'Plaats reactie' }}
          </button>
        </div>
      </div>

      <!-- Comment list -->
      <div class="comments-list">
        <div v-for="c in comments" :key="c.id" class="comment-card">
          <div class="comment-meta">
            <span class="comment-author">u/{{ c.author }}</span>
            <span class="comment-time">· {{ formatTime(c.createdAt) }}</span>
          </div>

          <div class="comment-body">{{ c.body }}</div>

          <div class="comment-footer">
            <button
              class="like-btn"
              :class="{ active: c.likedByMe }"
              @click="toggleLike(c)"
              :disabled="!isLoggedIn"
              type="button"
              title="Like"
            >
              👍 <span class="like-count">{{ c.likes }}</span>
            </button>
          </div>
        </div>

        <p v-if="!comments.length && !loading" class="muted empty">
          Nog geen reacties. Wees de eerste!
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ForumService } from '../services/ForumService.js'

const route = useRoute()
const id = computed(() => route.params.id)

const post = ref(null)
const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const submitting = ref(false)
const error = ref('')

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const title = computed(() => (post.value?.body || '').split('\n\n')[0] || '')
const content = computed(() => (post.value?.body || '').split('\n\n').slice(1).join('\n\n').trim())

function authorName(a) {
  if (!a) return 'Anoniem'
  if (typeof a === 'string') return a
  if (a.name) return a.lastName ? `${a.name} ${a.lastName}` : a.name
  return 'Anoniem'
}

function toDate(x) {
  return x ? new Date(x) : new Date()
}

function normalizeCommentDto(c) {
  return {
    id: c.id,
    body: c.body,
    author: authorName(c.author),
    createdAt: toDate(c.createdAt),
    likes: 0,
    likedByMe: false
  }
}

async function load() {
  error.value = ''
  loading.value = true
  try {
    const data = await ForumService.getPost(id.value)
    post.value = { ...data, createdAt: toDate(data.createdAt) }

    comments.value = (data.comments || []).map(normalizeCommentDto)

    // load like counts for each comment
    await Promise.all(
      comments.value.map(async (c) => {
        try {
          const res = await ForumService.getCommentLikes(c.id)
          c.likes = res?.likes ?? 0
          c.likedByMe = !!res?.likedByMe
        } catch {
          c.likes = 0
          c.likedByMe = false
        }
      })
    )
  } catch (e) {
    error.value = e?.message || 'Er is iets misgegaan.'
    post.value = null
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function toggleLike(comment) {
  error.value = ''
  if (!isLoggedIn.value) {
    error.value = 'Je moet ingelogd zijn om een comment te liken.'
    return
  }

  try {
    const res = comment.likedByMe
      ? await ForumService.unlikeComment(comment.id)
      : await ForumService.likeComment(comment.id)

    comment.likes = res?.likes ?? comment.likes
    comment.likedByMe = !comment.likedByMe
  } catch (e) {
    error.value = e?.message || 'Like actie mislukt.'
  }
}

async function submitComment() {
  error.value = ''
  if (!isLoggedIn.value) {
    error.value = 'Je moet ingelogd zijn om te reageren.'
    return
  }
  if (!newComment.value.trim()) return

  submitting.value = true
  try {
    await ForumService.addComment(id.value, newComment.value.trim())
    newComment.value = ''
    await load()
  } catch (e) {
    error.value = e?.message || 'Reactie plaatsen mislukt.'
  } finally {
    submitting.value = false
  }
}

function formatTime(date) {
  const d = date instanceof Date ? date : new Date(date)
  const diff = Date.now() - d.getTime()
  const m = Math.floor(diff / 60000)
  if (m < 1) return 'Zojuist'
  if (m < 60) return `${m} min geleden`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h} uur geleden`
  const days = Math.floor(h / 24)
  return `${days} dagen geleden`
}

onMounted(load)
</script>

<style scoped>
.page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
  font-family: 'Nunito', sans-serif;
  color: #0f172a;
  background: #f8fafc;
  min-height: 100vh;
}

.back {
  border: none;
  background: transparent;
  font-weight: 800;
  cursor: pointer;
  color: #334155;
  margin-bottom: 14px;
}

.muted { color: #64748b; }

.err {
  color: #dc2626;
  font-weight: 700;
  margin: 10px 0;
}

/* Post */
.post-card {
  display: flex;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  margin-bottom: 18px;
}

.post-votes {
  min-width: 52px;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 6px;
  gap: 8px;
}

.vote-btn {
  border: none;
  background: transparent;
  cursor: not-allowed;
  color: #94a3b8;
  padding: 4px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vote-btn svg { width: 22px; height: 22px; }

.score { font-weight: 900; color: #0f172a; }

.post-content { padding: 14px 16px; flex: 1; }

.post-header {
  font-size: 12px;
  color: #64748b;
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.post-author { color: #3b82f6; font-weight: 900; }

.post-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  line-height: 1.25;
  font-weight: 900;
}

.post-text {
  margin: 0 0 14px 0;
  color: #334155;
  line-height: 1.65;
  white-space: pre-wrap;
}

.post-footer {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.pill {
  background: #f1f5f9;
  color: #334155;
  padding: 8px 10px;
  border-radius: 999px;
  font-weight: 800;
  font-size: 12px;
}

.ghost-btn {
  border: none;
  background: #f1f5f9;
  color: #334155;
  padding: 8px 10px;
  border-radius: 10px;
  font-weight: 800;
  font-size: 12px;
  cursor: not-allowed;
  opacity: 0.9;
}

/* Comments */
.comments { margin-top: 6px; }

.comments-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin: 18px 0 10px;
}

.comments-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
}

.count {
  font-size: 12px;
  font-weight: 900;
  color: #1e40af;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  padding: 4px 8px;
  border-radius: 999px;
}

.comment-box {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  margin-bottom: 14px;
}

.comment-box textarea {
  width: 100%;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  resize: vertical;
  font-family: inherit;
  font-size: 14px;
  background: #f8fafc;
  outline: none;
}

.comment-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.primary-btn {
  border: none;
  border-radius: 12px;
  padding: 10px 14px;
  font-weight: 900;
  cursor: pointer;
  background: #3b82f6;
  color: white;
}

.primary-btn:hover { background: #2563eb; }

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.comment-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: 0 1px 6px rgba(15, 23, 42, 0.05);
}

.comment-meta {
  font-size: 12px;
  color: #64748b;
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author { color: #3b82f6; font-weight: 900; }

.comment-body {
  color: #334155;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

/* Like button */
.comment-footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
}

.like-btn {
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 900;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.like-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.like-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.like-btn.active {
  background: #eff6ff;
  border-color: #bfdbfe;
  color: #1e40af;
}

.like-count { font-variant-numeric: tabular-nums; }

.empty {
  text-align: center;
  padding: 12px 0;
}

@media (max-width: 520px) {
  .post-title { font-size: 18px; }
  .post-votes { min-width: 46px; }
  .comment-actions { flex-direction: column; align-items: stretch; }
  .primary-btn { width: 100%; }
}
</style>
