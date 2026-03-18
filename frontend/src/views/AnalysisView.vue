<template>
  <div>
    <div class="card">
      <h2>AI Resume Match Analysis</h2>
      <p class="subtitle">Paste a job description and your resume to get an AI-powered match score using Claude.</p>

      <div class="field">
        <label>Claude API Key</label>
        <input
          v-model="apiKey"
          type="password"
          placeholder="sk-ant-api03-..."
          class="api-key-input"
        />
      </div>

      <div class="two-col">
        <div class="field">
          <label>Your Resume</label>
          <textarea v-model="resumeText" placeholder="Paste your resume text here..." rows="12" />
        </div>
        <div class="field">
          <label>Job Description</label>
          <textarea v-model="jdText" placeholder="Paste the job description here..." rows="12" />
        </div>
      </div>

      <button class="analyze-btn" @click="analyze" :disabled="loading">
        <span v-if="loading" class="spinner"></span>
        {{ loading ? 'Analyzing...' : 'Analyze Match' }}
      </button>

      <div v-if="error" class="error-msg">{{ error }}</div>
    </div>

    <!-- Result -->
    <div v-if="result" class="card result-card">
      <div class="score-section">
        <div :class="['score-circle', scoreClass]">{{ result.score }}</div>
        <div class="score-label">
          <div class="score-title">Match Score</div>
          <div class="score-bar-wrap">
            <div class="score-bar" :style="{ width: result.score + '%', background: scoreColor }"></div>
          </div>
        </div>
      </div>

      <div class="skills-grid">
        <div class="skills-col matched">
          <h3>✅ Matched Skills ({{ result.matchedSkills.length }})</h3>
          <ul>
            <li v-for="skill in result.matchedSkills" :key="skill">{{ skill }}</li>
          </ul>
        </div>
        <div class="skills-col missing">
          <h3>⚠️ Missing Skills ({{ result.missingSkills.length }})</h3>
          <ul>
            <li v-for="skill in result.missingSkills" :key="skill">{{ skill }}</li>
          </ul>
        </div>
      </div>

      <div v-if="result.summary" class="summary">
        <h3>Analysis Summary</h3>
        <p>{{ result.summary }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const apiKey = ref('')
const resumeText = ref('')
const jdText = ref('')
const loading = ref(false)
const error = ref('')
const result = ref(null)

const scoreClass = computed(() => {
  if (!result.value) return ''
  const s = result.value.score
  if (s >= 75) return 'score-high'
  if (s >= 50) return 'score-mid'
  return 'score-low'
})

const scoreColor = computed(() => {
  if (!result.value) return '#ccc'
  const s = result.value.score
  if (s >= 75) return '#4caf50'
  if (s >= 50) return '#ff9800'
  return '#f44336'
})

async function analyze() {
  if (!apiKey.value) return (error.value = 'Please enter your Claude API key.')
  if (!resumeText.value) return (error.value = 'Please paste your resume text.')
  if (!jdText.value) return (error.value = 'Please paste the job description.')

  loading.value = true
  error.value = ''
  result.value = null

  try {
    const res = await fetch('http://localhost:8080/api/ai/analyze', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        apiKey: apiKey.value,
        resumeText: resumeText.value,
        jdText: jdText.value
      })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.error || 'Analysis failed')
    result.value = data
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card { background: white; border-radius: 10px; padding: 28px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.card h2 { font-size: 20px; color: #2c3e50; margin-bottom: 6px; }
.subtitle { color: #888; font-size: 14px; margin-bottom: 24px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 18px; }
.field label { font-size: 13px; font-weight: 600; color: #555; }
.field input, .field textarea {
  padding: 10px 12px; border: 1px solid #ddd; border-radius: 8px;
  font-size: 14px; font-family: inherit; resize: vertical;
}
.field input:focus, .field textarea:focus { outline: none; border-color: #2c3e50; }
.api-key-input { font-family: monospace; }
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.analyze-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 32px; background: #2c3e50; color: white;
  border: none; border-radius: 8px; cursor: pointer; font-size: 15px;
  transition: background 0.2s;
}
.analyze-btn:hover:not(:disabled) { background: #3d5166; }
.analyze-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.spinner {
  width: 16px; height: 16px; border: 2px solid rgba(255,255,255,0.4);
  border-top-color: white; border-radius: 50%; animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
.error-msg { margin-top: 14px; color: #c62828; background: #ffebee; padding: 10px 16px; border-radius: 8px; font-size: 14px; }

/* Result */
.result-card { }
.score-section { display: flex; align-items: center; gap: 24px; margin-bottom: 28px; padding-bottom: 24px; border-bottom: 1px solid #f0f0f0; }
.score-circle {
  width: 96px; height: 96px; border-radius: 50%; display: flex;
  align-items: center; justify-content: center;
  font-size: 34px; font-weight: bold; color: white; flex-shrink: 0;
}
.score-high { background: #4caf50; }
.score-mid { background: #ff9800; }
.score-low { background: #f44336; }
.score-label { flex: 1; }
.score-title { font-size: 16px; font-weight: 600; color: #555; margin-bottom: 10px; }
.score-bar-wrap { height: 10px; background: #eee; border-radius: 5px; overflow: hidden; }
.score-bar { height: 100%; border-radius: 5px; transition: width 0.6s ease; }
.skills-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 24px; }
.skills-col h3 { font-size: 14px; font-weight: 600; margin-bottom: 12px; }
.matched h3 { color: #2e7d32; }
.missing h3 { color: #e65100; }
.skills-col ul { list-style: none; display: flex; flex-direction: column; gap: 6px; }
.skills-col li { font-size: 14px; padding: 6px 12px; border-radius: 6px; }
.matched li { background: #f1f8e9; color: #33691e; }
.missing li { background: #fff3e0; color: #bf360c; }
.summary h3 { font-size: 14px; font-weight: 600; color: #555; margin-bottom: 8px; }
.summary p { font-size: 14px; color: #666; line-height: 1.6; background: #f9f9f9; padding: 12px 16px; border-radius: 8px; }
</style>
