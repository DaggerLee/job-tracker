<template>
  <div class="app">
    <header>
      <h1>🎯 Job Application Tracker</h1>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }
body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; color: #333; }
.app { min-height: 100vh; }
header { background: #2c3e50; color: white; padding: 20px 40px; }
header h1 { font-size: 24px; }
main { padding: 30px 40px; max-width: 1100px; margin: 0 auto; }
</style>