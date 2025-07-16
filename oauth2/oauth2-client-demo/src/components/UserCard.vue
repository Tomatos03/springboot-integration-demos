<template>
  <div class="user-card">
    <img :src="avatar" alt="avatar" class="user-card__avatar" />
    <h3 class="user-card__name">{{ name || '无用户名称' }}</h3>
    <p class="user-card__email">{{ email || '无邮箱信息' }}</p>
  </div>
</template>

<script setup>
  import { defineProps, computed, isRef } from 'vue'
  const props = defineProps({
    User: {
      type: Object,
      required: true,
    },
  })
  console.log(props.User)

  const user = computed(() => props.User)

  const name = computed(() => user.value.name)
  const email = computed(() => user.value.email)
  const avatar = computed(() => user.value.avatar)
</script>

<style scoped>
  .user-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 15px;
    padding: 16px;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    box-sizing: border-box;
  }
  .user-card__avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
    background: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .user-card__name {
    margin: 0;
    font-size: 1.2em;
    font-weight: 600;
    text-align: center;
  }
  .user-card__email {
    margin: 4px 0 0 0;
    color: #888;
    font-size: 0.95em;
    text-align: center;
  }
</style>
