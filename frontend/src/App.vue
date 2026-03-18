<template>
  <div class="app">
    <header>
      <h1>🎯 Job Application Tracker</h1>
      <nav>
        <RouterLink to="/">Applications</RouterLink>
        <RouterLink to="/analysis">AI Analysis</RouterLink>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView, RouterLink } from 'vue-router'
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; color: #333; }
.app { min-height: 100vh; }
header { background: #2c3e50; color: white; padding: 20px 40px; display: flex; align-items: center; justify-content: space-between; }
header h1 { font-size: 24px; }
header nav { display: flex; gap: 8px; }
header nav a { color: rgba(255,255,255,0.75); text-decoration: none; font-size: 14px; padding: 6px 16px; border-radius: 6px; transition: all 0.2s; }
header nav a:hover { color: white; background: rgba(255,255,255,0.1); }
header nav a.router-link-active { color: white; background: rgba(255,255,255,0.18); }
main { padding: 30px 40px; max-width: 1100px; margin: 0 auto; }
</style>
