<template>
  <div class="authorization-code">
    <user-card :User="userInfo" style="width: 400px; height: 500px" />
    <button class="authorization-code__authorize-btn" @click="tryAuthorize">
      从第三方应用获取权限
    </button>
  </div>
</template>

<script setup>
  import UserCard from '@/components/UserCard.vue'
  import { tryAuthorize } from '@/api/authorize'
  const userInfo = {
    avatar: '',
    name: '',
    email: '',
  }
</script>

<style scoped>
  .authorization-code {
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  .authorization-code__authorize-btn {
    width: 400px;
    height: 60px;
    margin-top: 20px;
    background-color: #1976d2;
    color: #fff;
    border: none;
    border-radius: 5px;
    font-size: 16px;
    cursor: pointer;
  }
</style>
