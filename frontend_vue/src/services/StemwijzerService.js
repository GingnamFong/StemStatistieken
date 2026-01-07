import { API_BASE_URL } from '../config/api.js'
import { stemwijzerQuestions, ANSWER_SCORES, IMPORTANCE_WEIGHTS, CATEGORIES } from '../data/stemwijzerQuestions.js'

/**
 * Service for stemwijzer (voting guide) functionality.
 * Handles API calls for calculating matches and saving favorite parties.
 * Now supports weighted answers based on importance levels.
 */

// Mock party positions for local calculation
// In production, this would come from the backend
const PARTY_POSITIONS = {
  'VVD': {
    1: -1, 2: -1, 3: 1, 4: 1, 5: -1, 6: 0, 7: 1, 8: 1, 9: -1, 10: 2,
    11: -1, 12: 1, 13: 0, 14: 0, 15: -1, 16: 0, 17: -1, 18: 2, 19: 2, 20: -1,
    color: '#FF6600'
  },
  'D66': {
    1: 2, 2: 2, 3: -1, 4: 1, 5: 0, 6: 2, 7: 1, 8: -1, 9: 1, 10: -1,
    11: 0, 12: 2, 13: -2, 14: 2, 15: 2, 16: 2, 17: 1, 18: 1, 19: 0, 20: 2,
    color: '#00AA00'
  },
  'GroenLinks-PvdA': {
    1: 2, 2: 2, 3: -2, 4: 0, 5: -1, 6: 2, 7: 0, 8: 2, 9: 2, 10: -2,
    11: 2, 12: 2, 13: -2, 14: 2, 15: 2, 16: 2, 17: 2, 18: 0, 19: -1, 20: 1,
    color: '#E5001A'
  },
  'PVV': {
    1: -2, 2: -2, 3: 2, 4: -2, 5: 1, 6: 1, 7: 2, 8: 0, 9: 1, 10: 1,
    11: 2, 12: -2, 13: 2, 14: 0, 15: 1, 16: -1, 17: -2, 18: 2, 19: 2, 20: -1,
    color: '#002F6C'
  },
  'CDA': {
    1: 1, 2: 0, 3: 1, 4: 0, 5: 0, 6: 1, 7: 1, 8: 1, 9: 0, 10: 0,
    11: 0, 12: 1, 13: 0, 14: 1, 15: 1, 16: 1, 17: 0, 18: 1, 19: 1, 20: -1,
    color: '#007B5F'
  },
  'SP': {
    1: 1, 2: 2, 3: -1, 4: -1, 5: -1, 6: 2, 7: 0, 8: 2, 9: 2, 10: -2,
    11: 2, 12: -1, 13: 1, 14: 2, 15: 2, 16: 1, 17: 1, 18: -1, 19: 0, 20: 1,
    color: '#EE2E22'
  },
  'ChristenUnie': {
    1: 1, 2: 1, 3: 0, 4: -1, 5: 0, 6: 1, 7: 0, 8: 1, 9: 1, 10: -1,
    11: 1, 12: 1, 13: 0, 14: 1, 15: 1, 16: 1, 17: 0, 18: 1, 19: 0, 20: -2,
    color: '#00A1DE'
  },
  'Partij voor de Dieren': {
    1: 2, 2: 2, 3: -2, 4: -1, 5: -1, 6: 1, 7: -2, 8: 1, 9: 2, 10: -2,
    11: 1, 12: 1, 13: -1, 14: 2, 15: 2, 16: 2, 17: 2, 18: -2, 19: -1, 20: 2,
    color: '#006C2E'
  },
  'BBB': {
    1: -1, 2: -2, 3: 1, 4: 1, 5: 1, 6: 0, 7: 2, 8: 0, 9: 1, 10: 1,
    11: 1, 12: -1, 13: 2, 14: 0, 15: 1, 16: 0, 17: -2, 18: 1, 19: 1, 20: 0,
    color: '#96BE1F'
  },
  'NSC': {
    1: 1, 2: 1, 3: 1, 4: 0, 5: 0, 6: 1, 7: 1, 8: 1, 9: 1, 10: 0,
    11: 0, 12: 1, 13: 0, 14: 1, 15: 1, 16: 1, 17: 0, 18: 1, 19: 1, 20: 0,
    color: '#4A90D9'
  }
}

