<template>
  <div>
    <!-- 统计栏 -->
    <div class="stats">
      <div class="stat-card" v-for="s in statuses" :key="s.label">
        <div class="stat-num">{{ countByStatus(s.label) }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="card chart-card">
        <h2>Status Distribution</h2>
        <div class="chart-wrap">
          <canvas ref="pieChartRef"></canvas>
        </div>
      </div>
      <div class="card chart-card">
        <h2>Weekly Applications</h2>
        <div class="chart-wrap">
          <canvas ref="lineChartRef"></canvas>
        </div>
      </div>
    </div>

    <!-- 添加表单 -->
    <div class="card">
      <h2>Add Application</h2>
      <div class="form-row">
        <input v-model="form.company" placeholder="Company" />
        <input v-model="form.position" placeholder="Position" />
        <input v-model="form.appliedDate" type="date" />
        <select v-model="form.status">
          <option v-for="s in statuses" :key="s.label" :value="s.label">{{ s.label }}</option>
        </select>
        <button @click="addApplication">Add</button>
      </div>
      <input v-model="form.notes" placeholder="Notes (optional)" class="notes-input" />
    </div>

    <!-- 筛选 -->
    <div class="filter-row">
      <button v-for="s in ['All', ...statuses.map(s=>s.label)]" :key="s"
        :class="['filter-btn', filter === s ? 'active' : '']"
        @click="filter = s">{{ s }}</button>
    </div>

    <!-- 列表 -->
    <div class="card" v-for="app in filteredApps" :key="app.id">
      <div class="app-row">
        <div class="app-info">
          <span class="company">{{ app.company }}</span>
          <span class="position">{{ app.position }}</span>
          <span class="date">{{ app.appliedDate || 'No date' }}</span>
          <span :class="['badge', app.status.toLowerCase().replace(' ', '-')]">{{ app.status }}</span>
        </div>
        <div class="app-actions">
          <select :value="app.status" @change="updateStatus(app, $event.target.value)">
            <option v-for="s in statuses" :key="s.label" :value="s.label">{{ s.label }}</option>
          </select>
          <button class="del-btn" @click="deleteApplication(app.id)">✕</button>
        </div>
      </div>
      <div v-if="app.notes" class="notes">{{ app.notes }}</div>
    </div>

    <div v-if="filteredApps.length === 0" class="empty">No applications yet. Add one above!</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const API = 'http://localhost:8080/api/applications'
const applications = ref([])
const filter = ref('All')
const statuses = [
  { label: 'Applied' },
  { label: 'Phone Screen' },
  { label: 'Interview' },
  { label: 'Offer' },
  { label: 'Rejected' }
]
const form = ref({ company: '', position: '', status: 'Applied', appliedDate: '', notes: '' })

const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const STATUS_COLORS = ['#4caf50', '#2196f3', '#ff9800', '#8bc34a', '#f44336']

const filteredApps = computed(() =>
  filter.value === 'All' ? applications.value : applications.value.filter(a => a.status === filter.value)
)
const countByStatus = (s) => applications.value.filter(a => a.status === s).length

function computeWeeklyData() {
  const now = new Date()
  // Find Monday of current week
  const day = now.getDay()
  const currentMonday = new Date(now)
  currentMonday.setHours(0, 0, 0, 0)
  currentMonday.setDate(now.getDate() - (day === 0 ? 6 : day - 1))

  const labels = []
  const counts = []

  for (let i = 7; i >= 0; i--) {
    const weekStart = new Date(currentMonday)
    weekStart.setDate(currentMonday.getDate() - i * 7)
    const weekEnd = new Date(weekStart)
    weekEnd.setDate(weekStart.getDate() + 7)

    const m = weekStart.getMonth() + 1
    const d = weekStart.getDate()
    labels.push(`${m}/${d}`)

    const count = applications.value.filter(app => {
      if (!app.appliedDate) return false
      const [y, mo, da] = app.appliedDate.split('-').map(Number)
      const date = new Date(y, mo - 1, da)
      return date >= weekStart && date < weekEnd
    }).length
    counts.push(count)
  }

  return { labels, counts }
}

function updateCharts() {
  if (!pieChartRef.value || !lineChartRef.value) return

  const statusCounts = statuses.map(s => countByStatus(s.label))

  if (pieChart) {
    pieChart.data.datasets[0].data = statusCounts
    pieChart.update()
  } else {
    pieChart = new Chart(pieChartRef.value, {
      type: 'pie',
      data: {
        labels: statuses.map(s => s.label),
        datasets: [{
          data: statusCounts,
          backgroundColor: STATUS_COLORS,
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        plugins: { legend: { position: 'bottom', labels: { font: { size: 12 } } } }
      }
    })
  }

  const { labels, counts } = computeWeeklyData()

  if (lineChart) {
    lineChart.data.labels = labels
    lineChart.data.datasets[0].data = counts
    lineChart.update()
  } else {
    lineChart = new Chart(lineChartRef.value, {
      type: 'line',
      data: {
        labels,
        datasets: [{
          label: 'Applications',
          data: counts,
          borderColor: '#2c3e50',
          backgroundColor: 'rgba(44, 62, 80, 0.08)',
          tension: 0.35,
          fill: true,
          pointBackgroundColor: '#2c3e50',
          pointRadius: 4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1 } }
        },
        plugins: { legend: { display: false } }
      }
    })
  }
}

async function fetchAll() {
  const res = await fetch(API)
  applications.value = await res.json()
}

async function addApplication() {
  if (!form.value.company || !form.value.position) return alert('Company and Position are required!')
  await fetch(API, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(form.value) })
  form.value = { company: '', position: '', status: 'Applied', appliedDate: '', notes: '' }
  fetchAll()
}

async function updateStatus(app, newStatus) {
  await fetch(`${API}/${app.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ ...app, status: newStatus }) })
  fetchAll()
}

async function deleteApplication(id) {
  await fetch(`${API}/${id}`, { method: 'DELETE' })
  fetchAll()
}

const SEED_DATA = [
  { company: 'Google', position: 'Frontend Engineer', status: 'Interview', appliedDate: daysAgo(3), notes: 'Referred by a friend' },
  { company: 'Meta', position: 'Software Engineer', status: 'Phone Screen', appliedDate: daysAgo(7), notes: '' },
  { company: 'Amazon', position: 'Full Stack Developer', status: 'Rejected', appliedDate: daysAgo(14), notes: 'OA completed' },
  { company: 'Apple', position: 'iOS Developer', status: 'Applied', appliedDate: daysAgo(2), notes: '' },
  { company: 'Netflix', position: 'Senior Engineer', status: 'Offer', appliedDate: daysAgo(21), notes: 'Negotiating salary' },
  { company: 'Stripe', position: 'Backend Engineer', status: 'Interview', appliedDate: daysAgo(10), notes: 'System design round' },
  { company: 'Airbnb', position: 'React Developer', status: 'Applied', appliedDate: daysAgo(1), notes: '' },
  { company: 'Spotify', position: 'Platform Engineer', status: 'Rejected', appliedDate: daysAgo(28), notes: 'No feedback provided' },
  { company: 'LinkedIn', position: 'Data Engineer', status: 'Phone Screen', appliedDate: daysAgo(5), notes: '' },
  { company: 'Figma', position: 'UI Engineer', status: 'Applied', appliedDate: daysAgo(0), notes: 'Dream job' },
]

function daysAgo(n) {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return d.toISOString().split('T')[0]
}

async function seedData() {
  if (applications.value.length > 0) return
  await Promise.all(SEED_DATA.map(app =>
    fetch(API, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(app) })
  ))
  await fetchAll()
}

watch(applications, () => nextTick(updateCharts))

onMounted(async () => {
  await fetchAll()
  await seedData()
  nextTick(updateCharts)
})
</script>

<style scoped>
.stats { display: flex; gap: 16px; margin-bottom: 24px; }
.stat-card { background: white; border-radius: 10px; padding: 16px 24px; text-align: center; flex: 1; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.stat-num { font-size: 32px; font-weight: bold; color: #2c3e50; }
.stat-label { font-size: 13px; color: #888; margin-top: 4px; }
.charts-row { display: flex; gap: 16px; margin-bottom: 24px; }
.chart-card { flex: 1; min-width: 0; }
.chart-card h2 { margin-bottom: 16px; font-size: 15px; color: #555; }
.chart-wrap { position: relative; height: 240px; display: flex; align-items: center; justify-content: center; }
.chart-wrap canvas { max-height: 220px; }
.card { background: white; border-radius: 10px; padding: 20px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.card h2 { margin-bottom: 14px; font-size: 16px; color: #555; }
.form-row { display: flex; gap: 10px; flex-wrap: wrap; }
.form-row input, .form-row select { padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.form-row button { padding: 8px 20px; background: #2c3e50; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 14px; }
.form-row button:hover { background: #3d5166; }
.notes-input { width: 100%; margin-top: 10px; padding: 8px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.filter-row { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-btn { padding: 6px 16px; border: 1px solid #ddd; border-radius: 20px; background: white; cursor: pointer; font-size: 13px; }
.filter-btn.active { background: #2c3e50; color: white; border-color: #2c3e50; }
.app-row { display: flex; justify-content: space-between; align-items: center; }
.app-info { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.company { font-weight: bold; font-size: 16px; }
.position { color: #555; }
.date { color: #aaa; font-size: 13px; }
.app-actions { display: flex; gap: 8px; align-items: center; }
.app-actions select { padding: 6px 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 13px; }
.del-btn { background: #e74c3c; color: white; border: none; border-radius: 6px; padding: 6px 10px; cursor: pointer; }
.notes { margin-top: 10px; color: #777; font-size: 13px; background: #f9f9f9; padding: 8px 12px; border-radius: 6px; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.badge.applied { background: #d5e8d4; color: #2e7d32; }
.badge.phone-screen { background: #dae8fc; color: #1565c0; }
.badge.interview { background: #fff3cd; color: #f57f17; }
.badge.offer { background: #d4edda; color: #155724; }
.badge.rejected { background: #f8d7da; color: #721c24; }
.empty { text-align: center; color: #aaa; padding: 40px; }
</style>
