<template>
  <router-view />
  <MessageModal />
</template>

<script setup lang="ts">
import MessageModal from './components/MessageModal.vue';
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