export const StemwijzerService = {
  /**
   * Calculates match scores between user answers and party positions.
   * Uses weighted scoring based on answer intensity and importance.
   * @param {Object} weightedAnswers - Map of questionId to { answer, importance, answerScore, importanceWeight }
   * @returns {Promise<Object>} Object containing partyScores and categoryBreakdown
   */
  async calculateMatches(weightedAnswers) {
    try {
      // Try API first
      const response = await fetch(`${API_BASE_URL}/api/stemwijzer/calculate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(weightedAnswers)
      })

      if (response.ok) {
        const results = await response.json()
        if (!results.error) {
          return results
        }
      }
    } catch {
      console.log('API not available, using local calculation')
    }

    // Fallback to local calculation
    return this.calculateMatchesLocally(weightedAnswers)
  },

  /**
   * Local calculation of match scores when API is not available.
   * @param {Object} weightedAnswers - User answers with weights
   * @returns {Object} Match results
   */
  calculateMatchesLocally(weightedAnswers) {
    const partyScores = []

    for (const [partyName, positions] of Object.entries(PARTY_POSITIONS)) {
      let weightedScore = 0
      let totalWeight = 0
      const categoryMatches = {}

      for (const [questionId, data] of Object.entries(weightedAnswers)) {
        const qId = parseInt(questionId)
        const partyPosition = positions[qId] || 0
        const userScore = data.answerScore
        const weight = data.importanceWeight

        // Calculate match for this question (0-100%)
        // Max difference is 4 (from -2 to 2), so we convert to 0-100%
        const difference = Math.abs(userScore - partyPosition)
        const questionMatch = ((4 - difference) / 4) * 100

        // Apply weight
        weightedScore += questionMatch * weight
        totalWeight += weight

        // Track by category
        const question = stemwijzerQuestions.find(q => q.id === qId)
        if (question) {
          if (!categoryMatches[question.category]) {
            categoryMatches[question.category] = { score: 0, weight: 0, count: 0 }
          }
          categoryMatches[question.category].score += questionMatch * weight
          categoryMatches[question.category].weight += weight
          categoryMatches[question.category].count++
        }
      }

      // Calculate final percentage
      const matchPercentage = totalWeight > 0
        ? Math.round(weightedScore / totalWeight)
        : 0

      partyScores.push({
        partyId: partyName.toLowerCase().replace(/\s+/g, '-'),
        partyName: partyName,
        partyColor: positions.color,
        matchPercentage: matchPercentage,
        categoryMatches: categoryMatches
      })
    }

    // Sort by match percentage (descending)
    partyScores.sort((a, b) => b.matchPercentage - a.matchPercentage)

    // Calculate category breakdown
    const categoryBreakdown = {}
    for (const category of Object.values(CATEGORIES)) {
      const questionsInCategory = stemwijzerQuestions.filter(q => q.category === category)
      const answeredInCategory = questionsInCategory.filter(q => weightedAnswers[q.id])
      const importantInCategory = answeredInCategory.filter(
        q => weightedAnswers[q.id]?.importanceWeight >= 2
      )

      categoryBreakdown[category] = {
        questionsCount: questionsInCategory.length,
        answeredCount: answeredInCategory.length,
        importantCount: importantInCategory.length
      }
    }

    return {
      partyScores: partyScores,
      categoryBreakdown: categoryBreakdown
    }
  },

  /**
   * Sets the favorite party for a user based on stemwijzer results.
   * @param {number} userId - User ID
   * @param {string} partyId - Party ID to set as favorite
   * @returns {Promise<Object>} Response data
   */
  async setFavoriteParty(userId, partyId) {
    const response = await fetch(`${API_BASE_URL}/api/stemwijzer/favorite-party/${userId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ partyId })
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`Server error: ${response.status} - ${errorText}`)
    }

    return await response.json()
  },

  /**
   * Get all available parties with their positions (for admin/debug)
   * @returns {Object} Party positions data
   */
  getPartyPositions() {
    return PARTY_POSITIONS
  },

  /**
   * Get answer score for a given answer option
   * @param {string} answer - Answer option value
   * @returns {number} Score value
   */
  getAnswerScore(answer) {
    return ANSWER_SCORES[answer] || 0
  },

  /**
   * Get importance weight for a given importance level
   * @param {string} importance - Importance level value
   * @returns {number} Weight value
   */
  getImportanceWeight(importance) {
    return IMPORTANCE_WEIGHTS[importance] || 1
  }
}

export default StemwijzerService
