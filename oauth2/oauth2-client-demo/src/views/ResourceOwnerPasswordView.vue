<template>
  <div class="resource-owner-password">
    <user-card :User="userInfo" style="width: 400px; height: 500px" />
    <button class="authorization-code__authorize-btn" @click="tryAuthorize">
      从第三方应用获取权限
    </button>
  </div>
</template>

<script setup>
  import UserCard from '@/components/UserCard.vue'
  const userInfo = {
    avatar: '',
    name: '',
    email: '',
  }
</script>

<style scoped></style>
